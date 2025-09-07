package com.myrpggame.Utils.Attack.boss;

public class BossProjectileHorizontal extends  BossProjectile{

    public BossProjectileHorizontal(double x, double y, double velocidadeX, double velocidadeY, double width, double height) {
        super(x, y, velocidadeX, velocidadeY, width, height, "/assets/inimigos/boss/projectileHorizontal.png");
    }

    @Override
    public void atualizar(double limiteFase , double limiteDireita) {
        x += velocidadeY;
        corpo.setTranslateX(x);
        corpo.setTranslateY(y);

        double limiteVisual = limiteDireita - 30; // fade-out antes do chão

        if (x + corpo.getFitHeight() >= limiteVisual && podeDarDano) {
            podeDarDano = false;
            if (onReachLimit != null) onReachLimit.run();
        }
    }

    @Override
    public boolean saiuDaTela(double alturaMax , double larguraMax) {
        return x > larguraMax;
    }


}

