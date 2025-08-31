package com.myrpggame.Utils.PlayerAttack;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.Iterator;
import java.util.List;

public class PlayerAttack {

    private final ImageView player;
    private final Pane root;
    private final Player personagem;
    private final ImageView ataqueHitbox = new ImageView();
    private List<Inimigo> inimigos;

    private boolean atacando = false;            // indica se o ataque está ativo
    private boolean atacandoEmCooldown = false;  // impede iniciar novo ataque enquanto animação não termina
    private boolean bloqueado = false;

    private long ataqueStartTime = 0;
    private long lastPlayerHitTime = 0;

    private List<Integer> inimigosMortosPorFase ;

    private static final long ATAQUE_DURATION = 300_000_000;        // duração do ataque
    private static final long PLAYER_ATTACK_COOLDOWN = 400_000_000; // cooldown entre ataques

    private int currentFrame;

    public PlayerAttack(ImageView player, Pane root, Player personagem, int currentFrame , List<Integer> inimigosMortosPorFase) {
        this.player = player;
        this.root = root;
        this.personagem = personagem;
        this.currentFrame = currentFrame;
        this.inimigosMortosPorFase = inimigosMortosPorFase;
    }

    public void setInimigos(List<Inimigo> inimigos) {
        this.inimigos = inimigos;
    }

    public void bloquearAtaque() { bloqueado = true; }
    public void desbloquearAtaque() { bloqueado = false; }

    // Tenta iniciar um ataque
    public void tentarAtaque() {
        long now = System.nanoTime();
        if (!atacandoEmCooldown && !atacando && now - lastPlayerHitTime >= PLAYER_ATTACK_COOLDOWN) {
            iniciarAtaque();
        }
    }

    private void iniciarAtaque() {
        atacando = true;
        atacandoEmCooldown = true; // bloqueia novo ataque até terminar a animação
        ataqueStartTime = System.nanoTime();
        currentFrame = 0;
    }

    // Processa o ataque e aplica dano aos inimigos
    public void processarAtaque(long now) {
        if (bloqueado || !atacando || inimigos == null) return;

        // Se a animação terminou, libera o cooldown
        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
            atacandoEmCooldown = false;
        }

//        Verificação para pegar o hit em mais de um inimigo
        Iterator<Inimigo> iter = inimigos.iterator();
        while (iter.hasNext()) {
            Inimigo inimigo = iter.next();
            if (inimigo == null) continue;

            if (ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                    && now - lastPlayerHitTime >= PLAYER_ATTACK_COOLDOWN) {

                inimigo.tomarDano(personagem.getDano());

                // Calcula direção para o knockback
                double dx = inimigo.getCorpo().getX() - player.getTranslateX();
                double dy = 0; // para horizontal apenas
                if (inimigo.getCorpo().getY() < player.getTranslateY()) dy = -0.5; // empurra levemente para cima
                inimigo.aplicarKnockback(dx >= 0 ? 10 : -10, dy, 1.0);

                lastPlayerHitTime = now;

                if (inimigo.estaMorto()) {
                    root.getChildren().remove(inimigo.getCorpo());
                    iter.remove();
                }
            }
            if (inimigo.estaMorto()) {
                root.getChildren().remove(inimigo.getCorpo());
                inimigosMortosPorFase.add(inimigo.getId());
                iter.remove();
            }
        }


    }

    // Atualiza posição da hitbox e animação do ataque
    public void atualizarHitboxAtaque() {
        if (atacando) {
            ataqueHitbox.setScaleX(player.getScaleX());
            ataqueHitbox.setTranslateX(player.getTranslateX() + (player.getScaleX() > 0 ? -200 : 100));
            ataqueHitbox.setTranslateY(player.getTranslateY() - 40);
            ataqueHitbox.setImage(ResourceLoader.loadImage("/assets/SlashLightAttack.png"));
            if (!root.getChildren().contains(ataqueHitbox)) root.getChildren().add(ataqueHitbox);
        }

        else {
            root.getChildren().remove(ataqueHitbox);
        }
    }

    public boolean isAtacando() {
        return atacando;
    }
}
