package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.Vector3;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class Bullet extends GameObject {
    public double MAX_VELOCITY = 5;

    private Circle graphics;
    private double radius = 3;


    private final DoubleProperty clickX = new SimpleDoubleProperty(0);
    private final DoubleProperty clickY = new SimpleDoubleProperty(0);


    private final DoubleProperty rotateProperty = new SimpleDoubleProperty(0D);

    public Vector3 position = new Vector3();//Позиция
    public Vector3 direction = new Vector3(0, 1, 0);//Направление

    public NetworkGame game;

    public final Vector3 velocity = new Vector3();//Скорость
    public final Vector3 acceleration = new Vector3();//Ускорение


    public double speed = 4;
    public Vector3 fTraction = new Vector3();


    private Player player;


    DoubleProperty startX = new SimpleDoubleProperty(0);
    DoubleProperty startY = new SimpleDoubleProperty(0);

    DoubleProperty endX = new SimpleDoubleProperty(0);
    DoubleProperty endY = new SimpleDoubleProperty(0);

    public Circle getGraphics() {
        return graphics;
    }

    private Pane container;

    private long timeShot;

    public Bullet(Vector3 direction, Vector3 position, NetworkGame game) {
        this.direction = direction;
        this.position = position;
        this.game = game;
        graphics = new Circle(radius);
        graphics.setStroke(Color.BLACK);
        graphics.setFill(Color.YELLOW);
    }

    public Bullet(Pane pane, NetworkGame game, Player player, long timeShot) {

        clickX.set(player.getMousePosition_X());
        clickY.set(player.getMousePosition_Y());

        graphics = new Circle(radius);
        graphics.setStroke(Color.BLACK);
        graphics.setVisible(true);

        container = pane;
        this.game = game;
        this.timeShot = timeShot;
        this.player = player;


        graphics.setCenterX(player.getCenter().x);
        graphics.setCenterY(player.getCenter().y);
        graphics.setRotate(0);

        position.set(player.getCenter().x, player.getCenter().y, 0);


        graphics.rotateProperty().bind(rotateProperty);
        graphics.setFill(Color.YELLOW);


        startX.set(graphics.getCenterX());
        startY.set(graphics.getCenterY());


        endX.set(clickX.get());
        endY.set(clickY.get());

        double rad = Math.atan2(clickY.get() - graphics.getCenterY(), clickX.get() - graphics.getCenterX());
        direction.x = Math.cos(rad);
        direction.y = Math.sin(rad);


        container.getChildren().addAll(graphics);


    }

    public Vector3 getCenter() {
        Vector3 vector3 = new Vector3();

        return vector3;
    }

    public void update() {

        calculateTraction();
        calculateVelocity(1);
        calculatePosition(1);
        draw();
    }

    public ConditionBullet getConditionBullet() {
        return new ConditionBullet(this);
    }


    private void draw() {
        graphics.setCenterX(position.x);
        graphics.setCenterY(position.y);
    }


    public DoubleProperty getRotateProperty() {
        return rotateProperty;
    }

    private void calculateTraction() {// Рассчитываем тягу двигателя
        fTraction.set(direction);
        fTraction.normalize();
        fTraction.scale(speed);
    }

    private void calculateVelocity(double deltaTime) {
        acceleration.scale(deltaTime);
        double getSize = 0;
        getSize = velocity.getSize();
        if (getSize > MAX_VELOCITY) {
            velocity.scale(MAX_VELOCITY / getSize);
        }
        velocity.add(fTraction);
    }

    private void calculatePosition(double deltaTime) {
        velocity.scale(deltaTime);
        position.add(velocity);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }


    public void setGraphics(Circle graphics) {
        this.graphics = graphics;
    }


    public DoubleProperty getClickX() {
        return clickX;
    }

    public void setClickX(double X) {
        this.clickX.set(X);
    }

    public void addX(double X) {
        this.clickX.set(this.clickX.get() + X);
    }

    public void addY(double Y) {
        this.clickY.set(this.clickY.get() + Y);
    }

    public DoubleProperty getClickY() {
        return clickY;
    }

    public DoubleProperty clickYProperty() {
        return clickY;
    }

    public void setClickY(double clickY) {
        this.clickY.set(clickY);
    }

    public long getTimeShot() {
        return timeShot;
    }
}
