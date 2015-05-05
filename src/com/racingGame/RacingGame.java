package com.racingGame;

import com.racingGame.levels.level01.Level_01;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class RacingGame extends Application {

    public static double width = 800, height = 600;
    static double FPS = 30.0;
    ArrayList<Line> edges = new ArrayList<>();
    Pane container;
    Polyline linesUpper;
    Polyline linesLower;
    int laps = 1;
    Line startLine;
    boolean gameStart = true;
    static String address;
    static boolean server;
    DataOutputStream out;
    DataInputStream in;
    Car car1;
    Car car2;
    double x = 0, y = 0, z = 0;
    ArrayList<Line> checkPoints = new ArrayList<>();
    int checks = 0;
    ImageView clouds[] = new ImageView[5];
    SpeedMeter meter;
    boolean keyPressed = false;
    KeyCode keyPressedCode = null;
    StatusUpdater statusUpdater = new StatusUpdater(width / 2 - 150, height / 2 - 35);
    Timeline gameLoop;
    long time = 0;

    // p1
    @Override

    public void start(Stage primaryStage) throws Exception {
        container = new Pane();
        Scene scene = new Scene(container, width, height);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadLevel(Level_01.lowerBounds, Level_01.upperBounds, container.getChildren());
        car1 = new Car(server);
        car2 = new Car(!server);
        if (server) {
            car1.setLocationByVector(Level_01.startCar1[0] - car1.w, height - Level_01.startCar1[1]);
            car1.setDirection(90);
            car2.setLocationByVector(Level_01.startCar2[0] - car2.w, height - Level_01.startCar2[1]);
            car2.setDirection(90);
            car1.getGraphics().setFill(Color.MEDIUMPURPLE);
            car2.getGraphics().setFill(Color.ORANGE);

        } else {
            car2.setLocationByVector(Level_01.startCar1[0] - car1.w, height - Level_01.startCar1[1]);
            car2.setDirection(90);
            car1.setLocationByVector(Level_01.startCar2[0] - car2.w, height - Level_01.startCar2[1]);
            car1.setDirection(90);
            car2.getGraphics().setFill(Color.MEDIUMPURPLE);
            car1.getGraphics().setFill(Color.ORANGE);
        }

        container.getChildren().addAll(car1.getGraphicsImg(), car2.getGraphicsImg());
        container.setOnKeyPressed(new EventHandler<KeyEvent>() {

            public void handle(KeyEvent e) {
                keyPressed = true;
                keyPressedCode = e.getCode();
            }
        });
        container.setOnKeyReleased(new EventHandler<KeyEvent>() {

            public void handle(KeyEvent e) {
                keyPressed = false;
            }
        });

        gameLoop = new Timeline(new KeyFrame(Duration.millis(1000 / FPS), new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                if (keyPressed) {
                    if (car1.speed != 0) {

                        if (keyPressedCode == KeyCode.LEFT) {
                            if (car1.speed != 0) {
                                car1.setDirection(car1.direction += (3));
                            }

                        } else if (keyPressedCode == KeyCode.RIGHT) {
                            if (car1.speed != 0)
                                car1.setDirection(car1.direction -= (3));
                        }
                    }

                    if (keyPressedCode == KeyCode.UP) {
                        car1.speed += 0.05;
                    } else if (keyPressedCode == KeyCode.DOWN) {

                        car1.speed -= 0.05;
                    }

                }

                car1.translateByRadius(car1.speed);

                meter.setSpeed(car1.speed);

                checkForCollisions(car1);

                try {
                    if (out != null) {
                        out.writeDouble(car1.direction);
                        out.writeDouble(car1.locationX.get());
                        out.writeDouble(car1.locationY.get());
                    }
                } catch (Exception ex) {

                }
                try {
                    if (in != null) {
                        car2.setDirection(x);
                        car2.locationX.set(y);
                        car2.locationY.set(z);
                    }
                } catch (Exception ex) {

                }
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);

        gameLoop.play();
        meter = new SpeedMeter();

        container.getChildren()
                .add(meter);
        meter.setLayoutX(
                110);
        meter.setLayoutY(height
                - 10);
        if (server) {
            Rectangle r = new Rectangle(width, height);
            r.setFill(Color.WHITE);
            r.setOpacity(0.6);
            container.getChildren().addAll(r);
            Text t = new Text("Waiting for player2 ...");
            ProgressIndicator p = new ProgressIndicator();

            p.setLayoutX(width / 2 + 50);
            p.setLayoutY(height / 2 - 33);
            t.setX(width / 2 - 200);
            t.setY(height / 2);
            t.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 17));
            t.setFill(new Color(107 / 255.0, 162 / 255.0, 252 / 255.0, 1.0));
            container.getChildren().addAll(t, p);
        }

        new Thread(new Runnable() {

            public void run() {
                try {
                    Socket socket;
                    if (server) {
                        ServerSocket serverSocket = new ServerSocket(8888);
                        socket = serverSocket.accept();
                        Platform.runLater(new Runnable() {

                            public void run() {
                                container.getChildren().remove(container.getChildren().size() - 3, container.getChildren().size());
                                container.requestFocus();
                                time = System.currentTimeMillis();
                            }
                        });
                    } else {
                        socket = new Socket(address, 8888);
                        Platform.runLater(new Runnable() {

                            public void run() {
                                container.requestFocus();
                                time = System.currentTimeMillis();

                            }
                        });
                    }
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                    in = new DataInputStream(socket.getInputStream());
                    while (true) {
                        x = in.readDouble();
                        y = in.readDouble();
                        z = in.readDouble();
                    }
                } catch (Exception e) {
                    System.out.println("Server error" + e.toString());
                }
            }
        }
        ).start();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent e) {
                System.exit(1);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void loadLevel(double[] level_upper, double[] level_lower, ObservableList<Node> list) {
        linesUpper = new Polyline(level_upper);
        linesLower = new Polyline(level_lower);
        list.add(Level_01.levelGraphics);
        Line l;
        for (int i = 0; i < Level_01.checkPoints.length; i += 4) {

            l = new Line(Level_01.checkPoints[i], Level_01.checkPoints[i + 1], Level_01.checkPoints[i + 2], Level_01.checkPoints[i + 3]);
            l.setStroke(Color.GREEN);
            l.setStrokeWidth(5);
            l.setOpacity(0.2);
            checkPoints.add(l);
            list.add(l);
        }

        list.addAll(statusUpdater);
    }

    private void checkForCollisions(Car car) {

        Path p3;
        for (int i = 0; i < checkPoints.size(); i++) {
            p3 = (Path) Shape.intersect(car.bounds, checkPoints.get(i));
            if (!p3.getElements().isEmpty()) {

                if (checks == i && checks == 0) {
                    if (laps == 0) {
                        time = System.currentTimeMillis() - time;
                        statusUpdater.setText("Finished in: " + String.format("%.2f", time / 1000.0) + " seconds");
                        gameLoop.stop();

                    } else {
                        statusUpdater.setTextAndAnimate("Laps Left: " + laps);
                        laps--;

                        checks++;
                        for (int j = 1; j < checkPoints.size(); j++) {
                            checkPoints.get(j).setStroke(Color.GREEN);
                        }
                        break;
                    }

                } else if (checks == i) {
                    checkPoints.get(i).setStroke(Color.RED);
                    statusUpdater.setTextAndAnimate("CheckPoint : " + checks);

                    checks++;
                    break;
                }
                if (checks == checkPoints.size()) {
                    checks = 0;
                }
            }
        }
        if (CollisionDetectors.PolylineIntersection(car.bounds, linesLower) || CollisionDetectors.PolylineIntersection(car.bounds, linesUpper)) {
            if (!car.isColliding) {
                car.speed *= -0.5;
            }

        } else {
            car.isColliding = false;
        }
    }

}

