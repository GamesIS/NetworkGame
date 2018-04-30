package ru.ilku0917.networkGame.model;

import java.io.Serializable;

public class ConditionClient implements Serializable {
    public boolean UP;
    public boolean DOWN;
    public boolean mousePressed;
    public double mousePosition_X, mousePosition_Y;
    public int ping;

    public ConditionClient(Player player, int ping) {
        this.ping = ping;
        UP = player.isUP();
        DOWN = player.isDOWN();
        mousePosition_X = player.getMousePosition_X();
        mousePosition_Y = player.getMousePosition_Y();
        mousePressed = player.isMousePressed();
    }
}
