package com.myrpggame.Utils;

import com.myrpggame.Enum.Difficulty;
import java.util.EnumMap;
import java.util.Map;

public class WinTimes {

    private final Map<Difficulty, Integer> vitorias = new EnumMap<>(Difficulty.class);

    public WinTimes() {
        for (Difficulty d : Difficulty.values()) {
            vitorias.put(d, 0);
        }
    }

    public void adicionarVitoria(Difficulty d) {
        vitorias.put(d, vitorias.get(d) + 1);
    }

    public int getVitorias(Difficulty d) {
        return vitorias.get(d);
    }

    public String gerarResumo() {
        return "Fácil: " + vitorias.get(Difficulty.EASY) + "\n" +
                "Médio: " + vitorias.get(Difficulty.MEDIUM) + "\n" +
                "Difícil: " + vitorias.get(Difficulty.HARD) + "\n" +
                "Impossível: " + vitorias.get(Difficulty.DEV);
    }
}
