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
    private boolean gameOver = false;

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
        gameOver = false;

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
        if (gameOver) {
            return "La partida ha terminado";
        }
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
        if (gameOver) {
            return "La partida ha terminado";
        }
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) {
            return "No hay carta en esta casilla.";
        }
        if (slot.getDice().size() >= 2) {
            selectedDie = null;
            return "Una carta no puede tener más de 2 dados.";
        }
        slot.addDie(selectedDie);
        int placedValue = selectedDie.getValue();
        selectedDie = null;
        if (!slot.isFaceUp()) {
            slot.setFaceUp(true);
        }

        if (slot.getCard().getCondition().isSatisfied(slotIndex, this)) {
            capture(slotIndex);
            return checkDefeatOrContinue("¡Captura exitosa!");
        }

        if (slot.getDice().size() >= 2) {
            String failMsg = handleFailedCatch(slotIndex);
            return checkDefeatOrContinue(failMsg);
        }

        String msg = "Necesitas otro dado para intentar la pesca.";
        if (placedValue == 1) {
            msg += " " + applyCurrent();
        }
        return checkDefeatOrContinue(msg);
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

    private String handleFailedCatch(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "Pesca fallida.";
        Die lost = slot.getDice().get(0);
        Die saved = slot.getDice().get(slot.getDice().size() - 1);
        if (slot.getDice().size() == 2) {
            if (slot.getDice().get(1).getValue() > slot.getDice().get(0).getValue()) {
                lost = slot.getDice().get(1);
                saved = slot.getDice().get(0);
            }
        }
        lostDice.add(lost);
        reserve.add(saved.getType());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        return "La pesca falló y perdiste " + lost.getLabel();
    }

    private String checkDefeatOrContinue(String base) {
        if (reserve.isEmpty() && selectedDie == null) {
            gameOver = true;
            return base + " | Sin dados en reserva: derrota.";
        }
        return base;
    }

    private String applyCurrent() {
        List<Card> toShuffle = new ArrayList<>();
        for (int c = 0; c < 3; c++) {
            int idx = c;
            BoardSlot leaving = board[idx];
            if (leaving.getCard() != null) {
                if (leaving.getDice().isEmpty()) {
                    toShuffle.add(leaving.getCard());
                } else {
                    lostDice.addAll(leaving.getDice());
                }
            }
        }

        BoardSlot[] newBoard = new BoardSlot[9];
        for (int i = 0; i < 9; i++) newBoard[i] = new BoardSlot();

        for (int r = 1; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int from = r * 3 + c;
                int to = (r - 1) * 3 + c;
                newBoard[to] = board[from];
            }
        }

        for (int c = 0; c < 3; c++) {
            int idx = 6 + c;
            newBoard[idx] = new BoardSlot();
            newBoard[idx].setCard(deck.isEmpty() ? null : deck.pop());
            newBoard[idx].setFaceUp(false);
            newBoard[idx].setStatus(new SlotStatus());
        }

        if (!toShuffle.isEmpty()) {
            List<Card> tmp = new ArrayList<>(deck);
            tmp.addAll(toShuffle);
            java.util.Collections.shuffle(tmp, rng);
            deck.clear();
            for (Card c : tmp) deck.push(c);
        }

        for (int i = 0; i < board.length; i++) {
            board[i] = newBoard[i];
        }
        return "Corrientes: el tablero se desplazó";
    }
}
