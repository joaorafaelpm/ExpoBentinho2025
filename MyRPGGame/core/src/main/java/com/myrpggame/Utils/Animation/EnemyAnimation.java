package com.myrpggame.Utils.Animation;

import com.myrpggame.Enum.EnemyState;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Config.ResourceLoader.ResourceLoader;

public class EnemyAnimation {

    private int currentFrame = 0;
    private long lastUpdate = 0;
    private EnemyState estado = EnemyState.IDLE;

    public void atualizarEstado(Inimigo inimigo) {
        if (inimigo.getEnemyType() != EnemyType.TANK) return;

        // Atacando tem prioridade
        if (inimigo.getAttack() != null && inimigo.getAttack().isAtacando()) {
            estado = EnemyState.ATTACKING;
        } else if (inimigo.getVelocidadeX() != 0) { // se estiver se movendo
            estado = EnemyState.RUNNING;
        } else {
            estado = EnemyState.IDLE;
        }
    }


    public void atualizarAnimacao(Inimigo inimigo) {
        if (inimigo.getEnemyType() != EnemyType.TANK) return; // Limita para TANK apenas

        long now = System.nanoTime();
        atualizarEstado(inimigo);

        long frameDuration = switch (estado) {
            case ATTACKING -> 250_000_000L;
            default -> 300_000_000L; // IDLE
        };

        if (now - lastUpdate < frameDuration) return;

        int maxFrame = switch (estado) {
            case ATTACKING -> 2;
            case RUNNING -> 1; // número de frames da animação de andar
            default -> 1; // IDLE
        };

        currentFrame++;
        if (currentFrame > maxFrame) currentFrame = 1;

        // Caminho do sprite apenas TANK
        String caminho = switch (estado) {
            case ATTACKING -> String.format("/assets/inimigos/tank/TankAttack_%d.png", currentFrame);
            default -> "/assets/inimigos/tank/TankIdle.png";
        };

        inimigo.getCorpo().setImage(ResourceLoader.loadImage(caminho));
        lastUpdate = now;
    }

    public void resetFrames() {
        currentFrame = 0;
        lastUpdate = 0;
    }
}
