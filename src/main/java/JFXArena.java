import javafx.scene.canvas.*;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.*;

/**
 * A JavaFX GUI element that displays a grid on which you can draw images, text and lines.
 */
public class JFXArena extends Pane {
    // Represents the image to draw. You can modify this to introduce multiple images.

    // The following values are arbitrary, and you may need to modify them according to the 
    // requirements of your application.
    public static final int gridWidth = 9;
    public static final int gridHeight = 9;

    private Score score = new Score();
    private Fortress fortress;

    private volatile int robotCounter = 1;
    private List<Robot> robotArmy = Collections.synchronizedList(new ArrayList<>());
    private List<Future> robotThreadIndicator = new ArrayList<>();
    // robotPool control robot characteristic: one each
    private ExecutorService robotsPool = Executors.newFixedThreadPool(gridWidth * gridHeight); //fully occupied
    //spawnPool will spawn robot
    private ExecutorService spawnPool = Executors.newSingleThreadExecutor();

    //scorePool will handle record and increase score
    private ExecutorService scorePool = Executors.newSingleThreadExecutor();
    //firingPool will handle reload and shoot bullet
    private ExecutorService firingPool = Executors.newSingleThreadExecutor();

    private BlockingQueue<Bullet> firingQueue = new LinkedBlockingQueue<>();

    private Object mutex = new Object();

    private double gridSquareSize; // Auto-calculated
    private Canvas canvas; // Used to provide a 'drawing surface'.

    private List<ArenaListener> listeners = null;

    /**
     * Creates a new arena object, loading the robot image and initialising a drawing surface.
     */
    public JFXArena() {
        //Initial GUI
        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
    }

    public void startGame(){
        fortress = new Fortress(gridWidth / 2, gridHeight / 2);
        Runnable spawnTask = () -> {
            try {
                while (true) {
                    Thread.sleep(2000); //default n value
                    spawnRobot();
                }
            } catch (InterruptedException e) {
                System.out.println("Stop Spawning Robots");
            }
        };
        Runnable firingTask = () -> {
            try {
                while (true) {
                    Bullet bullet = firingQueue.take(); //wait until new bullet load
                    shootBullet(bullet);
                    Thread.sleep(1000); //wait for 1 second for next bullet
                }
            } catch (InterruptedException e) {
                System.out.println("Stop Fire Queue");
            }
        };
        //notify App class to update GUI when the score change
        score.addListener((PropertyChangeEvent evt) -> {
            for (ArenaListener listener : listeners) {
                listener.getCurrentScore(score.getTotalScore());
            }
        });
        scorePool.execute(score); //start counting score
        firingPool.execute(firingTask); //notify firing Task to start.
        spawnPool.execute(spawnTask); //start spawn robots
    }

    //check if the position is occupied?
    //true = occupied
    //false = nothing there
    private boolean isOccupied(double x, double y) {

        boolean occupied = false;
        synchronized (mutex) {
            for (Robot robot : robotArmy) {
                //Current position of Standing robot
                if ((robot.getPositionX() == x)
                        && robot.getPositionY() == y) {
                    occupied = true;
                }
            }
        }
        return occupied;
    }

    //check the robot if
    //false: able to move to next position
    //true: unable to move to next position
    private boolean isOccupied(Robot checkRobot) {
        boolean occupied = false;
        synchronized (mutex) {
            for (Robot robot : robotArmy) {
                //some robots is moving occupied
                if (!robot.equals(checkRobot)) {
                    //robot occupy next square
                    if (((Math.ceil(robot.getPositionX()) == (checkRobot.getPositionX() + checkRobot.getDirectionX())
                            && (Math.ceil(robot.getPositionY()) == (checkRobot.getPositionY() + checkRobot.getDirectionY()))))) {
                        occupied = true;
                        checkRobot.resetDirection();
                        checkRobot.setDirectionFlag(true);
                    }
                    //robot occupy current square
                    if (((((int) (robot.getPositionX()) == (int) (checkRobot.getPositionX() + checkRobot.getDirectionX()))
                            && ((int) (robot.getPositionY()) == (int) (checkRobot.getPositionY() + checkRobot.getDirectionY()))))) {
                        occupied = true;
                        checkRobot.resetDirection();
                        checkRobot.setDirectionFlag(true);
                    }
                }
            }
        }
        return occupied;
    }

