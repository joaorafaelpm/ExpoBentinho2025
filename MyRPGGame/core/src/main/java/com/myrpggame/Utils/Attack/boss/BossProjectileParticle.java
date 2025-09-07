package com.myrpggame.Utils.Attack.boss;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class BossProjectileParticle {
    private final ImageView corpo;
    private final double velocidadeX;
    private final double velocidadeY;

    private final long spawnTime; // nanos
    private final long ttl;       // nanos

    private boolean dying = false;   // fade em progresso
    private boolean removed = false; // Node removido do scenegraph

    public BossProjectileParticle(double x, double y, double velocidadeX, double velocidadeY,
                                  double width, double height, long spawnTime, long ttl) {
        this.velocidadeX = velocidadeX;
        this.velocidadeY = velocidadeY;
        this.spawnTime = spawnTime;
        this.ttl = ttl;

        this.corpo = new ImageView("assets/inimigos/boss/projectileParticle.png");
        this.corpo.setFitWidth(width);
        this.corpo.setFitHeight(height);
        this.corpo.setTranslateX(x);
        this.corpo.setTranslateY(y);
    }

    public void seguir(double playerX, double playerY, long now) {
        if (dying || removed) return;

        double dx = playerX - corpo.getTranslateX();
        double dy = playerY - corpo.getTranslateY();
        double distancia = Math.hypot(dx, dy);

        if (distancia > 0) {
            corpo.setTranslateX(corpo.getTranslateX() + (dx / distancia) * velocidadeX);
            corpo.setTranslateY(corpo.getTranslateY() + (dy / distancia) * velocidadeY);
        }

        if (dx != 0) corpo.setScaleX(dx > 0 ? 1 : -1);
    }

    public ImageView getCorpo() { return corpo; }

    public long getSpawnTime() { return spawnTime; }
    public long getTtl() { return ttl; }

    public boolean isExpired(long now) { return now - spawnTime >= ttl; }

    public void remover(Pane gameWorld) {
        gameWorld.getChildren().remove(corpo);
    }

    /**
     * Inicia fade e, quando terminar, remove o Node do scenegraph e marca removed=true.
     * NÃO remove a entrada da lista bossParticles.
     */
    public void startFadeAndMark(Pane gameWorld) {
        if (dying || removed) return;
        dying = true;

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(corpo.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.45), new KeyValue(corpo.opacityProperty(), 0))
        );
        fadeOut.setOnFinished(e -> {
            // remove do scenegraph e marca removed
            gameWorld.getChildren().remove(corpo);
            removed = true;
        });
        fadeOut.play();
    }

    public boolean isDying() { return dying; }
    public boolean isRemoved() { return removed; }

    public boolean colidiu(ImageView alvo) {
        return !removed && corpo.getBoundsInParent().intersects(alvo.getBoundsInParent());
    }

    public boolean saiuDaTela(double alturaMax, double larguraMax) {
        double x = corpo.getTranslateX();
        double y = corpo.getTranslateY();
        return x + corpo.getFitWidth() < -50 || y + corpo.getFitHeight() < -50 ||
                x > larguraMax + 50 || y > alturaMax + 50;
    }
}
