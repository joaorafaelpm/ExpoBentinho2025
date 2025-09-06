package com.myrpggame.Utils.Attack;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PlayerAttack {



    private final ImageView player;
    private final Pane root;
    private final Player personagem;
    private final ImageView ataqueHitbox = new ImageView();
    private List<Inimigo> inimigos;

    private boolean atacando = false;
    private boolean atacandoEmCooldown = false;
    private boolean bloqueado = false;

    private long ataqueStartTime = 0;
    private long lastAttackEndTime = 0; // controla quando posso iniciar OUTRO ataque

    private final List<Integer> inimigosMortosPorFase;

    private static final long ATAQUE_DURATION = 300_000_000;        // 0.3s
    private static final long PLAYER_ATTACK_COOLDOWN = 300_000_000; // 0.3s



    // NEW: inimigos atingidos neste golpe
    private final Set<Integer> atingidosNesteAtaque = new HashSet<>();

    private int currentFrame;

    private final List<double[]> orbsParaGerar; // x,y

    public PlayerAttack(ImageView player, Pane root, Player personagem, int currentFrame, List<Integer> inimigosMortosPorFase, List<double[]> orbsParaGerar) {
        this.player = player;
        this.root = root;
        this.personagem = personagem;
        this.currentFrame = currentFrame;
        this.inimigosMortosPorFase = inimigosMortosPorFase;
        this.orbsParaGerar = orbsParaGerar;
    }


    public void setInimigos(List<Inimigo> inimigos) { this.inimigos = inimigos; }

    public void bloquearAtaque() { bloqueado = true; }
    public void desbloquearAtaque() { bloqueado = false; }

    public void tentarAtaque() {
        long now = System.nanoTime();
        // cooldown só para INICIAR um novo ataque
        if (!bloqueado && !atacandoEmCooldown && !atacando && (now - lastAttackEndTime) >= PLAYER_ATTACK_COOLDOWN) {
            iniciarAtaque(now);
        }
    }

    private void iniciarAtaque(long now) {
        atacando = true;
        atacandoEmCooldown = true;
        ataqueStartTime = now;
        currentFrame = 0;
        atingidosNesteAtaque.clear(); // <-- permite acertar vários, 1x cada, neste golpe
    }

    public void processarAtaque(long now) {
        if (bloqueado || !atacando || inimigos == null) return;

        // terminou a janela do golpe
        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
            atacandoEmCooldown = false;
            lastAttackEndTime = now; // inicia cooldown para o próximo ataque
            return;
        }

        // aplica dano 1x por inimigo no golpe atual (AOE)
        java.util.Iterator<Inimigo> iter = inimigos.iterator();
        while (iter.hasNext()) {
            Inimigo inimigo = iter.next();
            if (inimigo == null) continue;

            if (ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                    && !atingidosNesteAtaque.contains(inimigo.getId())) {

                // dano
                inimigo.tomarDano(personagem.getDano());
                atingidosNesteAtaque.add(inimigo.getId());

                // knockback (usa centros para decidir direção)
                var eb = inimigo.getCorpo().getBoundsInParent();
                var pb = player.getBoundsInParent();
                double enemyCenterX = eb.getMinX() + eb.getWidth() / 2.0;
                double enemyCenterY = eb.getMinY() + eb.getHeight() / 2.0;
                double playerCenterX = pb.getMinX() + pb.getWidth() / 2.0;
                double dir = Math.signum(enemyCenterX - playerCenterX);
                if (dir == 0) dir = player.getScaleX() >= 0 ? 1 : -1; // fallback

                inimigo.aplicarKnockback(10 * dir, -0.5, 1.0);

                if (inimigo.estaMorto()) {
                    // 50% de chance de dropar orb
                    if (Math.random() < 0.5) {
                        var bounds = inimigo.getCorpo().getBoundsInParent();
                        double centerX = bounds.getMinX() + bounds.getWidth() / 2.0;
                        double centerY = bounds.getMinY() + bounds.getHeight() / 2.0;

                        // Guarda posição centralizada para o GameLoop gerar orb
                        orbsParaGerar.add(new double[]{centerX, centerY});
                    }


                    root.getChildren().remove(inimigo.getCorpo());
                    inimigosMortosPorFase.add(inimigo.getId());
                    iter.remove();
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

    public boolean isAtacando() { return atacando; }
}

