package com.myrpggame.Utils.PlayerMovement;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.Set;

public class PlayerMovement {

    private final ImageView player;
    private double alturaChao; // chão da fase

    private double velocidadePlayer;

    private final Set<KeyCode> pressedKeys;

    private static boolean facingRight;
    private double gravidade = 0.5;
    private double velocidadeY = 0;
    private boolean pulando = false;
    private long tempoPuloInicio = 0;
    private final long tempoMaxPulo = 500_000_000;
    private final double impulsoPulo = -10;

    private boolean canDash = true;
    private boolean gravidadeAtivo = true;
    private boolean dashing = false;
    private long dashStartTime = 0;
    private double dashVelocidade = 0;
    private long lastDashTime = 0;
    private final long DASH_DURATION = 250_000_000;
    private final long DASH_COOLDOWN = 500_000_000;
    private int currentFrame;

    private boolean bloqueado = false;

//    Construtor padrão
    public PlayerMovement(ImageView player, double alturaChao, Set<KeyCode> pressedKeys, int currentFrame) {
        this.player = player;
        this.alturaChao = alturaChao;
        this.pressedKeys = pressedKeys;
        this.currentFrame = currentFrame;
    }

//    Método auxiliar para aplicar a gravidade da fase caso ele não esteja em dash e na altura máxima do pulo
    public void aplicarGravidade() {
        if (gravidadeAtivo) {
            velocidadeY += gravidade;
            player.setTranslateY(player.getTranslateY() + velocidadeY);
        }

        double chao = alturaChao - player.getBoundsInParent().getHeight();

        // Se encostou no chão, termina o pulo
        if (player.getTranslateY() >= chao) {
            player.setTranslateY(chao);
            velocidadeY = 0;
            pulando = false; // Só aqui, quando realmente toca o chão
        }
    }

//    Método auxiliar para ver se o usuário está pronto para pular
    public void processarPulo(long now) {
        // Inicia o pulo apenas se o player estiver no chão
        if (pressedKeys.contains(KeyCode.SPACE) && !pulando && onGround()) {
            pulando = true;
            tempoPuloInicio = now;
            velocidadeY = impulsoPulo;
        }

        // Mantém o impulso enquanto a tecla estiver pressionada e não ultrapassou o tempo máximo
        if (pulando && pressedKeys.contains(KeyCode.SPACE) && now - tempoPuloInicio < tempoMaxPulo) {
            velocidadeY = impulsoPulo;
        }
    }

    //    Método auxiliar para ver se o usuário está em posição de se mover, e definir a velocidade
    public void processarMovimento() {
        if (dashing || bloqueado) return;

        velocidadePlayer = pressedKeys.contains(KeyCode.SHIFT) ? 15 : 10;

        if (pressedKeys.contains(KeyCode.A)) {
            player.setTranslateX(player.getTranslateX() - velocidadePlayer);
            facingRight = false;  // apenas atualiza a direção
        }
        if (pressedKeys.contains(KeyCode.D)) {
            player.setTranslateX(player.getTranslateX() + velocidadePlayer);
            facingRight = true;   // apenas atualiza a direção
        }
    }

//    Método auxiliar para ver se o usuário pode usar o dash (se não está em cooldown e etc)
    public void processarDash(long now) {
        if (dashing) {
            if (now - dashStartTime < DASH_DURATION) {
                velocidadeY = 0;
                player.setTranslateX(player.getTranslateX() + dashVelocidade);
            } else {
                dashing = false;
                gravidadeAtivo = true;
                dashVelocidade = 0;
                lastDashTime = now;
            }
        }
        if (!canDash && !pressedKeys.contains(KeyCode.Q) && now - lastDashTime >= DASH_COOLDOWN) {
            canDash = true;
        }
    }

//    Faz a verificação de dash para avaliar o cooldown
    public void tentarDash() {
        long now = System.nanoTime();
        if (!dashing && canDash && now - lastDashTime >= DASH_COOLDOWN) {
            iniciarDash(now);
        }
    }

    private void iniciarDash(long now) {
        gravidadeAtivo = false;
        dashStartTime = now;
        dashing = true;
        canDash = false;
        currentFrame = 0;

        if (pressedKeys.contains(KeyCode.D)) dashVelocidade = 15;
        else if (pressedKeys.contains(KeyCode.A)) dashVelocidade = -15;
        else dashVelocidade = player.getScaleX() >= 0 ? -15 : 15;
    }

    public boolean onGround() {
        return player.getTranslateY() >= alturaChao - player.getBoundsInParent().getHeight();
    }

    public static boolean isFacingRight() { return facingRight; }
    public boolean isPulando() { return pulando; }
    public void setPulando(boolean pulando) { this.pulando = pulando; }
    public boolean isDashing() { return dashing; }
    public double getVelocidadeY () {return velocidadeY ;}
    public double getAlturaChao () {return alturaChao ;}


    public void bloquearMovimento() { bloqueado = true; }
    public void desbloquearMovimento() { bloqueado = false; }

}
