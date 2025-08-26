package com.myrpggame.Models;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player {
    private final ImageView sprite;
    private int vida;

    public Player(Image imagemInicial, double x, double y) {
        this.sprite = new ImageView(imagemInicial);
        this.sprite.setTranslateX(x);
        this.sprite.setTranslateY(y);
        this.vida = 100;
    }

    public ImageView getSprite() {
        return sprite;
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

    public boolean colidiuCom(Inimigo inimigo) {
        return sprite.getBoundsInParent().intersects(inimigo.getCorpo().getBoundsInParent());
    }
}
