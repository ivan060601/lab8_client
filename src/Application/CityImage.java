package Application;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class CityImage extends Rectangle {
    public CityImage(int x, int y, int w, int h, Image image) {
        this.setX(x);
        this.setY(y);
        this.setWidth(w);
        this.setHeight(h);
        this.setFill(new ImagePattern(image));
    }
}