package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.NetworkGame;
import ru.ilku0917.networkGame.Vector3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class ConditionGame implements Serializable {
    public Vector3 position1, position2, direction1, direction2;
    public double rotatePropery;
    public int point1, point2;
    public ArrayList<ConditionBullet> bullets = new ArrayList<>();
    public ArrayList<ConditionEnemy> enemies = new ArrayList<>();


    public ConditionGame() {
    }

    public ConditionGame(NetworkGame game) {
        position1 = game.getCurrentPlayer().position;
        position2 = game.getRemotePlayer().position;

        direction1 = game.getCurrentPlayer().direction;
        direction2 = game.getRemotePlayer().direction;

        point1 = game.getCurrentPlayer().getPoint();
        point2 = game.getRemotePlayer().getPoint();

        rotatePropery = game.getCurrentPlayer().getRotateProperty().doubleValue();

        initConditionBullets(game);
        initConditionEnemies(game);
    }

    private void initConditionBullets(NetworkGame game) {
        synchronized (game.bullets) {
            Iterator<Bullet> i = game.bullets.iterator();
            while (i.hasNext()) {
                Bullet bullet = i.next();
                bullets.add(bullet.getConditionBullet());
            }
        }
    }

    private void initConditionEnemies(NetworkGame game) {
        synchronized (game.enemies) {
            Iterator<Enemy> i = game.enemies.iterator();
            while (i.hasNext()) {
                Enemy enemy = i.next();
                enemies.add(enemy.getConditionEnemy());
            }
        }
    }
}
