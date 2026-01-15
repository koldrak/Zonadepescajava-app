package com.daille.zonadepescajava_app;

import androidx.lifecycle.ViewModel;

import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;
import java.util.List;
import java.util.Map;

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

    public void startNewGame(List<com.daille.zonadepescajava_app.model.DieType> reserve) {
        startNewGame(reserve, null);
    }

    public void startNewGame(List<com.daille.zonadepescajava_app.model.DieType> reserve,
                             Map<CardId, Integer> captureCounts) {
        startNewGame(reserve, captureCounts, null);
    }

    public void startNewGame(List<DieType> reserve, Map<CardId, Integer> captureCounts, List<Card> selectedDeck) {
        gameState.newGame(reserve, captureCounts, selectedDeck);
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
