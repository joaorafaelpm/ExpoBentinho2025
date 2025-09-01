package com.myrpggame.Models;

import com.myrpggame.Enum.EnemyState;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Utils.Animation.EnemyAnimation;
import com.myrpggame.Utils.Attack.EnemyAttack;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Inimigo {
    private int id;
    private ImageView corpo;
    private double velocidade;
    private int vida;
    private int dano;
    private boolean morto = false;
    private EnemyType enemyType;
    private EnemyAttack enemyAttack;



    private EnemyAnimation enemyAnimation;

    private double inicioX, inicioY; // posição inicial para patrulha
    private long lastSeenTime = 0;
    private static final long REACTION_DELAY = 500_000_000; // 0,5s
    private static final long FORGET_DELAY = 2_000_000_000; // 2s para esquecer player

    // Knockback
    private double knockbackX = 0;
    private double knockbackY = 0;
    private static final double FRICTION = 0.85; // diminui knockback gradualmente

    public Inimigo(double x, double y, double tamanho, double velocidade, int id, int vida, int dano, EnemyType enemyType, ImageView imageView) {
        this.id = id;
        this.corpo = imageView;
        this.corpo.setFitWidth(tamanho);
        this.corpo.setFitHeight(tamanho);
        this.corpo.setX(x);
        this.corpo.setY(enemyType == EnemyType.FLYING ? y - 200 : y - tamanho);
        this.velocidade = velocidade;
        this.vida = vida;
        this.dano = dano;
        this.enemyType = enemyType;

        this.inicioX = x;
        this.inicioY = corpo.getY();
    }

    public Rectangle getBounds() {
        return new Rectangle(corpo.getX(), corpo.getY(), corpo.getFitWidth(), corpo.getFitHeight());
    }

    public ImageView getCorpo() { return corpo; }

    public boolean estaMorto() { return morto; }

    public int getDano() { return dano; }

    public EnemyType getEnemyType () {
        return enemyType;
    }

    public int getId() { return id; }

    public void tomarDano(int dano) {
        this.vida -= dano;
        if (this.vida <= 0) this.morto = true;
    }

    public void aplicarKnockback(double dx, double dy, double forca) {
        knockbackX += dx * forca;
        knockbackY += dy * forca;
    }

    public void atualizar(long now) {
        // Aplica knockback
        if (Math.abs(knockbackX) > 0.1 || Math.abs(knockbackY) > 0.1) {
            corpo.setX(corpo.getX() + knockbackX);
            corpo.setY(corpo.getY() + knockbackY);
            knockbackX *= FRICTION;
            knockbackY *= FRICTION;
        }
    }

    public void seguir(double playerX, double playerY, long now) {
        double alcanceHorizontal = 400;
        double alcanceVertical = 300;

        double dx = playerX - corpo.getX();
        double dy = playerY - corpo.getY();

        boolean dentroCampoVisao = Math.abs(dx) <= alcanceHorizontal && Math.abs(dy) <= alcanceVertical;

        if (dentroCampoVisao) {
            lastSeenTime = now;
        }

        if (dentroCampoVisao || now - lastSeenTime <= REACTION_DELAY) {
            double distancia = Math.sqrt(dx * dx + dy * dy);
            if (distancia > 0) {
                corpo.setX(corpo.getX() + (dx / distancia) * velocidade);
                if (enemyType == EnemyType.FLYING) {
                    corpo.setY(corpo.getY() + (dy / distancia) * velocidade);
                }
            }
        } else if (now - lastSeenTime <= FORGET_DELAY) {
            // Retorna devagar à posição inicial
            double pdx = inicioX - corpo.getX();
            double pdy = inicioY - corpo.getY();
            double distPatrulha = Math.sqrt(pdx * pdx + pdy * pdy);
            if (distPatrulha > 0) {
                corpo.setX(corpo.getX() + (pdx / distPatrulha) * (velocidade / 2));
                if (enemyType == EnemyType.FLYING) {
                    corpo.setY(corpo.getY() + (pdy / distPatrulha) * (velocidade / 2));
                }
            }
        }
    }


    public void atualizarDirecao(double playerX) {
        double dx = playerX - corpo.getX();
        if (dx != 0) {
            corpo.setScaleX(dx > 0 ? 1 : -1); // olha para a direita se dx>0, esquerda se dx<0
        }
    }


    public EnemyAnimation getAnimation() {
        return enemyAnimation;
    }

    public void setAnimation(EnemyAnimation enemyAnimation) {
        this.enemyAnimation = enemyAnimation;
    }

    public EnemyAttack getAttack() {
        return enemyAttack;
    }

    public void setAttack(EnemyAttack enemyAttack) {
        this.enemyAttack = enemyAttack;
    }

    public double getVelocidadeX() {
        return velocidade;
    }
}