class Car {

    double w = 35, h = 18;
    public Rectangle graphics;
    public DoubleProperty locationX, locationY;
    public double direction = 0;
    double speed = 0;
    Line edgeLeft, edgeRight, edgeDown, edgeUp;
    boolean isColliding;
    ImageView graphicsImg;

    public Rectangle getGraphics() {
        return graphics;
    }
    Polyline bounds = new Polyline(
            34.0, 13.0, 34.0, 6.0,
            34.0, 7.0, 30.0, 3.0,
            30.0, 3.0, 6.0, 3.0,
            6.0, 3.0, 2.0, 7.0,
            2.0, 8.0, 2.0, 14.0,
            2.0, 14.0, 7.0, 16.0,
            7.0, 16.0, 32.0, 15.0,
            32.0, 15.0, 34.0, 14.0
    );

    public Car(boolean primary) {
        if (primary)
            graphicsImg = new ImageView(new Image(RacingGame.class.getResourceAsStream("car1.png")));
        else
            graphicsImg = new ImageView(new Image(RacingGame.class.getResourceAsStream("car2.png")));
        locationX = new SimpleDoubleProperty(0);
        locationY = new SimpleDoubleProperty(0);
        graphics = new Rectangle(w, h);
        graphics.setStroke(Color.BLACK);
        graphics.setRotationAxis(Rotate.Z_AXIS);
        graphics.xProperty().bind(locationX.add(w / 2));
        graphics.yProperty().bind(locationY.multiply(-1).add(RacingGame.height - w / 2));
        graphicsImg.setRotationAxis(Rotate.Z_AXIS);
        graphicsImg.xProperty().bind(graphics.xProperty());
        graphicsImg.yProperty().bind(graphics.yProperty());
        graphicsImg.rotateProperty().bind(graphics.rotateProperty());
        bounds.translateXProperty().bind(graphics.xProperty());
        bounds.translateYProperty().bind(graphics.yProperty());
        bounds.rotateProperty().bind(graphics.rotateProperty());
        graphics.setFill(Color.MEDIUMPURPLE);
        graphics.setWidth(w);
        graphics.setHeight(h);

    }

    public void translateByVector(Vector v) {
        locationX.set(locationX.get() + v.getX());
        locationY.set(locationY.get() + v.getY());

    }

    public void translateByVector(double x, double y) {
        locationX.set(locationX.get() + x);
        locationY.set(locationY.get() + y);

    }

    public void setLocationByVector(Vector vector) {
        locationX.set(vector.getX());
        locationY.set(vector.getY());

    }

    public void setLocationByVector(double x, double y) {
        locationX.set(x);
        locationY.set(y);
    }

    public void setDirection(double angle) {
        graphics.setRotate(180 - angle);
        direction = angle;
    }

    public void translateByRadius(double d) {
        Vector v = new Vector(d, direction / 180.0 * Math.PI, Vector.Polar);
        translateByVector(v);
    }

    public ImageView getGraphicsImg() {
        return graphicsImg;
    }
}
