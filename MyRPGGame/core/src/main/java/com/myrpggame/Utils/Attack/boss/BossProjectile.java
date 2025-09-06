package com.myrpggame.Utils.Attack.boss;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class BossProjectile {
    private final ImageView corpo;
    private final double velocidadeY;
    private double x;
    private double y;

    public BossProjectile(double x, double y, double velocidadeY, double width, double height) {
        this.x = x;
        this.y = y;
        this.velocidadeY = velocidadeY;

        this.corpo = new ImageView("/assets/inimigos/boss/projectile.png");
        this.corpo.setFitWidth(width);
        this.corpo.setFitHeight(height);
        this.corpo.setTranslateX(x);
        this.corpo.setTranslateY(y);
    }

    public void atualizar() {
        y += velocidadeY;
        corpo.setTranslateX(x);
        corpo.setTranslateY(y);
    }

    public boolean colidiu(ImageView alvo) {
        return corpo.getBoundsInParent().intersects(alvo.getBoundsInParent());
    }

    public boolean saiuDaTela(double alturaMax) {
        return y > alturaMax;
    }

    public ImageView getCorpo() {
        return corpo;
    }

    public void remover(Pane gameWorld) {
        gameWorld.getChildren().remove(corpo);
    }
}
