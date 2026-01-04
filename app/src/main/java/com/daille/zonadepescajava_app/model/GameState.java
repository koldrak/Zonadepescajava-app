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

    private enum CurrentDirection { UP, DOWN, LEFT, RIGHT }

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
        StringBuilder extraLog = new StringBuilder();
        if (!slot.isFaceUp()) {
            slot.setFaceUp(true);
            String reveal = handleOnReveal(slotIndex);
            if (!reveal.isEmpty()) {
                extraLog.append(" ").append(reveal);
            }
        }

        if (slot.getCard().getCondition().isSatisfied(slotIndex, this)) {
            String onCaptureLog = capture(slotIndex);
            return checkDefeatOrContinue("¡Captura exitosa!" + onCaptureLog + extraLog);
        }

        if (slot.getDice().size() >= 2) {
            String failMsg = handleFailedCatch(slotIndex);
            return checkDefeatOrContinue(failMsg + extraLog);
        }

        String msg = "Necesitas otro dado para intentar la pesca.";
        if (placedValue == 1) {
            msg += " " + applyCurrent(CurrentDirection.UP);
        }
        String corrientes = applyDeepCurrentIfTriggered(placedValue);
        if (!corrientes.isEmpty()) {
            msg += " " + corrientes;
        }
        return checkDefeatOrContinue(msg + extraLog);
    }

    private String capture(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        captures.add(slot.getCard());
        String captureLog = handleOnCapture(slotIndex);
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        return captureLog.isEmpty() ? "" : " " + captureLog;
    }

    public void toggleFace(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() != null) {
            slot.setFaceUp(!slot.isFaceUp());
        }
    }

    public int getScore() {
        int sum = 0;
        int crustaceos = 0, peces = 0, pecesGrandes = 0, objetos = 0;
        int krillCount = 0, sardinaCount = 0, tiburonMartilloCount = 0, limpiadorCount = 0;
        for (Card c : captures) {
            sum += c.getPoints();
            switch (c.getType()) {
                case CRUSTACEO: crustaceos++; break;
                case PEZ: peces++; break;
                case PEZ_GRANDE: pecesGrandes++; break;
                case OBJETO: objetos++; break;
            }
            if (c.getId() == CardId.KRILL) krillCount++;
            if (c.getId() == CardId.SARDINA) sardinaCount++;
            if (c.getId() == CardId.TIBURON_MARTILLO) tiburonMartilloCount++;
            if (c.getId() == CardId.LIMPIADOR_MARINO) limpiadorCount++;
        }

        sum += krillCount * crustaceos;
        sum += sardinaCount * peces;
        sum += tiburonMartilloCount * pecesGrandes * 2;
        sum += limpiadorCount * objetos * 2;
        return sum;
    }

    private String handleFailedCatch(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "Pesca fallida.";
        boolean loseTwo = isHookActive();
        List<Die> dice = new ArrayList<>(slot.getDice());
        dice.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
        List<Die> toLose = new ArrayList<>();
        List<Die> toSave = new ArrayList<>();

        if (loseTwo && dice.size() >= 2) {
            toLose.addAll(dice);
        } else {
            toLose.add(dice.get(0));
            for (int i = 1; i < dice.size(); i++) {
                toSave.add(dice.get(i));
            }
        }

        lostDice.addAll(toLose);
        for (Die d : toSave) {
            reserve.add(d.getType());
        }
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        if (loseTwo && toLose.size() > 1) {
            return "La pesca falló y perdiste 2 dados.";
        }
        return "La pesca falló y perdiste " + toLose.get(0).getLabel();
    }

    private String checkDefeatOrContinue(String base) {
        if ((reserve.isEmpty() && selectedDie == null) || (!hasAnyBoardCard() && deck.isEmpty())) {
            gameOver = true;
            if (reserve.isEmpty() && selectedDie == null) {
                return base + " | Sin dados en reserva: derrota.";
            }
            return base + " | No quedan cartas por capturar.";
        }
        return base;
    }

    private String applyCurrent(CurrentDirection direction) {
        List<Card> toShuffle = new ArrayList<>();
        List<Die> lostFromBoard = new ArrayList<>();
        BoardSlot[] newBoard = new BoardSlot[9];
        for (int i = 0; i < 9; i++) newBoard[i] = new BoardSlot();

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int idx = r * 3 + c;
                BoardSlot src = board[idx];
                int targetR = r, targetC = c;
                switch (direction) {
                    case UP: targetR = r - 1; break;
                    case DOWN: targetR = r + 1; break;
                    case LEFT: targetC = c - 1; break;
                    case RIGHT: targetC = c + 1; break;
                }
                if (targetR < 0 || targetR > 2 || targetC < 0 || targetC > 2) {
                    if (src.getCard() != null) {
                        if (src.getDice().isEmpty()) {
                            toShuffle.add(src.getCard());
                        } else {
                            lostFromBoard.addAll(src.getDice());
                        }
                    }
                    continue;
                }
                int targetIdx = targetR * 3 + targetC;
                newBoard[targetIdx] = src;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (newBoard[i].getCard() == null) {
                newBoard[i] = new BoardSlot();
                newBoard[i].setCard(deck.isEmpty() ? null : deck.pop());
                newBoard[i].setFaceUp(false);
                newBoard[i].setStatus(new SlotStatus());
            }
        }

        if (!toShuffle.isEmpty()) {
            List<Card> tmp = new ArrayList<>(deck);
            tmp.addAll(toShuffle);
            java.util.Collections.shuffle(tmp, rng);
            deck.clear();
            for (Card c : tmp) deck.push(c);
        }

        if (!lostFromBoard.isEmpty()) {
            lostDice.addAll(lostFromBoard);
        }

        for (int i = 0; i < board.length; i++) {
            board[i] = newBoard[i];
        }
        return "Corrientes: el tablero se desplazó";
    }

    private String applyDeepCurrentIfTriggered(int placedValue) {
        for (BoardSlot slot : board) {
            if (!slot.isFaceUp() || slot.getCard() == null || slot.getCard().getId() != CardId.CORRIENTES_PROFUNDAS) {
                continue;
            }
            for (Die d : slot.getDice()) {
                if (d.getValue() == placedValue) {
                    CurrentDirection dir = placedValue % 2 == 0 ? CurrentDirection.RIGHT : CurrentDirection.LEFT;
                    return applyCurrent(dir);
                }
            }
        }
        return "";
    }

    private boolean hasAnyBoardCard() {
        for (BoardSlot s : board) {
            if (s.getCard() != null) return true;
        }
        return false;
    }

    private boolean isHookActive() {
        for (BoardSlot s : board) {
            if (s.isFaceUp() && s.getCard() != null && s.getCard().getId() == CardId.ANZUELO_ROTO) {
                return true;
            }
        }
        return false;
    }

    private String handleOnReveal(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) return "";
        switch (slot.getCard().getId()) {
            case BOTELLA_PLASTICO:
                adjustAdjacentShift(slotIndex, 3);
                return "Botella: los peces adyacentes requieren +3 a la suma.";
            case BOTA_VIEJA:
                return "Bota vieja: −1 a la suma de adyacentes.";
            case CORRIENTES_PROFUNDAS:
                return "Corrientes profundas listas: si igualas su dado, activas marea lateral.";
            default:
                return "";
        }
    }

    private void adjustAdjacentShift(int slotIndex, int delta) {
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            BoardSlot adj = board[rr * 3 + cc];
            if (adj.getCard() != null && adj.getCard().getType() == CardType.PEZ) {
                adj.getStatus().sumConditionShift += delta;
            }
        }
    }

    private String handleOnCapture(int slotIndex) {
        Card captured = board[slotIndex].getCard();
        if (captured == null) return "";
        switch (captured.getId()) {
            case RED_ENREDADA:
                captureAdjacentFaceDown(slotIndex);
                return "Red enredada arrastra una carta adyacente.";
            case LATA_OXIDADA:
                if (!lostDice.isEmpty()) {
                    Die recovered = lostDice.remove(lostDice.size() - 1);
                    reserve.add(recovered.getType());
                    return "Recuperaste un dado perdido.";
                }
                return "";
            default:
                return "";
        }
    }

    private void captureAdjacentFaceDown(int slotIndex) {
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            BoardSlot adj = board[rr * 3 + cc];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                captures.add(adj.getCard());
                adj.clearDice();
                adj.setCard(deck.isEmpty() ? null : deck.pop());
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                break;
            }
        }
    }
}
