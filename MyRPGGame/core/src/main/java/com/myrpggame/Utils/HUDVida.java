package com.myrpggame.Utils;

import com.myrpggame.Config.ResourceLoader.ResourceLoader;
import com.myrpggame.Models.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class HUDVida {

    private final StackPane barraVidaContainer = new StackPane();
    private final HBox masksContainer = new HBox(5); // spacing entre máscaras
    private final List<ImageView> mascaras = new ArrayList<>();
    private int hpMax = 8;
    private int hpAtual;
    private final Player player;

    private boolean morto = false;
    private long morteStartTime = 0;

    private int numberLifeOrb = 0;

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

    private final double[] breakHeights = {65, 30, 50, 50};

    private final Image backgroundHUD = ResourceLoader.loadImage("/assets/MaskBackground.png");
    private final Image mascaraCheia = ResourceLoader.loadImage("/assets/FullMask.png");
    private final Image mascaraVazia = ResourceLoader.loadImage("/assets/EmptyMask.png");
    private final ImageView lifeOrbIcon = new ImageView(ResourceLoader.loadImage("/assets/lifeOrb.png"));
    private final Label lifeOrbLabel = new Label("0");

    public HUDVida(Player player) {
        this.player = player;
        this.hpAtual = player.getVida();
        this.numberLifeOrb = player.getLifeOrb();
        inicializar();
    }

    private void inicializar() {
        // Background
        ImageView background = new ImageView(backgroundHUD);
        background.setFitWidth(250);
        background.setFitHeight(100);

        // Container das máscaras
        masksContainer.setAlignment(Pos.CENTER_LEFT);
        masksContainer.setPadding(new Insets(30, 0, 0, 80)); // desloca as máscaras para a esquerda
        mascaras.clear();

        // Cria máscaras cheias
        for (int i = 0; i < hpAtual; i++) {
            ImageView mask = new ImageView(mascaraCheia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mascaras.add(mask);
            masksContainer.getChildren().add(mask);
        }

        // Cria máscaras vazias
        for (int i = 0; i < hpMax - hpAtual; i++) {
            ImageView mask = new ImageView(mascaraVazia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mascaras.add(mask);
            masksContainer.getChildren().add(mask);
        }

        lifeOrbIcon.setFitWidth(24);
        lifeOrbIcon.setFitHeight(24);
        lifeOrbLabel.setText(String.valueOf(numberLifeOrb));
        lifeOrbLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox lifeOrbContainer = new HBox(5, lifeOrbIcon, lifeOrbLabel);
        lifeOrbContainer.setAlignment(Pos.TOP_RIGHT);
        lifeOrbContainer.setPadding(new Insets(10, 15, 0, 0));

        // Adiciona background e máscaras ao container principal
        barraVidaContainer.getChildren().addAll(background, masksContainer , lifeOrbContainer);

        // Alinhamento
        StackPane.setAlignment(background, Pos.CENTER_LEFT);
        StackPane.setAlignment(masksContainer, Pos.CENTER_LEFT);

        // Botões de teste (fora do HUD se quiser)
        Button buttonTomarDano = new Button("Perder vida");
        buttonTomarDano.setOnAction(e -> tomarDano(1, player));

        Button buttonRecuperarVida = new Button("Recuperar vida");
        buttonRecuperarVida.setOnAction(e -> curar(1, player));

        // Aqui você pode adicionar os botões em outro container no GameGUI
    }

    public StackPane getBarraVida() {
        return barraVidaContainer;
    }

    public void tomarDano(int dano, Player player) {
        if (morto) return;

        int hpAnterior = hpAtual;
        hpAtual = Math.max(0, hpAtual - dano);
        player.tomarDano(dano);

        for (int i = hpAnterior - 1; i >= hpAtual; i--) {
            animarPerdaVida(mascaras.get(i));
        }

        if (hpAtual <= 0 && !morto) {
            morto = true;
        }
    }

    public void curar(int qtd, Player player) {
        if (morto) return;

        int hpAnterior = hpAtual;
        hpAtual = Math.min(hpMax, hpAtual + qtd);
        player.recuperarVida(qtd);

        for (int i = hpAnterior; i < hpAtual; i++) {
            animarRecuperacaoVida(mascaras.get(i));
        }
    }

    public void resetarVida() {
        hpAtual = hpMax;
        player.setVida(hpMax);
        morto = false;

        for (int i = 0; i < mascaras.size(); i++) {
            mascaras.get(i).setImage(i < hpAtual ? mascaraCheia : mascaraVazia);
        }
    }

    private void animarPerdaVida(ImageView mask) {
        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> {
            mask.setImage(ResourceLoader.loadImage(breakFrames[0]));
            mask.setFitHeight(breakHeights[0]);
            mask.setTranslateY(-15);
        }));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250), e -> {}));

        for (int i = 1; i < breakFrames.length; i++) {
            int index = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250 + 75 * i), e -> {
                mask.setImage(ResourceLoader.loadImage(breakFrames[index]));
                mask.setFitHeight(breakHeights[index]);
                mask.setTranslateY(0);
            }));
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250 + 75 * breakFrames.length), e -> {
            mask.setImage(mascaraVazia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mask.setTranslateY(0);
        }));

        timeline.play();
    }

    private void animarRecuperacaoVida(ImageView mask) {
        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> mask.setImage(ResourceLoader.loadImage(recoverFrames[0]))));

        for (int i = 1; i < recoverFrames.length; i++) {
            int index = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100 * i), e -> mask.setImage(ResourceLoader.loadImage(recoverFrames[index]))));
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100 * recoverFrames.length), e -> {
            mask.setImage(mascaraCheia);
            mask.setFitWidth(30);
            mask.setFitHeight(30);
            mask.setTranslateY(0);
        }));

        timeline.play();
    }

    private void atualizarLifeOrbs(int novoValor) {
        this.numberLifeOrb = novoValor;
        this.lifeOrbLabel.setText(String.valueOf(numberLifeOrb));
    }

    public void adicionarLifeOrb(int qtd) {
        player.adicionarLifeOrb(qtd);
        atualizarLifeOrbs(player.getLifeOrb());
    }

    public void removerLifeOrb(int qtd) {
        player.removerLifeOrb(qtd);
        atualizarLifeOrbs(player.getLifeOrb());
    }

    public Player getPlayer() {
        return player;
    }

    public int getHpMax() {
        return hpMax;
    }

    public boolean isMorto() {
        return morto;
    }

    public long getMorteStartTime() {
        return morteStartTime;
    }
}
