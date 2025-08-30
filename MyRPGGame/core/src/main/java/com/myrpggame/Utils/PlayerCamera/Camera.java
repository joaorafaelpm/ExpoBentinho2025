package com.myrpggame.Utils.PlayerCamera;

public class Camera {
    private double x, y;
    private double larguraTela, alturaTela;
    private double larguraFase, alturaFase;

    private double smoothFactor = 0.1;

    public Camera(double larguraTela, double alturaTela, double larguraFase, double alturaFase) {
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
        this.larguraFase = larguraFase;
        this.alturaFase = alturaFase;
        this.x = 0;
        this.y = 0;
    }

    public void setPhaseSize(double larguraFase, double alturaFase) {
        this.larguraFase = larguraFase;
        this.alturaFase = alturaFase;
    }

    public void update(double playerX, double playerY) {
        // Posição alvo da câmera (centralizando o player)
        double targetX = playerX - larguraTela / 2;
        double targetY = playerY - alturaTela / 2;

        // Limites da fase
        targetX = Math.max(0, Math.min(targetX, larguraFase - larguraTela));
        targetY = Math.max(0, Math.min(targetY, alturaFase - alturaTela));

        // Interpolação suave
        x += (targetX - x) * smoothFactor;
        y += (targetY - y) * smoothFactor;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
