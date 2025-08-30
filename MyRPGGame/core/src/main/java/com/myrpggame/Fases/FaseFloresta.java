package com.myrpggame.Fases;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class FaseFloresta {

    private final Pane root = new Pane();

    public FaseFloresta() {
        root.setPrefSize(4000, 600);
        criarFase();
    }

    private void criarFase() {
        // Fundo
        Rectangle fundo = new Rectangle(4000, 600, Color.DARKOLIVEGREEN);
        root.getChildren().add(fundo);

        // Chão
        Rectangle chao = new Rectangle(4000, 50, Color.SADDLEBROWN);
        chao.setTranslateY(550);
        root.getChildren().add(chao);

        // Plataformas
        for (int i = 1; i <= 10; i++) {
            Rectangle plat = new Rectangle(150, 20, Color.FORESTGREEN);
            plat.setTranslateX(i * 350);
            plat.setTranslateY(450 - (i % 3) * 50);
            root.getChildren().add(plat);
        }

        // Árvores
        for (int i = 0; i < 15; i++) {
            Rectangle tronco = new Rectangle(20, 60, Color.SIENNA);
            tronco.setTranslateX(i * 250 + 50);
            tronco.setTranslateY(490);
            root.getChildren().add(tronco);

            Circle copa = new Circle(40, Color.DARKGREEN);
            copa.setTranslateX(i * 250 + 60);
            copa.setTranslateY(470);
            root.getChildren().add(copa);
        }
    }

    public Pane getRoot() {
        return root;
    }
}
