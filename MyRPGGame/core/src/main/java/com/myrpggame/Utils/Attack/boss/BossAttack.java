package com.myrpggame.Utils.Attack.boss;

import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossAttack {

    private final Inimigo boss;
    private final Player player;
    private final Pane gameWorld;
    private final Random random = new Random();

    private boolean acordado = false;
    private long wakeUpTime = 0;
    private final long WAKE_UP_DELAY = 1_000_000_000L; // 1s para acordar
    private final long ATTACK_INTERVAL = 5_000_000_000L; // intervalo entre ataques
    private long lastAttackTime = 0;

    private final int projectileWidth = 50;
    private final int projectileHeight = 50;
    private final double projectileSpeed = 5;

    private final List<BossProjectile> bossProjeteis = new ArrayList<>();

    public BossAttack(Inimigo boss, Player player, Pane gameWorld) {
        this.boss = boss;
        this.player = player;
        this.gameWorld = gameWorld;
    }

    // retorna os projéteis atuais do ataque
    public List<BossProjectile> getProjeteis() {
        return bossProjeteis;
    }

    // atualiza o boss e dispara o ataque se necessário
    public void atualizar(long now) {
        double dx = player.getSprite().getTranslateX() - boss.getCorpo().getTranslateX();

        if (!acordado && Math.abs(dx) < 400) {
            acordado = true;
            wakeUpTime = now;
        }

        // só dispara depois de acordar e respeitando o intervalo
        if (acordado && now - wakeUpTime >= WAKE_UP_DELAY) {
            if (now - lastAttackTime >= ATTACK_INTERVAL) {
                lastAttackTime = now;
                gerarAtaque();
            }
        }
    }

    // gera ataque completo com todos os projéteis e 2 caminhos seguros
    private void gerarAtaque() {
        double faseWidth = gameWorld.getWidth();

        // define dois caminhos seguros aleatórios
        double safe1 = random.nextDouble() * (faseWidth - 200);
        double safe2 = random.nextDouble() * (faseWidth - 200);
        if (Math.abs(safe1 - safe2) < 200) { // garante distância mínima
            safe2 += 200;
        }

        double spacing = projectileWidth; // largura dos projéteis
        int quantidadeProjeteis = (int) (faseWidth / spacing);

        // limpa projéteis antigos
        for (BossProjectile p : bossProjeteis) {
            p.remover(gameWorld);
        }
        bossProjeteis.clear();

        for (int i = 0; i < quantidadeProjeteis; i++) {
            double projX = i * spacing;

            // ignora os caminhos seguros
            if (Math.abs(projX - safe1) < spacing * 2 || Math.abs(projX - safe2) < spacing * 2) {
                continue;
            }

            BossProjectile p = new BossProjectile(projX, 0, projectileSpeed, projectileWidth, projectileHeight);
            bossProjeteis.add(p);
            gameWorld.getChildren().add(p.getCorpo());
        }
    }

    // limpa projéteis ao trocar de sala
    public void resetar() {
        for (BossProjectile p : bossProjeteis) {
            p.remover(gameWorld);
        }
        bossProjeteis.clear();
        acordado = false;
        wakeUpTime = 0;
        lastAttackTime = 0;
    }
}