    /**
     * Moves a robot image to a new grid position. This is highly rudimentary, as you will need
     * many different robots in practice. This method currently just serves as a demonstration.
     */
    public void setRobotDirection(Robot robot) {
        Random random = new Random();
        int direction = random.nextInt(4);
        switch (direction) {
            case 0: //up
                robot.changeDirectionUp();
                if (!isOccupied(robot)
                        && robot.getPositionY() + robot.getDirectionY() >= 0) {
                    robot.setDirectionFlag(false);
                    break;
                }
                robot.resetDirection();
            case 1: //down
                robot.changeDirectionDown();
                if (!isOccupied(robot)
                        && robot.getPositionY() + robot.getDirectionY() < gridHeight) {
                    robot.setDirectionFlag(false);
                    break;
                }
                robot.resetDirection();
            case 2: //left
                robot.changeDirectionLeft();
                if (!isOccupied(robot)
                        && robot.getPositionX() + robot.getDirectionX() >= 0) {
                    robot.setDirectionFlag(false);
                    break;
                }
                robot.resetDirection();
            case 3: //right
                robot.changeDirectionRight();
                if (!isOccupied(robot)
                        && robot.getPositionX() + robot.getDirectionX() < gridWidth) {
                    robot.setDirectionFlag(false);
                    break;
                }
                robot.resetDirection();
            default:
                robot.resetDirection();
        }
    }

    /**
     * Adds a callback for when the user clicks on a grid square within the arena. The callback
     * (of type ArenaListener) receives the grid (x,y) coordinates as parameters to the
     * 'squareClicked()' method.
     * when The square is clicked reload the bullet, ready to shoot every one second interval
     */
    public void addListener(ArenaListener newListener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
            setOnMouseClicked(event ->
            {
                int gridX = (int) (event.getX() / gridSquareSize);
                int gridY = (int) (event.getY() / gridSquareSize);

                if (gridX < gridWidth && gridY < gridHeight) {
                    //reload Bullet
                    try {
                        Bullet newBullet = new Bullet(gridX, gridY);
                        firingQueue.put(newBullet);
                        for (ArenaListener listener : listeners) {
                            listener.reloadBullet(gridX, gridY); //
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Stop Reload  Firing Queue");
                    }
                }
            });
        }
        listeners.add(newListener);
    }

    //terminate the robot at the bullet location if there is any
    private void shootBullet(Bullet bullet) {
        Robot targetedRobot = selectRobot(bullet.getPositionX(), bullet.getPositionY());
        //hit
        if (targetedRobot != null) {
            //add score and notify to log message
            score.addScore(10.0 + 100.0 * ((double) bullet.stopTime() / (double) targetedRobot.getDelay()));
            for (ArenaListener listener : listeners) {
                listener.hitRobot(targetedRobot.getId()); //notify App class to print???
            }
            //remove robot from the game
            robotThreadIndicator.get(targetedRobot.getId() - 1).cancel(true); //terminate thread.
            robotArmy.remove(targetedRobot);
            requestLayout();

        } else { //miss
            for (ArenaListener listener : listeners) {
                listener.missBullet(bullet.getPositionX(), bullet.getPositionY()); //notify App class to print???
            }
        }
    }

    //    select the Robot at the location x,y
    private Robot selectRobot(double positionX, double positionY) {
        synchronized (mutex) {
            for (Robot robot : robotArmy) {
                if ((robot.getPositionX() == positionX
                        && robot.getPositionY() == positionY)
                ) {
                    return robot;
                }
            }
        }
        return null;
    }

