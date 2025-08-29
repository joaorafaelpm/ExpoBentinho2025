package com.myrpggame.Utils;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.PlayerState;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import javafx.animation.AnimationTimer;
import javafx.print.PageOrientation;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Set;

public class GameLoopBackUp extends AnimationTimer {

    private final ImageView player;
    private final Pane root;
    private final VBox pauseMenu;
    private final Set<KeyCode> pressedKeys;
    private final double alturaSala;
    private final double larguraSala;

    private boolean facingRight;
    private int currentFrame = 0;
    private long lastUpdate = 0;

    private final double gravidade = 0.5;
    private double velocidadeY = 0;
    private boolean pulando = false;
    private long tempoPuloInicio = 0;
    private final long tempoMaxPulo = 500_000_000;
    private final double impulsoPulo = -10;

    private boolean canDash = true;
    private boolean gravidadeAtivo = true;
    private boolean dashing = false;
    private long dashStartTime = 0;
    private static final long DASH_DURATION = 250_000_000;
    private double dashVelocidade = 0;
    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN = 500_000_000;

    private boolean atacando = false;
    private boolean inimigoAtingido = false ;
    private long ataqueStartTime = 0;
    private static final long ATAQUE_DURATION = 300_000_000;

    // === Cooldowns ===
    private static final long PLAYER_ATTACK_COOLDOWN = 400_000_000; // 0.4s
    private static final long ENEMY_ATTACK_COOLDOWN = 1000_000_000; // 1s

    private long tempoMorte;
    private final long DURACAO_MORTE = 1500_000_000L; // 1.5s em nanos
    private static final long MORTE_FREEZE_DURATION = 800_000_000; // 0.8s travado antes de animar


    private long lastPlayerHitTime = 0;
    private long lastEnemyHitTime = 0;



    private PlayerState playerState = PlayerState.IDLE;
    private Inimigo inimigo;
    private int salaAtual = 0;

    private Player personagem = new Player();
    private HUDVida hudVida = new HUDVida(personagem);
    private final ImageView ataqueHitbox = new ImageView();

    private boolean morto = false;
    private long morteStartTime = 0;
    private static final long MORTE_DURATION = 1500_000_000; // 1.5s de animação


    private final Fase[] fases = {
            new Fase(Color.LIGHTBLUE, 1),
            new Fase(Color.LIGHTGREEN, 2),
            new Fase(Color.LIGHTPINK, 3),
            new Fase(Color.DARKRED, 1) // Ex: boss
    };
    public GameLoopBackUp(ImageView player, Pane root, VBox pauseMenu, Set<KeyCode> pressedKeys,
                    double alturaSala, double larguraSala) {
        this.player = player;
        this.root = root;
        this.pauseMenu = pauseMenu;
        this.pressedKeys = pressedKeys;
        this.alturaSala = alturaSala;
        this.larguraSala = larguraSala;
    }

    @Override
    public void handle(long now) {
        if (pauseMenu.isVisible()) return;
        aplicarGravidade();
        processarPulo(now);
        processarMovimento();
        processarDash(now);
        processarAtaque(now);
        atualizarHitboxAtaque();
        atualizarInimigo();
        verificarTrocaSala();
        atualizarEstado(now);
        atualizarAnimacao(now);

        if (personagem.getVida() <= 0 && !morto) {
            morto = true;
            morteStartTime = now;
            playerState = PlayerState.DEAD;
            currentFrame = 0;
            System.out.println("☠ Player morreu!");
        }
        processarMorte(now);
    }

    // ===== Movimento e gravidade =====
    private void aplicarGravidade() {
        if (gravidadeAtivo) {
            velocidadeY += gravidade;
            player.setTranslateY(player.getTranslateY() + velocidadeY);
        }

        double chao = alturaSala - player.getBoundsInParent().getHeight();
        if (player.getTranslateY() > chao) {
            player.setTranslateY(chao);
            velocidadeY = 0;
            pulando = false;
        }
    }

    private void processarPulo(long now) {
        if (pressedKeys.contains(KeyCode.SPACE)) {
            if (!pulando && onGround()) {
                pulando = true;
                tempoPuloInicio = now;
                velocidadeY = impulsoPulo;
            } else if (pulando && now - tempoPuloInicio < tempoMaxPulo) {
                velocidadeY = impulsoPulo;
            }
        } else {
            pulando = false;
        }
    }

    private void processarMovimento() {
        if (dashing || atacando) return;

        boolean moving = false;
        boolean lookingDownUp = false;

        // Horizontal
        double velocidadePlayer = pressedKeys.contains(KeyCode.SHIFT) ? 10 : 5;
        if (pressedKeys.contains(KeyCode.A)) {
            if (player.getScaleX() < 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() - velocidadePlayer);
            facingRight = false;
            moving = true;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            if (player.getScaleX() > 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() + velocidadePlayer);
            facingRight = true;
            moving = true;
        }
    }

    // ===== Dash =====
    private void processarDash(long now) {
        // 🔹 Corrige o estado do player

        if (dashing) {
            if (now - dashStartTime < DASH_DURATION) {
                playerState = PlayerState.DASHING;
                velocidadeY = 0; // não deixa pular durante dash
                player.setTranslateX(player.getTranslateX() + dashVelocidade);
            } else {
                dashing = false;
                gravidadeAtivo = true;
                dashVelocidade = 0; // fim do dash: inicia cooldown
                lastDashTime = now; // só volta pro estado correto
            }
        }
        // --- Cooldown do dash ---
        if (!canDash && !pressedKeys.contains(KeyCode.Q) && now - lastDashTime >= DASH_COOLDOWN) {
            canDash = true;
        }
    }


