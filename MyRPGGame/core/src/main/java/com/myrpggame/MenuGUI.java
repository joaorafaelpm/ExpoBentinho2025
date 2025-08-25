package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuGUI {
    private final Scene scene;

    public MenuGUI(Stage stage) {

        Button startButton = new Button("Começar");
        Button configButton = new Button("Configurações");
        Button quitButton = new Button("Sair");

        startButton.setOnAction(e -> {
            // Quando clica em Start, troca para a cena do jogo
            mostrarJogo(stage);
        });

        configButton.setOnAction(e -> {
            mostrarConfiguracoes(stage);
        });

        quitButton.setOnAction(e -> {
            Platform.exit();
        });

        VBox root = new VBox(20 , startButton , configButton , quitButton);
        scene = new Scene(root, GameResolution.getLargura(), GameResolution.getAltura());
    }

    private static void mostrarJogo(Stage stage) {
        GameGUI gameView = new GameGUI(stage, GameResolution.getLargura() , GameResolution.getAltura());
        stage.setScene(gameView.getScene());
    }
    private static void mostrarConfiguracoes(Stage stage) {
        ConfigGUI configGUI = new ConfigGUI(stage);
        stage.setScene(configGUI.getScene());
    }

    public Scene getScene() {
        return scene;
    }
}
