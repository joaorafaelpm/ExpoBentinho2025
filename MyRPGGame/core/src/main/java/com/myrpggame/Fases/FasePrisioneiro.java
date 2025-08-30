package com.myrpggame.Fases;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FasePrisioneiro {

    private final Pane root = new Pane();

    public FasePrisioneiro() {
        root.setPrefSize(2000, 600);
        criarFase();
    }

    private void criarFase() {
        // Fundo
        Rectangle fundo = new Rectangle(2000, 600, Color.BLACK);
        root.getChildren().add(fundo);

        // Chão principal
        Rectangle chao = new Rectangle(2000, 50, Color.DARKGRAY);
        chao.setTranslateY(550);
        root.getChildren().add(chao);

        // Plataformas
        Rectangle plat1 = new Rectangle(150, 20, Color.GRAY);
        plat1.setTranslateX(400);
        plat1.setTranslateY(450);
        root.getChildren().add(plat1);

        Rectangle plat2 = new Rectangle(150, 20, Color.GRAY);
        plat2.setTranslateX(900);
        plat2.setTranslateY(400);
        root.getChildren().add(plat2);

        // Inimigo tutorial
        ImageView inimigo = new ImageView(new Image(getClass().getResource("/assets/inimigo_tutorial.png").toExternalForm()));
        inimigo.setTranslateX(850);
        inimigo.setTranslateY(520);
        root.getChildren().add(inimigo);
    }

    public Pane getRoot() {
        return root;
    }
}
