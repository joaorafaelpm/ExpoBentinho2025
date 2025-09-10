package com.myrpggame.Utils;

import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.*;
import com.myrpggame.Utils.Animation.EnemyAnimation;
import com.myrpggame.Utils.Animation.PlayerAnimation;
import com.myrpggame.Utils.Attack.EnemyAttack;
import com.myrpggame.Utils.Attack.PlayerAttack;
import com.myrpggame.Utils.Attack.boss.BossAttack;
import com.myrpggame.Utils.Attack.boss.BossProjectile;
import com.myrpggame.Utils.Attack.boss.BossProjectileParticle;
import com.myrpggame.Utils.PlayerCamera.Camera;
import com.myrpggame.Utils.PlayerMovement.PlayerMovement;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.List;
import java.util.Random;


import java.util.*;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class GameLoop extends AnimationTimer {

    private final ImageView player;
    private final Pane gameWorld;
    private final Pane pauseMenu;
    private final HUDVida hudVida;
    private final Camera camera;
    private final VBox telaParabens;

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

    private boolean invulneravel = false;
    private long invulneravelStart = 0;
    private static final long INVULNERAVEL_DURATION = 1_500_000_000L; // 1s de invulnerabilidade

    private BossAttack bossAttack; // inicializado somente se houver boss na sala

    private long lastLifeOrbUse = 0;
    private static final long LIFE_ORB_COOLDOWN = 100_000_000L; // 100ms em nanos

    private final Set<KeyCode> pressedKeys;

    final double BOSS_WAKE_DISTANCE = 600.0;
    final double BOSS_WAKE_DISTANCE_SQ = BOSS_WAKE_DISTANCE * BOSS_WAKE_DISTANCE;

    private boolean exibindoTelaParabens = false;

    private final List<String> bossMusicFiles = List.of(
            "audio/BackGroundSound (1).mp3",
            "audio/BackGroundSound (2).mp3",
            "audio/BackGroundSound (3).mp3",
            "audio/BackGroundSound (4).mp3",
            "audio/BackGroundSound (5).mp3",
            "audio/BackGroundSound (6).mp3",
            "audio/BackGroundSound (7).mp3",
            "audio/BackGroundSound (8).mp3",
            "audio/BackGroundSound (9).mp3"
    );
    private MediaPlayer bossMusicPlayer;
    private boolean bossMusicStarted = false;


    private void tocarMusicaBoss() {
        if (bossMusicStarted) return; // só inicia uma vez
        bossMusicStarted = true;

        Random random = new Random();
        String filePath = bossMusicFiles.get(random.nextInt(bossMusicFiles.size()));

        // Usa getResource para pegar o arquivo dentro do resources
        Media media = new Media(Objects.requireNonNull(getClass().getResource("/" + filePath)).toExternalForm());
        bossMusicPlayer = new MediaPlayer(media);
        bossMusicPlayer.setVolume(1);
        bossMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // toca em loop
        bossMusicPlayer.play();

    }

    public GameLoop(ImageView player, Pane gameWorld, Pane pauseMenu, Set<KeyCode> pressedKeys, HUDVida hudVida , VBox telaParabens) {
        this.player = player;
        this.telaParabens = telaParabens;
        this.gameWorld = gameWorld;
        this.pauseMenu = pauseMenu;
        this.hudVida = hudVida;
        this.pressedKeys = pressedKeys;
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
        if (pauseMenu.isVisible() || telaParabens.isVisible()) return;

        if (invulneravel && System.nanoTime() - invulneravelStart >= INVULNERAVEL_DURATION) {
            invulneravel = false;
        }

        if (invulneravel) {
            player.setVisible((System.nanoTime() / 100_000_000) % 2 == 0); // pisca
        } else {
            player.setVisible(true);
        }

        if (pressedKeys.contains(KeyCode.E)) {
            tentarUsarLifeOrb(now);
        }
        boolean atacando = playerAttack.isAtacando();
        boolean dashando = playerMovement.isDashing();

        if (bossAttack != null) {
            if (!bossAttack.isAcordado()) {
                tocarMusicaBoss();

                double px = player.getTranslateX();
                double py = player.getTranslateY();
                double bx = bossAttack.getBossX();
                double by = bossAttack.getBossY();

                double dx = px - bx;
                double dy = py - by;
                double dist2 = dx * dx + dy * dy;
                if (dist2 < BOSS_WAKE_DISTANCE_SQ) {
                    bossAttack.acordar(now);
                }
            }
            playerAttack.setBossParticles(bossAttack.getParticles());
            bossAttack.atualizar(now); // atualiza boss e dispara ataque se necessário
            // atualiza projéteis do boss
            Iterator<BossProjectile> iter = bossAttack.getProjeteis().iterator();
            Iterator<BossProjectileParticle> iterParticles = bossAttack.getParticles().iterator();
            while (iter.hasNext()) {
                BossProjectile projectile = iter.next();
                projectile.atualizar(gerenciadorDeFase.getFaseAtual().getAltura() , gerenciadorDeFase.getFaseAtual().getLargura());

                if (!invulneravel && projectile.colidiu(player)) {
                    tomarDano(1); // agora respeita invulnerabilidade
                    projectile.remover(gameWorld);
                    iter.remove();
                    continue;
                }

                if (projectile.saiuDaTela(
                        gerenciadorDeFase.getFaseAtual().getAltura(),
                        gerenciadorDeFase.getFaseAtual().getLargura()
                )) {
                    projectile.remover(gameWorld);
                    iter.remove();
                }
            }
            while (iterParticles.hasNext()) {
                BossProjectileParticle particle = iterParticles.next();

                // se expirou (TTL) e ainda não está em fade, inicia fade
                if (!particle.isDying() && particle.isExpired(now)) {
                    particle.startFadeAndMark(gameWorld);
                }

                // atualiza movimento (se ainda estiver visível)
                particle.seguir(player.getTranslateX(), player.getTranslateY(), now);

                // colisão com player
                if (!invulneravel && particle.colidiu(player)) {
                    tomarDano(1);
                    particle.startFadeAndMark(gameWorld);
                    iterParticles.remove(); // remove da lista imediatamente (node será removido no callback)
                    continue;
                }

                // se já foi removida do scenegraph (fade acabou) ou saiu da tela, remove da lista
                if (particle.isRemoved() || particle.getCorpo().getParent() == null
                        || particle.saiuDaTela(gerenciadorDeFase.getFaseAtual().getAltura(),
                        gerenciadorDeFase.getFaseAtual().getLargura())) {
                    iterParticles.remove();
                }
            }
        }

        if (bossAttack != null && bossAttack.isAcordado() && bossAttack.getBoss().estaMorto() && !exibindoTelaParabens) {
            exibindoTelaParabens = true;
            Platform.runLater(() -> {
                // Aqui você chama a tela que já existe no GameGUI
                telaParabens.setVisible(true);
                if (bossMusicPlayer != null) {
                    bossMusicPlayer.stop(); // para a música
                }
            });
        }

        if (!morto && !respawning) {
            playerMovement.aplicarGravidade();
            playerMovement.processarPulo(now);
            playerMovement.processarMovimento();
            playerMovement.processarDash(now);
            playerAttack.atualizarHitboxAtaque();
            playerAttack.processarAtaque(now);
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
                else {
                    hudVida.adicionarLifeOrb();
                    gameWorld.getChildren().remove(orb);
                    orbs.remove(i);
                    i--;
                }
            }
        }


        // Detecta morte
        if (personagem.getVida() <= 0 && !morto) iniciarMorte(now , bossAttack);

        // Processa respawn
        if (morto && now - morteStartTime >= MORTE_DURATION) iniciarRespawn(now);

        // Finaliza respawn
        if (respawning && now - reviveStartTime >= REVIVE_TOTAL_DURATION) {
            playerMovement.desbloquearMovimento();
            playerAttack.desbloquearAtaque();
            respawning = false;
        }
    }

    private void tentarUsarLifeOrb(long now) {
        if (now - lastLifeOrbUse < LIFE_ORB_COOLDOWN) return; // cooldown
        if (personagem.getLifeOrb() <= 0) return; // sem orbs
        if (personagem.getVida() >= personagem.getVidaMaxima()) return; // vida já cheia

        // consome orb
        hudVida.removerLifeOrb();
        hudVida.curar(1, personagem);

        lastLifeOrbUse = now;
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


    private void iniciarMorte(long now , BossAttack bossAttack) {
        if (bossAttack != null) {
            bossMusicPlayer.stop();
            bossMusicStarted = false;
        }
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
        hudVida.resetarLifeOrb();
        Player.reset();
        gerenciadorDeFase.resetarFases();

        carregarSala();
        posicionarPlayerNoInicio();
    }

    public void carregarSala() {
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
                    bossAttack = new BossAttack(inimigo, personagem , gameWorld , gerenciadorDeFase.getFaseAtual());
                    break; // só precisa de 1 boss
                }
            }
        }

        playerAttack.setInimigos(inimigos);
        camera.setPhaseSize(fase.getLargura(), fase.getAltura());
        camera.update(player.getTranslateX(), player.getTranslateY());
    }

    public void posicionarPlayerNoInicio() {
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
//            Recebendo o boss
            Inimigo inimigo = fase.getGerenciadorDeInimigo().getInimigos().get(0);
            if (!inimigo.estaMorto() && player.getTranslateX() < 0 ) {
                player.setTranslateX(0);
            }
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

    public void resetarMusicaBoss() {
        if (bossMusicPlayer != null) {
            bossMusicPlayer.stop();
            bossMusicPlayer.dispose(); // libera recurso de áudio
        }
        bossMusicPlayer = null;
        bossMusicStarted = false;
    }

    public PlayerMovement getPlayerMovement() { return playerMovement; }
    public PlayerAttack getPlayerAttack() { return playerAttack; }
    public HUDVida getHudVida() { return hudVida; }

    public GerenciadorDeFase getGerenciadorDeFase() {
        return gerenciadorDeFase;
    }


    public void setExibindoTelaParabens(boolean b) {
        this.exibindoTelaParabens = b;
    }
}
