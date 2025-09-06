package com.myrpggame.Utils;

import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.*;
import com.myrpggame.Utils.Animation.EnemyAnimation;
import com.myrpggame.Utils.Animation.PlayerAnimation;
import com.myrpggame.Utils.Attack.EnemyAttack;
import com.myrpggame.Utils.Attack.PlayerAttack;
import com.myrpggame.Utils.Attack.boss.BossAttack;
import com.myrpggame.Utils.Attack.boss.BossProjectile;
import com.myrpggame.Utils.PlayerCamera.Camera;
import com.myrpggame.Utils.PlayerMovement.PlayerMovement;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class GameLoop extends AnimationTimer {

    private final ImageView player;
    private final Pane gameWorld;
    private final Pane pauseMenu;
    private HUDVida hudVida;
    private final Camera camera;

    private final Player personagem;
    private final PlayerMovement playerMovement;
    private final PlayerAttack playerAttack;
    private final PlayerAnimation playerAnimation;

    private final List<Inimigo> inimigos = new ArrayList<>();
    private final GerenciadorDeFase gerenciadorDeFase = new GerenciadorDeFase();

    private boolean morto = false;
    private boolean respawning = false;

    private long morteStartTime = 0;
    private long reviveStartTime = 0;

    private static final long MORTE_DURATION = 1_500_000_000; // 1,5s
    private static final long REVIVE_TOTAL_DURATION = 5_000_000_000L; // 5s

    private final List<ImageView> orbs = new ArrayList<>();
    private final List<double[]> orbsPendentes = new ArrayList<>(); // posições de orbs geradas por inimigos
    private final List<BossProjectile> bossProjeteis = new ArrayList<>();

    private boolean invulneravel = false;
    private long invulneravelStart = 0;
    private static final long INVULNERAVEL_DURATION = 1_000_000_000L; // 1s de invulnerabilidade



    private BossAttack bossAttack; // inicializado somente se houver boss na sala


    public GameLoop(ImageView player, Pane gameWorld, Pane pauseMenu, Set<javafx.scene.input.KeyCode> pressedKeys, HUDVida hudVida) {
        this.player = player;
        this.gameWorld = gameWorld;
        this.pauseMenu = pauseMenu;
        this.hudVida = hudVida;

        Fase faseAtual = gerenciadorDeFase.getFaseAtual();

        this.camera = new Camera(getLargura(), getAltura(), faseAtual.getLargura(), faseAtual.getAltura());

        this.personagem = hudVida.getPlayer();
        this.playerMovement = new PlayerMovement(player, faseAtual.getAltura(), pressedKeys, 0);
        this.playerAttack = new PlayerAttack(player, gameWorld, personagem, 0, new ArrayList<>(), orbsPendentes);
        this.playerAnimation = new PlayerAnimation(getLargura(), faseAtual.getAltura(), morto, respawning,
                playerAttack.isAtacando(), playerMovement.isDashing(), morteStartTime,
                reviveStartTime, pressedKeys, player, 0 , playerMovement);

        carregarSala();
    }

    @Override
    public void handle(long now) {
        if (pauseMenu.isVisible()) return;

        if (invulneravel && System.nanoTime() - invulneravelStart >= INVULNERAVEL_DURATION) {
            invulneravel = false;
        }

        if (invulneravel) {
            player.setVisible((System.nanoTime() / 100_000_000) % 2 == 0); // pisca
        } else {
            player.setVisible(true);
        }

        boolean atacando = playerAttack.isAtacando();
        boolean dashando = playerMovement.isDashing();

        if (bossAttack != null) {
            bossAttack.atualizar(now); // atualiza boss e dispara ataque se necessário

            // atualiza projéteis do boss
            Iterator<BossProjectile> iter = bossAttack.getProjeteis().iterator();
            while (iter.hasNext()) {
                BossProjectile p = iter.next();
                p.atualizar();

                if (!invulneravel && p.colidiu(player)) {
                    tomarDano(1); // agora respeita invulnerabilidade
                    p.remover(gameWorld);
                    iter.remove();
                    continue;
                }

                if (p.saiuDaTela(gameWorld.getHeight())) {
                    p.remover(gameWorld);
                    iter.remove();
                }
            }
        }

        if (!morto && !respawning) {
            playerMovement.aplicarGravidade();
            playerMovement.processarPulo(now);
            playerMovement.processarMovimento();
            playerMovement.processarDash(now);
            playerAttack.processarAtaque(now);
            playerAttack.atualizarHitboxAtaque();
        }

        atualizarInimigos(now);
        verificarTrocaSala();

        // Atualiza câmera
        camera.update(player.getTranslateX(), player.getTranslateY());
        gameWorld.setTranslateX(-camera.getX());
        gameWorld.setTranslateY(-camera.getY());

        // Atualiza animações do player
        playerAnimation.atualizarFlags(morto, respawning, atacando, dashando, morteStartTime, reviveStartTime);
        playerAnimation.atualizarEstado(now);
        playerAnimation.atualizarAnimacao(now);

        // =================== Gera orbs pendentes centralizadas ===================
        if (!orbsPendentes.isEmpty()) {
            for (double[] pos : new ArrayList<>(orbsPendentes)) {
                gerarOrb(pos[0], pos[1]);
            }
            orbsPendentes.clear();
        }

        // =================== Coleta orbs ===================
        for (int i = 0; i < orbs.size(); i++) {
            ImageView orb = orbs.get(i);

            if (player.getBoundsInParent().intersects(orb.getBoundsInParent())) {
                // só coleta se não estiver com a vida cheia
                if (personagem.getVida() < personagem.getVidaMaxima()) {
                    hudVida.curar(1, personagem);
                    gameWorld.getChildren().remove(orb);
                    orbs.remove(i);
                    i--;
                }
            }
        }


        // Detecta morte
        if (personagem.getVida() <= 0 && !morto) iniciarMorte(now);

        // Processa respawn
        if (morto && now - morteStartTime >= MORTE_DURATION) iniciarRespawn(now);

        // Finaliza respawn
        if (respawning && now - reviveStartTime >= REVIVE_TOTAL_DURATION) {
            playerMovement.desbloquearMovimento();
            playerAttack.desbloquearAtaque();
            respawning = false;
        }
    }

    private void atualizarInimigos(long now) {
        for (Inimigo inimigo : new ArrayList<>(inimigos)) {
            inimigo.atualizar(now);
            inimigo.atualizarDirecao(player.getTranslateX());

            EnemyAttack ataque = inimigo.getAttack();
            if (ataque != null) {
                if (inimigo.getEnemyType() == EnemyType.FLYING && !ataque.isAtacando()) {
                    inimigo.seguir(player.getTranslateX(), player.getTranslateY(), now);
                } else if (inimigo.getEnemyType() != EnemyType.FLYING) {
                    inimigo.seguir(player.getTranslateX(), player.getTranslateY(), now);
                }

                ataque.processarInimigo(now, hudVida, player);

                if (inimigo.getAnimation() != null) {
                    inimigo.getAnimation().atualizarEstado(inimigo);
                    inimigo.getAnimation().atualizarAnimacao(inimigo);
                }
            }

            if (inimigo.estaMorto()) {
                inimigos.remove(inimigo);
                gameWorld.getChildren().remove(inimigo.getCorpo());
            }
        }
    }

    private void gerarOrb(double centerX, double centerY) {
        ImageView orb = new ImageView("/assets/lifeOrb.png");
        orb.setFitWidth(40);
        orb.setFitHeight(40);

        orb.setTranslateX(centerX - orb.getFitWidth() / 2.0);
        orb.setTranslateY(centerY - orb.getFitHeight() / 2.0);

        orbs.add(orb);
        gameWorld.getChildren().add(orb);
    }


    private void iniciarMorte(long now) {
        morto = true;
        morteStartTime = now;
        playerMovement.bloquearMovimento();
        playerAttack.bloquearAtaque();
    }

    private void iniciarRespawn(long now) {
        respawning = true;
        morto = false;
        reviveStartTime = now;

        hudVida.resetarVida();
        Player.reset();
        gerenciadorDeFase.resetarFases();

        carregarSala();
        posicionarPlayerNoInicio();
    }

    private void carregarSala() {
        Fase fase = gerenciadorDeFase.getFaseAtual();
        gameWorld.getChildren().clear();

        fase.inicializar();
        gameWorld.getChildren().add(fase.getRoot());
        gameWorld.getChildren().add(player);

        GerenciadorDeInimigo gerInimigos = fase.getGerenciadorDeInimigo();

        inimigos.clear();
        if (!Player.isSalaConcluida(fase)) {
            gerInimigos.inicializar();
            inimigos.addAll(gerInimigos.getInimigos());

            for (Inimigo inimigo : inimigos) {
                EnemyAttack ataque = new EnemyAttack(inimigo , personagem);
                EnemyAnimation anim = new EnemyAnimation();

                inimigo.setAttack(ataque);
                inimigo.setAnimation(anim);

                gameWorld.getChildren().add(inimigo.getCorpo());
            }

            if (bossAttack != null) {
                bossAttack.resetar(); // limpa projéteis
                bossAttack = null;
            }


            // Inicializa bossAttack se houver boss
            for (Inimigo inimigo : inimigos) {
                if (inimigo.getEnemyType() == EnemyType.BOSS) {
                    bossAttack = new BossAttack(inimigo, personagem , gameWorld );
                    break; // só precisa de 1 boss
                }
            }
        }

        playerAttack.setInimigos(inimigos);
        camera.setPhaseSize(fase.getLargura(), fase.getAltura());
        camera.update(player.getTranslateX(), player.getTranslateY());
    }

    private void posicionarPlayerNoInicio() {
        Fase fase = gerenciadorDeFase.getFaseAtual();
        player.setTranslateX(40);
        player.setTranslateY(fase.getAltura() - player.getBoundsInLocal().getHeight());
        camera.update(player.getTranslateX(), player.getTranslateY());
    }

    private void verificarTrocaSala() {
        Fase fase = gerenciadorDeFase.getFaseAtual();

        if (player.getTranslateX() > fase.getLargura() - 40 && !gerenciadorDeFase.ultimaFase()) {
            gerenciadorDeFase.avancarFase();
            carregarSala();
            posicionarPlayerNoInicio();
        }

        if (gerenciadorDeFase.ultimaFase()) {
            double maxX = fase.getLargura() - player.getBoundsInLocal().getWidth();
            if (player.getTranslateX() > maxX) player.setTranslateX(maxX);
        }

        if (player.getTranslateX() < 0) {
            if (!gerenciadorDeFase.primeiraFase()) {
                gerenciadorDeFase.voltarFase();
                carregarSala();
                player.setTranslateX(gerenciadorDeFase.getFaseAtual().getLargura() - 40);
            } else player.setTranslateX(0);
        }
    }

    private void tomarDano(int dano) {
        hudVida.tomarDano(dano, personagem);
        invulneravel = true;
        invulneravelStart = System.nanoTime();
        // aqui você pode adicionar efeito visual, como piscar o player
    }


    public PlayerMovement getPlayerMovement() { return playerMovement; }
    public PlayerAttack getPlayerAttack() { return playerAttack; }
}
