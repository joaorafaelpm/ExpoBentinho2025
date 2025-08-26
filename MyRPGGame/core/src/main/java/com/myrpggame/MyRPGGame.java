package com.myrpggame;

import javafx.application.Application;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MyRPGGame extends Application {

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        // Começa com o Menu
        MenuGUI menu = new MenuGUI(primaryStage);

        primaryStage.setTitle("My RPG Game");
        primaryStage.setScene(menu.getScene());
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
