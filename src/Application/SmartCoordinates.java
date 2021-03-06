package Application;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

/**
 * Класс, хранящий в себе только необходимые для отрисовки коллекции поля
 */
public class SmartCoordinates {
    private long id;
    private FloatProperty x;
    private DoubleProperty y;
    private Color color;
    private boolean redraw = true;
    private Image image = new Image("/Application/Transparent_white.png");

    /**
     * @param id ID города
     * @param x координата X города
     * @param y координата Y города
     * @param name имя создателя города
     */
    public SmartCoordinates(long id, float x, double y, String name) {
        this.id = id;
        this.x = new SimpleFloatProperty(x);
        this.y = new SimpleDoubleProperty(y);
        makeColour(name);
    }

    /**
     * Метод генерации уникального цвета для каждого пользователя
     * @param name имя пользователя
     */
    private void makeColour(String name){
        //из формулы ((Input - InputLow) / (InputHigh - InputLow)) * (OutputHigh - OutputLow) + OutputLow
        float h = (name.hashCode() % 1000) * 360/(999);
        float s = 0.7f;
        float v = 0.7f;
        this.color = Color.hsb(h,s,v);
        this.image = changeColour();
    }

    public FloatProperty getXProperty() {
        return x;
    }

    public DoubleProperty getYProperty() {
        return y;
    }

    public float getX() {
        return x.get();
    }

    public void setX(float x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public boolean toRedraw() {
        return redraw;
    }

    public void setRedraw(boolean redraw) {
        this.redraw = redraw;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setXY(float x, double y){
        this.x.set(x);
        this.y.set(y);
    }

    public Image getImage() {
        return image;
    }

    /**
     * Метод, меняющий цвет картинки попиксельно
     * @return картинка с измененным цветом
     */
    private Image changeColour(){
        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = writableImage.getPixelReader();
        for (int i = 0; i < writableImage.getHeight(); i++) {
            for (int j = 0; j < writableImage.getWidth(); j++) {
                Color c = pixelReader.getColor(j, i);
                if (c.getRed() > 0 || c.getGreen() > 0 || c.getBlue() > 0) {
                    pixelWriter.setColor(j, i, color);
                }
            }
        }
        return writableImage;
    }

    /**
     * Метод, добавляющий прозрачности к картинке
     * @param opacity непрозрачность
     * @return картинка с заданной opacity
     */
    public Image getTransparentImage(double opacity){
        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = writableImage.getPixelReader();
        for (int i = 0; i < writableImage.getHeight(); i++) {
            for (int j = 0; j < writableImage.getWidth(); j++) {
                Color newColor = Color.color(color.getRed(), color.getGreen(), color.getGreen(), opacity);
                Color c = pixelReader.getColor(j, i);
                if (c.getRed() > 0 || c.getGreen() > 0 || c.getBlue() > 0) {
                    pixelWriter.setColor(i, j, newColor);
                }
            }
        }
        return writableImage;
    }

}
