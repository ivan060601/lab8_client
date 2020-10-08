package Application;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.paint.Color;

public class SmartCoordinates {
    private long id;
    private FloatProperty x;
    private DoubleProperty y;
    private Color color;
    private boolean redraw = false;

    public SmartCoordinates(float x, double y, String name) {
        this.x = new SimpleFloatProperty(x);
        this.y = new SimpleDoubleProperty(y);
        makeColour(name);
    }

    public SmartCoordinates(long id, float x, double y, String name) {
        this.id = id;
        this.x = new SimpleFloatProperty(x);
        this.y = new SimpleDoubleProperty(y);
        makeColour(name);
    }

    private void makeColour(String name){
        //из формулы ((Input - InputLow) / (InputHigh - InputLow)) * (OutputHigh - OutputLow) + OutputLow
        float h = (name.hashCode() % 1000)/(999) * 360;
        float s = 0.2f;
        float v = 0.9f;
        this.color = Color.hsb(h,s,v);
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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
}
