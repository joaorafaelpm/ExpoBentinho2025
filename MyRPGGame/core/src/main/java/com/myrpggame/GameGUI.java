package com.myrpggame;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Models.Inimigo;
import com.myrpggame.Models.Player;
import com.myrpggame.Utils.GameUtils;
import com.myrpggame.Utils.HUDVida;
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
    private final Pane root;
    private final VBox pauseMenu;
    private int salaAtual = 0;

    Inimigo inimigo = new Inimigo(400, 200, 40, 10);

    public GameGUI(Stage stage, double larguraSala, double alturaSala) {
        MenuGUI menuGUI = new MenuGUI(stage);
        Image knightAFK = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/KnightAFK_1.png")).toExternalForm()
        );
        player = new ImageView(knightAFK);
        player.setTranslateX(100);
        player.setTranslateY(alturaSala - knightAFK.getHeight());




        root = new Pane(player);

//        Barra de vida do Jogador
        Player player1 = new Player(knightAFK , player.getLayoutX(), player.getLayoutY());
        HUDVida hudVida = new HUDVida(player1 , 8);
        root.getChildren().add(hudVida.getBarraVida());

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
        scene = new Scene(container , getLargura(), getAltura() );

        // 🔹 Captura teclas
        GameUtils gameLoop = new GameUtils(player, root, pauseMenu, pressedKeys, alturaSala , larguraSala);
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            if (event.getCode() == KeyCode.ESCAPE) pauseMenu.setVisible(!pauseMenu.isVisible());
            if (event.getCode() == KeyCode.Q) gameLoop.tentarDash();
        });
        // No construtor de GameUtils, adicione algo assim:
        scene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) { // botão esquerdo
                gameLoop.tentarAtaque();
            }
        });
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        gameLoop.start();

    }

    public Scene getScene() {
        return scene;
    }
}

