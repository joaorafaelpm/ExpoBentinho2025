package com.myrpggame.Fases;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.GerenciadorDeInimigo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class FaseFloresta extends Fase {

    private GerenciadorDeInimigo gerInimigos;
    private Set<Integer> inimigosMortos = new HashSet<>();

    public FaseFloresta() {
        super(10000, GameResolution.getAltura());

        // Quantidade aleatória de inimigos entre 5 e 10
        Random rand = new Random();
        setQuantidadeInimigos(8 + rand.nextInt(4));
        setQuantidadeInimigos(0);
    }

    @Override
    public void inicializar() {

        Image img = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/background/forest.png")).toExternalForm()
        );

        double larguraDesejada = getLargura(); // largura total que quer mostrar
        double alturaTela = getAltura();

// Quantas vezes precisamos repetir a imagem
        int repeats = (int) Math.ceil(larguraDesejada / img.getWidth());

        for (int i = 0; i < repeats; i++) {
            ImageView imageView = new ImageView(img);
            imageView.setFitHeight(alturaTela);  // mantém altura da tela
            imageView.setPreserveRatio(false);   // força preencher eixo Y
            imageView.setTranslateX(i * img.getWidth()); // posiciona horizontalmente
            root.getChildren().add( imageView);
        }

        // Inicializa Gerenciador de Inimigos
        gerInimigos = new GerenciadorDeInimigo(this);
        gerInimigos.inicializar();
        gerInimigos.getInimigos().removeIf(inim -> inimigosMortos.contains(inim.getId()));
    }

    @Override
    public GerenciadorDeInimigo getGerenciadorDeInimigo() {
        return gerInimigos;
    }

    public Set<Integer> getInimigosMortos() {
        return inimigosMortos;
    }



    @Override
    public void setQuantidadeInimigos(int quantidade) {
        try {
            Field field = Fase.class.getDeclaredField("quantidadeInimigos");
            field.setAccessible(true);
            field.set(this, quantidade);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
