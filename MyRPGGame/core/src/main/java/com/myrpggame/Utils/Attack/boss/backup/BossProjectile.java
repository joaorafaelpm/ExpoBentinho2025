
package com.myrpggame.Utils.Attack.boss.backup;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class BossProjectile {
    private final ImageView corpo;
    private final double velocidadeY;
    private double x;
    private double y;
    private Runnable onReachBottom;

    private boolean podeDarDano = true; // controla se ainda pode colidir

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

    public void atualizar(double limiteChao) {
        y += velocidadeY;
        corpo.setTranslateX(x);
        corpo.setTranslateY(y);

        // Define "limite visual" menor que o chão para fade-out
        double limiteVisual = limiteChao - 30; // 30px antes do chão

        if (y + corpo.getFitHeight() >= limiteVisual && podeDarDano) {
            podeDarDano = false; // não dá mais dano
            if (onReachBottom != null) onReachBottom.run();
        }
    }

    public boolean colidiu(ImageView alvo) {
        return podeDarDano && corpo.getBoundsInParent().intersects(alvo.getBoundsInParent());
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

    public void setOnReachBottom(Runnable r) {
        this.onReachBottom = r;
    }
}
