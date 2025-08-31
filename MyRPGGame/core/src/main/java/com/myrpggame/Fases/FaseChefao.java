package com.myrpggame.Fases;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.GerenciadorDeInimigo;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class FaseChefao extends Fase {

    private GerenciadorDeInimigo gerInimigos;

    public FaseChefao() {
        super(2000, GameResolution.getAltura());
        setQuantidadeInimigos(1); // Apenas o boss
    }

    @Override
    public void inicializar() {
        double alturaChao = 50;

        // Chão
        Rectangle chao = new Rectangle(getLargura(), alturaChao, Color.DARKRED);
        chao.setTranslateY(getAltura() - alturaChao);
        root.getChildren().add(chao);

        // Fundo
        Rectangle fundo = new Rectangle(getLargura(), getAltura() - alturaChao, Color.DIMGRAY);
        root.getChildren().add(0, fundo);

        gerInimigos = new GerenciadorDeInimigo(this);
        gerInimigos.inicializar();
    }

    @Override
    public GerenciadorDeInimigo getGerenciadorDeInimigo() {
        return gerInimigos;
    }

}
