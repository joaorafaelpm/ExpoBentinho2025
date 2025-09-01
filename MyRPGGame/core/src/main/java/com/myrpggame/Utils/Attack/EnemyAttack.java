package com.myrpggame.Utils.Attack;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.EnemyState;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.HUDVida;
import javafx.scene.image.ImageView;

public class EnemyAttack {

    private final Inimigo inimigo;
    private final ImageView ataqueHitbox = new ImageView();

    private boolean atacando = false;
    private boolean atacandoEmCooldown = false;
    private boolean bloqueado = false;
    private boolean danoAplicado = false;


    private long ataqueStartTime = 0;
    private long lastAttackEndTime = 0;

    private long ataqueDuration;
    private long ataqueCooldown;

    private Player personagem;

    public EnemyAttack(Inimigo inimigo , Player personagem) {
        this.inimigo = inimigo;
        this.personagem = personagem;
        definirDuracoes();
    }

    private void definirDuracoes() {
        // Define duração e cooldown com base no tipo
        switch (inimigo.getEnemyType()) {
            case COMMON -> {
                ataqueDuration = 400_000_000;     // 0.4s
                ataqueCooldown = 1_500_000_000;   // 1.5s
            }
            case TANK -> {
                ataqueDuration = 600_000_000;     // 0.6s
                ataqueCooldown = 2_000_000_000;   // 2s
            }
            case FLYING -> {
                ataqueDuration = 500_000_000;
                ataqueCooldown = 1_800_000_000;
            }
            case ARCHER -> {
                ataqueDuration = 700_000_000;
                ataqueCooldown = 2_500_000_000L;
            }
            case BOSS -> {
                ataqueDuration = 800_000_000;
                ataqueCooldown = 3_000_000_000L;
            }
        }
    }

    public void tentarAtaque(long now) {
        if (!bloqueado && !atacando && !atacandoEmCooldown) {
            iniciarAtaque(now);
        }
    }

    private void iniciarAtaque(long now) {
        atacando = true;
        atacandoEmCooldown = true;
        danoAplicado = false;
        ataqueStartTime = now;
    }

    public boolean podeAtacar(ImageView playerHitbox) {
        // Use BoundsInParent para pegar posição real
        double distanciaX = Math.abs(playerHitbox.getBoundsInParent().getCenterX() - inimigo.getCorpo().getBoundsInParent().getCenterX());
        double distanciaY = Math.abs(playerHitbox.getBoundsInParent().getCenterY() - inimigo.getCorpo().getBoundsInParent().getCenterY());

        // Ajuste os valores conforme alcance do inimigo
        return distanciaX < 60 && distanciaY < 40;
    }



    public void processarAtaque(long now, HUDVida hudVida, ImageView playerHitbox) {
        if (!atacando && podeAtacar(playerHitbox) && !bloqueado && !atacandoEmCooldown) {
            iniciarAtaque(now); // inicia imediatamente
        }

        if (!atacando) return;

        // Aplica dano apenas uma vez
        if (!danoAplicado && now - ataqueStartTime >= ataqueDuration / 2) {
            if (playerHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())) {
                hudVida.tomarDano(inimigo.getDano(), personagem);
            }
            danoAplicado = true;
        }

        // Finaliza ataque
        if (now - ataqueStartTime >= ataqueDuration) {
            atacando = false;
            atacandoEmCooldown = false;
            lastAttackEndTime = now;
        }
    }


    public boolean isAtacando() {
        return atacando;
    }
}
