package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Config.VideoGUI;
import com.myrpggame.Utils.WinTimes;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.myrpggame.Config.GameResolution.GameResolution.*;

public class ConfigGUI {
    private final Scene scene;
    private final WinTimes winTimes;

    public ConfigGUI(Stage stage , WinTimes winTimes) {
        this.winTimes = winTimes;
        Button voltar = new Button("Voltar");
        Button video = new Button("Video");

        voltar.setOnAction(e -> {
            MenuGUI menuView = new MenuGUI(stage , winTimes);
            GameResolution.changeScene(stage , menuView.getScene());

        });
        video.setOnAction(e -> {
            VideoGUI menuView = new VideoGUI(stage , winTimes);
            GameResolution.changeScene(stage , menuView.getScene());

        });

        VBox root = new VBox(20 , video, voltar );
        scene = new Scene(root, getLargura(), getAltura());
    }

    public Scene getScene() {
        return scene;
    }
}
