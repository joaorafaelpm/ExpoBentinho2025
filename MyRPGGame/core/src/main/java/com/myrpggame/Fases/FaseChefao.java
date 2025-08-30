package com.myrpggame.Fases;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FaseChefao {

    private final Pane root = new Pane();

    public FaseChefao() {
        root.setPrefSize(1900, 600);
        criarFase();
    }

    private void criarFase() {
        // Fundo
        Rectangle fundo = new Rectangle(1900, 600, Color.DIMGRAY);
        root.getChildren().add(fundo);

        // Chão
        Rectangle chao = new Rectangle(1900, 50, Color.DARKRED);
        chao.setTranslateY(550);
        root.getChildren().add(chao);

        // Chefão
        ImageView chefe = new ImageView(new Image(getClass().getResource("/assets/inimigos/chefe.png").toExternalForm()));
        chefe.setTranslateX(900);
        chefe.setTranslateY(450);
        root.getChildren().add(chefe);
    }

    public Pane getRoot() {
        return root;
    }
}
