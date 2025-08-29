package com.myrpggame.Models ;

import javafx.scene.paint.Color;

import java.util.List;

public class Fase {
    private final Color corFundo;
    private final int quantidadeInimigos;
    private List<Inimigo> inimigos ;

    public Fase(Color corFundo, int quantidadeInimigos) {
        this.corFundo = corFundo;
        this.quantidadeInimigos = quantidadeInimigos;
    }
    public Fase(Color corFundo, List<Inimigo> inimigos) {
        this.corFundo = corFundo;
        this.inimigos = inimigos;
        this.quantidadeInimigos = inimigos.size();
    }

    public Color getCorFundo() { return corFundo; }
    public int getQuantidadeInimigos() { return quantidadeInimigos; }

    public List<Inimigo> getInimigos() {
        return inimigos;
    }

    public void setInimigos(List<Inimigo> inimigos) {
        this.inimigos = inimigos;
    }
}