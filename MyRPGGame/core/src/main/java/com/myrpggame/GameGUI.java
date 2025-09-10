package com.myrpggame;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Models.GerenciadorDeFase ;
import com.myrpggame.Models.Player;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    private final WinTimes winTimes;

    private final VBox telaParabens; // adiciona no topo da classe

    private final Random random = new Random();

    private final List<String> finalImage = List.of(
            "/assets/telafinal/gatoBurro.jpg",
            "/assets/telafinal/gatoBurro.jpg",
            "/assets/telafinal/larry.jpeg",
            "/assets/telafinal/rigby.png",
            "/assets/telafinal/gatoDePe.jpg",
            "/assets/telafinal/gatobombaatomica.jpg",
            "/assets/telafinal/gatoguerra.jpg",
            "/assets/telafinal/gatoabsolutocinema.jpg",
            "/assets/telafinal/gatoroblox.jpg",
            "/assets/telafinal/gatonerd.jpg",
            "/assets/telafinal/gatonerd2.jpg",
            "/assets/telafinal/gatorezando.jpg",
            "/assets/telafinal/gatocozinheiro.jpg",
            "/assets/telafinal/gatobanana.jpg",
            "/assets/telafinal/gatouiia.jpg",
            "/assets/telafinal/gatoexplosivo.jpg",
            "/assets/telafinal/gatodeterno.jpg",
            "/assets/telafinal/gatofodadeterno.jpg",
            "/assets/telafinal/gatocorujadeterno.jpg",
            "/assets/telafinal/gatocoruja.jpg",
            "/assets/telafinal/gatomorango.jpg"
    );



    public GameGUI(Stage stage , WinTimes winTimes) {
        MenuGUI menuGUI = new MenuGUI(stage , winTimes);

        this.winTimes = winTimes;

        // Player
        Image knightAFK = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/KnightAFK_1.png")).toExternalForm()
        );
        player = new ImageView(knightAFK);

        // Game World (móvel com a câmera)
        gameWorld = new Pane();
        gameWorld.getChildren().add(player);

        // Player e HUD
        Player personagem = new Player(knightAFK, 100, 0);
        HUDVida hudVida = new HUDVida(personagem);

        // HUD fixo
        hudContainer = new VBox();
        hudContainer.getChildren().add(hudVida.getBarraVida());
        hudContainer.setAlignment(Pos.TOP_LEFT);  // fixa o HUD no canto superior esquerdo
        hudContainer.setPadding(new Insets(20, 0, 0, 20)); // distancia do topo e da esquerda

        // Menu de pausa
        pauseMenu = new VBox(20);
        pauseMenu.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-alignment: center; -fx-padding: 40;");
        pauseMenu.setVisible(false);
        pauseMenu.setPrefSize(getLargura(), getAltura());

        // --- Tela de parabéns ---
        telaParabens = new VBox(20);
        GameLoop gameLoop = new GameLoop(player, gameWorld, pauseMenu, pressedKeys, hudVida , telaParabens);
        telaParabens.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        telaParabens.setAlignment(Pos.CENTER);
        telaParabens.setPrefSize(getLargura(), getAltura());
        telaParabens.setVisible(false);

// Imagens laterais
        ImageView imgEsquerda = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource(finalImage.get(random.nextInt(finalImage.size())))).toExternalForm()));
        imgEsquerda.setFitWidth(300);
        imgEsquerda.setPreserveRatio(true);

        ImageView imgDireita = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource(finalImage.get(random.nextInt(finalImage.size())))).toExternalForm()));
        imgDireita.setFitWidth(300);
        imgDireita.setPreserveRatio(true);

// Texto central
        Label textoParabens = new Label("Parabéns! Você venceu!");
        textoParabens.setStyle("-fx-font-size: 36px; -fx-text-fill: #fff; -fx-font-weight: bold;");

// Contador de vitórias
        Label contadorVitorias = new Label();
        contadorVitorias.setStyle("-fx-font-size: 24px; -fx-text-fill: #fff;");

// Botão continuar
        Button continuarParabens = new Button("Continuar");
        continuarParabens.setStyle("-fx-font-size: 18px;");
        continuarParabens.setOnAction(e -> {
            telaParabens.setVisible(false);
            gameLoop.setExibindoTelaParabens(false); // reseta a flag
            resetarJogo(gameLoop);
        });

// VBox central com texto e botão
        VBox centro = new VBox(20, textoParabens, contadorVitorias, continuarParabens);
        centro.setAlignment(Pos.CENTER);

// HBox principal da tela de parabéns (imagem esquerda | centro | imagem direita)
        HBox hboxParabens = new HBox(50, imgEsquerda, centro, imgDireita);
        hboxParabens.setAlignment(Pos.CENTER);

        telaParabens.getChildren().add(hboxParabens);

// Atualiza contador de vitórias toda vez que a tela for exibida
        telaParabens.visibleProperty().addListener((obs, antigo, novo) -> {
            if (novo) {
                contadorVitorias.setText("Vitórias: " + winTimes.getNumeroDeVitorias());
            }
        });



        Button continuar = new Button("Continuar");
        Button voltarMenu = new Button("Voltar ao Menu");
        continuar.setOnAction(e -> pauseMenu.setVisible(false));
        voltarMenu.setOnAction(e -> {
            stage.setScene(menuGUI.getScene());
            stage.setFullScreen(true);
        });
        pauseMenu.getChildren().addAll(continuar, voltarMenu);

        // Evita que barra de espaço acione os botões
        continuarParabens.setFocusTraversable(false);
        continuar.setFocusTraversable(false);
        voltarMenu.setFocusTraversable(false);


        // Container principal
        StackPane container = new StackPane();
        container.getChildren().addAll(gameWorld, hudContainer, pauseMenu , telaParabens);

        // Scene
        scene = new Scene(container, getLargura(), getAltura());

        // GameLoop
        GerenciadorDeFase gerenciador = new GerenciadorDeFase();
        player.setTranslateX(100);
        player.setTranslateY(gerenciador.getFaseAtual().getAltura() - knightAFK.getHeight());

        gameLoop.start();
        // Controles
//        Quando eu apertar ESC não tira o fullScreen
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            if (event.getCode() == KeyCode.ESCAPE) pauseMenu.setVisible(!pauseMenu.isVisible());
            if (event.getCode() == KeyCode.Q) gameLoop.getPlayerMovement().tentarDash();
        });

        scene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                gameLoop.getPlayerAttack().tentarAtaque();
            }
        });

        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    private void resetarJogo(GameLoop gameLoop) {
        winTimes.addVitoria();
        gameLoop.getPlayerAttack().desbloquearAtaque();
        gameLoop.getPlayerMovement().desbloquearMovimento();
        gameLoop.getHudVida().resetarVida(); // reseta vida
        gameLoop.getPlayerAttack().getPlayer().resetLifeOrb(); // se tiver
        gameLoop.getGerenciadorDeFase().resetarFases(); // volta fase inicial
        gameLoop.carregarSala();
        gameLoop.posicionarPlayerNoInicio();
        gameLoop.resetarMusicaBoss();
    }


    public Scene getScene() {
        return scene;
    }
}
