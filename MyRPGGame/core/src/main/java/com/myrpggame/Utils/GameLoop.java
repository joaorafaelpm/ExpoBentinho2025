package com.myrpggame.Utils;

import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.*;
import com.myrpggame.Utils.Animation.EnemyAnimation;
import com.myrpggame.Utils.Animation.PlayerAnimation;
import com.myrpggame.Utils.Attack.EnemyAttack;
import com.myrpggame.Utils.Attack.PlayerAttack;
import com.myrpggame.Utils.PlayerCamera.Camera;
import com.myrpggame.Utils.PlayerMovement.PlayerMovement;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
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
    private static final long REVIVE_TOTAL_DURATION = 5_000_000_000L; // 8s

    public GameLoop(ImageView player, Pane gameWorld, Pane pauseMenu, Set<javafx.scene.input.KeyCode> pressedKeys, HUDVida hudVida) {
        this.player = player;
        this.gameWorld = gameWorld;
        this.pauseMenu = pauseMenu;
        this.hudVida = hudVida;

        Fase faseAtual = gerenciadorDeFase.getFaseAtual();

        // Inicializa câmera
        this.camera = new Camera(getLargura(), getAltura(), faseAtual.getLargura(), faseAtual.getAltura());

        // Inicializa player e sistemas
        this.personagem = hudVida.getPlayer();
        this.playerMovement = new PlayerMovement(player, faseAtual.getAltura(), pressedKeys, 0);
        this.playerAttack = new PlayerAttack(player, gameWorld, personagem, 0, new ArrayList<>());
        this.playerAnimation = new PlayerAnimation(getLargura(), faseAtual.getAltura(), morto, respawning,
                playerAttack.isAtacando(), playerMovement.isDashing(), morteStartTime,
                reviveStartTime, pressedKeys, player, 0 , playerMovement);

        carregarSala();
    }

    @Override
    public void handle(long now) {
        if (pauseMenu.isVisible()) return;

        boolean atacando = playerAttack.isAtacando();
        boolean dashando = playerMovement.isDashing();

        // Movimento e ataque
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
                // Para inimigos voadores
                if (inimigo.getEnemyType() == EnemyType.FLYING) {
                    if (!ataque.isAtacando()) {
                        inimigo.seguir(player.getTranslateX(), player.getTranslateY(), now);
                    }
                } else {
                    // Inimigos normais continuam seguindo
                    inimigo.seguir(player.getTranslateX(), player.getTranslateY(), now);
                }


                // Processa ataque
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

        //  resetar salas concluídas (assim ao recomeçar os inimigos voltam)
        Player.reset();

        //  voltar para a primeira fase
        gerenciadorDeFase.resetarFases(); // você vai criar esse método no GerenciadorDeFase

        carregarSala();
        posicionarPlayerNoInicio();
    }


    private void carregarSala() {
        Fase fase = gerenciadorDeFase.getFaseAtual();

        gameWorld.getChildren().clear();

        // Inicializa fase
        fase.inicializar();

        // Adiciona root da fase
        gameWorld.getChildren().add(fase.getRoot());

        // Adiciona player
        gameWorld.getChildren().add(player);

        // Inimigos
        GerenciadorDeInimigo gerInimigos = fase.getGerenciadorDeInimigo();

        inimigos.clear();
        if (!Player.isSalaConcluida(fase)) {
            gerInimigos.inicializar(); // só inicializa se não concluída
            inimigos.addAll(gerInimigos.getInimigos());

            for (Inimigo inimigo : inimigos) {
                // Cria EnemyAttack e EnemyAnimation individuais
                EnemyAttack ataque = new EnemyAttack(inimigo , personagem);
                EnemyAnimation anim = new EnemyAnimation();

                // Associa aos inimigos
                inimigo.setAttack(ataque);
                inimigo.setAnimation(anim);

                // Adiciona visual
                gameWorld.getChildren().add(inimigo.getCorpo());
            }
        }

        playerAttack.setInimigos(inimigos);

        // Atualiza câmera
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

        // Avançar fase
        if (player.getTranslateX() > fase.getLargura() - 40 && !gerenciadorDeFase.ultimaFase()) {
            gerenciadorDeFase.avancarFase();
            carregarSala();
            posicionarPlayerNoInicio();
        }

        // Limite direita última fase
        if (gerenciadorDeFase.ultimaFase()) {
            double maxX = fase.getLargura() - player.getBoundsInLocal().getWidth();
            if (player.getTranslateX() > maxX) player.setTranslateX(maxX);
        }

        // Limite esquerda / voltar fase
        if (player.getTranslateX() < 0) {
            if (!gerenciadorDeFase.primeiraFase()) {
                gerenciadorDeFase.voltarFase();
                carregarSala();

                player.setTranslateX(gerenciadorDeFase.getFaseAtual().getLargura() - 40);
            } else player.setTranslateX(0);
        }
    }

    public PlayerMovement getPlayerMovement() { return playerMovement; }
    public PlayerAttack getPlayerAttack() { return playerAttack; }
}
