import javafx.scene.image.Image;

import java.io.InputStream;

public class Fortress {
    private static final String IMAGE_FILE = "fortress.png";
    private double positionX = 0.0;
    private double positionY = 0.0;
    private Image image;

    public Fortress(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;

        InputStream is = getClass().getClassLoader().getResourceAsStream(IMAGE_FILE);
        if (is == null) {
            throw new AssertionError("Cannot find image file " + IMAGE_FILE);
        }
        image = new Image(is);
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public Image getImage() {
        return image;
    }
}
