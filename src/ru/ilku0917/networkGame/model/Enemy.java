package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.Vector3;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class Enemy extends GameObject {
    public static double MAX_ENGINE_FORCE = 8;
    public double MAX_VELOCITY = 5;

    private Rectangle graphics;
    private double w = 30, h = 18;
    private ImageView graphicsImg;

    public long lastCrashTime;


    private final DoubleProperty X = new SimpleDoubleProperty(0);
    private final DoubleProperty Y = new SimpleDoubleProperty(0);


    private final DoubleProperty rotateProperty = new SimpleDoubleProperty(0D);

    private double mass = 100;

    public Vector3 position = new Vector3();//Позиция
    public Vector3 direction = new Vector3(0, 1, 0);//Направление

    public NetworkGame game;

    public final Vector3 velocity = new Vector3();//Скорость
    public final Vector3 acceleration = new Vector3();//Ускорение

    private Player goal;

    public double engineForce = MAX_ENGINE_FORCE;//Мощность двигателя
    public Vector3 fTraction = new Vector3();

    private double centerX;
    private double centerY;


    public Rectangle getGraphics() {
        return graphics;
    }

    private Pane container;

    public Enemy(Vector3 direction, Vector3 position, NetworkGame game) {
        this.direction = direction;
        this.position = position;
        this.game = game;
        graphicsImg = new ImageView(new Image(NetworkGame.class.getResourceAsStream("spaceship.png"), 30, 30, true, true));
        game.getContainer().getChildren().addAll(graphicsImg);
    }

    public Enemy(Pane pane, NetworkGame game) {
        graphicsImg = new ImageView(new Image(NetworkGame.class.getResourceAsStream("spaceship.png"), 30, 30, true, true));

        graphics = new Rectangle(w, h);
        graphics.setStroke(Color.BLACK);
        graphics.setVisible(false);

        container = pane;
        this.game = game;


        double randomX = Math.random() * NetworkGame.width + 1;
        double randomY = Math.random() * NetworkGame.height + 1;

        //Min + (int)(Math.random() * ((Max - Min) + 1))
        mass = Math.random() * 40 + mass - 20;
        engineForce = Math.random() * 4 + engineForce - 2;
        MAX_VELOCITY = Math.random() * 2 + MAX_VELOCITY - 1;


        graphics.setX(randomX);
        graphics.setY(randomY);
        graphics.setRotate(0);
        graphicsImg.setRotate(0);
        graphicsImg.setX(randomX);
        graphicsImg.setY(randomY);

        position.set(randomX, randomY, 0);


        graphics.rotateProperty().bind(rotateProperty);
        graphicsImg.rotateProperty().bind(graphics.rotateProperty());
        graphics.setFill(Color.MEDIUMPURPLE);
        graphics.setWidth(w);
        graphics.setHeight(h);

        container.getChildren().addAll(graphics, graphicsImg);


    }

    public ImageView getGraphicsImg() {
        return graphicsImg;
    }

    public Vector3 getCenter() {
        Vector3 vector3 = new Vector3();


        return vector3;
    }

    public void update() {

        if (game.isServer()) {
            calculateTraction();
            calculateAcceleration();
            calculateVelocity(1);
            calculatePosition(1);

            centerX = (position.x + position.x + w) / 2;
            centerY = (position.y + position.y + h) / 2;

            calculateGoal();

            centerX = (position.x + position.x + w) / 2;
            centerY = (position.y + position.y + h) / 2;

       /* startX.set(centerX);
        startY.set(centerY);

        endX.set(goal.getCenter().x);
        endY.set(goal.getCenter().y);*/

            double rad = Math.atan2(goal.getCenter().y - centerY, goal.getCenter().x - centerX);
            direction.x = Math.cos(rad);
            direction.y = Math.sin(rad);
        }


        draw();
    }

    private void calculateGoal() {
        Vector3 result = new Vector3();
        double minimum = Double.MAX_VALUE;

        for (Player player : game.getPlayers()) {
            Vector3.sub(result, player.getCenter(), new Vector3(centerX, centerY, 0));
            if (minimum > result.getSize()) {
                minimum = result.getSize();
                goal = player;
            }
        }

    }

    public ConditionEnemy getConditionEnemy() {
        return new ConditionEnemy(this);
    }


    private void draw() {
        if (game.isServer()) {
            graphics.setX(position.x);
            graphics.setY(position.y);
        }
        graphicsImg.setX(position.x);
        graphicsImg.setY(position.y);
    }

    public DoubleProperty getRotateProperty() {
        return rotateProperty;
    }

    private void calculateTraction() {// Рассчитываем тягу двигателя
        fTraction.set(direction);
        fTraction.normalize();
        fTraction.scale(engineForce);
    }

    private void calculateAcceleration() {
        acceleration.set(fTraction);
        acceleration.scale(1 / mass);
    }

    private void calculateVelocity(double deltaTime) {
        acceleration.scale(deltaTime);
        double getSize = 0;
        getSize = velocity.getSize();
        if (getSize > MAX_VELOCITY) {
            velocity.scale(MAX_VELOCITY / getSize);
        }
        velocity.add(acceleration);
    }

    private void calculatePosition(double deltaTime) {
        velocity.scale(deltaTime);
        position.add(velocity);
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setGraphics(Rectangle graphics) {
        this.graphics = graphics;
    }


    public DoubleProperty getX() {
        return X;
    }

    public void setX(double X) {
        this.X.set(X);
    }

    public void addX(double X) {
        this.X.set(this.X.get() + X);
    }

    public void addY(double Y) {
        this.Y.set(this.Y.get() + Y);
    }

    public DoubleProperty getY() {
        return Y;
    }

    public DoubleProperty yProperty() {
        return Y;
    }

    public void setY(double y) {
        Y.set(y);
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public Player getGoal() {
        return goal;
    }

    public void setGoal(Player goal) {
        this.goal = goal;
    }
}
