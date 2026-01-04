package com.daille.zonadepescajava_app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardSlot {
    private final Card card;
    private final boolean faceUp;
    private final List<Die> dice;
    private final boolean protectedOnce;
    private final boolean calamarForcedFaceDown;
    private final int sumConditionShift;

    public BoardSlot(Card card, boolean faceUp, List<Die> dice, boolean protectedOnce,
                     boolean calamarForcedFaceDown, int sumConditionShift) {
        this.card = card;
        this.faceUp = faceUp;
        this.dice = new ArrayList<>(dice);
        this.protectedOnce = protectedOnce;
        this.calamarForcedFaceDown = calamarForcedFaceDown;
        this.sumConditionShift = sumConditionShift;
    }

    public Card getCard() {
        return card;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public List<Die> getDice() {
        return Collections.unmodifiableList(dice);
    }

    public boolean isProtectedOnce() {
        return protectedOnce;
    }

    public boolean isCalamarForcedFaceDown() {
        return calamarForcedFaceDown;
    }

    public int getSumConditionShift() {
        return sumConditionShift;
    }
}
