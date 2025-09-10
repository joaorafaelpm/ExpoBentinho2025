package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Enum.Difficulty;
import com.myrpggame.Utils.DifficultyLevel;
import com.myrpggame.Utils.WinTimes;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class DifficultyGUI {
    private final Scene scene;
    private final WinTimes winTimes ;

    public DifficultyGUI(Stage stage , WinTimes winTimes) {
        this.winTimes = winTimes;
        Button facil = new Button("Fácil");
        Button normal = new Button("Normal");
        Button dificil = new Button("Difícil");
        Button impossivel = new Button("Impossivel");
        Button voltar = new Button("Voltar");

        facil.setOnAction(e -> {
            DifficultyLevel.setDifficulty(Difficulty.EASY);
            iniciarJogo(stage);
        });
        normal.setOnAction(e -> {
            DifficultyLevel.setDifficulty(Difficulty.MEDIUM);
            iniciarJogo(stage);
        });
        dificil.setOnAction(e -> {
            DifficultyLevel.setDifficulty(Difficulty.HARD);
            iniciarJogo(stage);
        });
        impossivel.setOnAction(e -> {
            DifficultyLevel.setDifficulty(Difficulty.DEV);
            iniciarJogo(stage);
        });

        facil.setFocusTraversable(false);
        normal.setFocusTraversable(false);
        dificil.setFocusTraversable(false);
        impossivel.setFocusTraversable(false);

        voltar.setOnAction(e -> {
            MenuGUI menuGUI = new MenuGUI(stage , winTimes);
            GameResolution.changeScene(stage, menuGUI.getScene());
        });

        voltar.setFocusTraversable(false);





        VBox root = new VBox(20, facil, normal, dificil, impossivel, voltar);
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, getLargura(), getAltura());

        facil.getStyleClass().add("facil");
        normal.getStyleClass().add("normal");
        dificil.getStyleClass().add("dificil");
        impossivel.getStyleClass().add("impossivel");
        voltar.getStyleClass().add("voltar");

        scene.getStylesheets().add(getClass().getResource("/styles/styleDifficulty.css").toExternalForm());

    }

    private void iniciarJogo(Stage stage) {
        GameGUI gameView = new GameGUI(stage , winTimes);
        GameResolution.changeScene(stage, gameView.getScene());
    }

    public Scene getScene() {
        return scene;
    }
}
