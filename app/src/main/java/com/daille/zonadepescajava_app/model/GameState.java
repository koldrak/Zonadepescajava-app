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
    private final List<Card> failedDiscards = new ArrayList<>();
    private Die selectedDie;
    private boolean gameOver = false;
    private Integer pendingDieLossSlot = null;
    private int pendingLossTriggerValue = 0;
    private Integer forcedSlotIndex = null;

    private enum CurrentDirection { UP, DOWN, LEFT, RIGHT }

    public GameState() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardSlot();
        }
    }

    public void newGame() {
        captures.clear();
        lostDice.clear();
        failedDiscards.clear();
        deck.clear();
        reserve.clear();
        selectedDie = null;
        gameOver = false;
        pendingDieLossSlot = null;
        pendingLossTriggerValue = 0;
        forcedSlotIndex = null;

        reserve.add(DieType.D6);
        reserve.add(DieType.D6);
        reserve.add(DieType.D6);
        reserve.add(DieType.D8);
        reserve.add(DieType.D8);
        reserve.add(DieType.D4);
        reserve.add(DieType.D12);

        List<Card> allCards = GameUtils.buildDeck(rng);
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

    public boolean isAwaitingDieLoss() {
        return pendingDieLossSlot != null;
    }

    public List<Die> getPendingDiceChoices() {
        if (pendingDieLossSlot == null) {
            return java.util.Collections.emptyList();
        }
        return new ArrayList<>(board[pendingDieLossSlot].getDice());
    }

    public String chooseDieToLose(int dieIndex) {
        if (!isAwaitingDieLoss()) {
            return "No hay una pesca fallida pendiente.";
        }
        BoardSlot slot = board[pendingDieLossSlot];
        if (dieIndex < 0 || dieIndex >= slot.getDice().size()) {
            return "No se pudo elegir ese dado.";
        }

        Die lost = slot.getDice().get(dieIndex);
        lostDice.add(lost);
        for (int i = 0; i < slot.getDice().size(); i++) {
            if (i == dieIndex) continue;
            reserve.add(slot.getDice().get(i).getType());
        }

        failedDiscards.add(slot.getCard());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());

        int placedValue = pendingLossTriggerValue;
        pendingDieLossSlot = null;
        pendingLossTriggerValue = 0;

        String result = "La pesca falló y perdiste " + lost.getLabel();
        String corrientes = buildCurrentsLog(placedValue);
        if (!corrientes.isEmpty()) {
            result += " " + corrientes;
        }
        return checkDefeatOrContinue(result);
    }

    public String rollFromReserve(DieType type) {
        if (gameOver) {
            return "La partida ha terminado";
        }
        if (isAwaitingDieLoss()) {
            return "Debes elegir qué dado perder antes de continuar.";
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
        if (isAwaitingDieLoss()) {
            return "Debes elegir qué dado perder antes de continuar.";
        }
        if (forcedSlotIndex != null && slotIndex != forcedSlotIndex) {
            return "El próximo dado debe colocarse en la carta obligatoria.";
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
        if (forcedSlotIndex != null && slotIndex == forcedSlotIndex) {
            forcedSlotIndex = null;
        }

        StringBuilder extraLog = new StringBuilder();
        if (!slot.isFaceUp()) {
            slot.setFaceUp(true);
            String reveal = handleOnReveal(slotIndex, placedValue);
            if (!reveal.isEmpty()) {
                extraLog.append(" ").append(reveal);
            }
        }

        if (slot.getDice().size() < 2) {
            String msg = "Necesitas otro dado para intentar la pesca.";
            String corrientes = buildCurrentsLog(placedValue);
            if (!corrientes.isEmpty()) {
                msg += " " + corrientes;
            }
            return checkDefeatOrContinue(msg + extraLog);
        }

        if (slot.getCard().getCondition().isSatisfied(slotIndex, this)) {
            String onCaptureLog = capture(slotIndex);
            String corrientes = buildCurrentsLog(placedValue);
            String result = "¡Captura exitosa!" + onCaptureLog + extraLog;
            if (!corrientes.isEmpty()) {
                result += " " + corrientes;
            }
            return checkDefeatOrContinue(result);
        }

        if (slot.getStatus().protectedOnce) {
            String protectedFail = handleProtectedFailure(slotIndex);
            String corrientes = buildCurrentsLog(placedValue);
            if (!corrientes.isEmpty()) {
                protectedFail += " " + corrientes;
            }
            return checkDefeatOrContinue(protectedFail + extraLog);
        }

        boolean loseTwo = isHookActive();
        if (loseTwo) {
            String failMsg = handleFailedCatchImmediate(slotIndex, true);
            String corrientes = buildCurrentsLog(placedValue);
            if (!corrientes.isEmpty()) {
                failMsg += " " + corrientes;
            }
            return checkDefeatOrContinue(failMsg + extraLog);
        }

        pendingDieLossSlot = slotIndex;
        pendingLossTriggerValue = placedValue;
        return checkDefeatOrContinue("La pesca falló. Elige qué dado perder." + extraLog);
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
        int krillCount = 0, sardinaCount = 0, tiburonMartilloCount = 0, limpiadorCount = 0, tiburonBallenaCount = 0;
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
            if (c.getId() == CardId.TIBURON_BALLENA) tiburonBallenaCount++;
        }

        sum += krillCount * crustaceos;
        sum += sardinaCount * peces;
        sum += tiburonMartilloCount * pecesGrandes * 2;
        sum += limpiadorCount * objetos * 2;
        if (crustaceos >= 3) {
            sum += tiburonBallenaCount * 6;
        }
        return sum;
    }

    private String handleFailedCatchImmediate(int slotIndex, boolean loseTwo) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "Pesca fallida.";
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
        failedDiscards.add(slot.getCard());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        if (loseTwo && toLose.size() > 1) {
            return "La pesca falló y perdiste 2 dados.";
        }
        return "La pesca falló y perdiste " + toLose.get(0).getLabel();
    }

    private String handleProtectedFailure(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        for (Die d : slot.getDice()) {
            reserve.add(d.getType());
        }
        failedDiscards.add(slot.getCard());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        return "Pesca fallida pero protegida: los dados regresan a la reserva.";
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

    private String buildCurrentsLog(int placedValue) {
        StringBuilder msg = new StringBuilder();
        if (placedValue == 1) {
            msg.append(applyCurrent(CurrentDirection.UP));
        }
        String deep = applyDeepCurrentIfTriggered(placedValue);
        if (!deep.isEmpty()) {
            if (msg.length() > 0) msg.append(" ");
            msg.append(deep);
        }
        return msg.toString();
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
                            if (src.getCard().getId() == CardId.PEZ_LUNA) {
                                releaseHighestCapture();
                            }
                            failedDiscards.add(src.getCard());
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

    private String handleOnReveal(int slotIndex, int placedValue) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) return "";
        switch (slot.getCard().getId()) {
            case CANGREJO_ROJO:
                return moveOneDieBetweenAdjacents(slotIndex);
            case JAIBA_AZUL:
                return adjustLastDie(slotIndex);
            case CAMARON_FANTASMA:
                return peekAdjacentCards(slotIndex);
            case CANGREJO_ERMITANO:
                return replaceAdjacentObject(slotIndex);
            case CENTOLLA:
                forcedSlotIndex = slotIndex;
                return "Centolla atrae el próximo dado a esta carta.";
            case NAUTILUS:
                return retuneTwoDice();
            case CANGREJO_ARANA:
                return reviveFailedCard();
            case BOTELLA_PLASTICO:
                adjustAdjacentShift(slotIndex, 3);
                return "Botella: los peces adyacentes requieren +3 a la suma.";
            case BOTA_VIEJA:
                return "Bota vieja: −1 a la suma de adyacentes.";
            case CORRIENTES_PROFUNDAS:
                return "Corrientes profundas listas: si igualas su dado, activas marea lateral.";
            case PEZ_PAYASO:
                return protectAdjacentFish(slotIndex);
            case PEZ_LINTERNA:
                return revealAndPossiblyMoveDie(slotIndex, placedValue);
            case PEZ_VELA:
                return rerollLatestDie(slotIndex);
            case CALAMAR_GIGANTE:
                return flipAdjacentFaceUpCardsDown(slotIndex);
            case MANTA_GIGANTE:
                return recoverSpecificDie(DieType.D8);
            case ARENQUE:
                return seedAdjacentSmallFish(slotIndex);
            case PEZ_LUNA:
                return "Si la marea lo expulsa, liberarás tu captura de mayor valor.";
            default:
                return "";
        }
    }

    private String reviveFailedCard() {
        if (failedDiscards.isEmpty()) return "";
        for (int i = 0; i < board.length; i++) {
            if (board[i].getCard() == null) {
                Card rescued = failedDiscards.remove(failedDiscards.size() - 1);
                board[i].setCard(rescued);
                board[i].setFaceUp(false);
                board[i].setStatus(new SlotStatus());
                return "Cangrejo araña devolvió una carta descartada.";
            }
        }
        return "";
    }

    private List<Integer> adjacentIndices(int slotIndex, boolean includeDiagonals) {
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = includeDiagonals
                ? new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
                : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<Integer> indices = new ArrayList<>();
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            indices.add(rr * 3 + cc);
        }
        return indices;
    }

    private String moveOneDieBetweenAdjacents(int slotIndex) {
        List<Integer> adjs = adjacentIndices(slotIndex, true);
        Integer from = null, to = null;
        for (Integer idx : adjs) {
            if (board[idx].getDice().size() > 0) {
                from = idx;
                break;
            }
        }
        for (Integer idx : adjs) {
            if (board[idx].getDice().size() < 2) {
                to = idx;
                break;
            }
        }
        if (from == null || to == null || from.equals(to)) return "";
        Die moved = board[from].removeDie(0);
        board[to].addDie(moved);
        return "Cangrejo rojo movió un dado entre cartas adyacentes.";
    }

    private String adjustLastDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "";
        int idx = slot.getDice().size() - 1;
        Die last = slot.getDice().get(idx);
        int value = last.getValue();
        int sides = last.getType().getSides();
        if (value % 2 != 0) {
            if (value + 1 <= sides) value += 1; else if (value - 1 >= 1) value -= 1;
        } else if (value + 1 <= sides) {
            value += 1;
        }
        slot.setDie(idx, new Die(last.getType(), value));
        return "Jaiba azul ajustó el dado a " + value + ".";
    }

    private String peekAdjacentCards(int slotIndex) {
        List<Integer> adjs = adjacentIndices(slotIndex, true);
        List<String> names = new ArrayList<>();
        for (Integer idx : adjs) {
            if (names.size() >= 2) break;
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                names.add(adj.getCard().getName());
            }
        }
        if (names.isEmpty()) return "";
        return "Observaste: " + String.join(", ", names);
    }

    private String replaceAdjacentObject(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.OBJETO) {
                failedDiscards.add(adj.getCard());
                adj.clearDice();
                adj.setCard(deck.isEmpty() ? null : deck.pop());
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                return "Cangrejo ermitaño reemplazó un objeto adyacente.";
            }
        }
        return "";
    }

    private String retuneTwoDice() {
        int adjusted = 0;
        outer: for (BoardSlot s : board) {
            for (int i = 0; i < s.getDice().size(); i++) {
                Die d = s.getDice().get(i);
                int sides = d.getType().getSides();
                int newVal = d.getValue();
                if (newVal + 2 <= sides) newVal += 2; else if (newVal - 2 >= 1) newVal -= 2; else continue;
                s.setDie(i, new Die(d.getType(), newVal));
                adjusted++;
                if (adjusted >= 2) break outer;
            }
        }
        return adjusted == 0 ? "" : "Nautilus ajustó el valor de " + adjusted + " dado(s).";
    }

    private String protectAdjacentFish(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.getCard().getType() == CardType.PEZ) {
                adj.getStatus().protectedOnce = true;
                return "Pez payaso protege a un pez adyacente.";
            }
        }
        return "";
    }

    private String revealAndPossiblyMoveDie(int slotIndex, int placedValue) {
        BoardSlot origin = board[slotIndex];
        BoardSlot target = null;
        for (BoardSlot s : board) {
            if (s.getCard() != null && !s.isFaceUp()) {
                target = s;
                break;
            }
        }
        if (target == null) return "";
        target.setFaceUp(true);
        StringBuilder log = new StringBuilder("Revelaste " + target.getCard().getName());
        if (target.getCard().getType() == CardType.PEZ_GRANDE && !origin.getDice().isEmpty() && target.getDice().size() < 2) {
            Die moved = origin.removeDie(origin.getDice().size() - 1);
            target.addDie(moved);
            log.append(" y moviste el dado a ese pez grande.");
        } else if (target.getCard().getType() == CardType.OBJETO && !origin.getDice().isEmpty()) {
            Die lost = origin.removeDie(origin.getDice().size() - 1);
            lostDice.add(lost);
            log.append(", era un objeto: el dado se pierde.");
        }
        return log.toString();
    }

    private String rerollLatestDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "";
        int idx = slot.getDice().size() - 1;
        Die oldDie = slot.getDice().get(idx);
        Die rerolled = Die.roll(oldDie.getType(), rng);
        int best = Math.max(oldDie.getValue(), rerolled.getValue());
        slot.setDie(idx, new Die(oldDie.getType(), best));
        return "Pez vela eligió el mejor resultado: " + best;
    }

    private String flipAdjacentFaceUpCardsDown(int slotIndex) {
        int flipped = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                adj.setFaceUp(false);
                adj.getStatus().calamarForcedFaceDown = true;
                flipped++;
            }
        }
        return flipped == 0 ? "" : "Calamar gigante volvió boca abajo " + flipped + " carta(s).";
    }

    private String recoverSpecificDie(DieType type) {
        for (int i = 0; i < lostDice.size(); i++) {
            if (lostDice.get(i).getType() == type) {
                reserve.add(type);
                lostDice.remove(i);
                return "Recuperaste un dado " + type.getLabel();
            }
        }
        return "";
    }

    private String seedAdjacentSmallFish(int slotIndex) {
        List<Card> picked = new ArrayList<>();
        List<Card> remaining = new ArrayList<>(deck);
        deck.clear();
        for (Card c : remaining) {
            if (picked.size() < 2 && c.getType() == CardType.PEZ) {
                picked.add(c);
            } else {
                deck.push(c);
            }
        }
        int placed = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            if (picked.isEmpty()) break;
            BoardSlot adj = board[idx];
            if (adj.getCard() == null || !adj.isFaceUp()) {
                adj.setCard(picked.remove(0));
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                adj.clearDice();
                placed++;
            }
        }
        for (Card c : picked) deck.push(c);
        return placed == 0 ? "" : "Arenque sembró " + placed + " pez(es) pequeño(s).";
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
            case LANGOSTA_ESPINOSA:
                return recoverIfD4Used(slotIndex);
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
            case PERCEBES:
                return spreadDiceToAdjacents(slotIndex);
            case CABALLITO_DE_MAR:
                return recoverSpecificDie(DieType.D4);
            default:
                return "";
        }
    }

    private String recoverIfD4Used(int slotIndex) {
        boolean usedD4 = false;
        for (Die d : board[slotIndex].getDice()) {
            if (d.getType() == DieType.D4) {
                usedD4 = true;
                break;
            }
        }
        if (usedD4 && !lostDice.isEmpty()) {
            Die recovered = lostDice.remove(lostDice.size() - 1);
            reserve.add(recovered.getType());
            return "Langosta espinosa te devuelve un dado perdido.";
        }
        return "";
    }

    private String spreadDiceToAdjacents(int slotIndex) {
        List<Die> dice = new ArrayList<>(board[slotIndex].getDice());
        int moved = 0;
        for (Die d : dice) {
            boolean placed = false;
            for (Integer idx : adjacentIndices(slotIndex, true)) {
                if (board[idx].getCard() != null && board[idx].getDice().size() < 2) {
                    board[idx].addDie(d);
                    placed = true;
                    moved++;
                    break;
                }
            }
            if (!placed) {
                reserve.add(d.getType());
            }
        }
        return moved == 0 ? "" : "Percebes movió " + moved + " dado(s) a cartas adyacentes.";
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

    private void releaseHighestCapture() {
        if (captures.isEmpty()) return;
        Card highest = captures.get(0);
        for (Card c : captures) {
            if (c.getPoints() > highest.getPoints()) {
                highest = c;
            }
        }
        captures.remove(highest);
    }
}
