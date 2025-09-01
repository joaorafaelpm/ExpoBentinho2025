package com.myrpggame.Fases;

import com.myrpggame.Config.GameResolution.GameResolution;
import com.myrpggame.Enum.EnemyType;
import com.myrpggame.Models.Fase;
import com.myrpggame.Models.GerenciadorDeInimigo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FaseChefao extends Fase {

    private GerenciadorDeInimigo gerInimigos;

    public FaseChefao() {
        super(2560, GameResolution.getAltura());

        // Quantidade aleatória de inimigos entre 5 e 10
        setQuantidadeInimigos(1);
    }

    @Override
    public void inicializar() {
        Image img = new Image(
                Objects.requireNonNull(getClass().getResource("/assets/background/salachefao.png")).toExternalForm()
        );

        double alturaTela = getAltura();

        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(alturaTela);  // mantém altura da tela
        imageView.setPreserveRatio(false);   // força preencher eixo Y
        root.getChildren().add( imageView);

        // Inicializa Gerenciador de Inimigos
        gerInimigos = new GerenciadorDeInimigo(this);
        gerInimigos.inicializar();
    }

    @Override
    public GerenciadorDeInimigo getGerenciadorDeInimigo() {
        return gerInimigos;
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
