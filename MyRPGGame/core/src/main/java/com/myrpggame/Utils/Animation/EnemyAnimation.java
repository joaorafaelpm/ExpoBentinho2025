package com.myrpggame.Utils.Animation;

import com.myrpggame.Enum.EnemyState;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Utils.Attack.EnemyAttack;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static com.badlogic.gdx.math.MathUtils.random;

public class EnemyAnimation {

    private int currentFrame = 0;
    private long lastUpdate = 0;
    private EnemyState estado = EnemyState.IDLE;

    // Para escolher 1 vez só a animação de ataque
    private int ataqueAnimSelecionada = 1;
    private int maxFramesAtaque = 2; // padrão

    private int maxFramesFlyingRunning = 2;
    private int flyingRunningFrame = 2;

    public void atualizarEstado(Inimigo inimigo) {
        if (inimigo.getAttack().isAtacando()) {
            // Apenas TANK ou outros ataques específicos
            if (estado != EnemyState.ATTACKING && inimigo.getEnemyType() == EnemyType.TANK) {
                ataqueAnimSelecionada = random.nextInt(2) + 1; // 1 ou 2
                currentFrame = 0;
                maxFramesAtaque = (ataqueAnimSelecionada == 1) ? 2 : 6;
            }
            estado = EnemyState.ATTACKING;

        } else if (inimigo.getAttack().isRunning()) {
            estado = EnemyState.RUNNING;
        } else {
            estado = EnemyState.IDLE;
        }
    }


    public void atualizarAnimacao(Inimigo inimigo) {
        long now = System.nanoTime();
        atualizarEstado(inimigo);

        switch (inimigo.getEnemyType()) {
            case TANK -> {
                long frameDuration = switch (estado) {
                    case ATTACKING -> 200_000_000L;
                    default -> 400_000_000L;
                };

                if (now - lastUpdate < frameDuration) return;

                switch (estado) {
                    case ATTACKING -> {
                        if (ataqueAnimSelecionada == 1) animAttack1(inimigo);
                        else animAttack2(inimigo);
                    }
                    default -> inimigo.getCorpo().setImage(
                            ResourceLoader.loadImage("/assets/inimigos/tank/TankIdle.png")
                    );
                }
            }
            case FLYING -> {
                long frameDuration = 200_000_000L; // 0.2s por frame
                if (now - lastUpdate < frameDuration) return;
                switch (estado) {
                    case ATTACKING -> animFlyingAttack(inimigo);
                    default -> flyingIdleOrMoving(inimigo);
                }
            }
        }

        lastUpdate = now;
    }

    // ====== FLYING ======
    public void flyingIdleOrMoving(Inimigo inimigo) {
        flyingRunningFrame++;
        if (flyingRunningFrame > maxFramesFlyingRunning) flyingRunningFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/flying/FlyingRunning_%d.png", flyingRunningFrame)
        ));
    }

    // ===================== FLYING ATTACK ANIMATION =====================
    public void animFlyingAttack(Inimigo inimigo) {
        // Evita reiniciar a animação se já estiver rolando
        if (inimigo.getCorpo().getProperties().containsKey("flyingAttackAnim")) return;

        double originalX = inimigo.getCorpo().getTranslateX();
        double moveDistance = 200; // distância do recuo
        long durationMs = 1500; // duração total do ataque

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> inimigo.getCorpo().setTranslateX(originalX)),
                new KeyFrame(Duration.millis(durationMs * 0.25),
                        e -> inimigo.getCorpo().setTranslateX(originalX - moveDistance),
                        new javafx.animation.KeyValue(inimigo.getCorpo().translateXProperty(), originalX - moveDistance, javafx.animation.Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(durationMs * 0.75),
                        e -> inimigo.getCorpo().setTranslateX(originalX + moveDistance / 2),
                        new javafx.animation.KeyValue(inimigo.getCorpo().translateXProperty(), originalX + moveDistance / 2, javafx.animation.Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(durationMs),
                        e -> inimigo.getCorpo().setTranslateX(originalX),
                        new javafx.animation.KeyValue(inimigo.getCorpo().translateXProperty(), originalX, javafx.animation.Interpolator.EASE_BOTH)
                )
        );

        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> inimigo.getCorpo().getProperties().remove("flyingAttackAnim"));
        inimigo.getCorpo().getProperties().put("flyingAttackAnim", true); // marca que está animando
        timeline.play();
    }

    // ===== Ataque TANK =====
    private void animAttack1(Inimigo inimigo) {
        currentFrame++;
        if (currentFrame > maxFramesAtaque) currentFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/tank/TankAttack_1_%d.png", currentFrame)
        ));
    }

    private void animAttack2(Inimigo inimigo) {
        currentFrame++;
        if (currentFrame > maxFramesAtaque) currentFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/tank/TankAttack_2_%d.png", currentFrame)
        ));
    }

    public void resetFrames() {
        currentFrame = 0;
        lastUpdate = 0;
    }
}
