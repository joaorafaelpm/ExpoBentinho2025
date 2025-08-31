package com.myrpggame.Models;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public abstract class Fase {
    protected Pane root;
    protected double largura;
    protected double altura;
    protected int quantidadeInimigos = 0; // padrão 0
    protected Color corFundo = Color.BLACK; // fallback
    protected GerenciadorDeInimigo gerenciadorDeInimigo;

    public Fase(double largura, double altura) {
        this.largura = largura;
        this.altura = altura;
        this.root = new Pane();
        this.root.setPrefSize(largura, altura);
        inicializar();
    }

    public abstract void inicializar();

    public Pane getRoot() {
        return root;
    }

    public double getLargura() { return largura; }
    public double getAltura() { return altura; }

    public GerenciadorDeInimigo getGerenciadorDeInimigo () {
        return gerenciadorDeInimigo ;
    }

    public int getQuantidadeInimigos() { return quantidadeInimigos; }
    public void setQuantidadeInimigos(int quantidadeInimigos) { this.quantidadeInimigos = quantidadeInimigos; }
    public Color getCorFundo() { return corFundo; }

}
