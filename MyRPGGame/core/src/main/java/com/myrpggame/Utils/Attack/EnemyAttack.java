package com.myrpggame.Utils.Attack;

import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.HUDVida;
import javafx.scene.image.ImageView;

public class EnemyAttack {

    private final Inimigo inimigo;
    private final Player personagem;

    private boolean atacando = false;
    private boolean atacandoEmCooldown = false;
    private boolean danoAplicado = false;

    private long ataqueStartTime = 0;
    private long ataqueDuration;
    private long ataqueCooldown;

    private double attackTargetX;
    private double attackTargetY;
    private boolean attackTargetSet = false;

    public EnemyAttack(Inimigo inimigo, Player personagem) {
        this.inimigo = inimigo;
        this.personagem = personagem;
        definirDuracoes();
    }

    private void definirDuracoes() {
        switch (inimigo.getEnemyType()) {
            case COMMON -> { ataqueDuration = 400_000_000; ataqueCooldown = 1_500_000_000; }
            case TANK -> { ataqueDuration = 600_000_000; ataqueCooldown = 2_000_000_000; }
            case FLYING -> { ataqueDuration = 1_500_000_000; ataqueCooldown = 1_800_000_000; }
            case ARCHER -> { ataqueDuration = 700_000_000; ataqueCooldown = 2_500_000_000L; }
            case BOSS -> { ataqueDuration = 800_000_000; ataqueCooldown = 3_000_000_000L; }
        }
    }

    // =================== Processa cooldown ===================
    private void processCooldown(long now) {
        if (atacandoEmCooldown && !atacando) {
            if (now - ataqueStartTime >= ataqueCooldown) {
                atacandoEmCooldown = false; // cooldown acabou, pode atacar de novo
            }
        }
    }

    // =================== Método principal ===================
    public void processarInimigo(long now, HUDVida hudVida, ImageView playerHitbox) {
        // Atualiza cooldown sempre
        processCooldown(now);

        switch (inimigo.getEnemyType()) {
            case FLYING -> processFlying(now, hudVida, playerHitbox);
            default -> processDefault(now, hudVida, playerHitbox);
        }
    }

    // =================== Inimigos voadores ===================
    private void processFlying(long now, HUDVida hudVida, ImageView playerHitbox) {
        if (!atacando) {
            if (playerHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent()) && !atacandoEmCooldown) {
                iniciarAtaqueFlying(now);
                atacarFlying(hudVida, playerHitbox);
            }
        } else {
            atacarFlying(hudVida, playerHitbox);
        }
    }

    private void atacarFlying(HUDVida hudVida, ImageView playerHitbox) {
        if (!atacando) return;

        if (!danoAplicado && playerHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())) {
            hudVida.tomarDano(inimigo.getDano(), personagem);
            danoAplicado = true;
        }

        long now = System.nanoTime();
        if (now - ataqueStartTime >= ataqueDuration) {
            finalizarAtaque(now);
        }
    }

    private void iniciarAtaqueFlying(long now) {
        atacando = true;
        atacandoEmCooldown = true;
        danoAplicado = false;
        ataqueStartTime = now;
    }

    // =================== Inimigos padrão ===================
    private void processDefault(long now, HUDVida hudVida, ImageView playerHitbox) {
        if (!atacando && playerHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent()) && !atacandoEmCooldown) {
            iniciarAtaque(now);
        }

        if (atacando) {
            if (!danoAplicado && playerHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())) {
                hudVida.tomarDano(inimigo.getDano(), personagem);
                danoAplicado = true;
            }

            if (now - ataqueStartTime >= ataqueDuration) {
                finalizarAtaque(now);
            }
        }
    }

    private void iniciarAtaque(long now) {
        atacando = true;
        atacandoEmCooldown = true;
        danoAplicado = false;
        ataqueStartTime = now;
    }

    private void finalizarAtaque(long now) {
        atacando = false;
        danoAplicado = false;
        attackTargetSet = false;
        inimigo.getCorpo().setOpacity(1.0);

        // Inicia cooldown
        ataqueStartTime = now;
        atacandoEmCooldown = true;
    }

    // =================== Métodos de estado ===================
    public boolean isAttackTargetSet() { return attackTargetSet; }
    public boolean isAtacando() { return atacando; }
    public boolean isRunning() { return !atacando; } // apenas se não estiver atacando
}
