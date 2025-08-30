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

    public PlayerMovement(ImageView player, double alturaChao, Set<KeyCode> pressedKeys, int currentFrame) {
        this.player = player;
        this.alturaChao = alturaChao;
        this.pressedKeys = pressedKeys;
        this.currentFrame = currentFrame;
    }

    public void setAlturaChao(double alturaChao) {
        this.alturaChao = alturaChao;
    }

    public void aplicarGravidade() {
        if (gravidadeAtivo) {
            velocidadeY += gravidade;
            player.setTranslateY(player.getTranslateY() + velocidadeY);
        }

        double chao = alturaChao - player.getBoundsInParent().getHeight();
        if (player.getTranslateY() > chao) {
            player.setTranslateY(chao);
            velocidadeY = 0;
            pulando = false;
        }
    }

    public void processarPulo(long now) {
        if (pressedKeys.contains(KeyCode.SPACE)) {
            if (!pulando && onGround()) {
                pulando = true;
                tempoPuloInicio = now;
                velocidadeY = impulsoPulo;
            } else if (pulando && now - tempoPuloInicio < tempoMaxPulo) {
                velocidadeY = impulsoPulo;
            }
        } else {
            pulando = false;
        }
    }

    public void processarMovimento() {
        if (dashing || bloqueado) return;

        velocidadePlayer = pressedKeys.contains(KeyCode.SHIFT) ? 10 : 5;

        if (pressedKeys.contains(KeyCode.A)) {
            if (player.getScaleX() < 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() - velocidadePlayer);
            facingRight = false;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            if (player.getScaleX() > 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() + velocidadePlayer);
            facingRight = true;
        }
    }

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

    public double getVelocidadePlayer() {
        return velocidadePlayer;
    }

    public static boolean isFacingRight() { return facingRight; }
    public boolean isDashing() { return dashing; }

    public void bloquearMovimento() { bloqueado = true; }
    public void desbloquearMovimento() { bloqueado = false; }

}
