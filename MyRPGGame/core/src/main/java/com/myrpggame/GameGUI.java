package com.myrpggame;

import com.myrpggame.Models.GerenciadorDeFase ;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.GameLoop;
import com.myrpggame.Utils.HUDVida;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;
import static com.myrpggame.Config.GameResolution.GameResolution.getLargura;

public class GameGUI {

    private final Scene scene;
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final ImageView player;
    private final Pane gameWorld;
    private final VBox hudContainer;
    private final VBox pauseMenu;

    public GameGUI(Stage stage) {
        MenuGUI menuGUI = new MenuGUI(stage);

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

        Button continuar = new Button("Continuar");
        Button voltarMenu = new Button("Voltar ao Menu");
        continuar.setOnAction(e -> pauseMenu.setVisible(false));
        voltarMenu.setOnAction(e -> stage.setScene(menuGUI.getScene()));
        pauseMenu.getChildren().addAll(continuar, voltarMenu);

        // Container principal
        StackPane container = new StackPane();
        container.getChildren().addAll(gameWorld, hudContainer, pauseMenu);

        // Scene
        scene = new Scene(container, getLargura(), getAltura());

        // GameLoop
        GerenciadorDeFase gerenciador = new GerenciadorDeFase();
        player.setTranslateX(100);
        player.setTranslateY(gerenciador.getFaseAtual().getAltura() - knightAFK.getHeight());

        GameLoop gameLoop = new GameLoop(player, gameWorld, pauseMenu, pressedKeys, hudVida);
        gameLoop.start();




        // Controles
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

    public Scene getScene() {
        return scene;
    }
}
