package com.myrpggame.Models;

import com.myrpggame.Fases.FaseChefao;
import com.myrpggame.Fases.FaseFloresta;
import com.myrpggame.Fases.FasePrisioneiro;
import com.myrpggame.Fases.FaseTutorial;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorDeFase {
    private List<Fase> fases = new ArrayList<>();
    private int faseAtual = 0;

    public GerenciadorDeFase() {
        fases.add(new FaseTutorial());
        fases.add(new FasePrisioneiro());
        fases.add(new FaseFloresta());
        fases.add(new FaseChefao());
    }

    public Fase getFaseAtual() {
        return fases.get(faseAtual);
    }
    public Fase getFase (int faseN) {
        Fase fase = fases.get(faseN);
        if (fase != null) {
            return fase;
        } else {
            throw new RuntimeException("Fase não encontrada!");
        }
    }

    public void avancarFase() {
        if (faseAtual < fases.size() - 1) faseAtual++;
    }

    public void voltarFase() {
        if (faseAtual > 0) faseAtual--;
    }
    public void voltarPrimeiraFase() {
        faseAtual = 0;
    }


    public boolean ultimaFase() { return faseAtual == fases.size() - 1; }
    public boolean primeiraFase() { return faseAtual == 0; }

    public void resetarFases() {
        faseAtual = 0;
    }
}
