package Application;

import javafx.scene.paint.Color;

public class SmartCoordinates {
    private long id;
    private float x;
    private double y;
    private Color color;
    private boolean isDrawn = false;

    public SmartCoordinates(float x, double y, String name) {
        this.x = x;
        this.y = y;
        makeColour(name);
    }

    public SmartCoordinates(long id, float x, double y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        makeColour(name);
    }

    private void makeColour(String name){
        //из формулы ((Input - InputLow) / (InputHigh - InputLow)) * (OutputHigh - OutputLow) + OutputLow
        float h = (name.hashCode() % 1000)/(999) * 360;
        float s = 0.2f;
        float v = 0.9f;
        this.color = Color.hsb(h,s,v);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isDrawn() {
        return isDrawn;
    }

    public void setDrawn(boolean drawn) {
        isDrawn = drawn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setXY(float x, double y){
        this.x = x;
        this.y = y;
    }
}
