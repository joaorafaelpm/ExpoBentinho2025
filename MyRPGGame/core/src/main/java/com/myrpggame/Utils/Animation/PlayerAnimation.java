package com.myrpggame.Utils.Animation;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.PlayerState;
import com.myrpggame.Utils.PlayerMovement.PlayerMovement;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static com.myrpggame.Utils.PlayerMovement.PlayerMovement.isFacingRight;

public class PlayerAnimation {

    private static final long MORTE_DURATION = 1_500_000_000;
    private static final long REVIVE_PHASE1_DURATION = 3_000_000_000L;
    private static final long REVIVE_PHASE2_DURATION = 2_000_000_000L;
    private static final int REVIVE_PHASE1_FRAMES = 8;
    private static final int REVIVE_PHASE2_FRAMES = 2;

    private long lastUpdate = 0;
    private int currentFrame = 0;

    private final double larguraSala;
    private final double alturaSala;
    private final ImageView player;
    private final Set<KeyCode> pressedKeys;

    private boolean morto;
    private boolean respawning;
    private boolean atacando;
    private boolean dashando;
    private long morteStartTime;
    private long reviveStartTime;

    private PlayerState playerState;
    private PlayerMovement playerMovement;

    private int frameIdle = 0;
    private int frameRunning = 0;
    private int frameJumping = 0;
    private int frameDying = 0;

    private int frameDashing = 0;
    private int frameAttacking = 0;


    public PlayerAnimation(double larguraSala, double alturaSala,
                           boolean morto, boolean respawning, boolean atacando, boolean dashando,
                           long morteStartTime, long reviveStartTime,
                           Set<KeyCode> pressedKeys, ImageView player , int currentFrame , PlayerMovement playerMovement) {
        this.larguraSala = larguraSala;
        this.alturaSala = alturaSala;
        this.morto = morto;
        this.respawning = respawning;
        this.atacando = atacando;
        this.dashando = dashando;
        this.morteStartTime = morteStartTime;
        this.reviveStartTime = reviveStartTime;
        this.pressedKeys = pressedKeys;
        this.player = player;
        this.currentFrame = currentFrame;
        this.playerMovement = playerMovement ;
    }

    public void atualizarFlags(boolean morto, boolean respawning, boolean atacando, boolean dashando,
                               long morteStartTime, long reviveStartTime) {
        this.morto = morto;
        this.respawning = respawning;
        this.atacando = atacando;
        this.dashando = dashando;
        this.morteStartTime = morteStartTime;
        this.reviveStartTime = reviveStartTime;
    }

