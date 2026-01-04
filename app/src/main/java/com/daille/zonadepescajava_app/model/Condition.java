package com.daille.zonadepescajava_app.model;

public interface Condition {
    boolean isSatisfied(int slotIndex, GameState game);
}
