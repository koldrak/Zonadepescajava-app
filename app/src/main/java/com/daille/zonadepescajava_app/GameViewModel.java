package com.daille.zonadepescajava_app;

import androidx.lifecycle.ViewModel;

import com.daille.zonadepescajava_app.model.GameState;

public class GameViewModel extends ViewModel {
    private final GameState gameState = new GameState();
    private boolean initialized = false;

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
        gameState.newGame();
        initialized = true;
        return true;
    }
}