    public void atualizarEstado(long now) {
        if (morto) {
            playerState = PlayerState.DEAD;
            return;
        }
        if (respawning) {
            playerState = PlayerState.RESPAWNING;
            return;
        }

        if (atacando) playerState = PlayerState.ATTACKING;
        else if (dashando) playerState = PlayerState.DASHING;
        else if (pressedKeys.contains(KeyCode.SPACE) || playerMovement.isPulando()) {
            playerState = PlayerState.JUMPING;
        }
        else if (pressedKeys.contains(KeyCode.W)) playerState = PlayerState.LOOKING_UP;
        else if (pressedKeys.contains(KeyCode.S)) playerState = PlayerState.LOOKING_DOWN;
        else if (!playerMovement.isPulando() && (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D))) {
            playerState = PlayerState.RUNNING;
        }
        else playerState = PlayerState.IDLE;
    }

    public void atualizarAnimacao(long now) {
        long frameDuration;
        switch (playerState) {
            case IDLE -> frameDuration = 200_000_000;
            case RUNNING, DEAD -> frameDuration = 100_000_000;
            case DASHING -> frameDuration = 80_000_000;
            case ATTACKING -> frameDuration = 50_000_000;
            case RESPAWNING -> frameDuration = 150_000_000;
            default -> frameDuration = 200_000_000;
        }

        if (now - lastUpdate < frameDuration) return;

        switch (playerState) {
            case IDLE -> animIdle();
            case RUNNING -> animRunning();
            case DASHING -> animDashing();
            case ATTACKING -> animAttacking();
            case LOOKING_UP -> animLookingUp();
            case LOOKING_DOWN -> animLookingDown();
            case DEAD -> animDead(now);
            case RESPAWNING -> animRespawning(now);
            case JUMPING -> animJumping(now, playerMovement);
        }

        lastUpdate = now;
    }

    // ===== Método utilitário =====
    private void aplicarDirecao(boolean usarDirecaoPlayer) {
        if (usarDirecaoPlayer) {
            player.setScaleX(isFacingRight() ? -1 : 1);
        }
    }

    private void animIdle() {
        frameIdle = (frameIdle + 1) % 3;
        if (frameIdle == 0) frameIdle = 1;
        player.setImage(ResourceLoader.loadImage(
                String.format("/assets/KnightAFK_%d.png", frameIdle)));
        aplicarDirecao(true);
    }

    private void animRunning() {
        if (!playerMovement.isPulando()) {
            frameRunning = (frameRunning + 1) % 5;
            if (frameRunning == 0) frameRunning = 1;
            player.setImage(ResourceLoader.loadImage(
                    String.format("/assets/KnightSprint_%d.png", frameRunning)));
            aplicarDirecao(true); // só aplica direção no chão
        }
    }

    private void animDashing() {
        frameDashing = (frameDashing + 1) % 4;
        if (frameDashing == 0) frameDashing = 1;
        player.setImage(ResourceLoader.loadImage(
                String.format("/assets/KnightDash_%d.png", frameDashing)));
        if (playerMovement.onGround()) aplicarDirecao(true); // só aplica direção se estiver no chão
    }

    private void animAttacking() {
        if (frameAttacking == 0 || frameAttacking == 4) frameAttacking = 1;
        if (frameAttacking < 3) frameAttacking++;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightLightAttack_%d.png", frameAttacking)));
        aplicarDirecao(true);
    }

    private void animLookingUp() {
        player.setImage(ResourceLoader.loadImage("/assets/KnightLookUp.png"));
        aplicarDirecao(true);
    }

    private void animLookingDown() {
        player.setImage(ResourceLoader.loadImage("/assets/KnightLookDown.png"));
        aplicarDirecao(true);
    }

    private void animDead(long now) {
        if (frameDying == 0) frameDying = 1;
        if (frameDying < 14) frameDying++;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/dying/PlayerDying_%d.png", frameDying)));

        double elapsed = (now - morteStartTime) / 1_000_000_000.0;
        double alturaImagem = player.getImage().getHeight();
        double startY = alturaSala - alturaImagem;
        double endY = (alturaSala - alturaImagem) / 2;
        double duration = MORTE_DURATION / 2_000_000_000.0;
        double progress = Math.min(elapsed / duration, 1.0);

        player.setTranslateY(startY + (endY - startY) * progress);
    }

    private void animRespawning(long now) {
        long elapsedNanos = now - reviveStartTime;

        // ===== Fase 1 do respawn =====
        if (elapsedNanos < REVIVE_PHASE1_DURATION) {
            long frameDurationPhase1 = REVIVE_PHASE1_DURATION / REVIVE_PHASE1_FRAMES;
            int frameIndex = (int) (elapsedNanos / frameDurationPhase1) + 1;
            if (frameIndex > REVIVE_PHASE1_FRAMES) frameIndex = REVIVE_PHASE1_FRAMES;

            player.setImage(ResourceLoader.loadImage(
                    String.format("/assets/reviving/PlayerReviving_%d.png", frameIndex)));
            aplicarDirecao(true);

            // Interpolação vertical suave
            double t = (double) elapsedNanos / REVIVE_PHASE1_DURATION;
            t = 1 - Math.pow(1 - t, 2); // ease-out
            double alturaImagem = player.getImage().getHeight();
            double startY = (alturaSala - alturaImagem) / 2;
            double endY = alturaSala - alturaImagem;
            player.setTranslateY(startY + (endY - startY) * t);
            player.setTranslateX(40);
        }
        // ===== Fase 2 do respawn =====
        else if (elapsedNanos < REVIVE_PHASE1_DURATION + REVIVE_PHASE2_DURATION) {
            long phase2Elapsed = elapsedNanos - REVIVE_PHASE1_DURATION;

            // Agora cada frame tem duração uniforme
            long frameDurationPhase2 = REVIVE_PHASE2_DURATION / REVIVE_PHASE2_FRAMES;
            int frameIndex = 9 + (int) (phase2Elapsed / frameDurationPhase2);
            if (frameIndex > 10) frameIndex = 10; // limita ao último frame

            player.setImage(ResourceLoader.loadImage(
                    String.format("/assets/reviving/PlayerReviving_%d.png", frameIndex)));
            aplicarDirecao(true);

            // Mantém player no chão
            double alturaImagem = player.getImage().getHeight();
            player.setTranslateY(alturaSala - alturaImagem);
            player.setTranslateX(40);
        }
    }


    private void animJumping(long now, PlayerMovement movement) {
        double chao = movement.getAlturaChao() - player.getImage().getHeight();
        double velocidadeY = movement.getVelocidadeY();

        if (velocidadeY < 0) {
            if (frameJumping == 0 || frameJumping > 9) frameJumping = 1;
            else frameJumping++;
        } else {
            if (frameJumping < 10) frameJumping = 10;
            else frameJumping = frameJumping == 10 ? 11 : 10;
        }

        player.setImage(ResourceLoader.loadImage(
                String.format("/assets/jumping/PlayerJumping_%d.png", frameJumping)));

        aplicarDirecao(true); // sempre respeita direção do player no ar

        if (player.getTranslateY() > chao) {
            player.setTranslateY(chao);
        }
    }
}
