package com.myrpggame.Models;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Projectile {
        private final ImageView corpo;
        private final double velocidade;
        private final double direcaoX;
        private final double direcaoY;
        private final int dano;

        private final ImageView player ;

        public Projectile(double startX, double startY, double targetX, double targetY, double velocidade, int dano, String spritePath , ImageView player) {
            this.dano = dano;
            this.velocidade = velocidade;

            // cria imagem da flecha
            this.corpo = new ImageView(new Image(spritePath));
            this.corpo.setFitWidth(32);   // ajusta tamanho
            this.corpo.setFitHeight(32);
            this.corpo.setTranslateX(startX);
            this.corpo.setTranslateY(startY);

            // calcula direção normalizada
            double dx = targetX - startX;
            double dy = targetY - startY;
            double length = Math.sqrt(dx * dx + dy * dy);

            this.direcaoX = dx / length;
            this.direcaoY = dy / length;

            this.player = player;
        }

        public void update() {
            double dx = player.getTranslateX() - corpo.getTranslateX();
            double dy = player.getTranslateY() - corpo.getTranslateY();

            double length = Math.sqrt(dx * dx + dy * dy);

            if (length != 0) {
                dx /= length;
                dy /= length;
            }

            corpo.setTranslateX(corpo.getTranslateX() + dx * velocidade);
            corpo.setTranslateY(corpo.getTranslateY() + dy * velocidade);

        }

        public boolean isForaDaTela(double largura, double altura) {
            return corpo.getTranslateX() < 0 || corpo.getTranslateX() > largura ||
                    corpo.getTranslateY() < 0 || corpo.getTranslateY() > altura;
        }

        public ImageView getCorpo() {
            return corpo;
        }

        public int getDano() {
            return dano;
        }
    }
