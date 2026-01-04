package com.daille.zonadepescajava_app.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class GameState {
    private final Random rng = new Random();
    private final BoardSlot[] board = new BoardSlot[9];
    private final Deque<Card> deck = new ArrayDeque<>();
    private final List<Card> captures = new ArrayList<>();
    private final List<Die> lostDice = new ArrayList<>();
    private final List<DieType> reserve = new ArrayList<>();
    private Die selectedDie;

    public GameState() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardSlot();
        }
    }

    public void newGame() {
        captures.clear();
        lostDice.clear();
        deck.clear();
        reserve.clear();
        selectedDie = null;

        reserve.add(DieType.D6);
        reserve.add(DieType.D6);
        reserve.add(DieType.D6);
        reserve.add(DieType.D8);
        reserve.add(DieType.D8);
        reserve.add(DieType.D4);
        reserve.add(DieType.D12);

        List<Card> allCards = GameUtils.createAllCards();
        java.util.Collections.shuffle(allCards, rng);
        for (Card c : allCards) {
            deck.push(c);
        }

        for (BoardSlot slot : board) {
            slot.setCard(deck.isEmpty() ? null : deck.pop());
            slot.setFaceUp(false);
            slot.clearDice();
            slot.setStatus(new SlotStatus());
        }
    }

    public BoardSlot[] getBoard() {
        return board;
    }

    public List<Card> getCaptures() {
        return captures;
    }

    public int getDeckSize() {
        return deck.size();
    }

    public List<Die> getLostDice() {
        return lostDice;
    }

    public List<DieType> getReserve() {
        return reserve;
    }

    public Die getSelectedDie() {
        return selectedDie;
    }

    public String rollFromReserve(DieType type) {
        if (!reserve.remove(type)) {
            return "No hay más dados " + type.getLabel();
        }
        selectedDie = Die.roll(type, rng);
        return "Lanzaste " + selectedDie.getLabel();
    }

    public String placeSelectedDie(int slotIndex) {
        if (selectedDie == null) {
            return "No has lanzado ningún dado.";
        }
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) {
            return "No hay carta en esta casilla.";
        }
        slot.addDie(selectedDie);
        selectedDie = null;
        if (!slot.isFaceUp()) {
            slot.setFaceUp(true);
        }

        if (slot.getCard().getCondition().isSatisfied(slotIndex, this)) {
            capture(slotIndex);
            return "¡Captura exitosa!";
        }

        if (slot.getDice().size() >= 2) {
            Die lost = slot.getDice().get(slot.getDice().size() - 1);
            lostDice.add(lost);
            slot.clearDice();
            return "La pesca falló y perdiste " + lost.getLabel();
        }

        return "Necesitas otro dado para intentar la pesca.";
    }

    private void capture(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        captures.add(slot.getCard());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
    }

    public void toggleFace(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() != null) {
            slot.setFaceUp(!slot.isFaceUp());
        }
    }

    public int getScore() {
        int sum = 0;
        for (Card c : captures) {
            sum += c.getPoints();
        }
        return sum;
    }
}
