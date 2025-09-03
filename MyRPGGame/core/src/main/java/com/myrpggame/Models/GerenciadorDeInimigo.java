package com.myrpggame.Models;

import com.myrpggame.Enum.EnemyType;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GerenciadorDeInimigo {

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

        for (int i = 0; i < inimigosFase; i++) {
            EnemyType tipo;

            // Para fases normais, aleatoriza tipo exceto BOSS
            if (!(fase instanceof com.myrpggame.Fases.FaseChefao)) {
                EnemyType[] tipos = {EnemyType.COMMON, EnemyType.TANK, EnemyType.FLYING, EnemyType.ARCHER};
                tipo = tipos[random.nextInt(tipos.length)];
            } else {
                tipo = EnemyType.BOSS;
            }

            // Tamanho e atributos
            int tamanho = switch (tipo) {
                case COMMON, FLYING, ARCHER -> 100;
                case TANK -> 200;
                case BOSS -> 500;
            };
            int velocidade = switch (tipo) {
                case COMMON, FLYING -> 5;
                case TANK, BOSS -> 3;
                case ARCHER -> 0;
            };
            int vida = switch (tipo) {
                case COMMON, FLYING -> 50;
                case TANK -> 100;
                case ARCHER -> 50;
                case BOSS -> 200;
            };
            int dano = switch (tipo) {
                case COMMON, FLYING -> 1;
                case TANK -> 2;
                case ARCHER -> 0;
                case BOSS -> 3;
            };

            // Imagem
            ImageView imagem = switch (tipo) {
                case ARCHER-> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/archer/gothgirlstepsister_1.png")).toExternalForm()
                );
                case COMMON -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/common/CommonIdle_1.png")).toExternalForm()
                );
                case FLYING -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/flying/FlyingRunning_1.png")).toExternalForm()
                );
                case BOSS -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/inimigo-tutorial.jpg")).toExternalForm()
                );
                case TANK -> new ImageView(
                        Objects.requireNonNull(getClass().getResource("/assets/inimigos/tank/TankIdle.png")).toExternalForm()
                );
            };

            double x = 500 + random.nextInt((int)(fase.getLargura() - 500));
            // Posições
            if (tipo == EnemyType.BOSS) {
                x = fase.getLargura() - 500;
            }
            double yChao = fase.getAltura();

            // Cria inimigo passando tipo
            Inimigo novo = new Inimigo(x, yChao, tamanho, velocidade, i, vida, dano, tipo, imagem);
            inimigos.add(novo);
        }
    }

    public List<Inimigo> getInimigos() {
        return inimigos;
    }
}
