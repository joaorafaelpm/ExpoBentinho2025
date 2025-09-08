package com.myrpggame.Utils.Attack;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Utils.Attack.boss.BossProjectileParticle;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.HUDVida;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Iterator;
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
    private long lastAttackEndTime = 0;

    private final List<Integer> inimigosMortosPorFase;
    private final Set<Integer> atingidosNesteAtaque = new HashSet<>();
    private int currentFrame;

    private final List<double[]> orbsParaGerar; // x,y
    private List<BossProjectileParticle> bossParticles;

    private static final long ATAQUE_DURATION = 300_000_000;        // 0.3s
    private static final long PLAYER_ATTACK_COOLDOWN = 300_000_000; // 0.3s

    public PlayerAttack(ImageView player, Pane root, Player personagem, int currentFrame,
                        List<Integer> inimigosMortosPorFase, List<double[]> orbsParaGerar) {
        this.player = player;
        this.root = root;
        this.personagem = personagem;
        this.currentFrame = currentFrame;
        this.inimigosMortosPorFase = inimigosMortosPorFase;
        this.orbsParaGerar = orbsParaGerar;
    }

    public void setInimigos(List<Inimigo> inimigos) { this.inimigos = inimigos; }

    // NOVO: setar partículas do boss
    public void setBossParticles(List<BossProjectileParticle> bossParticles) {
        this.bossParticles = bossParticles;
    }

    public void bloquearAtaque() { bloqueado = true; }
    public void desbloquearAtaque() { bloqueado = false; }

    public void tentarAtaque() {
        long now = System.nanoTime();
        if (!bloqueado && !atacandoEmCooldown && !atacando && (now - lastAttackEndTime) >= PLAYER_ATTACK_COOLDOWN) {
            iniciarAtaque(now);
        }
    }

    private void iniciarAtaque(long now) {
        atacando = true;
        atacandoEmCooldown = true;
        ataqueStartTime = now;
        currentFrame = 0;
        atingidosNesteAtaque.clear();
    }

    public void processarAtaque(long now) {
        if (bloqueado || !atacando) return;

        // termina ataque
        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
            atacandoEmCooldown = false;
            lastAttackEndTime = now;
            return;
        }

        // check colisão com inimigos
        // check colisão com inimigos
        if (inimigos != null) {
            Iterator<Inimigo> iterInimigo = inimigos.iterator();
            while (iterInimigo.hasNext()) {
                Inimigo inimigo = iterInimigo.next();
                if (inimigo == null) continue;

                if (ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                        && !atingidosNesteAtaque.contains(inimigo.getId())) {

                    Timeline piscar = new Timeline(
                            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0),
                                    new javafx.animation.KeyValue(inimigo.getCorpo().opacityProperty(), 1)),
                            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.05),
                                    new javafx.animation.KeyValue(inimigo.getCorpo().opacityProperty(), 0.2)),
                            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.1),
                                    new javafx.animation.KeyValue(inimigo.getCorpo().opacityProperty(), 1))
                    );
                    piscar.setCycleCount(3); // número de piscadas
                    piscar.play();

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

                    if (inimigo.getEnemyType() != EnemyType.BOSS) {
                        inimigo.aplicarKnockback(10 * dir, -0.5, 1.0);
                    }

                    if (inimigo.estaMorto()) {
                        // 50% de chance de dropar orb
                        if (Math.random() < 1) {
                            var bounds = inimigo.getCorpo().getBoundsInParent();
                            double centerX = bounds.getMinX() + bounds.getWidth() / 2.0;
                            double centerY = bounds.getMinY() + bounds.getHeight() / 2.0;

                            // Guarda posição centralizada para o GameLoop gerar orb
                            orbsParaGerar.add(new double[]{centerX, centerY});
                        }


                        root.getChildren().remove(inimigo.getCorpo());
                        inimigosMortosPorFase.add(inimigo.getId());
                        iterInimigo.remove();
                    }
                }
            }
        }


        // check colisão com partículas do boss
        if (bossParticles != null) {
            Iterator<BossProjectileParticle> iterP = bossParticles.iterator();
            while (iterP.hasNext()) {
                BossProjectileParticle particle = iterP.next();
                if (particle == null) continue;

                if (ataqueHitbox.getBoundsInParent().intersects(particle.getCorpo().getBoundsInParent())) {

                    // fade out visual
                    Timeline fadeOut = new javafx.animation.Timeline(
                            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0),
                                    new javafx.animation.KeyValue(particle.getCorpo().opacityProperty(), 1)),
                            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.2),
                                    new javafx.animation.KeyValue(particle.getCorpo().opacityProperty(), 0))
                    );
                    fadeOut.setOnFinished(e -> {
                        root.getChildren().remove(particle.getCorpo());
                        particle.markRemoved(); // cria um flag no BossProjectileParticle
                    });

                    fadeOut.play();
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

    public Player getPlayer() {
        return personagem;
    }

}
