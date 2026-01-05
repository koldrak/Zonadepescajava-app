package com.daille.zonadepescajava_app;

import androidx.lifecycle.ViewModel;

import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;

import java.util.List;

public class GameViewModel extends ViewModel {
    private final GameState gameState = new GameState();
    private boolean initialized = false;
    private boolean finalScoreRecorded = false;

    public GameState getGameState() {
        return gameState;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean startNewGameIfNeeded() {
        if (initialized) {
            return false;
        }
        startNewGame();
        return true;
    }

    public void startNewGame() {
        startNewGame(null);
    }

    public void startNewGame(List<DieType> reserve) {
        gameState.newGame(reserve);
        initialized = true;
        finalScoreRecorded = false;
    }

    public boolean isFinalScoreRecorded() {
        return finalScoreRecorded;
    }

    public void markFinalScoreRecorded() {
        finalScoreRecorded = true;
    }
}
