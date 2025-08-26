package com.myrpggame.Utils;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Enum.PlayerState;
import com.myrpggame.Models.Inimigo;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Set;

public class GameUtils extends AnimationTimer {

    private final ImageView player;
    private final Pane root;
    private final VBox pauseMenu;
    private final Set<KeyCode> pressedKeys;
    private final double alturaSala;
    private final double larguraSala;

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
    private long ataqueStartTime = 0;
    private static final long ATAQUE_DURATION = 300_000_000;

    private PlayerState playerState = PlayerState.IDLE;
    private Inimigo inimigo;
    private int salaAtual = 0;

    private final ImageView ataqueHitbox = new ImageView();

    public GameUtils(ImageView player, Pane root, VBox pauseMenu, Set<KeyCode> pressedKeys,
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
        atualizarAnimacao(now);
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

        // Vertical
        if (pressedKeys.contains(KeyCode.W)) playerState = PlayerState.LOOKING_UP;
        else if (pressedKeys.contains(KeyCode.S)) playerState = PlayerState.LOOKING_DOWN;

        // Horizontal
        double velocidadePlayer = pressedKeys.contains(KeyCode.SHIFT) ? 10 : 5;
        if (pressedKeys.contains(KeyCode.A)) {
            if (player.getScaleX() < 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() - velocidadePlayer);
            moving = true;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            if (player.getScaleX() > 0) player.setScaleX(-player.getScaleX());
            player.setTranslateX(player.getTranslateX() + velocidadePlayer);
            moving = true;
        }
        playerState = moving ? PlayerState.RUNNING : PlayerState.IDLE;
    }

    // ===== Dash =====
    private void processarDash(long now) {
        // 🔹 Corrige o estado do player
        if (atacando) {
            playerState = PlayerState.ATTACKING;
        } else if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)) {
            playerState = PlayerState.RUNNING;
        } else {
            playerState = PlayerState.IDLE;
        }
        if (dashing) {
            if (now - dashStartTime < DASH_DURATION) {
                velocidadeY = 0; // não deixa pular durante dash
                player.setTranslateX(player.getTranslateX() + dashVelocidade);
            } else {
                dashing = false;
                gravidadeAtivo = true;
                dashVelocidade = 0; // fim do dash: inicia cooldown
                lastDashTime = now; // só volta pro estado correto
                if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)) {
                    playerState = PlayerState.RUNNING; }
                else { playerState = PlayerState.IDLE; }
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

    private void iniciarDash(long now) {
        playerState = PlayerState.DASHING;
        gravidadeAtivo = false;
        dashStartTime = now;
        dashing = true;
        canDash = false;

        if (pressedKeys.contains(KeyCode.D)) dashVelocidade = 15;
        else if (pressedKeys.contains(KeyCode.A)) dashVelocidade = -15;
        else dashVelocidade = player.getScaleX() >= 0 ? -15 : 15;
    }

    // ===== Ataque =====
    public void tentarAtaque() {
        if (!atacando) iniciarAtaque();
    }

    private void iniciarAtaque() {
        atacando = true;
        ataqueStartTime = System.nanoTime();
        playerState = PlayerState.ATTACKING;
    }

    private void processarAtaque(long now) {
        if (!atacando) return;

        // Depois da duração do ataque
        if (now - ataqueStartTime >= ATAQUE_DURATION) {
            atacando = false;
            playerState = pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.D)
                    ? PlayerState.RUNNING : PlayerState.IDLE;
        }

        // Colisão com inimigo
        if (inimigo != null && ataqueHitbox.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())) {
            inimigo.tomarDano(10);
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
        if (player.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent())) {
            System.out.println("Player colidiu com inimigo!");
        }
    }

    // ===== Animação =====
    private void atualizarAnimacao(long now) {
        long frameDuration;
        switch (playerState) {
            case IDLE -> frameDuration = 200_000_000;
            case RUNNING -> frameDuration = 100_000_000;
            case DASHING -> frameDuration = 50_000_000;
            case ATTACKING -> frameDuration = 300_000_000;
            default -> frameDuration = 200_000_000;
        }

        if (now - lastUpdate < frameDuration) return;

        switch (playerState) {
            case IDLE -> {
                currentFrame = (currentFrame + 1) % 4;
                player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightAFK_%d.png", currentFrame)));
            }
            case RUNNING -> {
                currentFrame = (currentFrame + 1) % 5;
                player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightSprint_%d.png", currentFrame)));
            }
            case DASHING -> {
                currentFrame = (currentFrame + 1) % 4;
                player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightDash_%d.png", currentFrame)));
            }
            case ATTACKING -> {
                currentFrame = (currentFrame + 1) % 3;
                player.setImage(ResourceLoader.loadImage(String.format("/assets/KnightLightAttack_%d.png", currentFrame)));
            }
            case LOOKING_UP -> player.setImage(ResourceLoader.loadImage("/assets/KnightLookUp.png"));
            case LOOKING_DOWN -> player.setImage(ResourceLoader.loadImage("/assets/KnightLookDown.png"));
        }

        lastUpdate = now;
    }

    // ===== Sala =====
    private void verificarTrocaSala() {
        if (player.getTranslateX() > larguraSala) {
            salaAtual++;
            carregarSala(salaAtual);
            player.setTranslateX(0);
        } else if (player.getTranslateX() < 0) {
            salaAtual--;
            carregarSala(salaAtual);
            player.setTranslateX(larguraSala - 40);
        }
    }

    private void carregarSala(int index) {
        root.getChildren().clear();
        Rectangle fundo = new Rectangle(larguraSala, alturaSala);
        switch (index) {
            case 0 -> fundo.setFill(Color.LIGHTBLUE);
            case 1 -> fundo.setFill(Color.LIGHTGREEN);
            case 2 -> fundo.setFill(Color.LIGHTPINK);
            default -> fundo.setFill(Color.GRAY);
        }
        root.getChildren().add(fundo);
        root.getChildren().add(player);

        if (index > 0 && index % 5 == 0) {
            inimigo = new Inimigo(50, 50, 30, 1.5);
            root.getChildren().add(inimigo.getCorpo());
        } else {
            inimigo = null;
        }
    }

    private boolean onGround() {
        return player.getTranslateY() >= alturaSala - player.getBoundsInParent().getHeight();
    }

    public void setInimigo(Inimigo inimigo) {
        this.inimigo = inimigo;
    }
}