    public void tentarDash() {
        long now = System.nanoTime();
        if (!dashing && canDash && now - lastDashTime >= DASH_COOLDOWN) {
            iniciarDash(now);
        }
    }

    // ===== Dash =====
    private void iniciarDash(long now) {
        playerState = PlayerState.DASHING;
        gravidadeAtivo = false;
        dashStartTime = now;
        dashing = true;
        canDash = false;
        currentFrame = 0; // 🔹 sempre reinicia a animação do dash

        if (pressedKeys.contains(KeyCode.D)) dashVelocidade = 15;
        else if (pressedKeys.contains(KeyCode.A)) dashVelocidade = -15;
        else dashVelocidade = player.getScaleX() >= 0 ? -15 : 15;
    }

    // ===== Ataque =====
    private void iniciarAtaque() {
        atacando = true;
        ataqueStartTime = System.nanoTime();
        playerState = PlayerState.ATTACKING;
        currentFrame = 0;
    }

    public void tentarAtaque() {
        if (!atacando) iniciarAtaque();
    }



    private void processarAtaque(long now) {
        if (!atacando) return;

        // Depois da duração do ataque
        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
        }

        // Colisão com inimigo (apenas se não estiver em cooldown)
        if (inimigo != null
                && now - ataqueStartTime <= ATAQUE_DURATION
                && ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                && now - lastPlayerHitTime >= PLAYER_ATTACK_COOLDOWN) {

            inimigo.tomarDano(personagem.getDano());
            lastPlayerHitTime = now;
            System.out.println("ATACOU INIMIGO ============================================");

            if (inimigo.estaMorto()) {
                root.getChildren().remove(inimigo.getCorpo());
                inimigo = null;
            }
        }
    }

    private void atualizarHitboxAtaque() {
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

    // ===== Inimigo =====
    private void atualizarInimigo() {
        if (inimigo == null) return;

        inimigo.seguir(player.getTranslateX(), player.getTranslateY());

        // Só dá dano se estiver sem cooldown de ataque
        long now = System.nanoTime();
        if (player.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())
                && now - lastEnemyHitTime >= ENEMY_ATTACK_COOLDOWN) {

            hudVida.tomarDano(inimigo.getDano(), personagem);
            lastEnemyHitTime = now;
            System.out.println("Player colidiu com inimigo!");
        }
    }

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
        personagem.setVida(personagem.getVidaMaxima());

        // Reposiciona no início da fase
        player.setTranslateX(40);
        player.setTranslateY(alturaSala - player.getBoundsInParent().getHeight());

        // Define estado como "levantando"
        playerState = PlayerState.RESPAWNING;
        currentFrame = 0;
    }

    private void atualizarEstado(long now) {
        if (morto) {
            playerState = PlayerState.DEAD;
            return; // nada mais sobrescreve
        }

        if (atacando) {
            playerState = PlayerState.ATTACKING;
        } else if (dashing) {
            playerState = PlayerState.DASHING;
        } else if (pressedKeys.contains(KeyCode.W)) {
            playerState = PlayerState.LOOKING_UP;
        } else if (pressedKeys.contains(KeyCode.S)) {
            playerState = PlayerState.LOOKING_DOWN;
        } else if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)) {
            playerState = PlayerState.RUNNING;
        } else {
            playerState = PlayerState.IDLE;
        }
    }


    // ===== Animação =====
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
                player.setScaleX(facingRight ? -1 : 1);
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
                if (currentFrame == 0) currentFrame=1;
                if (currentFrame < 3) {
                    currentFrame++;
                }
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/KnightLightAttack_%d.png", currentFrame)
                ));

            }
            case LOOKING_UP -> {
                player.setImage(ResourceLoader.loadImage("/assets/KnightLookUp.png"));
                player.setScaleX(facingRight ? -1 : 1);

            }
            case LOOKING_DOWN -> {
                player.setImage(ResourceLoader.loadImage("/assets/KnightLookDown.png"));
                player.setScaleX(facingRight ? -1 : 1);
            }
            case DEAD -> {
                if (currentFrame == 0) currentFrame=1;
                if (currentFrame < 14) currentFrame++;
                player.setImage(ResourceLoader.loadImage(
                        String.format("/assets/dying/PlayerDying_%d.png", currentFrame)
                ));
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

    // ===== Sala =====
    private void verificarTrocaSala() {
        // Player chegou ao final da fase
        if (player.getTranslateX() > larguraSala - 40) {
            if (salaAtual < fases.length - 1) {
                salaAtual++;
                carregarSala(salaAtual);
                player.setTranslateX(40); // começa no início da nova fase
            }
        }

        // Impede o player de voltar além do início da fase
        if (player.getTranslateX() < 0) {
            player.setTranslateX(0);
        }
    }

    private void carregarSala(int index) {
        root.getChildren().clear();

        // Fundo da fase
        Rectangle fundo = new Rectangle(larguraSala, alturaSala);
        fundo.setFill(fases[index].getCorFundo());
        root.getChildren().add(fundo);

        // Player
        root.getChildren().add(player);

        // HUD
        root.getChildren().add(hudVida.getBarraVida());

        // Inimigos
        for (int i = 0; i < fases[index].getQuantidadeInimigos(); i++) {
            inimigo = new Inimigo(50 + i * 100, alturaSala - 100, 30, 3);
            root.getChildren().add(inimigo.getCorpo());
        }
    }



    private boolean onGround() {
        return player.getTranslateY() >= alturaSala - player.getBoundsInParent().getHeight();
    }

    public void setInimigo(Inimigo inimigo) {
        this.inimigo = inimigo;
    }
}
