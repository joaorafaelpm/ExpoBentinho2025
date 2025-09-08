package com.myrpggame.Utils.Animation;

import com.myrpggame.Enum.EnemyState;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Config.ResourceLoader.ResourceLoader;
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
    private int ataqueAnimSelecionada = 0;
    private int maxTankFramesAtaqueOne = 2; // padrão
    private int maxTankFramesAtaqueTwo = 6; // padrão

    private int maxCommonFramesAtaque = 4; // padrão
    private int maxFlyingFramesRunning = 2;
    private int runningFrame = 1;

    private int maxBossIdle = 56;
    private int bossIdleFrame = 1;

    private int maxCommonFramesRunning = 8;
    private int runningFlyingFrame = 1;
    private int idleFrame = 1;
    private int maxFramesIdle = 2;

    public void atualizarEstado(Inimigo inimigo) {
        if (inimigo.getAttack().isAtacando()) {
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
                        ataqueAnimSelecionada = random.nextInt(2);
                        if (ataqueAnimSelecionada == 1) animTankAttack1(inimigo);
                        else animTankAttack2(inimigo);
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

            case COMMON -> {
                long frameDuration = switch (estado) {
                    case ATTACKING -> 200_000_000L;
                    default -> 400_000_000L;
                };

                if (now - lastUpdate < frameDuration) return;

                switch (estado) {
                    case ATTACKING -> animCommonAttack(inimigo);
                    case RUNNING -> commonRunning(inimigo);
                    default -> commonIdle(inimigo);
                }
            }

            case BOSS -> {
                long frameDuration = 125_000_000L; // 0.125s por frame (7s no total)
                if (now - lastUpdate < frameDuration) return;
                bossIdle(inimigo);
            }

        }

        lastUpdate = now;
    }

    public void bossIdle(Inimigo inimigo) {
        bossIdleFrame++;
        if (bossIdleFrame > maxBossIdle) bossIdleFrame = 1; // reinicia no loop

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/boss/idle/BossIdle (%d).png", bossIdleFrame)
        ));
    }


    public void commonIdle (Inimigo inimigo) {
        idleFrame++;
        if (idleFrame > maxFramesIdle) idleFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/common/CommonIdle_%d.png", idleFrame)
        ));
    }

   

    // ====== COMMON RUNNING ======
    public void commonRunning(Inimigo inimigo) {
        runningFrame++;
        if (runningFrame > maxCommonFramesRunning) runningFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/common/CommonWalk_%d.png", runningFrame)
        ));
    }

    // ===== Ataque COMMON =====
    private void animCommonAttack(Inimigo inimigo) {
        currentFrame++;
        if (currentFrame > maxCommonFramesAtaque) currentFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/common/CommonAtaque_%d.png", currentFrame)
        ));
    }

    // ====== FLYING ======
    public void flyingIdleOrMoving(Inimigo inimigo) {
        runningFlyingFrame++;
        if (runningFlyingFrame > maxFlyingFramesRunning) runningFlyingFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/flying/FlyingRunning_%d.png", runningFlyingFrame)
        ));
    }

    // ===================== FLYING ATTACK ANIMATION =====================
    public void animFlyingAttack(Inimigo inimigo) {
        if (inimigo.getCorpo().getProperties().containsKey("flyingAttackAnim")) return;

        double originalX = inimigo.getCorpo().getTranslateX();
        double moveDistance = 200;
        long durationMs = 1500;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> inimigo.getCorpo().setTranslateX(originalX)),
                new KeyFrame(Duration.millis(durationMs * 0.25),
                        e -> inimigo.getCorpo().setTranslateX(originalX - moveDistance),
                        new KeyValue(inimigo.getCorpo().translateXProperty(), originalX - moveDistance, javafx.animation.Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(durationMs * 0.75),
                        e -> inimigo.getCorpo().setTranslateX(originalX + moveDistance / 2),
                        new KeyValue(inimigo.getCorpo().translateXProperty(), originalX + moveDistance / 2, javafx.animation.Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(durationMs),
                        e -> inimigo.getCorpo().setTranslateX(originalX),
                        new KeyValue(inimigo.getCorpo().translateXProperty(), originalX, javafx.animation.Interpolator.EASE_BOTH)
                )
        );

        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> inimigo.getCorpo().getProperties().remove("flyingAttackAnim"));
        inimigo.getCorpo().getProperties().put("flyingAttackAnim", true);
        timeline.play();
    }

    // ===== Ataque TANK =====
    private void animTankAttack1(Inimigo inimigo) {
        currentFrame++;
        if (currentFrame > maxTankFramesAtaqueOne) currentFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/tank/TankAttack_1_%d.png", currentFrame)
        ));
    }

    private void animTankAttack2(Inimigo inimigo) {
        currentFrame++;
        if (currentFrame > maxTankFramesAtaqueTwo) currentFrame = 1;

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(
                String.format("/assets/inimigos/tank/TankAttack_2_%d.png", currentFrame)
        ));
    }


    public void resetFrames() {
        currentFrame = 0;
        lastUpdate = 0;
    }
}
