package com.myrpggame.Utils.Attack;

import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Models.Projectile;
import com.myrpggame.Utils.HUDVida;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnemyAttack {

    private final Inimigo inimigo;
    private final Player personagem;

    private boolean atacando = false;
    private boolean atacandoEmCooldown = false;
    private boolean danoAplicado = false;

    private boolean movendo = false;

    private long ataqueStartTime = 0;
    private long ataqueDuration;
    private long ataqueCooldown;

    private boolean attackTargetSet = false;

    // Para verificar movimento (uso da posição visual: layoutX + translateX)
    private double ultimoX = 0;
    private double ultimoY = 0;
    private static final double MOVE_EPS = 0.5; // tolerância pra evitar “tremida”

    private long lastShotTime = 0;
    private static final long SHOT_INTERVAL = 1_000_000_000; // 1 segundo


    private final List<Projectile> projectiles = new ArrayList<>();

    private final Pane gameWorld;

    public EnemyAttack(Inimigo inimigo, Player personagem , Pane gameWorld) {
        this.inimigo = inimigo;
        this.personagem = personagem;
        definirDuracoes();
        this.gameWorld = gameWorld;
        // salvar posição inicial do inimigo (posição visual)
        if (inimigo.getCorpo() != null) {
            this.ultimoX = getVisualX(inimigo.getCorpo());
            this.ultimoY = getVisualY(inimigo.getCorpo());
        }
    }

    private void definirDuracoes() {
        switch (inimigo.getEnemyType()) {
            case COMMON -> { ataqueDuration = 400_000_000; ataqueCooldown = 1_500_000_000; }
            case TANK   -> { ataqueDuration = 600_000_000; ataqueCooldown = 2_000_000_000; }
            case FLYING -> { ataqueDuration = 1_500_000_000; ataqueCooldown = 1_800_000_000; }
            case ARCHER -> { ataqueDuration = 700_000_000; ataqueCooldown = 2_500_000_000L; }
            case BOSS   -> { ataqueDuration = 800_000_000; ataqueCooldown = 3_000_000_000L; }
        }
    }

    // calcula posição visual atual do node (layout + translate)
    private double getVisualX(ImageView iv) {
        return iv.getX() + iv.getTranslateX();
    }
    private double getVisualY(ImageView iv) {
        return iv.getY() + iv.getTranslateY();
    }

    // =================== Verificar se está correndo (usa posição visual) ===================
    private void verificarMovimento() {
        double xAtual = getVisualX(inimigo.getCorpo());
        double yAtual = getVisualY(inimigo.getCorpo());

        movendo = (Math.abs(xAtual - ultimoX) > MOVE_EPS) || (Math.abs(yAtual - ultimoY) > MOVE_EPS);

        ultimoX = xAtual;
        ultimoY = yAtual;
    }

    // =================== Processa cooldown ===================
    private void processCooldown(long now) {
        if (atacandoEmCooldown && !atacando) {
            if (now - ataqueStartTime >= ataqueCooldown) {
                atacandoEmCooldown = false;
            }
        }
    }

    // =================== Método principal ===================
    public void processarInimigo(long now, HUDVida hudVida, ImageView playerHitbox , Pane gameWorld) {
        processCooldown(now);

        // verificar movimento aqui (GameLoop já chamou inimigo.seguir antes)
        verificarMovimento();

        switch (inimigo.getEnemyType()) {
            case FLYING -> processFlying(now, hudVida, playerHitbox);
            case ARCHER -> processArcher(now , hudVida , playerHitbox , gameWorld);
            default     -> processDefault(now, hudVida, playerHitbox);
        }
    }

    private void processArcher(long now, HUDVida hudVida, ImageView playerHitbox, Pane gameWorld) {
        if (!atacando && !atacandoEmCooldown) {
            if (povArcher(inimigo, playerHitbox) && now - lastShotTime >= SHOT_INTERVAL) {
                lastShotTime = now;
                iniciarAtaque(now);

                // cria a flecha
                double dx = playerHitbox.getTranslateX() - inimigo.getCorpo().getTranslateX();
                double dy = playerHitbox.getTranslateY() - inimigo.getCorpo().getTranslateY();

                Projectile flecha = new Projectile(
                        inimigo.getCorpo().getTranslateX() + inimigo.getCorpo().getBoundsInLocal().getWidth()/2,
                        inimigo.getCorpo().getTranslateY() + inimigo.getCorpo().getBoundsInLocal().getHeight()/2,
                        dx, dy,
                        8,
                        inimigo.getDano(),
                        "/assets/inimigos/archer/projectile.png",
                        playerHitbox
                );
                projectiles.add(flecha);
            }
        }

        // Atualiza projéteis
        updateProjectiles(hudVida, playerHitbox, gameWorld);

        if (now - ataqueStartTime >= ataqueDuration) {
            finalizarAtaque(now);
        }
    }

    public void updateProjectiles(HUDVida hudVida, ImageView playerHitbox, Pane gameWorld) {
        Iterator<Projectile> iterator = projectiles.iterator();

        while (iterator.hasNext()) {
            Projectile p = iterator.next();
            p.update();

            // adiciona visual se ainda não estiver
            if (!gameWorld.getChildren().contains(p.getCorpo())) {
                gameWorld.getChildren().add(p.getCorpo());
            }

            // colisão com player
            if (p.getCorpo().getBoundsInParent().intersects(playerHitbox.getBoundsInParent())) {
                hudVida.tomarDano(p.getDano(), personagem);
                gameWorld.getChildren().remove(p.getCorpo());
                iterator.remove();
                continue;
            }

            // remover se sair da tela
            if (p.getCorpo().getTranslateX() < 0 || p.getCorpo().getTranslateX() > 1920 ||
                    p.getCorpo().getTranslateY() < 0 || p.getCorpo().getTranslateY() > 1080) {
                gameWorld.getChildren().remove(p.getCorpo());
                iterator.remove();
            }
        }
    }


    private boolean povArcher(Inimigo inimigo, ImageView player) {
        double alcanceHorizontal = 400;
        double alcanceVertical = 300;

        double dx = player.getTranslateX() - inimigo.getCorpo().getTranslateX();
        double dy = player.getTranslateY() - inimigo.getCorpo().getTranslateY();

        return Math.abs(dx) <= alcanceHorizontal && Math.abs(dy) <= alcanceVertical;
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


    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    // =================== Métodos de estado ===================
    public boolean isAtacando() { return atacando; }
    public boolean isRunning() { return movendo; }
}
