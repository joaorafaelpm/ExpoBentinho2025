package com.myrpggame.Models ;

import javafx.scene.paint.Color;

import java.util.List;

public class Fase {

    private final Color corFundo;
    private final int quantidadeInimigos;
    private List<Inimigo> inimigos ;


    public Fase(Color corFundo, int quantidadeInimigos, double largura, double altura) {
        this.corFundo = corFundo;
        this.quantidadeInimigos = quantidadeInimigos;
        this.largura = largura;
        this.altura = altura;
    }


    private double largura;
    private double altura;



    public double getLargura() { return largura; }
    public double getAltura() { return altura; }


    public Color getCorFundo() { return corFundo; }
    public int getQuantidadeInimigos() { return quantidadeInimigos; }

}