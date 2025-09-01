package com.myrpggame.Models;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashSet;
import java.util.Set;

public class Player {
    private final ImageView sprite;

    private int vida;
    private int vidaMaxima = 8;
    private int dano;
    private static Set<Fase> salasConcluidas = new HashSet<>();

    public Player(Image imagemInicial, double x, double y) {
        this.sprite = new ImageView(imagemInicial);
        this.sprite.setTranslateX(x);
        this.sprite.setTranslateY(y);
        this.vida = 8;
        this.dano = 15;
    }

    public static boolean isSalaConcluida(Fase fase) {
        return salasConcluidas.contains(fase);
    }

    public static void concluirSala(Fase fase) {
        salasConcluidas.add(fase);
    }

    public static void reset() {
        salasConcluidas.clear();
    }

    public void tomarDano(double dano) {
        this.vida -= dano;
    }

    public void recuperarVida(double vida) {
        this.vida += vida;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    public ImageView getSprite () {
        return sprite;
    }

    public int getDano() {
        return dano;
    }
}

