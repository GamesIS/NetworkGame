package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.Vector3;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Player extends GameObject {
    public static final double MAX_ENGINE_FORCE = 10;
    public static final double DECREASE_POINT = 15;
    public static final double INCREASE_POINT = 10;

    private Rectangle graphics;
    private double w = 35, h = 18, speed = 0;
    private boolean isColliding;
    private ImageView graphicsImg;

    private int point = 0;

    private Vector3 center = new Vector3();

    private final DoubleProperty mousePosition_X = new SimpleDoubleProperty(0D);
    private final DoubleProperty mousePosition_Y = new SimpleDoubleProperty(0D);

    private final BooleanProperty UP = new SimpleBooleanProperty(false);
    private final BooleanProperty DOWN = new SimpleBooleanProperty(false);

    private final BooleanProperty mousePressed = new SimpleBooleanProperty(false);

    private final DoubleProperty rotateProperty = new SimpleDoubleProperty(0D);

    private final double mass = 800; // em kilograma

    private static int rateOfFire = 100;// ms

    private static int lifeBullet = 6000;// ms

    public Vector3 position = new Vector3();//Позиция
    public Vector3 direction = new Vector3(0, -1, 0);//Направление

    public NetworkGame game;

    /*
    * if you have an angle (A), in radians, in the range -Pi to Pi, then convert it to a vector (V) with:
      V.x = cos(A)
      V.y = sin(A)
      The inverse is A = atan2(V.y, V.x)
    * */
    public final Vector3 velocity = new Vector3();//Скорость
    public final Vector3 acceleration = new Vector3();//Ускорение


    public ArrayList<Bullet> bullets = new ArrayList<>();
    public long lastshot = 0;


    //////////////////////////////////////TODO
    public double engineForce = 0;//Мощность двигателя
    public Vector3 fTraction = new Vector3();
    //////////////////////////////////////TODO


    DoubleProperty startX = new SimpleDoubleProperty(0);
    DoubleProperty startY = new SimpleDoubleProperty(0);

    DoubleProperty endX = new SimpleDoubleProperty(0);
    DoubleProperty endY = new SimpleDoubleProperty(0);

    public Line line = new Line();


    public boolean isDrifting;

    public Rectangle getGraphics() {
        return graphics;
    }

    private Pane container;

    public Player(boolean primary, Pane pane, NetworkGame game) {
        int x, y;
        if (primary){
            graphicsImg = new ImageView(new Image(NetworkGame.class.getResourceAsStream("car1.png")));
            x = (int)(NetworkGame.width - 100);
            y = (int) NetworkGame.height/2;
        }
        else{
            graphicsImg = new ImageView(new Image(NetworkGame.class.getResourceAsStream("car2.png")));
            x = 100;
            y = (int) NetworkGame.height/2;
        }


        graphics = new Rectangle(w, h);
        graphics.setStroke(Color.BLACK);
        graphics.setVisible(false);

        container = pane;
        this.game = game;


        /*graphics.setX(x);
        graphics.setY(y);
        graphicsImg.setX(x);
        graphicsImg.setY(y);*/

        graphics.setRotate(1);
        graphicsImg.setRotate(0);

        position.set(x, y, 0);
        graphics.rotateProperty().bind(rotateProperty);
        graphicsImg.rotateProperty().bind(graphics.rotateProperty());
        graphics.setFill(Color.MEDIUMPURPLE);
        graphics.setWidth(w);
        graphics.setHeight(h);


        line.startXProperty().bind(startX);
        line.startYProperty().bind(startY);
        line.endXProperty().bind(endX);
        line.endYProperty().bind(endY);
        line.setFill(Color.BLACK);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(2);


    }

    public void shot() {
        long time = System.currentTimeMillis();

        if (time - lastshot > rateOfFire) {
            Bullet bullet = new Bullet(container, game, this, time);
            lastshot = time;
            bullets.add(bullet);
            synchronized (game.bullets) {
                game.bullets.add(bullet);
            }
        }
    }

    public ImageView getGraphicsImg() {
        return graphicsImg;
    }

    public void update() {

        double cTyre = 0.6;

        double dif = cTyre * (velocity.getSize() / 30.0);
        {
            double difAngle = velocity.getRelativeAngleBetween(direction);
            if (!Double.isNaN(difAngle)) {
                double r = Math.random() * 50;
                velocity.rotateZ(difAngle / ((50 + r) * (5 * dif)));

                isDrifting = Math.abs(Math.toDegrees(difAngle)) > 30;
            }
        }

        if (isUP()) {
            engineForce = MAX_ENGINE_FORCE;
        } else if (isDOWN()) {
            engineForce = -MAX_ENGINE_FORCE;
        } else {
            engineForce = 0;
        }

        if (isDrifting) {
            engineForce = engineForce / 5;
        }


        if (game.isServer()) {
            checkBulletLife();
            updateAndCheckBulletIntersection();
            checkEnemyIntersection();
        }

        calculateTraction();
        calculateAcceleration();
        calculateVelocity(1);
        calculatePosition(1);

        if (mousePressed.get()) {
            shot();
        }

        draw();
    }

    private void draw() {
        center.x = (position.x + position.x + w) / 2;
        center.y = (position.y + position.y + h) / 2;

        startX.set(center.x);
        startY.set(center.y);


        endX.set(mousePosition_X.get());
        endY.set(mousePosition_Y.get());

        double rad = Math.atan2(mousePosition_Y.get() - center.y, mousePosition_X.get() - center.x);
        direction.x = Math.cos(rad);
        direction.y = Math.sin(rad);



        double degree = Math.toDegrees(rad);

        if (!(!game.isServer() && game.getRemotePlayer() == this)) {
            rotateProperty.set(degree);
        }

        graphics.setX(position.x);
        graphics.setY(position.y);
        graphicsImg.setX(position.x);
        graphicsImg.setY(position.y);
    }

    private void updateAndCheckBulletIntersection() {
        Set<Bullet> bulletForRemove = new HashSet<>();
        Player otherPlayer;
        if (game.getRemotePlayer() == this) {
            otherPlayer = game.getCurrentPlayer();
        } else {
            otherPlayer = game.getRemotePlayer();
        }

        for (Bullet bullet : bullets) {
            bullet.update();

            boolean collisionDetected = false;

            Enemy tmpEnemy = null;

            synchronized (game.enemies) {
                Iterator<Enemy> iter = game.enemies.iterator();
                while (iter.hasNext()) {
                    Enemy enemy = iter.next();
                    if (bullet.getGraphics().intersects(enemy.getGraphics().getBoundsInLocal())) {
                        collisionDetected = true;
                        bulletForRemove.add(bullet);
                        tmpEnemy = enemy;
                        point += INCREASE_POINT;
                    }
                }
            }


            if (collisionDetected) {
                //System.out.println("BOOOM");
                synchronized (game.enemies) {
                    game.enemies.remove(tmpEnemy);
                }
                container.getChildren().removeAll(bullet.getGraphics(), tmpEnemy.getGraphics(), tmpEnemy.getGraphicsImg());
                continue;
            }

            //For player

            if (bullet.getGraphics().intersects(otherPlayer.getGraphics().getBoundsInLocal())) {
                bulletForRemove.add(bullet);
                if (otherPlayer.point != 0) {

                    if (otherPlayer.point - DECREASE_POINT < 0) {
                        point += otherPlayer.point;
                        otherPlayer.point = 0;
                    } else {
                        point += DECREASE_POINT;
                        otherPlayer.point -= DECREASE_POINT;
                    }
                }
                container.getChildren().removeAll(bullet.getGraphics());
            }
        }
        bullets.removeAll(bulletForRemove);
        synchronized (game.bullets) {
            game.bullets.removeAll(bulletForRemove);
        }
    }

    private void checkEnemyIntersection() {
        long currentTime = System.currentTimeMillis();

        synchronized (game.enemies) {
            Iterator<Enemy> iter = game.enemies.iterator();
            while (iter.hasNext()) {
                Enemy enemy = iter.next();
                if (currentTime - enemy.lastCrashTime > 1000
                        && enemy.getGraphics().intersects(graphics.getBoundsInLocal())) {
                    enemy.lastCrashTime = currentTime;
                    if (point - DECREASE_POINT < 0) {
                        point = 0;
                    } else {
                        point -= DECREASE_POINT;
                    }
                }
            }
        }
    }


    public void checkBulletLife() {//Сделать поиск по все пулям
        long currentTime = System.currentTimeMillis();

        ArrayList<Bullet> tmp = new ArrayList<>();

        for (Bullet bullet : bullets) {
            if (currentTime - bullet.getTimeShot() > lifeBullet) {
                tmp.add(bullet);
            }
        }
        for (Bullet bullet : tmp) {
            container.getChildren().remove(bullet.getGraphics());
        }
        bullets.removeAll(tmp);
        synchronized (game.bullets) {
            game.bullets.removeAll(tmp);
        }
    }


    private Point2D driftLastPoint1;
    private Point2D driftLastPoint2;

    public void drawTrack() {
        double angle = -Math.atan2(direction.x, direction.y);
        if (isDrifting) {
            Affine transf = new Affine();
            transf.rotate(angle, position.x, position.y + 0);

            if (driftLastPoint1 != null && driftLastPoint2 != null) {
                Point2D p1 = new Point2D((int) (position.x - 10), (int) (position.y - 15));
                Point2D p2 = new Point2D((int) (position.x + 10), (int) (position.y - 15));

                Line line = new Line((int) p1.getX(), (int) p1.getY(), (int) driftLastPoint1.getX(), (int) driftLastPoint1.getY());
                Line line2 = new Line((int) p2.getX(), (int) p2.getY(), (int) driftLastPoint2.getX(), (int) driftLastPoint2.getY());

                line.setFill(new Color(0, 0, 0, 0.9));
                line2.setFill(new Color(0, 0, 0, 0.9));

                line.setStrokeWidth(3);
                line2.setStrokeWidth(3);
                line.setOpacity(1);
                line2.setOpacity(1);


                container.getChildren().addAll(line, line2);

            }

            driftLastPoint1 = new Point2D((int) (position.x - 10), (int) (position.y - 15));
            driftLastPoint2 = new Point2D((int) (position.x + 10), (int) (position.y - 15));
        } else {
            driftLastPoint1 = null;
            driftLastPoint2 = null;
        }
    }

    public DoubleProperty getRotateProperty() {
        return rotateProperty;
    }

    public void setRotateProperty(double rotateProperty) {
        this.rotateProperty.set(rotateProperty);
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


    public double getSpeed() {
        return speed;
    }

    public boolean isColliding() {
        return isColliding;
    }

    public void setColliding(boolean colliding) {
        isColliding = colliding;
    }

    public boolean isUP() {
        return UP.get();
    }

    public BooleanProperty UPProperty() {
        return UP;
    }

    public void setUP(boolean UP) {
        this.UP.set(UP);
    }

    public boolean isDOWN() {
        return DOWN.get();
    }

    public BooleanProperty DOWNProperty() {
        return DOWN;
    }

    public void setDOWN(boolean DOWN) {
        this.DOWN.set(DOWN);
    }

    public double getMousePosition_X() {
        return mousePosition_X.get();
    }

    public DoubleProperty mousePosition_XProperty() {
        return mousePosition_X;
    }

    public void setMousePosition_X(double mousePosition_X) {
        this.mousePosition_X.set(mousePosition_X);
    }

    public double getMousePosition_Y() {
        return mousePosition_Y.get();
    }

    public DoubleProperty mousePosition_YProperty() {
        return mousePosition_Y;
    }

    public void setMousePosition_Y(double mousePosition_Y) {
        this.mousePosition_Y.set(mousePosition_Y);
    }

    public Vector3 getCenter() {
        return center;
    }

    public boolean isMousePressed() {
        return mousePressed.get();
    }

    public BooleanProperty mousePressedProperty() {
        return mousePressed;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed.set(mousePressed);
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
