package com.myrpggame.Config.GameResolution;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameResolution {

    static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    static double largura = screenBounds.getWidth();
    static double altura = screenBounds.getHeight();

    private static final List<Stage> stages = new ArrayList<>();

    public static double getLargura() {
        return largura;
    }

    public static double getAltura() {
        return altura;
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
