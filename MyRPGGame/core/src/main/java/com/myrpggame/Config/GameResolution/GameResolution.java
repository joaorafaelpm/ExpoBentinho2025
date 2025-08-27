package com.myrpggame.Config.GameResolution;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameResolution {

    static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    static double largura = screenBounds.getWidth();
    static double altura = screenBounds.getHeight();
    static boolean isFullScreen = false ;
    private static final List<Stage> stages = new ArrayList<>();

    public static double getLargura() {
        return largura;
    }

    public static double getAltura() {
        return altura;
    }


    public static boolean isFullScreen (Stage stage) {
        isFullScreen = stage.isFullScreen() ? true : false ;
        return isFullScreen;
    }

    public static void changeScene(Stage stage, Scene scene) {
        boolean wasFullScreen = stage.isFullScreen();
        stage.setScene(scene);
        if (wasFullScreen) {
            stage.setFullScreen(true);
        }
    }

    public static void setResolucao(Stage stage ,double novaLargura, double novaAltura) {
        if (stage.isFullScreen()) return; // não muda nada se estiver em fullscreen
        largura = novaLargura;
        altura = novaAltura;

        // atualiza todos os stages registrados
        for (Stage s : stages) {
            s.setWidth(largura);
            s.setHeight(altura);
        }
    }


}
