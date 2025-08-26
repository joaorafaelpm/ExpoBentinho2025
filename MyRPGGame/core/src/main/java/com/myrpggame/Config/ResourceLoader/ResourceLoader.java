package com.myrpggame.Config.ResourceLoader;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.Objects;

public class ResourceLoader {

    /** Carrega uma imagem do resources/assets */
    public static Image loadImage(String path) {
        URL resource = ResourceLoader.class.getResource(path);
        if (resource == null) {
            throw new RuntimeException("Imagem não encontrada: " + path);
        }
        return new Image(resource.toExternalForm());
    }

    /** Retorna o path para adicionar um stylesheet sem gerar NullPointerException */
    public static String loadStyle(String path) {
        URL resource = ResourceLoader.class.getResource(path);
        if (resource == null) {
            throw new RuntimeException("Stylesheet não encontrada: " + path);
        }
        return resource.toExternalForm();
    }
}
