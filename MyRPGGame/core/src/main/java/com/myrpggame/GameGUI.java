package com.myrpggame;

import com.myrpggame.Enum.PlayerState;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameGUI {
    private static final long FRAME_DURATION_NS = 200_000_000;
    private static final int NUM_FRAMES_IDLE = 4;
    private static final int NUM_FRAMES_RUN = 5;

    private int currentFrame = 0;
    private long lastUpdate = 0;

    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN = 500_000_000; // 500ms

    private int salaAtual = 0;

    private final Scene scene;
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private final ImageView player;
    private final Pane root;
    private final VBox pauseMenu;

    private PlayerState playerState = PlayerState.IDLE;

    public GameGUI(Stage stage, double larguraSala, double alturaSala) {
        MenuGUI menuGUI = new MenuGUI(stage);

        Image knightAFK = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/KnightAFK_1.png")).toExternalForm()
        );
        player = new ImageView(knightAFK);
        player.setTranslateX(100);
        player.setTranslateY(alturaSala - knightAFK.getHeight());

        root = new Pane(player);

        // 🔹 Menu de pausa (overlay)
        pauseMenu = new VBox(20);
        pauseMenu.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-alignment: center; -fx-padding: 40;");
        pauseMenu.setVisible(false);
        pauseMenu.setPrefSize(larguraSala, alturaSala);

        Button continuar = new Button("Continuar");
        Button voltarMenu = new Button("Voltar ao Menu");

        continuar.setOnAction(e -> pauseMenu.setVisible(false));
        voltarMenu.setOnAction(e -> stage.setScene(menuGUI.getScene()));

        pauseMenu.getChildren().addAll(continuar, voltarMenu);

        // 🔹 Container com jogo + menu sobreposto
        StackPane container = new StackPane(root, pauseMenu);
        scene = new Scene(container, larguraSala, alturaSala);

        // 🔹 Captura teclas (apenas uma vez)
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            if (event.getCode() == KeyCode.ESCAPE) {
                pauseMenu.setVisible(!pauseMenu.isVisible());
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            if (pressedKeys.isEmpty()) {
                playerState = PlayerState.IDLE;
            }
        });

        // 🔹 Loop do jogo
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (pauseMenu.isVisible()) return; // pausa o jogo

                double playerX = player.getTranslateX();

                // --- Movimento & Estados ---
                if (pressedKeys.contains(KeyCode.W)) {
                    playerState = PlayerState.LOOKING_UP;
                } else if (pressedKeys.contains(KeyCode.S)) {
                    playerState = PlayerState.LOOKING_DOWN;
                } else if (pressedKeys.contains(KeyCode.A)) {
                    if (player.getScaleX() < 0) player.setScaleX(-player.getScaleX());
                    player.setTranslateX(playerX - 5);
                    playerState = PlayerState.RUNNING;
                } else if (pressedKeys.contains(KeyCode.D)) {
                    if (player.getScaleX() > 0) player.setScaleX(-player.getScaleX());
                    player.setTranslateX(playerX + 5);
                    playerState = PlayerState.RUNNING;
                } else {
                    if (playerState != PlayerState.IDLE) {
                        playerState = PlayerState.IDLE;
                    }
                }

                // --- Dash (Q + direção) ---
                if (now - lastDashTime >= DASH_COOLDOWN) {
                    if (pressedKeys.contains(KeyCode.Q) && pressedKeys.contains(KeyCode.D)) {
                        player.setTranslateX(playerX + 100);
                        lastDashTime = now;
                    }
                    if (pressedKeys.contains(KeyCode.Q) && pressedKeys.contains(KeyCode.A)) {
                        player.setTranslateX(playerX - 100);
                        lastDashTime = now;
                    }
                }

                // --- Troca de sala ---
                if (player.getTranslateX() > larguraSala) {
                    salaAtual++;
                    carregarSala(salaAtual, larguraSala, alturaSala);
                    player.setTranslateX(0);
                } else if (player.getTranslateX() < 0) {
                    salaAtual--;
                    carregarSala(salaAtual, larguraSala, alturaSala);
                    player.setTranslateX(larguraSala - 40);
                }

                // --- Animações por estado ---
                if (now - lastUpdate >= FRAME_DURATION_NS) {
                    switch (playerState) {
                        case IDLE -> {
                            currentFrame = (currentFrame + 1) % NUM_FRAMES_IDLE;
                            player.setImage(new Image(Objects.requireNonNull(
                                    getClass().getResource(
                                            String.format("/assets/KnightAFK_%d.png", currentFrame)
                                    )).toExternalForm()));
                        }
                        case RUNNING -> {
                            currentFrame = (currentFrame + 1) % NUM_FRAMES_RUN;
                            player.setImage(new Image(Objects.requireNonNull(
                                    getClass().getResource(
                                            String.format("/assets/KnightSprint_%d.png", currentFrame)
                                    )).toExternalForm()));
                        }
                        case LOOKING_UP -> player.setImage(new Image(
                                Objects.requireNonNull(getClass().getResource("/assets/KnightLookUp.png")).toExternalForm()
                        ));
                        case LOOKING_DOWN -> player.setImage(new Image(
                                Objects.requireNonNull(getClass().getResource("/assets/KnightLookDown.png")).toExternalForm()
                        ));
                    }
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    public Scene getScene() {
        return scene;
    }

    private void carregarSala(int index, double larguraSala, double alturaSala) {
        root.getChildren().clear();
        Rectangle fundo = new Rectangle(larguraSala, alturaSala);

        switch (index) {
            case 0 -> fundo.setFill(Color.LIGHTBLUE);
            case 1 -> fundo.setFill(Color.LIGHTGREEN);
            case 2 -> fundo.setFill(Color.LIGHTPINK);
            default -> fundo.setFill(Color.GRAY);
        }

        root.getChildren().addAll(fundo, player);
    }
}
