package com.myrpggame;

import com.myrpggame.Models.GerenciadorDeFase;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.DifficultyLevel;
import com.myrpggame.Utils.GameLoop;
import com.myrpggame.Utils.HUDVida;
import com.myrpggame.Utils.WinTimes;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class GameGUI {
    private final Scene scene;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final ImageView player;
    private final Pane gameWorld;
    private final VBox hudContainer;
    private final VBox pauseMenu;
    private final VBox telaParabens;
    private final WinTimes winTimes;
    private final Random random = new Random();

    private final List<String> finalImage = List.of(
            "/assets/telafinal/FinalImage (1).jpg", "/assets/telafinal/FinalImage (20).jpg", "/assets/telafinal/FinalImage (1).png",
            "/assets/telafinal/FinalImage (2).jpg", "/assets/telafinal/FinalImage (3).jpg", "/assets/telafinal/FinalImage (4).jpg",
            "/assets/telafinal/FinalImage (5).jpg", "/assets/telafinal/FinalImage (6).jpg", "/assets/telafinal/FinalImage (7).jpg",
            "/assets/telafinal/FinalImage (8).jpg", "/assets/telafinal/FinalImage (9).jpg", "/assets/telafinal/FinalImage (10).jpg",
            "/assets/telafinal/FinalImage (11).jpg", "/assets/telafinal/FinalImage (12).jpg", "/assets/telafinal/FinalImage (13).jpg",
            "/assets/telafinal/FinalImage (14).jpg", "/assets/telafinal/FinalImage (15).jpg", "/assets/telafinal/FinalImage (16).jpg",
            "/assets/telafinal/FinalImage (17).jpg", "/assets/telafinal/FinalImage (18).jpg", "/assets/telafinal/FinalImage (19).jpg"
    );

    public GameGUI(Stage stage, WinTimes winTimes) {
        this.winTimes = winTimes;
        MenuGUI menuGUI = new MenuGUI(stage, winTimes);

        // ==== Player e Mundo ====
        Image knightAFK = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/KnightAFK_1.png")).toExternalForm()
        );
        player = new ImageView(knightAFK);
        gameWorld = new Pane(player);

        // ==== HUD ====
        Player personagem = new Player(knightAFK, 100, 0);
        HUDVida hudVida = new HUDVida(personagem);
        hudContainer = new VBox(hudVida.getBarraVida());
        hudContainer.setAlignment(Pos.TOP_LEFT);
        hudContainer.setPadding(new Insets(20, 0, 0, 20));

        // ==== Menus e telas ====
        pauseMenu = criarPauseMenu(stage, menuGUI);
        telaParabens = criarTelaParabens();

        // ==== GameLoop ====
        GameLoop gameLoop = new GameLoop(player, gameWorld, pauseMenu, pressedKeys, hudVida, telaParabens);
        inicializarTelaParabens(gameLoop);

        // ==== Container principal ====
        StackPane container = new StackPane(gameWorld, hudContainer, pauseMenu, telaParabens);
        scene = new Scene(container, getLargura(), getAltura());

        // ==== Controles ====
        GerenciadorDeFase gerenciador = new GerenciadorDeFase();
        player.setTranslateX(100);
        player.setTranslateY(gerenciador.getFaseAtual().getAltura() - knightAFK.getHeight());

        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            if (event.getCode() == KeyCode.ESCAPE) pauseMenu.setVisible(!pauseMenu.isVisible());
            if (event.getCode() == KeyCode.Q) gameLoop.getPlayerMovement().tentarDash();
        });
        scene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) gameLoop.getPlayerAttack().tentarAtaque();
        });
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

        gameLoop.start();
    }

    private VBox criarPauseMenu(Stage stage, MenuGUI menuGUI) {
        Button continuar = new Button("Continuar");
        Button voltarMenu = new Button("Voltar ao Menu");
        continuar.setOnAction(e -> pauseMenu.setVisible(false));
        voltarMenu.setOnAction(e -> {
            stage.setScene(menuGUI.getScene());
            stage.setFullScreen(true);
        });
        VBox pause = new VBox(20, continuar, voltarMenu);
        pause.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-alignment: center; -fx-padding: 40;");
        pause.setPrefSize(getLargura(), getAltura());
        pause.setVisible(false);
        return pause;
    }

    private VBox criarTelaParabens() {
        VBox tela = new VBox();
        tela.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        tela.setPrefSize(getLargura(), getAltura());
        tela.setVisible(false);
        return tela;
    }

    private void inicializarTelaParabens(GameLoop gameLoop) {
        winTimes.adicionarVitoria(DifficultyLevel.getDifficulty());
        ImageView imgEsquerda = criarImagemFinal();
        ImageView imgDireita = criarImagemFinal();

        Label parabens = new Label("Parabéns! Você venceu!");
        parabens.setStyle("-fx-font-size: 36px; -fx-text-fill: #fff; -fx-font-weight: bold;");

        Label resumo = new Label();
        resumo.setStyle("-fx-font-size: 20px; -fx-text-fill: #fff;");

        Button continuar = new Button("Continuar");
        continuar.setStyle("-fx-font-size: 18px;");
        continuar.setOnAction(e -> {
            telaParabens.setVisible(false);
            gameLoop.setExibindoTelaParabens(false);
            resetarJogo(gameLoop);
        });

        VBox centro = new VBox(20, parabens, resumo, continuar);
        centro.setAlignment(Pos.CENTER);

        HBox layout = new HBox(50, imgEsquerda, centro, imgDireita);
        layout.setAlignment(Pos.CENTER);

        telaParabens.getChildren().add(layout);

        telaParabens.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) resumo.setText(winTimes.gerarResumo());
        });
    }

    private ImageView criarImagemFinal() {
        ImageView img = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource(
                        finalImage.get(random.nextInt(finalImage.size())))).toExternalForm()));
        img.setFitWidth(300);
        img.setPreserveRatio(true);
        return img;
    }

    private void resetarJogo(GameLoop gameLoop) {
        gameLoop.getPlayerAttack().desbloquearAtaque();
        gameLoop.getPlayerMovement().desbloquearMovimento();
        gameLoop.getHudVida().resetarVida();
        gameLoop.getHudVida().resetarLifeOrb();
        gameLoop.getGerenciadorDeFase().resetarFases();
        gameLoop.carregarSala();
        gameLoop.posicionarPlayerNoInicio();
        gameLoop.resetarMusicaBoss();
    }

    public Scene getScene() {
        return scene;
    }
}
