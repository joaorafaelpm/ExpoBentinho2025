package com.myrpggame.Models;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

import static com.myrpggame.Config.GameResolution.GameResolution.getAltura;

public class GerenciadorDeFase {
    private final List<Fase> fases = new ArrayList<>();
    private int faseAtualIndex = 0;

    public GerenciadorDeFase() {
        // Cria as fases
        fases.add(new Fase(Color.BLACK, 0, 1900, getAltura()));
        fases.add(new Fase(Color.LIGHTBLUE, 1, 2000, getAltura()));
        fases.add(new Fase(Color.LIGHTGREEN, 2, 3000, getAltura()));
        fases.add(new Fase(Color.LIGHTPINK, 3, 2500, getAltura()));
        fases.add(new Fase(Color.DARKRED, 1, 1800, getAltura()));
    }

    public Fase getFaseAtual() {
        return fases.get(faseAtualIndex);
    }

    public void avancarFase() {
        if (faseAtualIndex < fases.size() - 1) {
            faseAtualIndex++;
        }
    }

    public void voltarFase() {
        if (faseAtualIndex > 0) {
            faseAtualIndex--;
        }
    }

    public boolean ultimaFase() {
        return faseAtualIndex == fases.size() - 1;
    }

    public boolean primeiraFase() {
        return faseAtualIndex == 0;
    }
}
