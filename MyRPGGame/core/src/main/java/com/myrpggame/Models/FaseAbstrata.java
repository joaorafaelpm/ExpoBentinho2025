package com.myrpggame.Models;

import javafx.scene.paint.Color;

public abstract class FaseAbstrata {
    protected double largura;
    protected double altura;
    protected int quantidadeInimigos;
    protected Color corFundo;

    public double getLargura() { return largura; }
    public double getAltura() { return altura; }
    public int getQuantidadeInimigos() { return quantidadeInimigos; }
    public Color getCorFundo() { return corFundo; }

    // Você pode adicionar métodos para obstáculos, plataformas, spawn de inimigos, etc.
}
