package com.myrpggame.Utils;

public class WinTimes {
    private int numeroDeVitorias = 1 ;

    public int getNumeroDeVitorias () {
        return numeroDeVitorias;
    }

    public void addVitoria () {
        numeroDeVitorias++;
    }

    public void resetVictories() {
        this.numeroDeVitorias = 0;
    }
}
