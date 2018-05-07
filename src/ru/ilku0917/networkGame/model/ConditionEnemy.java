package ru.ilku0917.networkGame.model;

import ru.ilku0917.networkGame.Vector3;

import java.io.Serializable;

public class ConditionEnemy implements Serializable{
    public Vector3 direction;
    public Vector3 position;

    public ConditionEnemy(Enemy enemy) {
        direction = enemy.direction;
        position = enemy.position;
    }
}