    //    Spawn new Robot at one of the corner if it is not occupied
    private void spawnRobot() {
        double x = 0.0;
        double y = 0.0;
        int robotDelay;

        Random random = new Random();
        robotDelay = random.nextInt(2000 - 500) + 500; //range between 500~2000
        int nextPosition = random.nextInt(4);
        switch (nextPosition) {
            case 0:
                x = 0.0;
                y = 0.0;
                if (!isOccupied(x, y))
                    break;
            case 1:
                x = gridWidth - 1.0;
                y = 0.0;
                if (!isOccupied(x, y))
                    break;
            case 2:
                x = 0.0;
                y = gridHeight - 1.0;
                if (!isOccupied(x, y))
                    break;
            case 3:
                x = gridWidth - 1.0;
                y = gridHeight - 1.0;
                if (!isOccupied(x, y))
                    break;
        }
        //add new robot if corner is free
        Robot newRobot = new Robot(robotCounter, robotDelay, x, y);
        newRobot.addListener((PropertyChangeEvent evt) -> {
                    //listen to Robot Delay, when robot is able to move to new Direction
                    if (evt.getPropertyName().equals("directionFlag")) {
                        setRobotDirection(newRobot);
                    }
                    //End Game Condition
                    if(isOccupied(fortress.getPositionX(),fortress.getPositionY())){
                        for (ArenaListener listener : listeners) {
                            listener.gameOver(score.getTotalScore()); //notify App class to log
                        }
                        scorePool.shutdownNow();
                        firingPool.shutdownNow();
                        spawnPool.shutdownNow();
                        robotArmy.clear();
                        for (Future e :robotThreadIndicator) {
                            e.cancel(true);
                        }
                        robotsPool.shutdownNow();
                    }
                    requestLayout(); //update robot movement
                }
        );
        if (!isOccupied(newRobot)) {
            robotArmy.add(newRobot);
            robotCounter += 1;
            robotThreadIndicator.add(robotsPool.submit(newRobot)); //start robot movement(task) thread
            for (ArenaListener listener : listeners) {
                listener.spawnRobot(newRobot.getId()); //notify App class to log
            }
            requestLayout();
        }
    }

    /**
     * This method is called in order to redraw the screen, either because the user is manipulating
     * the window, OR because you've called 'requestLayout()'.
     * <p>
     * You will need to modify the last part of this method; specifically the sequence of calls to
     * the other 'draw...()' methods. You shouldn't need to modify anything else about it.
     */
    @Override
    public void layoutChildren() {
        super.layoutChildren();
        GraphicsContext gfx = canvas.getGraphicsContext2D();
        gfx.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        // First, calculate how big each grid cell should be, in pixels. (We do need to do this
        // every time we repaint the arena, because the size can change.)
        gridSquareSize = Math.min(
                getWidth() / (double) gridWidth,
                getHeight() / (double) gridHeight);

        double arenaPixelWidth = gridWidth * gridSquareSize;
        double arenaPixelHeight = gridHeight * gridSquareSize;


        // Draw the arena grid lines. This may help for debugging purposes, and just generally
        // to see what's going on.
        gfx.setStroke(Color.DARKGREY);
        gfx.strokeRect(0.0, 0.0, arenaPixelWidth - 1.0, arenaPixelHeight - 1.0); // Outer edge

        for (int gridX = 1; gridX < gridWidth; gridX++) // Internal vertical grid lines
        {
            double x = (double) gridX * gridSquareSize;
            gfx.strokeLine(x, 0.0, x, arenaPixelHeight);
        }

        for (int gridY = 1; gridY < gridHeight; gridY++) // Internal horizontal grid lines
        {
            double y = (double) gridY * gridSquareSize;
            gfx.strokeLine(0.0, y, arenaPixelWidth, y);
        }

        // Invoke helper methods to draw things at the current location.
        // ** You will need to adapt this to the requirements of your application. **

        //wrap in task and run with thread when
        //access resource robotArmy need mutex
        synchronized (mutex) {
            //draw Fortress
            drawImage(gfx, fortress.getImage(), fortress.getPositionX(), fortress.getPositionY());
            //draw robot Army
            for (Robot e : robotArmy) {
                drawImage(gfx, e.getImage(), e.getPositionX(), e.getPositionY());
                drawLabel(gfx, String.valueOf(e.getId()), e.getPositionX(), e.getPositionY());
                if (!e.getDirectionFlag()) // trying to move
                {
                    drawLine(gfx, e.getPositionX(), e.getPositionY(), e.getPositionX() + e.getDirectionX(), e.getPositionY() + e.getDirectionY());
                }
            }
        }
    }


