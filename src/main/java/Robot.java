import com.sun.jdi.InternalException;
import javafx.scene.image.Image;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.util.concurrent.*;

public class Robot implements Runnable {
    //robot characteristics
    private int id;
    private int delay;
    private Image image;
    private double positionX;
    private double positionY;
    private int directionX = 0;
    private int directionY = 0;
    private boolean directionFlag = false;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private static final String IMAGE_FILE = "1554047213.png";

    public Robot(int id, int delay, double positionX, double positionY) {
        this.id = id;
        this.delay = delay;
        this.positionX = positionX;
        this.positionY = positionY;

        InputStream is = getClass().getClassLoader().getResourceAsStream(IMAGE_FILE);
        if (is == null) {
            throw new AssertionError("Cannot find image file " + IMAGE_FILE);
        }
        image = new Image(is);
    }

    public Image getImage() {
        return image;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public boolean getDirectionFlag() {
        return directionFlag;
    }

    public void setDirectionFlag(boolean directionFlag) {
        this.directionFlag = directionFlag;
    }

    public void changeDirectionUp() {
        directionX = 0;
        directionY = -1;
    }

    public void changeDirectionDown() {
        directionX = 0;
        directionY = 1;
    }

    public void changeDirectionLeft() {
        directionX = -1;
        directionY = 0;
    }

    public void changeDirectionRight() {
        directionX = 1;
        directionY = 0;
    }

    public void resetDirection() {
        directionX = 0;
        directionY = 0;
    }

    private void move(double moveX, double moveY) {
        double oldValueX = positionX;
        double oldValueY = positionY;
        positionX += moveX;
        positionY += moveY;
        support.firePropertyChange("positionX", oldValueX, positionX);
        support.firePropertyChange("positionY", oldValueY, positionY);
    }

    public int getId() {
        return id;
    }

    public int getDelay() {
        return delay;
    }

    public double getPositionX() {
        return Math.round(positionX * 100.0) / 100.0;
    }

    public double getPositionY() {
        return Math.round(positionY * 100.0) / 100.0;
    }

    public int getDirectionX() {
        return directionX;
    }

    public int getDirectionY() {
        return directionY;
    }


    //take 500ms to move
    private void processOfMoving() {
        //start process of moving
        //move 10% every 50ms
        setDirectionFlag(false);
        try {
            for (int i = 0; i < 10; i++) {
                if(getDirectionFlag() || (directionX == 0 && directionY == 0) ) //being shutdown
                    Thread.interrupted();
                this.move(directionX / 10.0, directionY / 10.0);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("(thread shutdown) Robot " + id + " Killed While moving");
        }

    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(delay); //wait for next available move
                setDirectionFlag(true); //do not move this line
                support.firePropertyChange("directionFlag", false, directionFlag);
                //find the next square to move to
                //stop finding new direction when have desire square to go.
                if ((directionX != 0 || directionY != 0) && !getDirectionFlag()) {
                    processOfMoving();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("(thread shutdown) Robot class: Robot " + id + "  killed");
        }

    }
}
