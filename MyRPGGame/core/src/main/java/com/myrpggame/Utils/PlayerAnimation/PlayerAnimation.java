package com.myrpggame.Utils.PlayerAnimation;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.PlayerState;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static com.myrpggame.Utils.PlayerMovement.PlayerMovement.isFacingRight;

public class PlayerAnimation {

    private static final long MORTE_DURATION = 1_500_000_000;
    private static final long REVIVE_PHASE1_DURATION = 5_000_000_000L;
    private static final long REVIVE_PHASE2_DURATION = 3_000_000_000L;
    private static final int REVIVE_PHASE1_FRAMES = 8;
    private static final int REVIVE_PHASE2_FRAMES = 3;

    private long lastUpdate = 0;
    private int currentFrame = 0;

    private final double larguraSala;
    private final double alturaSala;
    private final ImageView player;
    private final Set<KeyCode> pressedKeys;

    // Flags e tempos são passados do GameLoop
    private boolean morto;
    private boolean respawning;
    private boolean atacando;
    private boolean dashando;
    private long morteStartTime;
    private long reviveStartTime;

    private PlayerState playerState;

    public PlayerAnimation(double larguraSala, double alturaSala,
                           boolean morto, boolean respawning, boolean atacando, boolean dashando,
                           long morteStartTime, long reviveStartTime,
                           Set<KeyCode> pressedKeys, ImageView player , int currentFrame) {
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
        else if (pressedKeys.contains(KeyCode.W)) playerState = PlayerState.LOOKING_UP;
        else if (pressedKeys.contains(KeyCode.S)) playerState = PlayerState.LOOKING_DOWN;
        else if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)) playerState = PlayerState.RUNNING;
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
        }

        lastUpdate = now;
    }

    // ===== Métodos privados =====
    private void animIdle() {
        currentFrame = (currentFrame + 1) % 3;
        if (currentFrame == 0) currentFrame = 1;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightAFK_%d.png", currentFrame)));
        player.setScaleX(isFacingRight() ? -1 : 1);
    }

    private void animRunning() {
        currentFrame = (currentFrame + 1) % 5;
        if (currentFrame == 0) currentFrame = 1;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightSprint_%d.png", currentFrame)));
    }

    private void animDashing() {
        if (currentFrame == 0) currentFrame = 1;
        if (currentFrame < 4) currentFrame++;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightDash_%d.png", currentFrame)));
    }

    private void animAttacking() {
        if (currentFrame == 0 || currentFrame == 4) currentFrame = 1;
        if (currentFrame < 3) currentFrame++;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightLightAttack_%d.png", currentFrame)));
    }

    private void animLookingUp() {
        player.setImage(ResourceLoader.loadImage("/assets/KnightLookUp.png"));
        player.setScaleX(isFacingRight() ? -1 : 1);
    }

    private void animLookingDown() {
        player.setImage(ResourceLoader.loadImage("/assets/KnightLookDown.png"));
        player.setScaleX(isFacingRight() ? -1 : 1);
    }

    private void animDead(long now) {
        if (currentFrame == 0) currentFrame = 1;
        if (currentFrame < 14) currentFrame++;
        player.setImage(ResourceLoader.loadImage(String.format("/assets/dying/PlayerDying_%d.png", currentFrame)));

        double elapsed = (now - morteStartTime) / 1_000_000_000.0;
        double alturaImagem = player.getImage().getHeight();
        double startY = alturaSala - alturaImagem;
        double endY = (alturaSala - alturaImagem) / 2;
        double duration = MORTE_DURATION / 2_000_000_000.0;
        double progress = Math.min(elapsed / duration, 1.0);

        player.setTranslateY(startY + (endY - startY) * progress);
        player.setTranslateX(larguraSala / 2 - player.getBoundsInParent().getWidth() / 2);
    }

    private void animRespawning(long now) {
        long elapsedNanos = now - reviveStartTime;

        // ===== FASE 1: frames 1–8 =====
        if (elapsedNanos < REVIVE_PHASE1_DURATION) {
            long frameDurationPhase1 = REVIVE_PHASE1_DURATION / REVIVE_PHASE1_FRAMES;
            int frameIndex = (int) (elapsedNanos / frameDurationPhase1) + 1;
            if (frameIndex > REVIVE_PHASE1_FRAMES) frameIndex = REVIVE_PHASE1_FRAMES;

            player.setImage(ResourceLoader.loadImage(String.format("/assets/reviving/PlayerReviving_%d.png", frameIndex)));
            player.setScaleX(isFacingRight() ? 1 : -1);

            double t = (double) elapsedNanos / REVIVE_PHASE1_DURATION;
            t = 1 - Math.pow(1 - t, 2);
            double alturaImagem = player.getImage().getHeight();
            double startY = (alturaSala - alturaImagem) / 2;
            double endY = alturaSala - alturaImagem;
            player.setTranslateY(startY + (endY - startY) * t);
            player.setTranslateX(40);
        }
        // ===== FASE 2: frames 9–11 =====
        else if (elapsedNanos < REVIVE_PHASE1_DURATION + REVIVE_PHASE2_DURATION) {
            long phase2Elapsed = elapsedNanos - REVIVE_PHASE1_DURATION;
            long frameDurationPhase2 = REVIVE_PHASE2_DURATION / REVIVE_PHASE2_FRAMES;
            int frameIndex = 9 + (int) (phase2Elapsed / frameDurationPhase2);
            if (frameIndex > 11) frameIndex = 11;

            player.setImage(ResourceLoader.loadImage(String.format("/assets/reviving/PlayerReviving_%d.png", frameIndex)));
            player.setScaleX(isFacingRight() ? 1 : -1);

            double alturaImagem = player.getImage().getHeight();
            player.setTranslateY(alturaSala - alturaImagem);
            player.setTranslateX(40);
        }
        // Fim da animação → o GameLoop deve limpar respawning
    }
}
