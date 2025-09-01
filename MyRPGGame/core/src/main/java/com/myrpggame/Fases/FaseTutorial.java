package com.myrpggame.Fases;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.GerenciadorDeInimigo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class FaseTutorial extends Fase {

    private GerenciadorDeInimigo gerInimigos;

    public FaseTutorial() {
        super(2000, GameResolution.getAltura());
        setQuantidadeInimigos(0); // Apenas o boss
    }

    @Override
    public void inicializar() {

        Image img = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/background/tutorial.png")).toExternalForm()
        );

        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(getAltura());  // mantém altura da tela
        imageView.setPreserveRatio(false);   // força preencher eixo Y
        root.getChildren().add( imageView);



        gerInimigos = new GerenciadorDeInimigo(this);
        gerInimigos.inicializar();
    }

    @Override
    public GerenciadorDeInimigo getGerenciadorDeInimigo() {
        return gerInimigos;
    }

}
