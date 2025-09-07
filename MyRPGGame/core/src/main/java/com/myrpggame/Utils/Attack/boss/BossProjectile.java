package com.myrpggame.Utils.Attack.boss;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
public abstract class BossProjectile {
    protected final ImageView corpo;
    protected double x;
    protected double y;
    protected double velocidadeX;
    protected double velocidadeY;
    protected Runnable onReachLimit;

    protected boolean podeDarDano = true;

    public BossProjectile(double x, double y, double velocidadeX, double velocidadeY,
                          double width, double height, String url) {
        this.x = x;
        this.y = y;
        this.velocidadeX = velocidadeX;
        this.velocidadeY = velocidadeY;

        this.corpo = new ImageView(url);
        this.corpo.setFitWidth(width);
        this.corpo.setFitHeight(height);
        this.corpo.setTranslateX(x);
        this.corpo.setTranslateY(y);
    }

    public abstract void atualizar(double limiteChao, double limiteDireita);


    public boolean colidiu(ImageView alvo) {
        return podeDarDano && corpo.getBoundsInParent().intersects(alvo.getBoundsInParent());
    }

    public ImageView getCorpo() {
        return corpo;
    }

    public void remover(Pane gameWorld) {
        gameWorld.getChildren().remove(corpo);
    }



    public void setOnReachLimit(Runnable r) {
        this.onReachLimit = r;
    }

    // --- Novo método genérico ---
    public boolean saiuDaTela(double alturaMax, double larguraMax) {
        return (y > alturaMax || x > larguraMax || y + corpo.getFitHeight() < 0 || x + corpo.getFitWidth() < 0);
    }
}

