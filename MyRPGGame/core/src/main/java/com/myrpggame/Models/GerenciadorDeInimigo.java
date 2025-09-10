package com.myrpggame.Models;

import com.myrpggame.Enum.Difficulty;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Utils.DifficultyLevel;
import javafx.scene.image.ImageView;

import java.util.*;

public class GerenciadorDeInimigo {

    private final Difficulty difficulty = DifficultyLevel.getDifficulty();
    private final Fase fase;
    private final List<Inimigo> inimigos = new ArrayList<>();
    private final int inimigosFase;
    private final Random random = new Random();
    public GerenciadorDeInimigo(Fase fase) {
        this.fase = fase;
        this.inimigosFase = fase.getQuantidadeInimigos();
    }

    public void inicializar() {
        inimigos.clear();
        int tentativasMax = 200; // evita loop infinito
        int distanciaMinima = 150; // distância mínima entre inimigos (ajuste conforme quiser)

        for (int i = 0; i < inimigosFase; i++) {
            EnemyType tipo;
            if (!(fase instanceof com.myrpggame.Fases.FaseChefao)) {
                EnemyType[] tipos = {EnemyType.COMMON, EnemyType.TANK, EnemyType.FLYING};
                tipo = tipos[random.nextInt(tipos.length)];
            } else {
                tipo = EnemyType.BOSS;
            }

            int width = switch (tipo) {
                case COMMON, FLYING -> 100;
                case TANK, BOSS -> 200;
            };

            int heigth = switch (tipo) {
                case COMMON, TANK ->  200 ;
                case FLYING -> 100;
                case BOSS -> 400;
            };
            int velocidade = switch (tipo) {
                case COMMON, FLYING -> 5;
                case TANK -> 3;
                case BOSS -> 0;
            };
            int vida = getVida(tipo);

            int dano = switch (tipo) {
                case COMMON, FLYING -> 1;
                case TANK -> 2;
                case BOSS -> 0;
            };

            ImageView imagem = switch (tipo) {
                case COMMON -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/common/CommonIdle_1.png")).toExternalForm()
                );
                case FLYING -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/flying/FlyingRunning_1.png")).toExternalForm()
                );
                case BOSS -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/boss/BossIdle.png")).toExternalForm()
                );
                case TANK -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/tank/TankIdle.png")).toExternalForm()
                );
            };

            double x = 0;
            double yChao = fase.getAltura();

            if (tipo == EnemyType.BOSS) {
                x = (fase.getLargura() / 2.0) - (width / 2.0);
            } else {
                // gera posição X garantindo distância mínima
                boolean posValida = false;
                int tentativas = 0;
                while (!posValida && tentativas < tentativasMax) {
                    x = 500 + random.nextInt((int)(fase.getLargura()));
                    posValida = true;
                    for (Inimigo inim : inimigos) {
                        if (Math.abs(inim.getCorpo().getTranslateX() - x) < distanciaMinima) {
                            posValida = false;
                            break;
                        }
                    }
                    tentativas++;
                }
            }

            Inimigo novo = new Inimigo(x, yChao, width , heigth, velocidade, i, vida, dano, tipo, imagem);
            inimigos.add(novo);
        }
    }

    private int getVida(EnemyType tipo) {
        int vida = 0;

        if (difficulty == Difficulty.EASY) {
            vida = switch (tipo) {
                case COMMON, FLYING -> 15;
                case TANK -> 60;
                case BOSS -> 150;
            };
        }
        if (difficulty == Difficulty.MEDIUM) {
            vida = switch (tipo) {
                case COMMON, FLYING -> 45;
                case TANK -> 105;
                case BOSS -> 400;
            };
        }
        if (difficulty == Difficulty.HARD) {
            vida = switch (tipo) {
                case COMMON, FLYING -> 60;
                case TANK -> 200;
                case BOSS -> 800;
            };
        }
        if (difficulty == Difficulty.DEV) {
            vida = switch (tipo) {
                case COMMON, FLYING -> 100;
                case TANK -> 300;
                case BOSS -> 1500;
            };
        }
        return vida;
    }


    public List<Inimigo> getInimigos() {
        return inimigos;
    }
}
