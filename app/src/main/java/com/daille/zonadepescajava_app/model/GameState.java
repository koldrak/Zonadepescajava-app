package com.daille.zonadepescajava_app.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
    private boolean awaitingLanternChoice = false;
    private int lanternOriginSlot = -1;
    private boolean awaitingAtunDecision = false;
    private int atunSlotIndex = -1;
    private int atunDieIndex = -1;
    private boolean awaitingPezVelaDecision = false;
    private boolean awaitingPezVelaResultChoice = false;
    private int pezVelaSlotIndex = -1;
    private int pezVelaDieIndex = -1;
    private Die pezVelaOriginalDie = null;
    private Die pezVelaRerolledDie = null;
    private final List<Die> pendingPercebesDice = new ArrayList<>();
    private final List<Integer> pendingPercebesTargets = new ArrayList<>();
    private final List<Die> pendingBallenaDice = new ArrayList<>();
    private final List<Card> pendingArenquePool = new ArrayList<>();
    private final List<Card> pendingArenqueChosen = new ArrayList<>();
    private int arenqueSlotIndex = -1;
    private enum PendingSelection {
        NONE,
        RED_CRAB_FROM,
        RED_CRAB_TO,
        BLUE_CRAB,
        NAUTILUS_FIRST,
        NAUTILUS_SECOND,
        GHOST_TARGET,
        SALMON_FLIP,
        CLOWNFISH_PROTECT,
        PERCEBES_MOVE,
        ATUN_DESTINATION,
        MORENA_FROM,
        MORENA_TO,
        ARENQUE_DESTINATION,
        BLUE_WHALE_PLACE
    }
    private PendingSelection pendingSelection = PendingSelection.NONE;
    private int pendingSelectionActor = -1;
    private int pendingSelectionAux = -1;

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
        awaitingLanternChoice = false;
        lanternOriginSlot = -1;
        pendingSelection = PendingSelection.NONE;
        pendingSelectionActor = -1;
        pendingSelectionAux = -1;
        awaitingAtunDecision = false;
        atunSlotIndex = -1;
        atunDieIndex = -1;
        clearPezVelaState();
        pendingPercebesDice.clear();
        pendingPercebesTargets.clear();
        awaitingPezVelaDecision = false;
        awaitingPezVelaResultChoice = false;
        pezVelaSlotIndex = -1;
        pezVelaDieIndex = -1;
        pezVelaOriginalDie = null;
        pezVelaRerolledDie = null;
        pendingBallenaDice.clear();
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        arenqueSlotIndex = -1;

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

    public boolean isAwaitingLanternChoice() {
        return awaitingLanternChoice;
    }

    public boolean isAwaitingBoardSelection() {
        return pendingSelection != PendingSelection.NONE;
    }

    public boolean isAwaitingAtunDecision() {
        return awaitingAtunDecision;
    }

    public boolean isAwaitingPezVelaDecision() {
        return awaitingPezVelaDecision;
    }

    public boolean isAwaitingPezVelaResultChoice() {
        return awaitingPezVelaResultChoice;
    }

    public boolean isAwaitingArenqueChoice() {
        return awaitingArenqueChoice;
    }

    public Die getPezVelaOriginalDie() {
        return pezVelaOriginalDie;
    }

    public Die getPezVelaRerolledDie() {
        return pezVelaRerolledDie;
    }

    public List<String> getPendingArenqueNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingArenquePool) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Integer> getHighlightSlots() {
        List<Integer> highlight = new ArrayList<>();
        if (pendingSelection == PendingSelection.NONE) {
            if (awaitingLanternChoice) {
                for (int i = 0; i < board.length; i++) {
                    if (board[i].getCard() != null && !board[i].isFaceUp()) {
                        highlight.add(i);
                    }
                }
            } else if (forcedSlotIndex != null) {
                highlight.add(forcedSlotIndex);
            }
            return highlight;
        }

        switch (pendingSelection) {
            case RED_CRAB_FROM:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (!board[idx].getDice().isEmpty()) {
                        highlight.add(idx);
                    }
                }
                break;
            case RED_CRAB_TO:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (idx != pendingSelectionAux && board[idx].getCard() != null && board[idx].getDice().size() < 2) {
                        highlight.add(idx);
                    }
                }
                break;
            case BLUE_CRAB:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) highlight.add(i);
                }
                break;
            case MORENA_FROM:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (!board[idx].getDice().isEmpty()) {
                        highlight.add(idx);
                    }
                }
                break;
            case MORENA_TO:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (idx != pendingSelectionAux && board[idx].getCard() != null
                            && board[idx].getDice().size() < 2) {
                        highlight.add(idx);
                    }
                }
                break;
            case NAUTILUS_FIRST:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) highlight.add(i);
                }
                break;
            case NAUTILUS_SECOND:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty() && i != pendingSelectionAux) highlight.add(i);
                }
                break;
            case GHOST_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case SALMON_FLIP:
                for (int i = 0; i < board.length; i++) {
                    if (board[i].getCard() != null && !board[i].isFaceUp()) {
                        highlight.add(i);
                    }
                }
                break;
            case CLOWNFISH_PROTECT:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].isFaceUp()
                            && board[idx].getCard().getType() != CardType.OBJETO) {
                        highlight.add(idx);
                    }
                }
                break;
            case PERCEBES_MOVE:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].getDice().size() < 2
                            && !pendingPercebesTargets.contains(idx)) {
                        highlight.add(idx);
                    }
                }
                break;
            case ATUN_DESTINATION:
                for (int i = 0; i < board.length; i++) {
                    if (board[i].getCard() != null && board[i].getDice().size() < 2) {
                        highlight.add(i);
                    }
                }
                break;
            case ARENQUE_DESTINATION:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot target = board[idx];
                    if (target.getDice().isEmpty() && (target.getCard() == null || !target.isFaceUp())) {
                        highlight.add(idx);
                    }
                }
                break;
            case BLUE_WHALE_PLACE:
                for (int i = 0; i < board.length; i++) {
                    if (board[i].getCard() != null && board[i].getDice().size() < 2) {
                        highlight.add(i);
                    }
                }
                break;
            case NONE:
            default:
                break;
        }
        return highlight;
    }

    public List<Die> getPendingDiceChoices() {
        if (pendingDieLossSlot == null) {
            return java.util.Collections.emptyList();
        }
        return new ArrayList<>(board[pendingDieLossSlot].getDice());
    }

    public String chooseLanternTarget(int slotIndex) {
        if (!awaitingLanternChoice) {
            return "No hay selección pendiente del Pez Linterna.";
        }
        if (slotIndex < 0 || slotIndex >= board.length) {
            return "Selecciona una casilla válida.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) {
            return "Debes elegir una carta boca abajo.";
        }

        target.setFaceUp(true);
        StringBuilder log = new StringBuilder("Pez Linterna reveló " + target.getCard().getName());
        BoardSlot origin = lanternOriginSlot >= 0 && lanternOriginSlot < board.length ? board[lanternOriginSlot] : null;
        if (origin != null && origin.getDice().size() > 0) {
            if (target.getCard().getType() == CardType.PEZ_GRANDE && target.getDice().size() < 2) {
                Die moved = origin.removeDie(origin.getDice().size() - 1);
                target.addDie(moved);
                log.append(" y movió el dado a ese pez grande.");
            } else if (target.getCard().getType() == CardType.OBJETO) {
                Die lost = origin.removeDie(origin.getDice().size() - 1);
                lostDice.add(lost);
                log.append(", era un objeto: el dado se pierde.");
            }
        }
        awaitingLanternChoice = false;
        lanternOriginSlot = -1;
        String chained = handleOnReveal(slotIndex, 0);
        if (!chained.isEmpty()) {
            log.append(" ").append(chained);
        }
        recomputeBottleAdjustments();
        return log.toString();
    }

    public String handleBoardSelection(int slotIndex) {
        if (!isAwaitingBoardSelection()) {
            return "No hay acciones pendientes.";
        }
        if (slotIndex < 0 || slotIndex >= board.length) {
            return "Selecciona una casilla válida.";
        }

        String result;
        switch (pendingSelection) {
            case RED_CRAB_FROM:
                result = chooseRedCrabOrigin(slotIndex);
                break;
            case RED_CRAB_TO:
                result = chooseRedCrabDestination(slotIndex);
                break;
            case BLUE_CRAB:
                result = adjustSelectedDie(slotIndex);
                break;
            case NAUTILUS_FIRST:
            case NAUTILUS_SECOND:
                result = retuneChosenDie(slotIndex);
                break;
            case GHOST_TARGET:
                result = hideChosenAdjacent(slotIndex);
                break;
            case SALMON_FLIP:
                result = flipChosenFaceDown(slotIndex);
                break;
            case CLOWNFISH_PROTECT:
                result = protectChosenCard(slotIndex);
                break;
            case PERCEBES_MOVE:
                result = movePercebesDie(slotIndex);
                break;
            case ATUN_DESTINATION:
                result = repositionAtunDie(slotIndex);
                break;
            case MORENA_FROM:
                result = chooseMorenaOrigin(slotIndex);
                break;
            case MORENA_TO:
                result = chooseMorenaDestination(slotIndex);
                break;
            case ARENQUE_DESTINATION:
                result = placeArenqueFish(slotIndex);
                break;
            case BLUE_WHALE_PLACE:
                result = placeBlueWhaleDie(slotIndex);
                break;
            default:
                result = "No hay acciones pendientes.";
        }
        return result;
    }

    public String chooseAtunReroll(boolean reroll) {
        if (!awaitingAtunDecision) {
            return "No hay decisión pendiente del Atún.";
        }
        if (atunSlotIndex < 0 || atunSlotIndex >= board.length) {
            clearAtunState();
            return "El Atún ya no está disponible.";
        }
        BoardSlot slot = board[atunSlotIndex];
        if (slot.getDice().isEmpty() || atunDieIndex < 0 || atunDieIndex >= slot.getDice().size()) {
            clearAtunState();
            return "No hay dado para reposicionar.";
        }
        Die current = slot.getDice().get(atunDieIndex);
        Die finalDie = current;
        if (reroll) {
            finalDie = Die.roll(current.getType(), rng);
        }
        slot.setDie(atunDieIndex, finalDie);
        awaitingAtunDecision = false;
        pendingSelection = PendingSelection.ATUN_DESTINATION;
        pendingSelectionActor = atunSlotIndex;
        return (reroll ? "Atún relanzó el dado " : "Atún conserva el dado ")
                + "(" + finalDie.getLabel() + "). Elige una carta para reposicionarlo.";
    }

    public String choosePezVelaReroll(boolean reroll) {
        if (!awaitingPezVelaDecision) {
            return "No hay decisión pendiente del Pez Vela.";
        }
        if (pezVelaSlotIndex < 0 || pezVelaSlotIndex >= board.length) {
            clearPezVelaState();
            return "El Pez Vela ya no está disponible.";
        }
        BoardSlot slot = board[pezVelaSlotIndex];
        if (pezVelaDieIndex < 0 || pezVelaDieIndex >= slot.getDice().size()) {
            clearPezVelaState();
            return "El dado del Pez Vela ya no está disponible.";
        }
        if (!reroll) {
            Die current = slot.getDice().get(pezVelaDieIndex);
            clearPezVelaState();
            return "Pez vela conserva el " + current.getLabel() + ".";
        }
        pezVelaRerolledDie = Die.roll(slot.getDice().get(pezVelaDieIndex).getType(), rng);
        awaitingPezVelaDecision = false;
        awaitingPezVelaResultChoice = true;
        return "Pez vela obtuvo " + pezVelaRerolledDie.getLabel() + ". Elige qué resultado conservar.";
    }

    public String choosePezVelaResult(boolean useReroll) {
        if (!awaitingPezVelaResultChoice) {
            return "No hay elección pendiente del Pez Vela.";
        }
        if (pezVelaSlotIndex < 0 || pezVelaSlotIndex >= board.length) {
            clearPezVelaState();
            return "El Pez Vela ya no está disponible.";
        }
        BoardSlot slot = board[pezVelaSlotIndex];
        if (pezVelaDieIndex < 0 || pezVelaDieIndex >= slot.getDice().size()) {
            clearPezVelaState();
            return "El dado del Pez Vela ya no está disponible.";
        }
        Die chosen = useReroll && pezVelaRerolledDie != null ? pezVelaRerolledDie : pezVelaOriginalDie;
        if (chosen == null) {
            clearPezVelaState();
            return "No se pudo conservar ningún resultado.";
        }
        slot.setDie(pezVelaDieIndex, chosen);
        String msg = "Pez vela conserva el resultado " + chosen.getLabel() + ".";
        clearPezVelaState();
        return msg;
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
        recomputeBottleAdjustments();

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
        if (awaitingAtunDecision) {
            return "Decide primero si relanzar el dado del Atún.";
        }
        if (awaitingPezVelaDecision || awaitingPezVelaResultChoice) {
            return "Resuelve la decisión del Pez Vela antes de lanzar otro dado.";
        }
        if (awaitingLanternChoice) {
            return "Resuelve la selección del Pez Linterna antes de lanzar otro dado.";
        }
        if (awaitingArenqueChoice) {
            return "Selecciona primero los peces pequeños del Arenque.";
        }
        if (pendingSelection == PendingSelection.BLUE_WHALE_PLACE && !pendingBallenaDice.isEmpty()) {
            return "Coloca los dados pendientes de la Ballena azul antes de continuar.";
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
        if (awaitingAtunDecision) {
            return "Resuelve primero la habilidad del Atún.";
        }
        if (awaitingPezVelaDecision || awaitingPezVelaResultChoice) {
            return "Resuelve primero la habilidad del Pez Vela.";
        }
        if (awaitingLanternChoice) {
            return "Debes elegir primero qué carta revelar con el Pez Linterna.";
        }
        if (isAwaitingBoardSelection()) {
            return "Resuelve la acción pendiente antes de colocar otro dado.";
        }
        if (awaitingArenqueChoice) {
            return "Selecciona los peces pequeños del Arenque antes de colocar otros dados.";
        }
        if (pendingSelection == PendingSelection.BLUE_WHALE_PLACE && !pendingBallenaDice.isEmpty()) {
            return "Primero coloca los dados pendientes de la Ballena azul.";
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
            markHookPenaltyUsed();
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

    private String addDieToSlot(int slotIndex, Die die) {
        BoardSlot target = board[slotIndex];
        target.addDie(die);
        if (target.getCard() == null || target.isFaceUp()) {
            return "";
        }
        target.setFaceUp(true);
        String reveal = handleOnReveal(slotIndex, die.getValue());
        recomputeBottleAdjustments();
        return reveal == null ? "" : reveal;
    }

    private String capture(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        List<Die> diceOnCard = new ArrayList<>(slot.getDice());
        captures.add(slot.getCard());
        String captureLog = handleOnCapture(slotIndex, diceOnCard);
        for (Die d : new ArrayList<>(slot.getDice())) {
            reserve.add(d.getType());
        }
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        recomputeBottleAdjustments();
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
        recomputeBottleAdjustments();
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
        recomputeBottleAdjustments();
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
                                List<Die> dice = new ArrayList<>(src.getDice());
                                if (!dice.isEmpty()) {
                                    lostFromBoard.add(dice.remove(0));
                                    for (Die remaining : dice) {
                                        reserve.add(remaining.getType());
                                    }
                                }
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
        recomputeBottleAdjustments();
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

    private boolean hasFaceDownCards() {
        for (BoardSlot s : board) {
            if (s.getCard() != null && !s.isFaceUp()) {
                return true;
            }
        }
        return false;
    }

    private boolean isHookActive() {
        for (BoardSlot s : board) {
            if (s.isFaceUp() && s.getCard() != null && s.getCard().getId() == CardId.ANZUELO_ROTO && !s.getStatus().hookPenaltyUsed) {
                return true;
            }
        }
        return false;
    }

    private void markHookPenaltyUsed() {
        for (BoardSlot s : board) {
            if (s.isFaceUp() && s.getCard() != null && s.getCard().getId() == CardId.ANZUELO_ROTO && !s.getStatus().hookPenaltyUsed) {
                s.getStatus().hookPenaltyUsed = true;
                break;
            }
        }
    }

    private void clearPendingSelection() {
        pendingSelection = PendingSelection.NONE;
        pendingSelectionActor = -1;
        pendingSelectionAux = -1;
        clearPercebesState();
        clearBallenaState();
    }

    private void clearPercebesState() {
        pendingPercebesDice.clear();
        pendingPercebesTargets.clear();
    }

    private void clearBallenaState() {
        pendingBallenaDice.clear();
    }

    private void clearArenqueState() {
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        awaitingArenqueChoice = false;
        arenqueSlotIndex = -1;
    }

    private void clearAtunState() {
        awaitingAtunDecision = false;
        atunSlotIndex = -1;
        atunDieIndex = -1;
    }

    private void clearPezVelaState() {
        awaitingPezVelaDecision = false;
        awaitingPezVelaResultChoice = false;
        pezVelaSlotIndex = -1;
        pezVelaDieIndex = -1;
        pezVelaOriginalDie = null;
        pezVelaRerolledDie = null;
    }

    private String revealAndTrigger(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= board.length) return "";
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) return "";
        target.setFaceUp(true);
        String result = handleOnReveal(slotIndex, 0);
        return result;
    }

    private String handleOnReveal(int slotIndex, int placedValue) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) return "";
        String result;
        switch (slot.getCard().getId()) {
            case CANGREJO_ROJO:
                pendingSelection = PendingSelection.RED_CRAB_FROM;
                pendingSelectionActor = slotIndex;
                result = "Cangrejo rojo: elige un dado adyacente para mover.";
                break;
            case JAIBA_AZUL:
                pendingSelection = PendingSelection.BLUE_CRAB;
                pendingSelectionActor = slotIndex;
                result = "Jaiba azul: elige un dado para ajustar ±1.";
                break;
            case CAMARON_FANTASMA:
                result = peekAdjacentCards(slotIndex);
                break;
            case ATUN:
                result = startAtunDecision(slotIndex);
                break;
            case PEZ_GLOBO:
                result = inflateAnyFishDie();
                break;
            case MORENA:
                result = moveMorenaDie(slotIndex);
                break;
            case CANGREJO_ERMITANO:
                result = replaceAdjacentObject(slotIndex);
                break;
            case CENTOLLA:
                forcedSlotIndex = slotIndex;
                result = "Centolla atrae el próximo dado a esta carta.";
                break;
            case NAUTILUS:
                pendingSelection = PendingSelection.NAUTILUS_FIRST;
                pendingSelectionActor = slotIndex;
                result = "Nautilus: elige un dado para ajustar ±2.";
                break;
            case CANGREJO_ARANA:
                result = reviveFailedCard();
                break;
            case BOTELLA_PLASTICO:
                recomputeBottleAdjustments();
                result = "Botella: los peces adyacentes requieren +3 a la suma.";
                break;
            case BOTA_VIEJA:
                result = "Bota vieja: −1 a la suma de adyacentes.";
                break;
            case CORRIENTES_PROFUNDAS:
                result = "Corrientes profundas listas: si igualas su dado, activas marea lateral.";
                break;
            case PEZ_PAYASO:
                result = startClownfishProtection(slotIndex);
                break;
            case PEZ_LINTERNA:
                result = startLanternSelection(slotIndex);
                break;
            case KOI:
                result = swapDieWithFaceUpSingle(slotIndex);
                break;
            case PEZ_VELA:
                result = startPezVelaDecision(slotIndex);
                break;
            case PIRANA:
                result = biteAdjacentSmallFish(slotIndex);
                break;
            case PEZ_FANTASMA:
                pendingSelection = PendingSelection.GHOST_TARGET;
                pendingSelectionActor = slotIndex;
                result = "Pez fantasma: elige una carta adyacente boca arriba para ocultar.";
                break;
            case PULPO:
                result = replacePulpoIfEven(slotIndex, placedValue);
                break;
            case CALAMAR_GIGANTE:
                result = flipAdjacentFaceUpCardsDown(slotIndex);
                break;
            case MANTA_GIGANTE:
                result = recoverSpecificDie(DieType.D8);
                break;
            case ARENQUE:
                result = startArenqueSelection(slotIndex);
                break;
            case REMORA:
                result = attachToBigFish(slotIndex);
                break;
            case BALLENA_AZUL:
                result = startBlueWhaleReposition(slotIndex);
                break;
            case TIBURON_BLANCO:
                result = devourAdjacentFaceUp(slotIndex);
                break;
            case MERO_GIGANTE:
                result = flipAdjacentCardsDown(slotIndex);
                recomputeBottleAdjustments();
                break;
            case PEZ_LUNA:
                result = "Si la marea lo expulsa, liberarás tu captura de mayor valor.";
                break;
            default:
                result = "";
                break;
        }
        if (slot.getCard().getType() == CardType.PEZ) {
            String clams = triggerAdjacentClams(slotIndex);
            if (!clams.isEmpty()) {
                result = result.isEmpty() ? clams : result + " " + clams;
            }
        }
        return result;
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
        String reveal = addDieToSlot(to, moved);
        return reveal.isEmpty()
                ? "Cangrejo rojo movió un dado entre cartas adyacentes."
                : "Cangrejo rojo movió un dado entre cartas adyacentes. " + reveal;
    }

    private boolean isAdjacentToActor(int slotIndex) {
        return pendingSelectionActor >= 0 && adjacentIndices(pendingSelectionActor, true).contains(slotIndex);
    }

    private String chooseRedCrabOrigin(int slotIndex) {
        if (!isAdjacentToActor(slotIndex) || board[slotIndex].getDice().isEmpty()) {
            return "Elige una carta adyacente con dado para mover.";
        }
        pendingSelectionAux = slotIndex;
        pendingSelection = PendingSelection.RED_CRAB_TO;
        return "Selecciona la carta adyacente destino (máx. 2 dados).";
    }

    private String chooseRedCrabDestination(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Debes elegir otra carta adyacente al cangrejo.";
        }
        if (slotIndex == pendingSelectionAux) {
            return "Debes elegir una carta distinta como destino.";
        }
        BoardSlot origin = board[pendingSelectionAux];
        BoardSlot target = board[slotIndex];
        if (target.getDice().size() >= 2) {
            return "El destino ya tiene 2 dados.";
        }
        if (origin.getDice().isEmpty()) {
            clearPendingSelection();
            return "No hay dados para mover.";
        }
        Die moved = origin.removeDie(origin.getDice().size() - 1);
        String reveal = addDieToSlot(slotIndex, moved);
        clearPendingSelection();
        return reveal.isEmpty()
                ? "Cangrejo rojo movió un dado entre cartas adyacentes."
                : "Cangrejo rojo movió un dado entre cartas adyacentes. " + reveal;
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

    private String adjustSelectedDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "Elige una carta con un dado para ajustar.";
        }
        int idx = slot.getDice().size() - 1;
        Die die = slot.getDice().get(idx);
        int value = die.getValue();
        int sides = die.getType().getSides();
        if (value + 1 <= sides) {
            value += 1;
        } else if (value - 1 >= 1) {
            value -= 1;
        }
        slot.setDie(idx, new Die(die.getType(), value));
        clearPendingSelection();
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

    private String startAtunDecision(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "";
        awaitingAtunDecision = true;
        atunSlotIndex = slotIndex;
        atunDieIndex = slot.getDice().size() - 1;
        return "Atún: decide si relanzar el dado y elige destino.";
    }

    private String startPezVelaDecision(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) return "";
        awaitingPezVelaDecision = true;
        pezVelaSlotIndex = slotIndex;
        pezVelaDieIndex = slot.getDice().size() - 1;
        pezVelaOriginalDie = slot.getDice().get(pezVelaDieIndex);
        pezVelaRerolledDie = null;
        return "Pez vela: decide si quieres relanzar el dado.";
    }

    private String repositionAtunDie(int slotIndex) {
        if (atunSlotIndex < 0 || atunSlotIndex >= board.length) {
            clearPendingSelection();
            clearAtunState();
            return "El dado del Atún ya no está disponible.";
        }
        BoardSlot origin = board[atunSlotIndex];
        if (origin.getDice().isEmpty() || atunDieIndex < 0 || atunDieIndex >= origin.getDice().size()) {
            clearPendingSelection();
            clearAtunState();
            return "El dado del Atún ya no está disponible.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) {
            return "Elige una carta válida para reposicionar el dado.";
        }
        if (target.getDice().size() >= 2) {
            return "La carta seleccionada ya tiene 2 dados.";
        }
        Die moved = origin.removeDie(atunDieIndex);
        String reveal = addDieToSlot(slotIndex, moved);
        clearPendingSelection();
        clearAtunState();
        return reveal.isEmpty()
                ? "Atún reposicionó el dado en la carta elegida."
                : "Atún reposicionó el dado en la carta elegida. " + reveal;
    }

    private String inflateAnyFishDie() {
        for (BoardSlot s : board) {
            if (s.getCard() == null) continue;
            if (s.getCard().getType() == CardType.PEZ || s.getCard().getType() == CardType.PEZ_GRANDE) {
                for (int i = 0; i < s.getDice().size(); i++) {
                    Die d = s.getDice().get(i);
                    int max = d.getType().getSides();
                    if (d.getValue() < max) {
                        s.setDie(i, new Die(d.getType(), max));
                        return "Pez globo infló un dado a " + max + ".";
                    }
                }
            }
        }
        return "";
    }

    private String moveMorenaDie(int slotIndex) {
        boolean hasOrigin = false, hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            if (!board[idx].getDice().isEmpty()) hasOrigin = true;
            if (board[idx].getCard() != null && board[idx].getDice().size() < 2) hasTarget = true;
        }
        if (!hasOrigin || !hasTarget) {
            return "No hay movimientos válidos para la morena.";
        }
        pendingSelection = PendingSelection.MORENA_FROM;
        pendingSelectionActor = slotIndex;
        return "Morena: elige una carta adyacente con dado para mover.";
    }

    private String chooseMorenaOrigin(int slotIndex) {
        if (!isAdjacentToActor(slotIndex) || board[slotIndex].getDice().isEmpty()) {
            return "Elige una carta adyacente con dado.";
        }
        pendingSelectionAux = slotIndex;
        pendingSelection = PendingSelection.MORENA_TO;
        return "Selecciona la carta adyacente destino para el dado.";
    }

    private String chooseMorenaDestination(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "La carta destino debe ser adyacente a la morena.";
        }
        if (slotIndex == pendingSelectionAux) {
            return "Elige una carta distinta como destino.";
        }
        BoardSlot origin = board[pendingSelectionAux];
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.getDice().size() >= 2) {
            return "La carta destino no puede recibir más dados.";
        }
        if (origin.getDice().isEmpty()) {
            clearPendingSelection();
            return "No hay dados para mover.";
        }
        Die moved = origin.removeDie(origin.getDice().size() - 1);
        String reveal = addDieToSlot(slotIndex, moved);
        clearPendingSelection();
        return reveal.isEmpty() ? "Morena movió un dado entre cartas adyacentes." :
                "Morena movió un dado entre cartas adyacentes. " + reveal;
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
                recomputeBottleAdjustments();
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

    private String retuneChosenDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "Elige una carta con dado para ajustar.";
        }
        int idx = slot.getDice().size() - 1;
        Die d = slot.getDice().get(idx);
        int sides = d.getType().getSides();
        int newVal = d.getValue();
        if (newVal + 2 <= sides) {
            newVal += 2;
        } else if (newVal - 2 >= 1) {
            newVal -= 2;
        }
        slot.setDie(idx, new Die(d.getType(), newVal));

        if (pendingSelection == PendingSelection.NAUTILUS_FIRST) {
            pendingSelection = PendingSelection.NAUTILUS_SECOND;
            pendingSelectionAux = slotIndex;
            return "Nautilus ajustó un dado a " + newVal + ". Elige un segundo dado.";
        }
        clearPendingSelection();
        return "Nautilus ajustó un dado a " + newVal + ".";
    }

    private String startClownfishProtection(int slotIndex) {
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() != CardType.OBJETO) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "No hay cartas boca arriba adyacentes para proteger.";
        }
        pendingSelection = PendingSelection.CLOWNFISH_PROTECT;
        pendingSelectionActor = slotIndex;
        return "Pez payaso: elige una carta adyacente boca arriba para proteger.";
    }

    private String protectChosenCard(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige una carta adyacente al pez payaso.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || !target.isFaceUp()) {
            return "La carta seleccionada debe estar boca arriba.";
        }
        if (target.getCard().getType() == CardType.OBJETO) {
            return "No puedes proteger objetos.";
        }
        target.getStatus().protectedOnce = true;
        clearPendingSelection();
        return "Pez payaso protege la carta seleccionada.";
    }

    private String swapDieWithFaceUpSingle(int slotIndex) {
        BoardSlot origin = board[slotIndex];
        if (origin.getDice().isEmpty()) return "";
        for (BoardSlot target : board) {
            if (target == origin) continue;
            if (target.getCard() != null && target.isFaceUp() && target.getDice().size() == 1) {
                Die fromOrigin = origin.removeDie(origin.getDice().size() - 1);
                Die targetDie = target.removeDie(0);
                origin.addDie(targetDie);
                target.addDie(fromOrigin);
                return "Koi intercambió un dado con otra carta.";
            }
        }
        return "";
    }

    private String startLanternSelection(int slotIndex) {
        if (!hasFaceDownCards()) {
            return "No hay cartas boca abajo que revelar.";
        }
        awaitingLanternChoice = true;
        lanternOriginSlot = slotIndex;
        return "Pez Linterna: elige una carta boca abajo para revelar.";
    }

    private String biteAdjacentSmallFish(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.PEZ) {
                Card removed = adj.getCard();
                List<Die> preservedDice = new ArrayList<>(adj.getDice());
                failedDiscards.add(removed);
                adj.clearDice();
                adj.setCard(deck.isEmpty() ? null : deck.pop());
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                StringBuilder extra = new StringBuilder();
                for (Die d : preservedDice) {
                    if (adj.getDice().size() < 2) {
                        String reveal = addDieToSlot(idx, d);
                        if (!reveal.isEmpty()) {
                            if (extra.length() > 0) extra.append(" ");
                            extra.append(reveal);
                        }
                    } else {
                        reserve.add(d.getType());
                    }
                }
                String base = "Piraña descartó a " + removed.getName() + " sin perder sus dados.";
                return extra.length() == 0 ? base : base + " " + extra;
            }
        }
        return "";
    }

    private String hideAdjacentFaceUp(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                for (Die d : new ArrayList<>(adj.getDice())) {
                    reserve.add(d.getType());
                }
                adj.clearDice();
                adj.setFaceUp(false);
                return "Pez fantasma ocultó una carta y recuperó sus dados.";
            }
        }
        return "";
    }

    private String hideChosenAdjacent(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige una carta adyacente al pez fantasma.";
        }
        BoardSlot adj = board[slotIndex];
        if (adj.getCard() == null || !adj.isFaceUp()) {
            return "La carta elegida debe estar boca arriba.";
        }
        for (Die d : new ArrayList<>(adj.getDice())) {
            reserve.add(d.getType());
        }
        adj.clearDice();
        adj.setFaceUp(false);
        adj.setStatus(new SlotStatus());
        recomputeBottleAdjustments();
        clearPendingSelection();
        return "Pez fantasma ocultó una carta y recuperó sus dados.";
    }

    private String replacePulpoIfEven(int slotIndex, int placedValue) {
        if (placedValue % 2 != 0) return "";
        List<Card> remaining = new ArrayList<>(deck);
        deck.clear();
        Card replacement = null;
        for (Card c : remaining) {
            if (c.getType() != CardType.OBJETO) {
                replacement = c;
                break;
            } else {
                deck.push(c);
            }
        }
        if (replacement == null) {
            deck.addAll(remaining);
            return "";
        }
        for (Card c : remaining) {
            if (c != replacement && !deck.contains(c)) deck.push(c);
        }
        BoardSlot slot = board[slotIndex];
        slot.setCard(replacement);
        slot.setFaceUp(true);
        String reveal = handleOnReveal(slotIndex, placedValue);
        return reveal.isEmpty()
                ? "Pulpo fue reemplazado por " + replacement.getName() + "."
                : "Pulpo fue reemplazado por " + replacement.getName() + ". " + reveal;
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
        if (flipped > 0) {
            recomputeBottleAdjustments();
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

    private String startArenqueSelection(int slotIndex) {
        List<Card> remaining = new ArrayList<>(deck);
        deck.clear();
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        arenqueSlotIndex = slotIndex;
        for (Card c : remaining) {
            if (c.getType() == CardType.PEZ) {
                pendingArenquePool.add(c);
            } else {
                deck.push(c);
            }
        }
        if (pendingArenquePool.isEmpty()) {
            deck.addAll(remaining);
            arenqueSlotIndex = -1;
            return "No hay peces pequeños en el mazo.";
        }
        awaitingArenqueChoice = true;
        return "Arenque: elige hasta 2 peces pequeños del mazo.";
    }

    public String chooseArenqueFish(List<Integer> indices) {
        if (!awaitingArenqueChoice) {
            return "No hay selección de Arenque pendiente.";
        }
        awaitingArenqueChoice = false;
        pendingArenqueChosen.clear();
        List<Integer> unique = new ArrayList<>();
        for (Integer i : indices) {
            if (i == null) continue;
            if (i < 0 || i >= pendingArenquePool.size()) continue;
            if (unique.contains(i)) continue;
            unique.add(i);
            if (unique.size() >= 2) break;
        }
        for (int idx : unique) {
            pendingArenqueChosen.add(pendingArenquePool.get(idx));
        }
        for (int i = 0; i < pendingArenquePool.size(); i++) {
            if (!unique.contains(i)) {
                deck.push(pendingArenquePool.get(i));
            }
        }
        pendingArenquePool.clear();
        shuffleDeck();
        if (pendingArenqueChosen.isEmpty()) {
            clearArenqueState();
            return "No se eligieron peces pequeños.";
        }
        pendingSelection = PendingSelection.ARENQUE_DESTINATION;
        pendingSelectionActor = arenqueSlotIndex;
        return pendingArenqueChosen.size() == 1
                ? "Coloca el pez pequeño elegido adyacente al Arenque."
                : "Coloca los 2 peces pequeños adyacentes al Arenque.";
    }

    private String placeArenqueFish(int slotIndex) {
        if (pendingArenqueChosen.isEmpty()) {
            clearPendingSelection();
            clearArenqueState();
            return "No hay peces pequeños por colocar.";
        }
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return "Debes elegir una casilla adyacente al Arenque.";
        }
        BoardSlot target = board[slotIndex];
        if (!target.getDice().isEmpty()) {
            return "Debes elegir una casilla sin dados.";
        }
        if (target.getCard() != null && target.isFaceUp()) {
            return "Solo puedes reemplazar cartas boca abajo o espacios vacíos.";
        }
        if (target.getCard() != null) {
            deck.push(target.getCard());
        }
        Card placing = pendingArenqueChosen.remove(0);
        target.setCard(placing);
        target.setFaceUp(false);
        target.setStatus(new SlotStatus());
        target.clearDice();
        recomputeBottleAdjustments();
        if (pendingArenqueChosen.isEmpty()) {
            clearPendingSelection();
            clearArenqueState();
            shuffleDeck();
            return "Arenque colocó todos los peces pequeños y barajó el mazo.";
        }
        return "Pez pequeño colocado. Elige otra casilla adyacente.";
    }

    private void recomputeBottleAdjustments() {
        for (BoardSlot slot : board) {
            slot.getStatus().sumConditionShift = 0;
        }
        for (int i = 0; i < board.length; i++) {
            BoardSlot slot = board[i];
            if (slot.getCard() == null || !slot.isFaceUp()) continue;
            if (slot.getCard().getId() != CardId.BOTELLA_PLASTICO) continue;
            for (Integer adjIndex : adjacentIndices(i, true)) {
                BoardSlot adj = board[adjIndex];
                if (adj.getCard() != null && adj.getCard().getType() == CardType.PEZ) {
                    adj.getStatus().sumConditionShift += 3;
                }
            }
        }
    }

    private void shuffleDeck() {
        List<Card> shuffledDeck = new ArrayList<>(deck);
        java.util.Collections.shuffle(shuffledDeck, rng);
        deck.clear();
        for (Card c : shuffledDeck) {
            deck.push(c);
        }
    }

    private String triggerAdjacentClams(int triggeredSlotIndex) {
        StringBuilder log = new StringBuilder();
        for (Integer idx : adjacentIndices(triggeredSlotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() == null || !adj.isFaceUp() || adj.getCard().getId() != CardId.ALMEJAS) {
                continue;
            }
            if (adj.getDice().size() >= 2 || lostDice.isEmpty()) {
                continue;
            }
            Die recovered = lostDice.remove(lostDice.size() - 1);
            Die rolled = Die.roll(recovered.getType(), rng);
            adj.addDie(rolled);
            if (log.length() > 0) log.append(" ");
            log.append("Almejas lanzó un ").append(rolled.getLabel()).append(".");
        }
        return log.toString();
    }

    private String attachToBigFish(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        List<Integer> candidates = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.PEZ_GRANDE) {
                candidates.add(idx);
            }
        }
        if (candidates.isEmpty()) {
            return "No hay peces grandes boca arriba adyacentes para la rémora.";
        }
        BoardSlot adj = board[candidates.get(0)];
        adj.getStatus().attachedRemoras.add(slot.getCard());
        for (Die d : new ArrayList<>(slot.getDice())) {
            reserve.add(d.getType());
        }
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        return "Rémora se adhirió a un pez grande.";
    }

    private String devourAdjacentFaceUp(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                List<Die> dice = new ArrayList<>(adj.getDice());
                failedDiscards.add(adj.getCard());
                adj.clearDice();
                adj.setCard(deck.isEmpty() ? null : deck.pop());
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                BoardSlot shark = board[slotIndex];
                StringBuilder reveal = new StringBuilder();
                for (Die d : dice) {
                    if (shark.getDice().size() < 2) {
                        String extra = addDieToSlot(slotIndex, d);
                        if (!extra.isEmpty()) {
                            if (reveal.length() > 0) reveal.append(" ");
                            reveal.append(extra);
                        }
                    } else {
                        reserve.add(d.getType());
                    }
                }
                recomputeBottleAdjustments();
                return reveal.length() == 0
                        ? "Tiburón blanco devoró una carta adyacente."
                        : "Tiburón blanco devoró una carta adyacente. " + reveal;
            }
        }
        return "";
    }

    private String flipAdjacentCardsDown(int slotIndex) {
        int flipped = 0;
        StringBuilder chain = new StringBuilder();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                String reveal = revealAndTrigger(idx);
                if (!reveal.isEmpty()) {
                    if (chain.length() > 0) chain.append(" ");
                    chain.append(reveal);
                }
                flipped++;
            }
        }
        String base = flipped == 0 ? "" : "Mero gigante volteó " + flipped + " carta(s).";
        if (chain.length() > 0) {
            base = base.isEmpty() ? chain.toString() : base + " " + chain;
        }
        return base;
    }

    private String handleOnCapture(int slotIndex, List<Die> diceOnCard) {
        Card captured = board[slotIndex].getCard();
        if (captured == null) return "";
        String remoraLog = collectAttachedRemoras(slotIndex);
        String result;
        switch (captured.getId()) {
            case LANGOSTA_ESPINOSA:
                result = recoverIfD4Used(slotIndex) + remoraLog;
                break;
            case RED_ENREDADA:
                captureAdjacentFaceDown(slotIndex);
                result = "Red enredada arrastra una carta adyacente." + remoraLog;
                break;
            case LATA_OXIDADA:
                if (!lostDice.isEmpty()) {
                    Die recovered = lostDice.remove(lostDice.size() - 1);
                    reserve.add(recovered.getType());
                    result = "Recuperaste un dado perdido." + remoraLog;
                } else {
                    result = remoraLog;
                }
                break;
            case PERCEBES:
                result = startPercebesMove(slotIndex) + remoraLog;
                break;
            case CABALLITO_DE_MAR:
                result = recoverSpecificDie(DieType.D4) + remoraLog;
                break;
            case SALMON:
                result = startSalmonFlip() + remoraLog;
                break;
            case PEZ_VOLADOR:
                result = flipLineFromFlyingFish(slotIndex, diceOnCard) + remoraLog;
                break;
            default:
                result = remoraLog;
                break;
        }
        if (captured.getType() == CardType.PEZ) {
            String clams = triggerAdjacentClams(slotIndex);
            if (!clams.isEmpty()) {
                result = result.isEmpty() ? clams : result + " " + clams;
            }
        }
        return result;
    }

    private String recoverIfD4Used(int slotIndex) {
        if (board[slotIndex].getStatus().langostaRecovered) {
            return "";
        }
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
            board[slotIndex].getStatus().langostaRecovered = true;
            return "Langosta espinosa te devuelve un dado perdido.";
        }
        return "";
    }

    private String startPercebesMove(int slotIndex) {
        List<Die> dice = new ArrayList<>(board[slotIndex].getDice());
        board[slotIndex].clearDice();
        clearPercebesState();
        if (dice.isEmpty()) return "";

        List<Integer> available = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            if (board[idx].getCard() != null && board[idx].getDice().size() < 2) {
                available.add(idx);
            }
        }

        if (available.size() < dice.size()) {
            for (Die d : dice) addDieToSlot(slotIndex, d);
            return "No hay suficientes cartas adyacentes con espacio para mover los dados.";
        }

        pendingPercebesDice.addAll(dice);
        pendingSelection = PendingSelection.PERCEBES_MOVE;
        pendingSelectionActor = slotIndex;
        return "Percebes: elige una carta adyacente para mover un dado.";
    }

    private String movePercebesDie(int slotIndex) {
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return "Elige una carta adyacente a Percebes.";
        }
        if (pendingPercebesTargets.contains(slotIndex)) {
            return "No puedes mover más de un dado a la misma carta.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) {
            return "Esa casilla no tiene carta.";
        }
        if (target.getDice().size() >= 2) {
            return "La carta seleccionada ya tiene 2 dados.";
        }
        if (pendingPercebesDice.isEmpty()) {
            clearPendingSelection();
            return "No hay dados por mover.";
        }
        Die moved = pendingPercebesDice.remove(0);
        String reveal = addDieToSlot(slotIndex, moved);
        pendingPercebesTargets.add(slotIndex);
        if (pendingPercebesDice.isEmpty()) {
            clearPendingSelection();
            return reveal.isEmpty()
                    ? "Percebes movió todos los dados a cartas adyacentes."
                    : "Percebes movió todos los dados a cartas adyacentes. " + reveal;
        }
        return reveal.isEmpty()
                ? "Percebes movió un dado. Elige otra carta adyacente distinta."
                : "Percebes movió un dado. " + reveal + " Elige otra carta adyacente distinta.";
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
                recomputeBottleAdjustments();
                break;
            }
        }
    }

    private String collectAttachedRemoras(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getStatus().attachedRemoras.isEmpty()) return "";
        int count = 0;
        for (Card c : slot.getStatus().attachedRemoras) {
            captures.add(c);
            count++;
        }
        slot.getStatus().attachedRemoras.clear();
        return count == 0 ? "" : " Capturaste también " + count + " rémora(s).";
    }

    private String startSalmonFlip() {
        boolean hasFaceDown = false;
        for (BoardSlot s : board) {
            if (s.getCard() != null && !s.isFaceUp()) {
                hasFaceDown = true;
                break;
            }
        }
        if (!hasFaceDown) {
            return "No hay cartas boca abajo para revelar.";
        }
        pendingSelection = PendingSelection.SALMON_FLIP;
        pendingSelectionActor = -1;
        return "Salmón: selecciona una carta boca abajo para revelarla.";
    }

    private String flipChosenFaceDown(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null || slot.isFaceUp()) {
            return "Debes elegir una carta boca abajo.";
        }
        clearPendingSelection();
        String reveal = revealAndTrigger(slotIndex);
        String base = "Revelaste " + slot.getCard().getName() + ".";
        if (reveal != null && !reveal.isEmpty()) {
            base += " " + reveal;
        }
        recomputeBottleAdjustments();
        return base;
    }

    private String flipLineFromFlyingFish(int slotIndex, List<Die> diceOnCard) {
        if (diceOnCard.size() < 2) return "";
        Die even = null, odd = null;
        for (Die d : diceOnCard) {
            if (d.getValue() % 2 == 0) even = d; else odd = d;
        }
        if (even == null || odd == null) return "";
        boolean vertical = even.getValue() > odd.getValue();
        int row = slotIndex / 3, col = slotIndex % 3;
        int flipped = 0;
        StringBuilder extraLogs = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int idx = vertical ? i * 3 + col : row * 3 + i;
            BoardSlot target = board[idx];
            if (target.getCard() != null && !target.isFaceUp()) {
                String reveal = revealAndTrigger(idx);
                if (!reveal.isEmpty()) {
                    extraLogs.append(extraLogs.length() == 0 ? reveal : " " + reveal);
                }
                flipped++;
            }
        }
        String base = flipped == 0 ? "" : "Pez volador reveló " + flipped + " carta(s).";
        if (extraLogs.length() > 0) {
            base = base.isEmpty() ? extraLogs.toString() : base + " " + extraLogs;
        }
        return base;
    }

    private String startBlueWhaleReposition(int slotIndex) {
        List<Die> pool = new ArrayList<>();
        for (BoardSlot s : board) {
            if (!s.getDice().isEmpty()) {
                pool.addAll(s.getDice());
                s.clearDice();
            }
        }
        if (pool.isEmpty()) {
            return "No hay dados para reposicionar.";
        }
        pendingBallenaDice.clear();
        pendingBallenaDice.addAll(pool);
        pendingSelection = PendingSelection.BLUE_WHALE_PLACE;
        pendingSelectionActor = slotIndex;
        return "Ballena azul: coloca cada dado nuevamente en el tablero.";
    }

    private String placeBlueWhaleDie(int slotIndex) {
        if (pendingBallenaDice.isEmpty()) {
            clearPendingSelection();
            return "No hay dados por colocar.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) {
            return "Selecciona una carta válida para el dado.";
        }
        if (target.getDice().size() >= 2) {
            return "La carta seleccionada ya tiene 2 dados.";
        }
        Die moved = pendingBallenaDice.remove(0);
        String reveal = addDieToSlot(slotIndex, moved);
        if (pendingBallenaDice.isEmpty()) {
            clearPendingSelection();
            return reveal.isEmpty()
                    ? "Ballena azul reposicionó todos los dados en el tablero."
                    : "Ballena azul reposicionó todos los dados en el tablero. " + reveal;
        }
        return reveal.isEmpty()
                ? "Dado colocado. Elige destino para el siguiente."
                : "Dado colocado. " + reveal + " Elige destino para el siguiente.";
    }

    private String repositionAllDice(List<Die> diceOnCard) {
        List<Die> pool = new ArrayList<>();
        for (BoardSlot s : board) {
            if (s.getDice().isEmpty()) continue;
            pool.addAll(s.getDice());
            s.clearDice();
        }
        int placed = 0;
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() == null) continue;
            while (s.getDice().size() < 2 && !pool.isEmpty()) {
                addDieToSlot(i, pool.remove(0));
                placed++;
            }
        }
        for (Die remaining : pool) {
            reserve.add(remaining.getType());
        }
        return placed == 0 ? "" : "Ballena azul reposicionó dados en el tablero.";
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
