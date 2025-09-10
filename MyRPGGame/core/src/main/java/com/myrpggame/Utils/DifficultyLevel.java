package com.myrpggame.Utils;

import com.myrpggame.Enum.Difficulty;

public class DifficultyLevel {
    private static Difficulty currentDifficulty = Difficulty.MEDIUM;

    public static Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public static void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }
}
