package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Config.VideoGUI;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.myrpggame.Config.GameResolution.GameResolution.*;

public class ConfigGUI {
    private final Scene scene;

    public ConfigGUI(Stage stage) {
        Button voltar = new Button("Voltar");
        Button video = new Button("Video");

        voltar.setOnAction(e -> {
            MenuGUI menuView = new MenuGUI(stage);
            GameResolution.changeScene(stage , menuView.getScene());

        });
        video.setOnAction(e -> {
            VideoGUI menuView = new VideoGUI(stage);
            GameResolution.changeScene(stage , menuView.getScene());

        });

        VBox root = new VBox(20 , video, voltar );
        scene = new Scene(root, getLargura(), getAltura());
    }

    public Scene getScene() {
        return scene;
    }
}
