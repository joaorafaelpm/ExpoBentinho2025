package com.myrpggame.Config;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.MenuGUI;
import com.myrpggame.Utils.WinTimes;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class VideoGUI {
    private final Scene scene;
    private final WinTimes winTimes;

    public VideoGUI(Stage stage , WinTimes winTimes) {

        this.winTimes = winTimes;

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
            MenuGUI menuView = new MenuGUI(stage , winTimes);
            GameResolution.changeScene(stage , menuView.getScene());
        });

        VBox root = new VBox(20, resolucao800x600, resolucao1280x720, resolucao1920x1080, fullScreen ,voltar);
        scene = new Scene(root , getLargura() , getAltura());
    }


    private void aplicarResolucao(Stage stage, double largura, double altura) {
        if (!stage.isFullScreen()) {
            // Atualiza a resolução da própria cena
            stage.setWidth(largura);
            stage.setHeight(altura);
        }
        stage.setFullScreen(true);
    }

    private void aplicarFullScreen(Stage stage) {
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            GameResolution.isFullScreen(stage);
            return ;
        }
        stage.setFullScreen(true);
        GameResolution.isFullScreen(stage);
        }

    public Scene getScene() {
        return scene;
    }
}
