package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
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
        root.setAlignment(Pos.CENTER);
        scene = new Scene(root , getLargura() , getAltura());
        scene.getStylesheets().add(getClass().getResource("/styles/styleMenu.css").toExternalForm());
    }

    private static void mostrarJogo(Stage stage) {
        GameGUI gameView = new GameGUI(stage);
        GameResolution.changeScene(stage , gameView.getScene());
    }
    private static void mostrarConfiguracoes(Stage stage) {
        ConfigGUI configGUI = new ConfigGUI(stage);
        GameResolution.changeScene(stage , configGUI.getScene());
    }

    public Scene getScene() {
        return scene;
    }
}
