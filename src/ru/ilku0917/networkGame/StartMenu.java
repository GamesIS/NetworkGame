package ru.ilku0917.networkGame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Первичное окно для подключения. Начинается все с него
 */

public class StartMenu extends Application {

    static Stage window;

    @Override
    public void start(Stage window) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("DialogDesign.fxml"));
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.show();
        StartMenu.window = window;

    }

    public static void main(String[] args) {
        launch(args);
    }

}
