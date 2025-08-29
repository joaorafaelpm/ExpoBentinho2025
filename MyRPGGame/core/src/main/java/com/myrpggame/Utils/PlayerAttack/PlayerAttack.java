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

    private boolean atacando = false;
    private long ataqueStartTime = 0;
    private static final long ATAQUE_DURATION = 300_000_000;
    private static final long PLAYER_ATTACK_COOLDOWN = 400_000_000;
    private long lastPlayerHitTime = 0;
    private int currentFrame;
    private boolean bloqueado = false;


    public PlayerAttack(ImageView player, Pane root, Player personagem, int currentFrame) {
        this.player = player;
        this.root = root;
        this.personagem = personagem;
        this.currentFrame = currentFrame;
    }

    public void setInimigos(List<Inimigo> inimigos) {
        this.inimigos = inimigos;
    }

    private void iniciarAtaque() {
        atacando = true;
        ataqueStartTime = System.nanoTime();
        currentFrame = 0;
    }

    public void tentarAtaque() {
        long now = System.nanoTime();
        if (!atacando && now - lastPlayerHitTime >= PLAYER_ATTACK_COOLDOWN) {
            iniciarAtaque();
        }
    }

    public void bloquearAtaque() { bloqueado = true; }
    public void desbloquearAtaque() { bloqueado = false; }

    public void processarAtaque(long now) {
        if (bloqueado) return;
        if (!atacando || inimigos == null) return;

        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
        }

        Iterator<Inimigo> iter = inimigos.iterator();
        while (iter.hasNext()) {
            Inimigo inimigo = iter.next();
            if (inimigo == null) continue;

            if (ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                    && now - lastPlayerHitTime >= PLAYER_ATTACK_COOLDOWN) {

                inimigo.tomarDano(personagem.getDano());
                lastPlayerHitTime = now;
                System.out.println("ATACOU INIMIGO ============================================");

                if (inimigo.estaMorto()) {
                    root.getChildren().remove(inimigo.getCorpo());
                    iter.remove(); // remove da lista para não processar mais
                }
            }
        }
    }

    public void atualizarHitboxAtaque() {
        if (atacando) {
            ataqueHitbox.setScaleX(player.getScaleX());
            ataqueHitbox.setTranslateX(player.getTranslateX() + (player.getScaleX() > 0 ? -200 : 100));
            ataqueHitbox.setTranslateY(player.getTranslateY() - 40);
            ataqueHitbox.setImage(ResourceLoader.loadImage("/assets/SlashLightAttack.png"));
            if (!root.getChildren().contains(ataqueHitbox)) root.getChildren().add(ataqueHitbox);
        } else {
            root.getChildren().remove(ataqueHitbox);
        }
    }

    public boolean isAtacando() {
        return atacando;
    }
}
