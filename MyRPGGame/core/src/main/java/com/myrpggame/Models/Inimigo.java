package com.myrpggame.Models;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Inimigo {
    private final Rectangle corpo;
    private final double velocidade;
    private int vida ;
    private int dano ;



    public Inimigo(double x, double y, double tamanho, double velocidade) {
        this.corpo = new Rectangle(tamanho, tamanho, Color.RED);
        this.corpo.setX(x);
        this.corpo.setY(y);
        this.velocidade = velocidade;
        this.vida = 100;
        this.dano = 1;
    }

    public Rectangle getCorpo() {
        return corpo;
    }

    public void seguir(double playerX, double playerY) {
        double dx = playerX - corpo.getX();
        double dy = playerY - corpo.getY();
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia > 0) {
            corpo.setX(corpo.getX() + (dx / distancia) * velocidade);
            corpo.setY(corpo.getY() + (dy / distancia) * velocidade);
        }
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    public void tomarDano (int dano) {
        this.vida -= dano ;
    }

    public void setDano(int dano) {
        this.dano = dano;
    }

    public boolean estaMorto() {
        return vida <= 0;
    }

    public int getDano() { return dano; }
}
