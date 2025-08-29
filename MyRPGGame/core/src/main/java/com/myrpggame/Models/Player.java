package com.myrpggame.Models;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player {
    private final ImageView sprite;

    private int vida;
    private int vidaMaxima = 8;
    private int dano;

    

    public Player(Image imagemInicial, double x, double y) {
        this.sprite = new ImageView(imagemInicial);
        this.sprite.setTranslateX(x);
        this.sprite.setTranslateY(y);
        this.vida = 8;
        this.dano = 15;
    }
    public Player () {
        this.sprite = null ;
        this.vida = 8 ;
        this.dano = 15;
    }

    public ImageView getSprite() {
        return sprite;
    }
    public void tomarDano (double dano) {
        this.vida -= dano ;
    }

    public void recuperarVida (double vida) {
        this.vida += vida ;
    }

    public double getX() {
        return sprite.getTranslateX();
    }

    public double getY() {
        return sprite.getTranslateY();
    }



    public void move(double dx, double dy) {
        sprite.setTranslateX(sprite.getTranslateX() + dx);
        sprite.setTranslateY(sprite.getTranslateY() + dy);
    }

    public void receberDano(int dmg) {
        vida -= dmg;
        if (vida < 0) vida = 0;
        System.out.println("Player sofreu " + dmg + " de dano! Vida: " + vida);
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    public boolean colidiuCom(Inimigo inimigo) {
        return sprite.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent());
    }

    public int getDano() {
        return dano;
    }

    public void setDano(int dano) {
        this.dano = dano;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }
}
