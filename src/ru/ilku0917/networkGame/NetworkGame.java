package ru.ilku0917.networkGame;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import ru.ilku0917.networkGame.model.*;
import ru.ilku0917.networkGame.network.Client;
import ru.ilku0917.networkGame.network.Server;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkGame extends Application {

    private NetworkGame networkGame;

    public static final int MAX_POINT = 4000;
    private static final double MAX_FPS = 60.0;
    private static final int MAX_ENEMIES = 200;

    public static double width = 800, height = 600;
    private static String address;
    private static Boolean server;
    private Pane container;
    public static boolean gameStart;

    //MediaPlayer mediaPlayer;


    private Player currentPlayer;
    private Player remotePlayer;

    private Timeline gameLoop;

    private Rectangle waitRectangle;
    private Text waitText;
    private ProgressIndicator waitIndicator;

    private double FPS = MAX_FPS;

    private Client client;
    private Server host;
    private int ping;
    private boolean connectionSuccessfuly;


    private Stage stage;

    public ArrayList<Player> players = new ArrayList<>();

    public final List<Enemy> enemies = Collections.synchronizedList(new ArrayList<>());
    public final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());

    private static final BackgroundImage myBI = new BackgroundImage(new Image(NetworkGame.class.getResourceAsStream("space.jpg")),
            BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);

    // p1
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        container = new Pane();
        networkGame = this;

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        width = primaryScreenBounds.getWidth();
        height = primaryScreenBounds.getHeight();
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());

        Scene scene = new Scene(container, width, height);

        //String file = "C:\\Users\\Ilya\\IdeaProjects\\Racing-Game\\src\\com\\racingGame\\shot.mp3";
        //Media sound = new Media(new File(file).toURI().toString());
        //mediaPlayer = new MediaPlayer(sound);


        primaryStage.setScene(scene);
        primaryStage.show();

        loadLevel();

        mouseMonitoring(scene);

        loadEvents(scene, primaryStage);

        checkFPS();

        loadGameLoop();

        {
            waitRectangle = new Rectangle(width, height);
            waitRectangle.setFill(Color.WHITE);
            waitRectangle.setOpacity(0.6);

            waitIndicator = new ProgressIndicator();
            waitIndicator.setLayoutX(width / 2 + 50);
            waitIndicator.setLayoutY(height / 2 - 33);

            waitText = new Text("Ожидание подключения игрока");
            waitText.setX(width / 2 - 240);
            waitText.setY(height / 2);
            waitText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 17));
            waitText.setFill(new Color(0.098, 0.2039, 0.9882, 1));

            container.getChildren().addAll(waitRectangle, waitText, waitIndicator);
        }

        if(!server){
            waitText.setVisible(false);
            waitIndicator.setVisible(false);
            waitRectangle.setVisible(false);
        }

        loadConnection();

        container.setBackground(new Background(myBI));

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent e) {
                System.exit(1);
            }
        });
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean checkGameEnd() {
        boolean winP1 = currentPlayer.getPoint() >= MAX_POINT;
        boolean winP2 = remotePlayer.getPoint() >= MAX_POINT;

        boolean win = false;

        if (winP1 && winP2) {
            waitText.setText("Ничья!");
            waitText.setFill(new Color(0.9882, 0.6627, 0.0471, 1));
            win = true;
        } else if (winP1) {
            waitText.setText("Вы победили!");
            waitText.setFill(new Color(0.2745, 0.6588, 0, 1));
            win = true;
        } else if (winP2) {
            waitText.setText("Вы проиграли!");
            waitText.setFill(new Color(0.6588, 0.0314, 0, 1));
            win = true;
        }
        if(win){
            waitText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 25));
            waitRectangle.setVisible(true);
            waitText.setVisible(true);
        }

        return win;
    }

    private void loadLevel() {
        currentPlayer = new Player(true, container, this);
        remotePlayer = new Player(false, container, this);

        players.add(currentPlayer);
        players.add(remotePlayer);

        container.getChildren().addAll(currentPlayer.getGraphicsImg(), remotePlayer.getGraphicsImg(), currentPlayer.getGraphics(), remotePlayer.getGraphics());

    }

    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    private void checkFPS() {
        AnimationTimer frameRateMeter = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long oldFrameTime = frameTimes[frameTimeIndex];
                frameTimes[frameTimeIndex] = now;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
                if (frameTimeIndex == 0) {
                    arrayFilled = true;
                }
                if (arrayFilled) {
                    long elapsedNanos = now - oldFrameTime;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                    double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
                    FPS = frameRate;
                }
            }
        };
        frameRateMeter.start();
    }

    private void loadEvents(Scene scene, Stage primaryStage) {
        primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t) {
                    currentPlayer.setUP(false);
                    currentPlayer.setDOWN(false);
                    currentPlayer.setMousePressed(false);
                }
            }
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                        currentPlayer.setUP(true);
                    } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                        currentPlayer.setDOWN(true);
                    }
                });

        scene.addEventFilter(KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                        currentPlayer.setUP(false);
                    } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                        currentPlayer.setDOWN(false);
                    }
                });
    }

    private int i = 0;

    double maxSize = 0;

    int j;

    private void loadGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(1000 / MAX_FPS), new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if(checkGameEnd()){
                    gameLoop.stop();
                }
                if(server){
                    if(gameStart && System.currentTimeMillis() - host.lastTimeRequest > 3000){
                        lostConnection();
                    }
                }
                else {
                    //if(j++ == 5){
                        client.updateCondition();
                      //  j = 0;
                    //}
                }
                if(connectionSuccessfuly){
                    if (i++ > 20) {
                        stage.setTitle("Ping " + ping + "ms " + String.format("FPS: %.3f", FPS) + " Ваши очки:" + currentPlayer.getPoint() + " Очки противника:" + remotePlayer.getPoint());
                        i = 0;
                    }
                    if (server && j++ >= 4) {
                        synchronized (enemies) {
                            if (enemies.size() < MAX_ENEMIES) {
                                enemies.add(new Enemy(container, networkGame));
                                j=0;
                            }
                        }
                    } else {
                        synchronized (bullets) {
                            Iterator<Bullet> iter = bullets.iterator();
                            while (iter.hasNext()) {
                                iter.next().update();
                            }
                        }
                    }

                    currentPlayer.update();
                    remotePlayer.update();

                    maxSize = 0;

                    synchronized (enemies) {
                        Iterator<Enemy> iter = enemies.iterator();
                        while (iter.hasNext()) {
                            iter.next().update();
                        }
                    }
                }
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }


    private void mouseMonitoring(Scene monitored) {
        container.getChildren().add(currentPlayer.line);
        monitored.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String msg =
                        "(x: " + event.getX() + ", y: " + event.getY() + ") -- " +
                                "(sceneX: " + event.getSceneX() + ", sceneY: " + event.getSceneY() + ") -- " +
                                "(screenX: " + event.getScreenX() + ", screenY: " + event.getScreenY() + ")";

                currentPlayer.setMousePosition_X(event.getSceneX());
                currentPlayer.setMousePosition_Y(event.getSceneY());

            }
        });

        monitored.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Курсор за пределами сцены");
            }
        });

        monitored.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentPlayer.setMousePressed(true);
            }
        });

        monitored.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentPlayer.setMousePressed(true);

                currentPlayer.setMousePosition_X(event.getSceneX());
                currentPlayer.setMousePosition_Y(event.getSceneY());
                //mediaPlayer.play();Возможно сделать в отдельном потоке
            }
        });
        monitored.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentPlayer.setMousePressed(false);
                //mediaPlayer.play();Возможно сделать в отдельном потоке
            }
        });
    }


    private void loadConnection() {
        if (server) {
            host = new Server(this);
            host.start();
        } else {
            client = new Client(this);
        }
    }

    public void connectionRestored(){
        connectionSuccessfuly = true;
        waitText.setVisible(false);
        waitRectangle.setVisible(false);
        waitIndicator.setVisible(false);
    }

    public void lostConnection(){
        connectionSuccessfuly = false;
        if(server){
            waitText.setText("Потеряно соединение с игроком");
        }
        else {
            waitText.setText("Потеряно соединение с сервером");
        }
        waitText.setVisible(true);
        waitRectangle.setVisible(true);
        waitIndicator.setVisible(true);
    }

    public void setProperties(ConditionGame properties) {
        currentPlayer.position = properties.position2;
        currentPlayer.direction = properties.direction2;
        currentPlayer.setPoint(properties.point2);

        remotePlayer.position = properties.position1;
        remotePlayer.direction = properties.direction1;
        remotePlayer.setPoint(properties.point1);

        hideUnnecessaryBullets(properties.bullets);
        hideUnnecessaryEnemies(properties.enemies);

        remotePlayer.getRotateProperty().set(properties.rotatePropery);
    }

    public void hideUnnecessaryBullets(List<ConditionBullet> conditionBullets) {
        synchronized (bullets) {
            if (bullets.size() > conditionBullets.size()) {
                int index = 0;
                Iterator<Bullet> i = bullets.iterator();
                while (i.hasNext()) {
                    Bullet bullet = i.next();
                    if (index < conditionBullets.size()) {
                        initializeBullet(bullet, conditionBullets, index);
                    } else {
                        bullet.getGraphics().setVisible(false);
                    }
                    index++;
                }
            } else if (bullets.size() < conditionBullets.size()) {
                for (int index = 0; index < conditionBullets.size(); index++) {
                    if (index < bullets.size()) {
                        initializeBullet(bullets.get(index), conditionBullets, index);
                    } else {
                        bullets.add(new Bullet(conditionBullets.get(index).direction,
                                conditionBullets.get(index).position,
                                this));
                    }
                    index++;
                }
            } else {
                int index = 0;
                Iterator<Bullet> i = bullets.iterator();
                while (i.hasNext()) {
                    Bullet bullet = i.next();
                    initializeBullet(bullet, conditionBullets, index);
                    index++;
                }
            }
        }
    }

    private void initializeBullet(Bullet bullet, List<ConditionBullet> conditionBullets, int index) {
        bullet.position = conditionBullets.get(index).position;
        bullet.direction = conditionBullets.get(index).direction;
        bullet.getGraphics().setVisible(true);
    }

    public void hideUnnecessaryEnemies(List<ConditionEnemy> conditionEnemies) {
        synchronized (enemies) {
            if (enemies.size() > conditionEnemies.size()) {
                int index = 0;
                Iterator<Enemy> i = enemies.iterator();
                while (i.hasNext()) {
                    Enemy enemy = i.next();
                    if (index < conditionEnemies.size()) {
                        initializeEnemy(enemy, conditionEnemies, index);
                    } else {
                        enemy.getGraphicsImg().setVisible(false);
                    }
                    index++;
                }
            } else if (enemies.size() < conditionEnemies.size()) {
                for (int index = 0; index < conditionEnemies.size(); index++) {
                    if (index < enemies.size()) {
                        initializeEnemy(enemies.get(index), conditionEnemies, index);
                    } else {
                        enemies.add(new Enemy(conditionEnemies.get(index).direction,
                                conditionEnemies.get(index).position,
                                this));
                    }
                }
            } else {
                int index = 0;
                Iterator<Enemy> i = enemies.iterator();
                while (i.hasNext()) {
                    Enemy enemy = i.next();
                    initializeEnemy(enemy, conditionEnemies, index);
                    index++;
                }
            }
        }
    }

    private void initializeEnemy(Enemy enemy, List<ConditionEnemy> conditionEnemies, int index) {
        enemy.position = conditionEnemies.get(index).position;
        enemy.direction = conditionEnemies.get(index).direction;
        enemy.getGraphicsImg().setVisible(true);
    }


    public Player getRemotePlayer() {
        return remotePlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public Pane getContainer() {
        return container;
    }

    public void setContainer(Pane container) {
        this.container = container;
    }

    public boolean isConnectionSuccessfuly() {
        return connectionSuccessfuly;
    }

    public void setConnectionSuccessfuly(boolean connectionSuccessfuly) {
        this.connectionSuccessfuly = connectionSuccessfuly;
    }

    public static double getWidth() {
        return width;
    }

    public static void setWidth(double width) {
        NetworkGame.width = width;
    }

    public static double getHeight() {
        return height;
    }

    public static void setHeight(double height) {
        NetworkGame.height = height;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        NetworkGame.address = address;
    }

    public static Boolean isServer() {
        return server;
    }

    public static void setServer(boolean server) {
        NetworkGame.server = server;
    }

    public static void main(String[] args) {
        launch(args);
    }


}