// ===== GameLoop.java =====
package com.myrpggame.Utils;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.PlayerState;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.PlayerAttack.PlayerAttack;
import com.myrpggame.Utils.PlayerMovement.PlayerMovement;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.myrpggame.Utils.PlayerMovement.PlayerMovement.isFacingRight;

public class GameLoop extends AnimationTimer {

    private final ImageView player;
    private final Pane root;
    private final VBox pauseMenu;
    private final Set<KeyCode> pressedKeys;
    private final double alturaSala;
    private final double larguraSala;

    private int currentFrame = 0;
    private long lastUpdate = 0;

    private static final long ENEMY_ATTACK_COOLDOWN = 1_000_000_000; // 1s
    private long lastEnemyHitTime = 0;

    private PlayerState playerState = PlayerState.IDLE;
    private int salaAtual = 0;

    private final Player personagem = new Player();
    private HUDVida hudVida = new HUDVida(personagem);

    private boolean morto = false;
    private long morteStartTime = 0;
    private static final long MORTE_DURATION = 1_500_000_000;

    private PlayerMovement playerMovement;
    private PlayerAttack playerAttack;
    private List<Inimigo> inimigos = new ArrayList<>();

    private final Fase[] fases = {
            new Fase(Color.LIGHTBLUE, 1),
            new Fase(Color.LIGHTGREEN, 2),
            new Fase(Color.LIGHTPINK, 3),
            new Fase(Color.DARKRED, 1)
    };


    public GameLoop(ImageView player, Pane root, VBox pauseMenu, Set<KeyCode> pressedKeys,
                    double alturaSala, double larguraSala) {
        this.player = player;
        this.root = root;
        this.pauseMenu = pauseMenu;
        this.pressedKeys = pressedKeys;
        this.alturaSala = alturaSala;
        this.larguraSala = larguraSala;

        this.playerMovement = new PlayerMovement(player, alturaSala, pressedKeys, currentFrame);
        this.playerAttack = new PlayerAttack(player, root, personagem, currentFrame);

        carregarSala(0);
    }

    @Override
    public void handle(long now) {
        if (pauseMenu.isVisible()) return;

        if (!morto) {
            playerMovement.aplicarGravidade();
            playerMovement.processarPulo(now);
            playerMovement.processarMovimento();
            playerMovement.processarDash(now);
            playerAttack.processarAtaque(now);
            playerAttack.atualizarHitboxAtaque();
        }

        atualizarInimigos(now);
        verificarTrocaSala();
        atualizarEstado(now);
        atualizarAnimacao(now);

        if (personagem.getVida() <= 0 && !morto) {
            morto = true;
            morteStartTime = now;
            playerState = PlayerState.DEAD;
            currentFrame = 0;
        }

        // Enquanto o player estiver morto, bloqueia ações e mantém a tela estática
        if (morto) {
            playerMovement.bloquearMovimento();
            playerAttack.bloquearAtaque();
        }

        processarMorte(now);
    }

    // ===== Inimigos =====
    private void atualizarInimigos(long now) {
        for (Inimigo inimigo : new ArrayList<>(inimigos)) { // evita ConcurrentModification
            inimigo.seguir(player.getTranslateX(), player.getTranslateY());

            if (player.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                    && now - lastEnemyHitTime >= ENEMY_ATTACK_COOLDOWN) {
                hudVida.tomarDano(inimigo.getDano(), personagem);
                lastEnemyHitTime = now;
            }
        }
    }

    // ===== Morte =====
    private void processarMorte(long now) {
        if (!morto) return;

        // Enquanto a animação de morte não terminar, não faz mais nada
        if (now - morteStartTime < MORTE_DURATION) {
            playerState = PlayerState.DEAD;
            return;
        }

        // Depois que a animação terminou, respawna
        respawnPlayer();
        morto = false;
    }

    private void respawnPlayer() {
        // Restaura vida
        personagem.recuperarVida(personagem.getVidaMaxima());
        hudVida = new HUDVida(personagem);

        playerAttack.desbloquearAtaque();
        playerMovement.desbloquearMovimento();
        // Verifica se estava na última fase
        if (salaAtual >= fases.length - 1) {
            salaAtual = 0; // volta para a primeira fase
        }

        // Carrega a fase correta
        carregarSala(salaAtual);

        // Reposiciona no início da fase
        player.setTranslateX(40);
        player.setTranslateY(alturaSala - player.getBoundsInParent().getHeight());

        // Define estado como "levantando"
        playerState = PlayerState.RESPAWNING;
        currentFrame = 0;
    }



