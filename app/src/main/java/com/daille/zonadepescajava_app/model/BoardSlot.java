package com.daille.zonadepescajava_app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardSlot {
    private Card card;
    private Card underCard;
    private boolean faceUp;
    private final List<Die> dice = new ArrayList<>();
    private SlotStatus status = new SlotStatus();

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
        this.underCard = null;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public List<Die> getDice() {
        return Collections.unmodifiableList(dice);
    }

    public void addDie(Die die) {
        dice.add(die);
    }

    public void setDie(int index, Die die) {
        dice.set(index, die);
    }

    public Die removeDie(int index) {
        return dice.remove(index);
    }

    public void clearDice() {
        dice.clear();
    }

    public Card getUnderCard() {
        return underCard;
    }

    public void setUnderCard(Card underCard) {
        this.underCard = underCard;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }
}
