import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Example App (JavaFX)");
        JFXArena arena = new JFXArena();
        Logger logger = new Logger();

        ToolBar toolbar = new ToolBar();
        Label label = new Label("Score: 0.0");
        toolbar.getItems().addAll(label);


        TextArea loggerArea = new TextArea();


        arena.addListener(new ArenaListener() {
            @Override
            public void reloadBullet(double x, double y) {
                logger.addNewMessage("!!!!!!!!!!!!!!!!!!!!!!!RELOAD BULLET!!!!!!!!!!!!!!!!!!!!!!!!\n"
                        + "Aim at ( " + x + "," + y + ")\n" +
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
                String newMessage = logger.takeMessage();
                if (newMessage != null) {
                    String finalNewMessage = newMessage;
                    Platform.runLater(() -> {
                        loggerArea.appendText(finalNewMessage);
                    });
                }
            }

            @Override
            public void hitRobot(int robotID) {
                logger.addNewMessage(">>>>>>>>>>>>>HIT>>>>>>>>>>>>>\n"
                        + "Robot " + robotID + " Killed\n" +
                        ">>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
                //log Hit Robot
                String newMessage = logger.takeMessage();
                if (newMessage != null) {
                    String finalNewMessage = newMessage;
                    Platform.runLater(() -> {
                        loggerArea.appendText(finalNewMessage);
                    });
                }
            }

            @Override
            public void missBullet(double x, double y) {
                logger.addNewMessage("xxxxxxxxxxxxxxxxxMISSxxxxxxxxxxxxxxx\n"
                        + "Bullet ( " + x + "," + y + ") \n" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n");

                String newMessage = logger.takeMessage();
                if (newMessage != null) {
                    String finalNewMessage = newMessage;
                    Platform.runLater(() -> {
                        loggerArea.appendText(finalNewMessage);
                    });
                }
            }

            @Override
            public void spawnRobot(int robotID) {
                logger.addNewMessage("=============SPAWN============\n"
                        + "Robot " + robotID + " Spawn\n" +
                        "==============================\n");
                String newMessage = logger.takeMessage();
                if (newMessage != null) {
                    String finalNewMessage = newMessage;
                    Platform.runLater(() -> {
                        loggerArea.appendText(finalNewMessage);
                    });
                }
            }

            @Override
            public void getCurrentScore(double currentScore) {
                logger.addNewScore(String.valueOf(currentScore)); //add new score fine
                String newScore = logger.takeScore();
                if (newScore != null) {
                    String finalNewScore = newScore;
                    Platform.runLater(() -> {
                        Label newLabel = new Label("Score: " + finalNewScore);
                        toolbar.getItems().setAll(newLabel);
                    });
                }
            }

            @Override
            public void gameOver(double finalScore) {
                logger.addNewMessage("!!!!!!!!!!!!!!!!!!!!!!!GAME OVER!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                        "FINAL SCORE : " + finalScore +  "\n" +
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
                String newMessage = logger.takeMessage();
                if (newMessage != null) {
                    String finalNewMessage = newMessage;
                    Platform.runLater(() -> {
                        loggerArea.appendText(finalNewMessage);
                    });
                }
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, loggerArea);
        arena.setMinWidth(300.0);

        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);

        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
    }
}