    /**
     * Draw an image in a specific grid location. *Only* call this from within layoutChildren().
     * <p>
     * Note that the grid location can be fractional, so that (for instance), you can draw an image
     * at location (3.5,4), and it will appear on the boundary between grid cells (3,4) and (4,4).
     * <p>
     * You shouldn't need to modify this method.
     */
    private void drawImage(GraphicsContext gfx, Image image, double gridX, double gridY) {
        // Get the pixel coordinates representing the centre of where the image is to be drawn.
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;

        // We also need to know how "big" to make the image. The image file has a natural width
        // and height, but that's not necessarily the size we want to draw it on the screen. We
        // do, however, want to preserve its aspect ratio.
        double fullSizePixelWidth = image.getWidth();
        double fullSizePixelHeight = image.getHeight();

        double displayedPixelWidth, displayedPixelHeight;
        if (fullSizePixelWidth > fullSizePixelHeight) {
            // Here, the image is wider than it is high, so we'll display it such that it's as
            // wide as a full grid cell, and the height will be set to preserve the aspect
            // ratio.
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        } else {
            // Otherwise, it's the other way around -- full height, and width is set to
            // preserve the aspect ratio.
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        // Actually put the image on the screen.
        gfx.drawImage(image,
                x - displayedPixelWidth / 2.0,  // Top-left pixel coordinates.
                y - displayedPixelHeight / 2.0,
                displayedPixelWidth,              // Size of displayed image.
                displayedPixelHeight);
    }


    /**
     * Displays a string of text underneath a specific grid location. *Only* call this from within
     * layoutChildren().
     * <p>
     * You shouldn't need to modify this method.
     */
    private void drawLabel(GraphicsContext gfx, String label, double gridX, double gridY) {
        gfx.setTextAlign(TextAlignment.CENTER);
        gfx.setTextBaseline(VPos.TOP);
        gfx.setStroke(Color.BLUE);
        gfx.strokeText(label, (gridX + 0.5) * gridSquareSize, (gridY + 1.0) * gridSquareSize);
    }

    /**
     * Draws a (slightly clipped) line between two grid coordinates.
     * <p>
     * You shouldn't need to modify this method.
     */
    private void drawLine(GraphicsContext gfx, double gridX1, double gridY1,
                          double gridX2, double gridY2) {
        gfx.setStroke(Color.RED);

        // Recalculate the starting coordinate to be one unit closer to the destination, so that it
        // doesn't overlap with any image appearing in the starting grid cell.
        final double radius = 0.5;
        double angle = Math.atan2(gridY2 - gridY1, gridX2 - gridX1);
        double clippedGridX1 = gridX1 + Math.cos(angle) * radius;
        double clippedGridY1 = gridY1 + Math.sin(angle) * radius;

        gfx.strokeLine((clippedGridX1 + 0.5) * gridSquareSize,
                (clippedGridY1 + 0.5) * gridSquareSize,
                (gridX2 + 0.5) * gridSquareSize,
                (gridY2 + 0.5) * gridSquareSize);
    }


}
