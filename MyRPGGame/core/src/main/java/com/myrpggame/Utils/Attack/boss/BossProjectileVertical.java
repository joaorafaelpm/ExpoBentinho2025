package com.myrpggame.Utils.Attack.boss;

public class BossProjectileVertical extends BossProjectile {

    public BossProjectileVertical(double x, double y,double velocidadeX, double velocidadeY, double width, double height) {
        super(x, y, velocidadeX, velocidadeY, width, height, "/assets/inimigos/boss/projectile.png");
    }

    @Override
    public void atualizar(double limiteChao , double limiteDireita) {
        y += velocidadeY;
        corpo.setTranslateX(x);
        corpo.setTranslateY(y);

        double limiteVisual = limiteChao - 30; // fade-out antes do chão

        if (y + corpo.getFitHeight() >= limiteVisual && podeDarDano) {
            podeDarDano = false;
            if (onReachLimit != null) onReachLimit.run();
        }
    }

    @Override
    public boolean saiuDaTela(double alturaMax , double larguraMax) {
        return y > alturaMax;
    }
}
