/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilku0917.networkGame;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogDesignController implements Initializable {

    @FXML
    Button continuebtn;
    @FXML
    ToggleButton playbtn;
    @FXML
    ToggleButton hostbtn;
    @FXML
    TextField address;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playbtn.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                playbtn.setSelected(true);
                hostbtn.setSelected(false);
                address.setDisable(false);
                continuebtn.setDisable(false);
            }
        });
        hostbtn.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                hostbtn.setSelected(true);
                playbtn.setSelected(false);
                continuebtn.setDisable(false);
            }
        });
        continuebtn.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                NetworkGame.setAddress(address.getText());
                NetworkGame.setServer(hostbtn.isSelected());
                NetworkGame game = new NetworkGame();
                try {
                    game.start(StartMenu.window);
                    StartMenu.window.centerOnScreen();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