    // ===== Estado e Animação =====
    private void atualizarEstado(long now) {
        if (morto) { playerState = PlayerState.DEAD; return; }

        if (playerAttack.isAtacando()) playerState = PlayerState.ATTACKING;
        else if (playerMovement.isDashing()) playerState = PlayerState.DASHING;
        else if (pressedKeys.contains(KeyCode.W)) playerState = PlayerState.LOOKING_UP;
        else if (pressedKeys.contains(KeyCode.S)) playerState = PlayerState.LOOKING_DOWN;
        else if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)) playerState = PlayerState.RUNNING;
        else playerState = PlayerState.IDLE;
    }

    // ===== Sala =====
    private void verificarTrocaSala() {
        if (player.getTranslateX() > larguraSala - 40 && salaAtual < fases.length - 1) {
            salaAtual++;
            carregarSala(salaAtual);
            player.setTranslateX(40);
        }
        if (player.getTranslateX() < 0) player.setTranslateX(0);
    }

    private void carregarSala(int index) {
        root.getChildren().clear();
        inimigos.clear();

        // Fundo da fase
        Rectangle fundo = new Rectangle(larguraSala, alturaSala);
        fundo.setFill(fases[index].getCorFundo());
        root.getChildren().add(fundo);

        // Player
        root.getChildren().add(player);

        // HUD
        root.getChildren().add(hudVida.getBarraVida());

        // Inimigos nascem no final da fase
        int quantidade = fases[index].getQuantidadeInimigos();
        for (int i = 0; i < quantidade; i++) {
            double x = larguraSala - 100 - (i * 100); // ajusta para spawn no final
            Inimigo novo = new Inimigo(x, alturaSala - 100, 30, 3);
            root.getChildren().add(novo.getCorpo());
            inimigos.add(novo);
        }
        playerAttack.setInimigos(inimigos);
    }

    private void atualizarAnimacao(long now) {
        long frameDuration;
        switch (playerState) {
            case IDLE -> frameDuration = 200_000_000;
            case RUNNING , DEAD -> frameDuration = 100_000_000;
            case DASHING -> frameDuration = 80_000_000;
            case ATTACKING -> frameDuration = 50_000_000;
            default -> frameDuration = 200_000_000;
        }

        if (now - lastUpdate < frameDuration) return;

        switch (playerState) {
            case IDLE -> {
                currentFrame = (currentFrame + 1) % 3;
                if (currentFrame == 0) currentFrame=1;
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightAFK_%d.png", currentFrame)
                ));
                player.setScaleX(isFacingRight() ? -1 : 1);
            }
            case RUNNING -> {
                currentFrame = (currentFrame + 1) % 5;
                if (currentFrame == 0) currentFrame=1;
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightSprint_%d.png", currentFrame)
                ));
            }
            case DASHING -> {
                if (currentFrame == 0) currentFrame=1;
                if (currentFrame < 4) {
                    currentFrame++;
                }
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightDash_%d.png", currentFrame)
                ));
            }
            case ATTACKING -> {
                if (currentFrame == 0 || currentFrame == 4) currentFrame=1;
                if (currentFrame < 3) {
                    currentFrame++;
                }
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightLightAttack_%d.png", currentFrame)
                ));

            }
            case LOOKING_UP -> {
                player.setImage(ResourceLoader.loadImage("/assets/KnightLookUp.png"));
                player.setScaleX(isFacingRight() ? -1 : 1);

            }
            case LOOKING_DOWN -> {
                player.setImage(ResourceLoader.loadImage("/assets/KnightLookDown.png"));
                player.setScaleX(isFacingRight() ? -1 : 1);
            }
            case DEAD -> {
                if (currentFrame == 0) currentFrame=1;
                if (currentFrame < 14) currentFrame++;
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/dying/PlayerDying_%d.png", currentFrame)
                ));

                double elapsed = (now - morteStartTime) / 1_000_000_000.0; // segundos
                double duration = MORTE_DURATION / 2_000_000_000.0; // 1.5s

                // Interpolação linear da posição Y (do chão até o centro)
                double alturaImagem = player.getImage().getHeight();
                double startY = alturaSala - alturaImagem; // posição inicial
                double endY = (alturaSala - alturaImagem) / 2; // posição final
                double progress = Math.min(elapsed / duration, 1.0); // garante 0..1
                player.setTranslateY(startY + (endY - startY) * progress);

                // Mantém horizontalmente no mesmo lugar
                player.setTranslateX(larguraSala / 2 - player.getBoundsInParent().getWidth() / 2);
            }

            case RESPAWNING -> {
                if (currentFrame < 3) currentFrame++;
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightRespawn_%d.png", currentFrame)
                ));
                if (currentFrame >= 3) {
                    playerState = PlayerState.IDLE;
                }
            }
        }

        lastUpdate = now;
    }


    // ===== Getter para os sistemas =====
    public PlayerMovement getPlayerMovement() { return playerMovement; }
    public PlayerAttack getPlayerAttack() { return playerAttack; }
}
