package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Utils.WinTimes;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class MenuGUI {
    private final Scene scene;
    private final WinTimes winTimes;
    public MenuGUI(Stage stage , WinTimes winTimes) {
        this.winTimes = winTimes;
        Button startButton = new Button("Começar");
        Button configButton = new Button("Configurações");
        Button quitButton = new Button("Sair");

        startButton.setOnAction(e -> {
            DifficultyGUI difficultyGUI = new DifficultyGUI(stage , winTimes);
            GameResolution.changeScene(stage, difficultyGUI.getScene());
        });


        configButton.setOnAction(e -> {
            mostrarConfiguracoes(stage);
        });

        quitButton.setOnAction(e -> {
            Platform.exit();
        });

        VBox root = new VBox(20 , startButton , configButton , quitButton);
        root.setAlignment(Pos.CENTER);
        scene = new Scene(root , getLargura() , getAltura());
        scene.getStylesheets().add(getClass().getResource("/styles/styleMenu.css").toExternalForm());
    }

    private void mostrarConfiguracoes(Stage stage) {
        ConfigGUI configGUI = new ConfigGUI(stage , winTimes);
        GameResolution.changeScene(stage , configGUI.getScene());
    }

    public Scene getScene() {
        return scene;
    }
}
