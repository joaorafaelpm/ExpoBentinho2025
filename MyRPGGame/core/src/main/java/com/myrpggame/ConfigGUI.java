package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Config.VideoGUI;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConfigGUI {
    private final Scene scene;

    public ConfigGUI(Stage stage) {
        Button voltar = new Button("Voltar");
        Button video = new Button("Video");

        voltar.setOnAction(e -> {
            MenuGUI menuView = new MenuGUI(stage);
            stage.setScene(menuView.getScene());
        });
        video.setOnAction(e -> {
            VideoGUI menuView = new VideoGUI(stage);
            stage.setScene(menuView.getScene());
        });

        VBox root = new VBox(20 , video, voltar );
        scene = new Scene(root, GameResolution.getLargura(), GameResolution.getAltura());
    }

    public Scene getScene() {
        return scene;
    }
}
