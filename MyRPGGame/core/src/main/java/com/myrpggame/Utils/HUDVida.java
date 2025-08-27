package com.myrpggame.Utils;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Models.Player;
import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class HUDVida {
    private final StackPane barraVidaContainer = new StackPane();
    private final HBox masksContainer = new HBox(5);
    private final List<ImageView> mascaras = new ArrayList<>();
    private int hpMax;
    private int hpAtual;

    private final String[] breakFrames = {
            "/assets/LostLife_1.png",
            "/assets/LostLife_2.png",
            "/assets/LostLife_3.png",
            "/assets/LostLife_4.png"
    };

    private final String[] recoverFrames = {
            "/assets/RecoverLife_1.png",
            "/assets/RecoverLife_2.png",
            "/assets/RecoverLife_3.png"
    };

    private final double[] breakHeights = {65, 30 ,50,50}; // ajuste altura de cada frame

    private final Image backgroundHUD = ResourceLoader.loadImage("/assets/MaskBackground.png");
    private final Image mascaraCheia = ResourceLoader.loadImage("/assets/FullMask.png");
    private final Image mascaraVazia = ResourceLoader.loadImage("/assets/EmptyMask.png");

    public HUDVida(Player player , int hpMax) {
        this.hpMax = hpMax;
        this.hpAtual = player.getVida();
        inicializar();
    }

    private void inicializar() {
        ImageView background = new ImageView(backgroundHUD);
        background.setFitWidth(250);
        background.setFitHeight(100);
        background.setTranslateX(10);
        background.setTranslateY(5);

        for (int i = 0; i < hpMax; i++) {
            ImageView mask = new ImageView(mascaraCheia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mascaras.add(mask);
            masksContainer.getChildren().add(mask);
        }

        masksContainer.setTranslateX(60);
        masksContainer.setTranslateY(45);
        StackPane.setAlignment(masksContainer, javafx.geometry.Pos.BOTTOM_LEFT);

        Button buttonTomarDano = new Button("Perder vida");
        buttonTomarDano.setOnAction(e -> tomarDano(1));
        Button buttonRecuperarVida = new Button("Recuperar vida");
        buttonRecuperarVida.setOnAction(e -> curar(1));

        buttonTomarDano.setTranslateY(100);
        buttonRecuperarVida.setTranslateY(100);
        buttonRecuperarVida.setTranslateX(100);

        barraVidaContainer.getChildren().addAll(background, masksContainer, buttonTomarDano, buttonRecuperarVida);
    }

    public StackPane getBarraVida() {
        return barraVidaContainer;
    }

    public void tomarDano(int dano) {
        int hpAnterior = hpAtual;
        hpAtual = Math.max(0, hpAtual - dano);

        for (int i = hpAnterior - 1; i >= hpAtual; i--) {
            animarPerdaVida(mascaras.get(i));
        }
    }

    public void curar(int qtd) {
        int hpAnterior = hpAtual;
        hpAtual = Math.min(hpMax, hpAtual + qtd);

        for (int i = hpAnterior; i < hpAtual; i++) {
            animarRecuperacaoVida(mascaras.get(i));
        }
    }

    // ==== Animação de perda de vida ====
    private void animarPerdaVida(ImageView mask) {
        Timeline timeline = new Timeline();

        // Primeira imagem com offset Y
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> {
            mask.setImage(ResourceLoader.loadImage(breakFrames[0]));
            mask.setFitHeight(breakHeights[0]);
            mask.setTranslateY(-15);
        }));

        // Congela a primeira imagem por 250ms
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250), e -> {}));

        // Frames restantes
        for (int i = 1; i < breakFrames.length; i++) {
            int index = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250 + 75 * i), e -> {
                mask.setImage(ResourceLoader.loadImage(breakFrames[index]));
                mask.setFitHeight(breakHeights[index]);
                mask.setTranslateY(0);
            }));
        }

        // Último frame: máscara vazia
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250 + 75 * breakFrames.length), e -> {
            mask.setImage(mascaraVazia);
            mask.setTranslateY(0);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
        }));

        timeline.play();
    }

    // ==== Animação de recuperação de vida ====
    private void animarRecuperacaoVida(ImageView mask) {
        Timeline timeline = new Timeline();

        // Primeira imagem (sem offset)
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> {
            mask.setImage(ResourceLoader.loadImage(recoverFrames[0]));
        }));

        // Frames restantes
        for (int i = 1; i < recoverFrames.length; i++) {
            int index = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100 * i), e -> {
                mask.setImage(ResourceLoader.loadImage(recoverFrames[index]));
            }));
        }

        // Último frame: máscara cheia
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100 * recoverFrames.length), e -> {
            mask.setImage(mascaraCheia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mask.setTranslateY(0);
        }));

        timeline.play();
    }



}

