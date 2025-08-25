package com.myrpggame.Config;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.MenuGUI;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VideoGUI {
    private final Scene scene;

    public VideoGUI(Stage stage) {
        Button voltar = new Button("Voltar");
        Button resolucao800x600 = new Button("800 x 600");
        Button resolucao1280x720 = new Button("1280 x 720");
        Button resolucao1920x1080 = new Button("1920 x 1080");
        CheckBox fullScreen = new CheckBox("Full Screen");

        // Botões de resolução
        resolucao800x600.setOnAction(e -> aplicarResolucao(stage, 800, 600));
        resolucao1280x720.setOnAction(e -> aplicarResolucao(stage, 1280, 720));
        resolucao1920x1080.setOnAction(e -> aplicarResolucao(stage, 1920, 1080));
        fullScreen.setOnAction(e -> aplicarFullScreen(stage));

        // Botão de voltar
        voltar.setOnAction(e -> {
            MenuGUI menuView = new MenuGUI(stage);
            stage.setScene(menuView.getScene());
        });

        VBox root = new VBox(20, resolucao800x600, resolucao1280x720, resolucao1920x1080, fullScreen ,voltar);
        scene = new Scene(root, GameResolution.getLargura(), GameResolution.getAltura());
    }

    private void aplicarResolucao(Stage stage, double largura, double altura) {
        GameResolution.setResolucao(stage , largura, altura);

        // Atualiza a resolução da própria cena
        stage.setWidth(largura);
        stage.setHeight(altura);
    }

    private void aplicarFullScreen(Stage stage) {
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            return ;
        }
        stage.setFullScreen(true);
    }

    public Scene getScene() {
        return scene;
    }
}
