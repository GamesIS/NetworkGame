package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.Vector3;

import java.io.Serializable;

public class ConditionBullet implements Serializable {
    public Vector3 direction;
    public Vector3 position;

    public ConditionBullet(Bullet bullet) {
        direction = bullet.direction;
        position = bullet.position;
    }

}
