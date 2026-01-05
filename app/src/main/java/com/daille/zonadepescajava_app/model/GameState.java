package com.daille.zonadepescajava_app.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
    private boolean awaitingBlueCrabDecision = false;
    private int blueCrabSlotIndex = -1;
    private boolean awaitingBlowfishDecision = false;
    private int blowfishSlotIndex = -1;
    private boolean awaitingValueAdjustment = false;
    private int adjustmentSlotIndex = -1;
    private int adjustmentDieIndex = -1;
    private int adjustmentAmount = 0;
    private CardId adjustmentSource = null;
    private boolean awaitingPezVelaDecision = false;
    private boolean awaitingPezVelaResultChoice = false;
    private int pezVelaSlotIndex = -1;
    private int pezVelaDieIndex = -1;
    private Die pezVelaOriginalDie = null;
    private Die pezVelaRerolledDie = null;
    private boolean awaitingGhostShrimpDecision = false;
    private int ghostShrimpFirstChoice = -1;
    private int ghostShrimpSecondChoice = -1;
    private final List<Card> recentlyRevealedCards = new ArrayList<>();
    private final List<Die> pendingPercebesDice = new ArrayList<>();
    private final List<Integer> pendingPercebesTargets = new ArrayList<>();
    private final List<Die> pendingBallenaDice = new ArrayList<>();
    private int pendingBallenaTotal = 0;
    private final List<Card> pendingPulpoOptions = new ArrayList<>();
    private final Deque<PendingSelectionState> pendingSelectionQueue = new ArrayDeque<>();
    private final List<Card> pendingArenquePool = new ArrayList<>();
    private final List<Card> pendingArenqueChosen = new ArrayList<>();
    private boolean awaitingArenqueChoice = false;
    private int arenqueSlotIndex = -1;
    private boolean awaitingPulpoChoice = false;
    private int pulpoSlotIndex = -1;
    private int pulpoPlacedValue = 0;
    private boolean pendingGameOver = false;
    private String pendingGameOverMessage = null;
    private enum PendingSelection {
        NONE,
        RED_CRAB_FROM,
        RED_CRAB_TO,
        BLUE_CRAB,
        NAUTILUS_FIRST,
        NAUTILUS_SECOND,
        BLOWFISH,
        PIRANA_TARGET,
        GHOST_TARGET,
        GHOST_SHRIMP_FIRST,
        GHOST_SHRIMP_SECOND,
        WHITE_SHARK_TARGET,
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

    private static class PendingSelectionState {
        private final PendingSelection selection;
        private final int actor;
        private final int aux;

        private PendingSelectionState(PendingSelection selection, int actor, int aux) {
            this.selection = selection;
            this.actor = actor;
            this.aux = aux;
        }
    }

    private enum CurrentDirection { UP, DOWN, LEFT, RIGHT }

    public GameState() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardSlot();
        }
    }

    public void newGame() {
        newGame(null);
    }

    public void newGame(List<DieType> startingReserve) {
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
        awaitingBlueCrabDecision = false;
        blueCrabSlotIndex = -1;
        awaitingBlowfishDecision = false;
        blowfishSlotIndex = -1;
        awaitingValueAdjustment = false;
        adjustmentSlotIndex = -1;
        adjustmentDieIndex = -1;
        adjustmentAmount = 0;
        adjustmentSource = null;
        clearPezVelaState();
        pendingPercebesDice.clear();
        pendingPercebesTargets.clear();
        pendingSelectionQueue.clear();
        awaitingPezVelaDecision = false;
        awaitingPezVelaResultChoice = false;
        pezVelaSlotIndex = -1;
        pezVelaDieIndex = -1;
        pezVelaOriginalDie = null;
        pezVelaRerolledDie = null;
        awaitingGhostShrimpDecision = false;
        ghostShrimpFirstChoice = -1;
        ghostShrimpSecondChoice = -1;
        recentlyRevealedCards.clear();
        pendingBallenaDice.clear();
        pendingBallenaTotal = 0;
        pendingPulpoOptions.clear();
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        awaitingArenqueChoice = false;
        arenqueSlotIndex = -1;
        awaitingPulpoChoice = false;
        pulpoSlotIndex = -1;
        pulpoPlacedValue = 0;
        pendingGameOver = false;
        pendingGameOverMessage = null;

        if (startingReserve == null || startingReserve.isEmpty()) {
            reserve.add(DieType.D6);
            reserve.add(DieType.D6);
            reserve.add(DieType.D6);
            reserve.add(DieType.D8);
            reserve.add(DieType.D8);
            reserve.add(DieType.D4);
            reserve.add(DieType.D12);
        } else {
            reserve.addAll(startingReserve);
        }

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

    public boolean isGameOver() {
        return gameOver;
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

    public boolean isAwaitingBlueCrabDecision() {
        return awaitingBlueCrabDecision;
    }

    public boolean isAwaitingBlowfishDecision() {
        return awaitingBlowfishDecision;
    }

    private boolean hasPendingTurnResolutions() {
        return isAwaitingDieLoss()
                || awaitingAtunDecision
                || awaitingBlueCrabDecision
                || awaitingBlowfishDecision
                || awaitingPezVelaDecision
                || awaitingPezVelaResultChoice
                || awaitingLanternChoice
                || isAwaitingBoardSelection()
                || awaitingArenqueChoice
                || awaitingPulpoChoice
                || awaitingValueAdjustment
                || awaitingGhostShrimpDecision
                || (pendingSelection == PendingSelection.BLUE_WHALE_PLACE && !pendingBallenaDice.isEmpty())
                || !recentlyRevealedCards.isEmpty();
    }

    public String resolvePendingGameOverIfReady() {
        if (pendingGameOver && !hasPendingTurnResolutions()) {
            gameOver = true;
            pendingGameOver = false;
            return pendingGameOverMessage;
        }
        return null;
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

    public boolean isAwaitingValueAdjustment() {
        return awaitingValueAdjustment;
    }

    public boolean isAwaitingPulpoChoice() {
        return awaitingPulpoChoice;
    }

    public List<String> getPendingPulpoNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingPulpoOptions) {
            names.add(c.getName());
        }
        return names;
    }

    public int getPendingAdjustmentAmount() {
        return adjustmentAmount;
    }

    public CardId getPendingAdjustmentSource() {
        return adjustmentSource;
    }

    public boolean isAwaitingGhostShrimpDecision() {
        return awaitingGhostShrimpDecision;
    }

    public String getGhostShrimpPeekNames() {
        if (!awaitingGhostShrimpDecision
                || ghostShrimpFirstChoice < 0
                || ghostShrimpSecondChoice < 0
                || ghostShrimpFirstChoice >= board.length
                || ghostShrimpSecondChoice >= board.length) {
            return "";
        }
        Card first = board[ghostShrimpFirstChoice].getCard();
        Card second = board[ghostShrimpSecondChoice].getCard();
        String firstName = first != null ? first.getName() : "?";
        String secondName = second != null ? second.getName() : "?";
        return firstName + " y " + secondName;
    }

    public List<Card> consumeRecentlyRevealedCards() {
        List<Card> copy = new ArrayList<>(recentlyRevealedCards);
        recentlyRevealedCards.clear();
        return copy;
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
            case BLOWFISH:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) highlight.add(i);
                }
                break;
            case PIRANA_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].isFaceUp()
                            && board[idx].getCard().getType() == CardType.PEZ) {
                        highlight.add(idx);
                    }
                }
                break;
            case GHOST_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case GHOST_SHRIMP_FIRST:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && !board[idx].isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case GHOST_SHRIMP_SECOND:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && !board[idx].isFaceUp()
                            && idx != ghostShrimpFirstChoice) {
                        highlight.add(idx);
                    }
                }
                break;
            case WHITE_SHARK_TARGET:
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
            case BLOWFISH:
                result = inflateChosenDie(slotIndex);
                break;
            case PIRANA_TARGET:
                result = resolvePiranhaBite(pendingSelectionActor, slotIndex);
                break;
            case GHOST_TARGET:
                result = hideChosenAdjacent(slotIndex);
                break;
            case GHOST_SHRIMP_FIRST:
                result = chooseGhostShrimpFirst(slotIndex);
                break;
            case GHOST_SHRIMP_SECOND:
                result = chooseGhostShrimpSecond(slotIndex);
                break;
            case SALMON_FLIP:
                result = flipChosenFaceDown(slotIndex);
                break;
            case WHITE_SHARK_TARGET:
                result = devourChosenFaceUp(pendingSelectionActor, slotIndex);
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
        if (slot.getDice().isEmpty()) {
            clearAtunState();
            return "No hay dado para reposicionar.";
        }
        if (atunDieIndex < 0 || atunDieIndex >= slot.getDice().size()) {
            atunDieIndex = slot.getDice().size() - 1;
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

    public String chooseBlueCrabUse(boolean useAbility) {
        if (!awaitingBlueCrabDecision) {
            return "No hay decisión pendiente de la Jaiba azul.";
        }
        if (blueCrabSlotIndex < 0 || blueCrabSlotIndex >= board.length) {
            clearBlueCrabState();
            return "La Jaiba azul ya no está disponible.";
        }
        if (!useAbility) {
            clearBlueCrabState();
            return "Omitiste la habilidad de la Jaiba azul.";
        }
        String msg = queueableSelection(
                PendingSelection.BLUE_CRAB,
                blueCrabSlotIndex,
                "Jaiba azul: elige un dado para ajustar ±1.");
        clearBlueCrabState();
        return msg;
    }

    public String chooseBlowfishUse(boolean useAbility) {
        if (!awaitingBlowfishDecision) {
            return "No hay decisión pendiente del Pez globo.";
        }
        if (blowfishSlotIndex < 0 || blowfishSlotIndex >= board.length) {
            awaitingBlowfishDecision = false;
            blowfishSlotIndex = -1;
            return "El Pez globo ya no está disponible.";
        }
        if (!useAbility) {
            awaitingBlowfishDecision = false;
            blowfishSlotIndex = -1;
            return "Omitiste la habilidad del Pez globo.";
        }
        awaitingBlowfishDecision = false;
        String msg = queueableSelection(
                PendingSelection.BLOWFISH,
                blowfishSlotIndex,
                "Pez globo: elige un dado para inflarlo a su valor máximo.");
        blowfishSlotIndex = -1;
        return msg;
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
        if (awaitingBlueCrabDecision) {
            return "Resuelve la decisión de la Jaiba azul antes de continuar.";
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
        if (awaitingPulpoChoice) {
            return "Elige la carta para reemplazar al Pulpo antes de lanzar otro dado.";
        }
        if (awaitingValueAdjustment) {
            return "Resuelve el ajuste pendiente antes de lanzar otro dado.";
        }
        if (awaitingGhostShrimpDecision) {
            return "Decide primero si intercambiar las cartas vistas por el Camarón fantasma.";
        }
        if (pendingSelection == PendingSelection.BLUE_WHALE_PLACE && !pendingBallenaDice.isEmpty()) {
            return "Coloca los dados pendientes de la Ballena azul antes de continuar.";
        }
        if (selectedDie != null) {
            return "Coloca el dado ya lanzado antes de lanzar otro.";
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
        if (awaitingBlueCrabDecision) {
            return "Resuelve primero la habilidad de la Jaiba azul.";
        }
        if (awaitingBlowfishDecision) {
            return "Decide primero si usarás la habilidad del Pez globo.";
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
        if (awaitingPulpoChoice) {
            return "Elige primero la carta que reemplazará al Pulpo.";
        }
        if (awaitingValueAdjustment) {
            return "Resuelve el ajuste pendiente antes de colocar dados.";
        }
        if (awaitingGhostShrimpDecision) {
            return "Decide si intercambiar las cartas vistas por el Camarón fantasma antes de continuar.";
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
            slot.getStatus().calamarForcedFaceDown = false;
            markRevealed(slotIndex);
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
        target.getStatus().calamarForcedFaceDown = false;
        markRevealed(slotIndex);
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
        String ending = null;
        if (reserve.isEmpty() && selectedDie == null) {
            ending = "Sin dados en reserva: derrota.";
        } else if (!hasAnyBoardCard() && deck.isEmpty()) {
            ending = "No quedan cartas por capturar.";
        }

        if (ending == null) {
            pendingGameOver = false;
            pendingGameOverMessage = null;
            return base;
        }

        String message = base + " | " + ending;
        if (hasPendingTurnResolutions()) {
            pendingGameOver = true;
            pendingGameOverMessage = message;
            return message;
        }

        gameOver = true;
        pendingGameOver = false;
        pendingGameOverMessage = null;
        return message;
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
        Integer newForcedSlot = forcedSlotIndex;
        BoardSlot[] newBoard = new BoardSlot[9];
        int[] indexMap = new int[9];
        Arrays.fill(indexMap, -1);
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
                if (forcedSlotIndex != null && idx == forcedSlotIndex) {
                    if (targetR < 0 || targetR > 2 || targetC < 0 || targetC > 2) {
                        newForcedSlot = null;
                    } else {
                        newForcedSlot = targetR * 3 + targetC;
                    }
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
                            collectDiceLostToCurrents(src, lostFromBoard);
                        }
                    }
                    continue;
                }
                int targetIdx = targetR * 3 + targetC;
                newBoard[targetIdx] = src;
                indexMap[idx] = targetIdx;
            }
        }

        if (!toShuffle.isEmpty()) {
            List<Card> tmp = new ArrayList<>(deck);
            tmp.addAll(toShuffle);
            java.util.Collections.shuffle(tmp, rng);
            deck.clear();
            for (Card c : tmp) deck.push(c);
        }

        for (int i = 0; i < 9; i++) {
            if (newBoard[i].getCard() == null) {
                newBoard[i] = new BoardSlot();
                newBoard[i].setCard(deck.isEmpty() ? null : deck.pop());
                newBoard[i].setFaceUp(false);
                newBoard[i].setStatus(new SlotStatus());
            }
        }

        if (!lostFromBoard.isEmpty()) {
            lostDice.addAll(lostFromBoard);
        }

        for (int i = 0; i < board.length; i++) {
            board[i] = newBoard[i];
        }
        forcedSlotIndex = newForcedSlot;
        remapPendingState(indexMap);
        recomputeBottleAdjustments();
        String baseLog = "Corrientes: el tablero se desplazó";
        if (lostFromBoard.isEmpty()) {
            return baseLog;
        }
        return baseLog + " y perdiste " + formatDiceList(lostFromBoard) + ".";
    }

    private void collectDiceLostToCurrents(BoardSlot src, List<Die> lostFromBoard) {
        List<Die> dice = new ArrayList<>(src.getDice());
        if (dice.isEmpty()) {
            return;
        }
        lostFromBoard.add(dice.remove(0));
        for (Die remaining : dice) {
            reserve.add(remaining.getType());
        }
        src.clearDice();
    }

    private void remapPendingState(int[] indexMap) {
        pendingDieLossSlot = remapNullableIndex(pendingDieLossSlot, indexMap);
        lanternOriginSlot = remapIndex(lanternOriginSlot, indexMap);
        if (awaitingLanternChoice && lanternOriginSlot < 0) {
            awaitingLanternChoice = false;
        }

        atunSlotIndex = remapIndex(atunSlotIndex, indexMap);
        if (awaitingAtunDecision && atunSlotIndex < 0) {
            clearAtunState();
        }

        blueCrabSlotIndex = remapIndex(blueCrabSlotIndex, indexMap);
        if (awaitingBlueCrabDecision && blueCrabSlotIndex < 0) {
            clearBlueCrabState();
        }

        blowfishSlotIndex = remapIndex(blowfishSlotIndex, indexMap);
        if (awaitingBlowfishDecision && blowfishSlotIndex < 0) {
            awaitingBlowfishDecision = false;
        }

        adjustmentSlotIndex = remapIndex(adjustmentSlotIndex, indexMap);
        if (awaitingValueAdjustment && adjustmentSlotIndex < 0) {
            awaitingValueAdjustment = false;
            adjustmentDieIndex = -1;
            adjustmentAmount = 0;
            adjustmentSource = null;
        }

        pezVelaSlotIndex = remapIndex(pezVelaSlotIndex, indexMap);
        if ((awaitingPezVelaDecision || awaitingPezVelaResultChoice) && pezVelaSlotIndex < 0) {
            clearPezVelaState();
        }

        ghostShrimpFirstChoice = remapIndex(ghostShrimpFirstChoice, indexMap);
        ghostShrimpSecondChoice = remapIndex(ghostShrimpSecondChoice, indexMap);
        if (awaitingGhostShrimpDecision && (ghostShrimpFirstChoice < 0 || ghostShrimpSecondChoice < 0)) {
            awaitingGhostShrimpDecision = false;
            ghostShrimpFirstChoice = -1;
            ghostShrimpSecondChoice = -1;
        }

        pendingPercebesTargets.replaceAll(idx -> remapIndex(idx, indexMap));
        pendingPercebesTargets.removeIf(idx -> idx < 0);

        remapPendingSelectionQueue(indexMap);
        remapCurrentSelection(indexMap);

        if (awaitingArenqueChoice) {
            arenqueSlotIndex = remapIndex(arenqueSlotIndex, indexMap);
            if (arenqueSlotIndex < 0) {
                clearArenqueState();
            }
        }

        if (awaitingPulpoChoice) {
            pulpoSlotIndex = remapIndex(pulpoSlotIndex, indexMap);
            if (pulpoSlotIndex < 0) {
                clearPulpoState();
            }
        }
    }

    private void remapCurrentSelection(int[] indexMap) {
        if (pendingSelection == PendingSelection.NONE) return;

        pendingSelectionActor = remapIndex(pendingSelectionActor, indexMap);
        if (pendingSelectionActor < 0) {
            clearSelectionState(pendingSelection);
            pendingSelection = PendingSelection.NONE;
            pendingSelectionAux = -1;
            pendingSelectionQueue.clear();
            return;
        }

        if (pendingSelectionAux >= 0) {
            pendingSelectionAux = remapIndex(pendingSelectionAux, indexMap);
            if (pendingSelectionAux < 0) {
                clearSelectionState(pendingSelection);
                pendingSelection = PendingSelection.NONE;
                pendingSelectionQueue.clear();
                return;
            }
        }
    }

    private void remapPendingSelectionQueue(int[] indexMap) {
        if (pendingSelectionQueue.isEmpty()) return;

        Deque<PendingSelectionState> remapped = new ArrayDeque<>();
        for (PendingSelectionState state : pendingSelectionQueue) {
            int actor = remapIndex(state.actor, indexMap);
            int aux = state.aux >= 0 ? remapIndex(state.aux, indexMap) : state.aux;
            if (actor >= 0) {
                remapped.add(new PendingSelectionState(state.selection, actor, aux));
            } else {
                clearSelectionState(state.selection);
            }
        }
        pendingSelectionQueue.clear();
        pendingSelectionQueue.addAll(remapped);
    }

    private int remapIndex(int original, int[] indexMap) {
        if (original < 0 || original >= indexMap.length) return -1;
        return indexMap[original];
    }

    private Integer remapNullableIndex(Integer original, int[] indexMap) {
        if (original == null) return null;
        int remapped = remapIndex(original, indexMap);
        return remapped >= 0 ? remapped : null;
    }

    private String formatDiceList(List<Die> dice) {
        if (dice.isEmpty()) return "";
        List<String> labels = new ArrayList<>();
        for (Die d : dice) {
            labels.add(d.getLabel());
        }
        return String.join(", ", labels);
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
        clearSelectionState(pendingSelection);
        PendingSelectionState next = pendingSelectionQueue.poll();
        if (next != null) {
            pendingSelection = next.selection;
            pendingSelectionActor = next.actor;
            pendingSelectionAux = next.aux;
        } else {
            pendingSelection = PendingSelection.NONE;
            pendingSelectionActor = -1;
            pendingSelectionAux = -1;
        }
    }

    private void clearSelectionState(PendingSelection selection) {
        switch (selection) {
            case PERCEBES_MOVE:
                clearPercebesState();
                break;
            case BLUE_WHALE_PLACE:
                clearBallenaState();
                break;
            case ARENQUE_DESTINATION:
                clearArenqueState();
                break;
            default:
                break;
        }
    }

    private void clearPercebesState() {
        pendingPercebesDice.clear();
        pendingPercebesTargets.clear();
    }

    private void clearBallenaState() {
        pendingBallenaDice.clear();
        pendingBallenaTotal = 0;
    }

    private void clearArenqueState() {
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        awaitingArenqueChoice = false;
        arenqueSlotIndex = -1;
    }

    private void clearPulpoState() {
        awaitingPulpoChoice = false;
        pulpoSlotIndex = -1;
        pulpoPlacedValue = 0;
        pendingPulpoOptions.clear();
    }

    private void markRevealed(int slotIndex) {
        Card card = board[slotIndex].getCard();
        if (card != null && !recentlyRevealedCards.contains(card)) {
            recentlyRevealedCards.add(card);
        }
    }

    private String queueableSelection(PendingSelection selection, int actor, int aux, String message) {
        if (pendingSelection == PendingSelection.NONE) {
            pendingSelection = selection;
            pendingSelectionActor = actor;
            pendingSelectionAux = aux;
            return message;
        }
        pendingSelectionQueue.add(new PendingSelectionState(selection, actor, aux));
        return message + " Queda en espera hasta que finalice la acción actual.";
    }

    private String queueableSelection(PendingSelection selection, int actor, String message) {
        return queueableSelection(selection, actor, -1, message);
    }

    private void clearAtunState() {
        awaitingAtunDecision = false;
        atunSlotIndex = -1;
        atunDieIndex = -1;
    }

    private void clearBlueCrabState() {
        awaitingBlueCrabDecision = false;
        blueCrabSlotIndex = -1;
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
        markRevealed(slotIndex);
        String result = handleOnReveal(slotIndex, 0);
        return result;
    }

    private String handleOnReveal(int slotIndex, int placedValue) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) return "";
        String result;
        switch (slot.getCard().getId()) {
            case CANGREJO_ROJO:
                result = queueableSelection(
                        PendingSelection.RED_CRAB_FROM,
                        slotIndex,
                        "Cangrejo rojo: elige un dado adyacente para mover.");
                break;
            case JAIBA_AZUL:
                awaitingBlueCrabDecision = true;
                blueCrabSlotIndex = slotIndex;
                result = "Jaiba azul: ¿quieres ajustar un dado ±1?";
                break;
            case CAMARON_FANTASMA:
                result = startGhostShrimpPeek(slotIndex);
                break;
            case ATUN:
                result = startAtunDecision(slotIndex);
                break;
            case PEZ_GLOBO:
                result = startBlowfishInflate(slotIndex);
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
                result = queueableSelection(
                        PendingSelection.NAUTILUS_FIRST,
                        slotIndex,
                        "Nautilus: elige un dado para ajustar ±2.");
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
                result = startGhostFishSelection(slotIndex);
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
                result = startWhiteSharkSelection(slotIndex);
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
        String clams = triggerAdjacentClams(slotIndex);
        if (!clams.isEmpty()) {
            result = result.isEmpty() ? clams : result + " " + clams;
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
        return startValueAdjustment(
                slotIndex,
                idx,
                1,
                CardId.JAIBA_AZUL,
                "Jaiba azul: elige si sumar o restar 1 al dado seleccionado.");
    }

    private String startGhostShrimpPeek(int slotIndex) {
        int facedown = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                facedown++;
            }
        }
        if (facedown < 2) {
            return "No hay suficientes cartas boca abajo adyacentes para el Camarón fantasma.";
        }
        ghostShrimpFirstChoice = -1;
        ghostShrimpSecondChoice = -1;
        return queueableSelection(
                PendingSelection.GHOST_SHRIMP_FIRST,
                slotIndex,
                "Camarón fantasma: elige la primera carta boca abajo adyacente.");
    }

    private String chooseGhostShrimpFirst(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige una carta adyacente al camarón.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) {
            return "La carta debe estar boca abajo.";
        }
        ghostShrimpFirstChoice = slotIndex;
        pendingSelection = PendingSelection.GHOST_SHRIMP_SECOND;
        pendingSelectionAux = slotIndex;
        return "Elige una segunda carta boca abajo adyacente.";
    }

    private String chooseGhostShrimpSecond(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige una carta adyacente al camarón.";
        }
        if (ghostShrimpFirstChoice < 0 || ghostShrimpFirstChoice >= board.length) {
            clearPendingSelection();
            clearGhostShrimpState();
            return "Las cartas a mirar ya no están disponibles.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) {
            return "La carta debe estar boca abajo.";
        }
        if (slotIndex == ghostShrimpFirstChoice) {
            return "Debes elegir una carta distinta.";
        }
        ghostShrimpSecondChoice = slotIndex;
        awaitingGhostShrimpDecision = true;
        String firstName = board[ghostShrimpFirstChoice].getCard().getName();
        String secondName = board[ghostShrimpSecondChoice].getCard().getName();
        clearPendingSelection();
        return "Camarón fantasma vio " + firstName + " y " + secondName + ". ¿Intercambiarlas?";
    }

    public String resolveGhostShrimpSwap(boolean swap) {
        if (!awaitingGhostShrimpDecision) {
            return "No hay decisión pendiente del Camarón fantasma.";
        }
        if (ghostShrimpFirstChoice < 0 || ghostShrimpSecondChoice < 0) {
            clearGhostShrimpState();
            return "Las cartas a intercambiar ya no están disponibles.";
        }
        if (swap) {
            swapSlots(ghostShrimpFirstChoice, ghostShrimpSecondChoice);
            recomputeBottleAdjustments();
            clearGhostShrimpState();
            return "Intercambiaste las cartas vistas por el Camarón fantasma.";
        }
        clearGhostShrimpState();
        return "Decidiste mantener las cartas en su lugar.";
    }

    private void clearGhostShrimpState() {
        awaitingGhostShrimpDecision = false;
        ghostShrimpFirstChoice = -1;
        ghostShrimpSecondChoice = -1;
    }

    private void swapSlots(int first, int second) {
        if (first < 0 || second < 0 || first >= board.length || second >= board.length) return;
        BoardSlot slotA = board[first];
        BoardSlot slotB = board[second];

        Card cardA = slotA.getCard();
        Card cardB = slotB.getCard();
        boolean faceA = slotA.isFaceUp();
        boolean faceB = slotB.isFaceUp();
        List<Die> diceA = new ArrayList<>(slotA.getDice());
        List<Die> diceB = new ArrayList<>(slotB.getDice());
        SlotStatus statusA = copyStatus(slotA.getStatus());
        SlotStatus statusB = copyStatus(slotB.getStatus());

        slotA.setCard(cardB);
        slotA.setFaceUp(faceB);
        slotA.clearDice();
        for (Die d : diceB) slotA.addDie(d);
        slotA.setStatus(statusB);

        slotB.setCard(cardA);
        slotB.setFaceUp(faceA);
        slotB.clearDice();
        for (Die d : diceA) slotB.addDie(d);
        slotB.setStatus(statusA);
    }

    private SlotStatus copyStatus(SlotStatus status) {
        SlotStatus copy = new SlotStatus();
        copy.protectedOnce = status.protectedOnce;
        copy.calamarForcedFaceDown = status.calamarForcedFaceDown;
        copy.sumConditionShift = status.sumConditionShift;
        copy.attachedRemoras = new ArrayList<>(status.attachedRemoras);
        copy.hookPenaltyUsed = status.hookPenaltyUsed;
        copy.langostaRecovered = status.langostaRecovered;
        return copy;
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
        if (origin.getDice().isEmpty()) {
            clearPendingSelection();
            clearAtunState();
            return "El dado del Atún ya no está disponible.";
        }
        if (atunDieIndex < 0 || atunDieIndex >= origin.getDice().size()) {
            atunDieIndex = origin.getDice().size() - 1;
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

    private String startBlowfishInflate(int slotIndex) {
        boolean hasInflatable = false;
        for (BoardSlot s : board) {
            if (s.getDice().isEmpty()) continue;
            Die top = s.getDice().get(s.getDice().size() - 1);
            if (top.getValue() < top.getType().getSides()) {
                hasInflatable = true;
                break;
            }
        }
        if (!hasInflatable) {
            return "Todos los dados en la zona de pesca ya muestran su valor máximo.";
        }
        awaitingBlowfishDecision = true;
        blowfishSlotIndex = slotIndex;
        return "Pez globo: ¿quieres inflar un dado al máximo?";
    }

    private String inflateChosenDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "Elige una carta con un dado para inflar.";
        }
        int idx = slot.getDice().size() - 1;
        Die die = slot.getDice().get(idx);
        int max = die.getType().getSides();
        if (die.getValue() == max) {
            return "Elige un dado que no esté ya en su valor máximo.";
        }
        slot.setDie(idx, new Die(die.getType(), max));
        clearPendingSelection();
        return "Pez globo infló un dado a " + max + ".";
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
        if (pendingSelection == PendingSelection.NAUTILUS_SECOND && slotIndex == pendingSelectionAux) {
            return "Elige un dado diferente al primero.";
        }
        return startValueAdjustment(
                slotIndex,
                idx,
                2,
                CardId.NAUTILUS,
                "Nautilus: elige si sumar o restar 2 al dado seleccionado.");
    }

    private String startValueAdjustment(int slotIndex, int dieIndex,
                                        int amount, CardId source, String prompt) {
        if (awaitingValueAdjustment) {
            return "Finaliza el ajuste pendiente antes de iniciar otro.";
        }
        adjustmentSlotIndex = slotIndex;
        adjustmentDieIndex = dieIndex;
        adjustmentAmount = amount;
        adjustmentSource = source;
        awaitingValueAdjustment = true;
        return prompt;
    }

    public String chooseValueAdjustment(boolean increase) {
        if (!awaitingValueAdjustment) {
            return "No hay ajustes pendientes.";
        }
        if (adjustmentSlotIndex < 0 || adjustmentSlotIndex >= board.length) {
            clearValueAdjustmentState();
            return "El dado a ajustar ya no está disponible.";
        }
        BoardSlot slot = board[adjustmentSlotIndex];
        if (slot.getDice().isEmpty() || adjustmentDieIndex < 0 || adjustmentDieIndex >= slot.getDice().size()) {
            clearValueAdjustmentState();
            return "El dado a ajustar ya no está disponible.";
        }
        Die die = slot.getDice().get(adjustmentDieIndex);
        int delta = increase ? adjustmentAmount : -adjustmentAmount;
        int newVal = die.getValue() + delta;
        if (newVal < 1 || newVal > die.getType().getSides()) {
            return "No puedes ajustar el dado más allá de sus caras. Elige la otra opción.";
        }
        slot.setDie(adjustmentDieIndex, new Die(die.getType(), newVal));
        String actor = adjustmentSource == CardId.NAUTILUS ? "Nautilus" : "Jaiba azul";
        boolean fromNautilus = adjustmentSource == CardId.NAUTILUS;
        String result = actor + " ajustó el dado a " + newVal + ".";

        awaitingValueAdjustment = false;

        if (fromNautilus && pendingSelection == PendingSelection.NAUTILUS_FIRST) {
            boolean hasOtherDie = false;
            for (int i = 0; i < board.length; i++) {
                if (!board[i].getDice().isEmpty() && i != adjustmentSlotIndex) {
                    hasOtherDie = true;
                    break;
                }
            }
            if (hasOtherDie) {
                pendingSelection = PendingSelection.NAUTILUS_SECOND;
                pendingSelectionAux = adjustmentSlotIndex;
                clearValueAdjustmentState();
                return result + " Elige un segundo dado.";
            }
            clearPendingSelection();
            clearValueAdjustmentState();
            return result + " No hay un segundo dado disponible.";
        }

        if (fromNautilus && pendingSelection == PendingSelection.NAUTILUS_SECOND) {
            clearPendingSelection();
        } else if (adjustmentSource == CardId.JAIBA_AZUL) {
            clearPendingSelection();
        }

        clearValueAdjustmentState();
        return result;
    }

    private void clearValueAdjustmentState() {
        adjustmentSlotIndex = -1;
        adjustmentDieIndex = -1;
        adjustmentAmount = 0;
        adjustmentSource = null;
        awaitingValueAdjustment = false;
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
        return queueableSelection(
                PendingSelection.CLOWNFISH_PROTECT,
                slotIndex,
                "Pez payaso: elige una carta adyacente boca arriba para proteger.");
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
        List<Integer> targets = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.PEZ) {
                targets.add(idx);
            }
        }
        if (targets.isEmpty()) {
            return "No hay peces pequeños boca arriba adyacentes para la piraña.";
        }
        if (targets.size() == 1) {
            return resolvePiranhaBite(slotIndex, targets.get(0));
        }
        return queueableSelection(
                PendingSelection.PIRANA_TARGET,
                slotIndex,
                "Piraña: elige un pez pequeño adyacente boca arriba para descartarlo.");
    }

    private String resolvePiranhaBite(int actorIndex, int targetIndex) {
        if (!adjacentIndices(actorIndex, true).contains(targetIndex)) {
            return "Elige un pez pequeño adyacente a la piraña.";
        }
        BoardSlot adj = board[targetIndex];
        if (adj.getCard() == null || !adj.isFaceUp() || adj.getCard().getType() != CardType.PEZ) {
            return "Debes elegir un pez pequeño boca arriba.";
        }
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
                String reveal = addDieToSlot(targetIndex, d);
                if (!reveal.isEmpty()) {
                    if (extra.length() > 0) extra.append(" ");
                    extra.append(reveal);
                }
            } else {
                reserve.add(d.getType());
            }
        }
        String base = "Piraña descartó a " + removed.getName() + " sin perder sus dados.";
        if (pendingSelection == PendingSelection.PIRANA_TARGET) {
            clearPendingSelection();
        }
        return extra.length() == 0 ? base : base + " " + extra;
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

    private String startGhostFishSelection(int slotIndex) {
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            if (board[idx].getCard() != null && board[idx].isFaceUp()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "No hay cartas boca arriba adyacentes para ocultar.";
        }
        return queueableSelection(
                PendingSelection.GHOST_TARGET,
                slotIndex,
                "Pez fantasma: elige una carta adyacente boca arriba para ocultar.");
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
        pendingPulpoOptions.clear();
        for (Card c : deck) {
            if (c.getType() != CardType.OBJETO) {
                pendingPulpoOptions.add(c);
            }
        }
        if (pendingPulpoOptions.isEmpty()) {
            return "";
        }
        awaitingPulpoChoice = true;
        pulpoSlotIndex = slotIndex;
        pulpoPlacedValue = placedValue;
        return "Pulpo: elige una carta del cardumen para reemplazarlo.";
    }

    public String choosePulpoReplacement(int index) {
        if (!awaitingPulpoChoice) {
            return "No hay selección pendiente del Pulpo.";
        }
        if (pulpoSlotIndex < 0 || pulpoSlotIndex >= board.length) {
            clearPulpoState();
            return "El Pulpo ya no está disponible.";
        }
        if (index < 0 || index >= pendingPulpoOptions.size()) {
            return "Debes elegir una carta válida del cardumen.";
        }
        Card replacement = pendingPulpoOptions.get(index);
        if (!deck.remove(replacement)) {
            clearPulpoState();
            shuffleDeck();
            return "La carta seleccionada ya no está en el mazo.";
        }
        BoardSlot slot = board[pulpoSlotIndex];
        if (slot.getCard() == null) {
            clearPulpoState();
            shuffleDeck();
            return "El Pulpo ya no está en la mesa.";
        }
        slot.setCard(replacement);
        slot.setFaceUp(true);
        markRevealed(pulpoSlotIndex);
        String reveal = handleOnReveal(pulpoSlotIndex, pulpoPlacedValue);
        String result = reveal.isEmpty()
                ? "Pulpo fue reemplazado por " + replacement.getName() + "."
                : "Pulpo fue reemplazado por " + replacement.getName() + ". " + reveal;
        clearPulpoState();
        shuffleDeck();
        return result;
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

    private String startWhiteSharkSelection(int slotIndex) {
        List<Integer> targets = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                targets.add(idx);
            }
        }
        if (targets.isEmpty()) {
            return "No hay cartas boca arriba adyacentes para devorar.";
        }
        if (targets.size() == 1) {
            return devourChosenFaceUp(slotIndex, targets.get(0));
        }
        return queueableSelection(
                PendingSelection.WHITE_SHARK_TARGET,
                slotIndex,
                "Tiburón blanco: elige una carta adyacente boca arriba para devorarla.");
    }

    private String devourChosenFaceUp(int sharkSlot, int targetIndex) {
        if (!adjacentIndices(sharkSlot, true).contains(targetIndex)) {
            return "Elige una carta adyacente al tiburón blanco.";
        }
        BoardSlot adj = board[targetIndex];
        if (adj.getCard() == null || !adj.isFaceUp()) {
            return "Debes elegir una carta boca arriba para devorar.";
        }
        List<Die> dice = new ArrayList<>(adj.getDice());
        failedDiscards.add(adj.getCard());
        adj.clearDice();
        adj.setCard(deck.isEmpty() ? null : deck.pop());
        adj.setFaceUp(false);
        adj.setStatus(new SlotStatus());
        BoardSlot shark = board[sharkSlot];
        StringBuilder reveal = new StringBuilder();
        for (Die d : dice) {
            if (shark.getDice().size() < 2) {
                String extra = addDieToSlot(sharkSlot, d);
                if (!extra.isEmpty()) {
                    if (reveal.length() > 0) reveal.append(" ");
                    reveal.append(extra);
                }
            } else {
                reserve.add(d.getType());
            }
        }
        recomputeBottleAdjustments();
        if (pendingSelection == PendingSelection.WHITE_SHARK_TARGET) {
            clearPendingSelection();
        }
        return reveal.length() == 0
                ? "Tiburón blanco devoró una carta adyacente."
                : "Tiburón blanco devoró una carta adyacente. " + reveal;
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
            clearPercebesState();
            if (pendingSelection == PendingSelection.PERCEBES_MOVE) {
                clearPendingSelection();
            }
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
        pendingBallenaTotal = pendingBallenaDice.size();
        return queueableSelection(
                PendingSelection.BLUE_WHALE_PLACE,
                slotIndex,
                "Ballena azul: coloca cada dado nuevamente en el tablero. Comienza con "
                        + pendingBallenaDice.get(0).getLabel() + ".");
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
        int placedCount = pendingBallenaTotal - pendingBallenaDice.size();
        String nextLabel = pendingBallenaDice.get(0).getLabel();
        String base = "Dado " + placedCount + "/" + pendingBallenaTotal + " colocado.";
        String prompt = " El siguiente es " + nextLabel + ".";
        return reveal.isEmpty()
                ? base + prompt
                : "" + base + " " + reveal + prompt;
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
