package com.myrpggame.Utils.Attack.boss;

import com.myrpggame.Enum.EstadoBossAtaque;
import com.myrpggame.Enum.TipoBossAtaque;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossAttack {

    private final Inimigo boss;
    private final Player player;
    private final Pane gameWorld;
    private final Fase fase;
    private final Random random = new Random();

    private boolean acordado = false;

    private final double projectileSpeed = 5;

    // adicione estes campos (ajuste se quiser):
    private static final int SAFE_ZONE_SIZE = 1; // quantas linhas acima e abaixo ficam seguras
    private static final int SAFE_ZONES = 6;          // agora 4 caminhos seguros
    private static final int SAFE_MIN_SEP_COLS = 2;   // exige ao menos 2 colunas de distância
    private static final double GAP_X = 10.0;         // espaçamento horizontal entre projéteis (px)
    private static final double GAP_Y = 10.0;         // espaçamento horizontal entre projéteis (py)

    // Invisibilidade + múltiplas ondas
    private long lastSwitch = 0;  // quando começou o estado atual
    private long lastTimeAttack = 0;

    private final long DURACAO_HELLBULLET = 15_000_000_000L; // 15s
    private final long DURACAO_PARTICLE   = 15_000_000_000L; // 15s
    private final long DURACAO_PAUSA      = 5_000_000_000L;  // 5s

    private final long INTERVALO_HELLBULLET = 2_000_000_000L; // 2s entre projéteis
    private final long INTERVALO_PARTICLE   = 5_000_000_000L; // 5s entre partículas

    private final long PARTICLE_LIFE_TIME = 5_000_000_000L; // 5s


    private final TipoBossAtaque tipoBossAtaque;
    private final List<BossProjectile> bossProjeteis = new ArrayList<>();
    private final List<BossProjectileParticle> bossParticles = new ArrayList<>();

    private EstadoBossAtaque estadoAtual = EstadoBossAtaque.HELL_BULLET;

    public BossAttack(Inimigo boss, Player player, Pane gameWorld , Fase fase) {
        this.boss = boss;
        this.player = player;
        this.gameWorld = gameWorld;
        this.fase = fase;
        this.tipoBossAtaque = TipoBossAtaque.AMBOS;
    }

    // retorna os projéteis atuais do ataque
    public List<BossProjectile> getProjeteis() {
        return bossProjeteis;
    }
    public List<BossProjectileParticle> getParticles() {
        return bossParticles;
    }

    public void atualizar(long now) {
        double dx = player.getSprite().getTranslateX() - boss.getCorpo().getTranslateX();
        if (!acordado && dx < 200) {
            acordado = true;
            estadoAtual = EstadoBossAtaque.HELL_BULLET;
            lastSwitch = now;
            lastTimeAttack = now;
        }

        if (!acordado) return;

        switch (estadoAtual) {
            case HELL_BULLET -> {
                // dispara ondas contínuas a cada 2s
                if (now - lastTimeAttack >= INTERVALO_HELLBULLET) {
                    lastTimeAttack = now;
                    gerarAtaque();
                }
                // troca para PARTICLE após 15s
                if (now - lastSwitch >= DURACAO_HELLBULLET) {
                    estadoAtual = EstadoBossAtaque.PARTICLE;
                    lastSwitch = now;
                    lastTimeAttack = now;
                }
            }
            case PARTICLE -> {
                if (now - lastTimeAttack >= INTERVALO_PARTICLE) {
                    lastTimeAttack = now;
                    gerarParticulasSeguidoras(now);
                }

                if (now - lastSwitch >= DURACAO_PARTICLE) {
                    estadoAtual = EstadoBossAtaque.PAUSA;
                    lastSwitch = now;
                }
            }

            case PAUSA -> {
                // nada acontece, apenas espera
                if (now - lastSwitch >= DURACAO_PAUSA) {
                    estadoAtual = EstadoBossAtaque.HELL_BULLET;
                    lastSwitch = now;
                    lastTimeAttack = now;
                }
            }
        }
    }
    private void expireAllParticles() {
        List<BossProjectileParticle> copia = new ArrayList<>(bossParticles);
        for (BossProjectileParticle p : copia) {
            if (!p.isDying()) {
                p.startFadeAndMark(gameWorld); // inicia fade; NÃO remove da lista
            }
        }
    }

    // gerar com spawnTime e ttl
    private void gerarParticulasSeguidoras(long now) {
        // marca todas as partículas existentes para sumir (fade)
        expireAllParticles();

        int particleCount = 1 + random.nextInt(2); // 1 a 2 partículas
        for (int i = 0; i < particleCount; i++) {
            double x = random.nextDouble() * (fase.getLargura() - 40);
            double y = random.nextDouble() * (fase.getAltura() - 40);

            BossProjectileParticle particle = new BossProjectileParticle(
                    x, y, 5, 5, 60, 60, now, PARTICLE_LIFE_TIME
            );
            bossParticles.add(particle);
            gameWorld.getChildren().add(particle.getCorpo());
        }
    }

    private void gerarAtaque() {
        dispararOnda();
    }

    private void dispararOnda() {
        double faseWidth = fase.getLargura();
        int projectileVerticalWidth = 100;
        double columnWidth = projectileVerticalWidth + GAP_X;

        // --- Calcula colunas para ataque vertical ---
        List<Double> colXs = new ArrayList<>();
        for (double x = 0; x + projectileVerticalWidth <= faseWidth; x += columnWidth) {
            colXs.add(x);
        }
        int cols = colXs.size();
        if (cols == 0) return;

        // Calcula safeCols em índices
        List<Integer> safeCols = pickSafeIndices(cols);

        // Cria um array que marca todas as colunas seguras (expandindo metade do projetil)
        boolean[] isSafeColumn = new boolean[cols];
        for (int idx : safeCols) {
            isSafeColumn[idx] = true;
            // Expande metade do projetil para a direita, se não estourar o array
            if (idx + 1 < cols) isSafeColumn[idx + 1] = true;
        }

        // --- Ataque Vertical ---
        if (tipoBossAtaque == TipoBossAtaque.VERTICAL || tipoBossAtaque == TipoBossAtaque.AMBOS) {
            for (int i = 0; i < cols; i++) {
                if (isSafeColumn[i]) continue; // pula safe zones

                double projX = colXs.get(i);
                int projectileVerticalHeight = 150;
                BossProjectile verticalProj = new BossProjectileVertical(
                        projX, 0, projectileSpeed ,projectileSpeed, projectileVerticalWidth, projectileVerticalHeight
                );
                adicionarProjetil(verticalProj);
            }
        }

        // --- Ataque Horizontal continua igual ---
        int projectileHorizontalHeigth = 100;
        int horizontalCount = (int)((fase.getAltura() - projectileHorizontalHeigth) / (projectileHorizontalHeigth + GAP_Y)) + 1;
        int startRow = horizontalCount / 2;
        int safeRow = startRow + random.nextInt(horizontalCount - startRow);

        for (int i = 0; i < horizontalCount; i++) {
            boolean isSafe = i <= safeRow && i >= safeRow - SAFE_ZONE_SIZE;
            if (isSafe) continue;

            double startY = i * (projectileHorizontalHeigth + GAP_Y);
            int projectileHorizontalWidth = 150;
            BossProjectile horizontalProj = new BossProjectileHorizontal(
                    0, startY, projectileSpeed, projectileSpeed,
                    projectileHorizontalWidth, projectileHorizontalHeigth
            );
            adicionarProjetil(horizontalProj);
        }
    }

    /**
     * Escolhe 'count' índices de colunas em [0, cols-1] garantindo distância mínima entre eles.
     * Agora SAFE_ZONE_SIZE adiciona meio projétil para cima da safe zone.
     */
    private List<Integer> pickSafeIndices(int cols) {
        List<Integer> result = new ArrayList<>();
        if (cols <= 0 || BossAttack.SAFE_ZONES <= 0) return result;

        int attempts = 0, maxAttempts = 500;

        while (result.size() < BossAttack.SAFE_ZONES && attempts < maxAttempts) {
            int idx = random.nextInt(cols);
            boolean ok = true;
            for (int s : result) {
                // aqui consideramos a safeZone expandida: SAFE_ZONE_SIZE = 1 → bloqueia idx e +1
                if (Math.abs(idx - s) <= SAFE_ZONE_SIZE + BossAttack.SAFE_MIN_SEP_COLS) {
                    ok = false;
                    break;
                }
            }
            if (ok) result.add(idx);
            attempts++;
        }

        // fallback determinístico
        if (result.size() < BossAttack.SAFE_ZONES) {
            for (int i = 0; i < cols && result.size() < BossAttack.SAFE_ZONES; i++) {
                boolean ok = true;
                for (int s : result) {
                    if (Math.abs(i - s) <= SAFE_ZONE_SIZE + BossAttack.SAFE_MIN_SEP_COLS) {
                        ok = false;
                        break;
                    }
                }
                if (ok) result.add(i);
            }
        }

        return result;
    }

    private void adicionarProjetil(BossProjectile proj) {
        bossProjeteis.add(proj);
        gameWorld.getChildren().add(proj.getCorpo());

        proj.setOnReachLimit(() -> {
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(proj.getCorpo().opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(proj.getCorpo().opacityProperty(), 0))
            );
            fadeOut.setOnFinished(e -> proj.remover(gameWorld));
            fadeOut.play();
        });
    }

    // limpa projéteis ao trocar de sala
    public void resetar() {
        for (BossProjectile projetil : bossProjeteis) {
            projetil.remover(gameWorld);
        }
        for (BossProjectileParticle particle : bossParticles) {
            particle.remover(gameWorld);
        }
        bossProjeteis.clear();
        bossParticles.clear();
        acordado = false;
        lastTimeAttack = 0;
    }
}
