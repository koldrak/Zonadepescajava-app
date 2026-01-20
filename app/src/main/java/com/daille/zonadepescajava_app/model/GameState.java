package com.daille.zonadepescajava_app.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
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
    private boolean lastDiePlaced = false;
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
    private boolean awaitingPezLoboDecision = false;
    private int pezLoboSlotIndex = -1;
    private boolean awaitingMantisDecision = false;
    private boolean awaitingMantisLostDieChoice = false;
    private boolean awaitingLangostaRecovery = false;
    private int mantisSlotIndex = -1;
    private Die mantisRerolledDie = null;
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
    private final List<Integer> recentlyRerolledSlots = new ArrayList<>();
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
    private int arenquePlacementSlots = 0;
    private final List<Card> pendingDecoradorOptions = new ArrayList<>();
    private boolean awaitingDecoradorChoice = false;
    private Card pendingDecoradorCard = null;
    private final List<Card> pendingPezBorronTargets = new ArrayList<>();
    private final List<Card> pendingSepiaOptions = new ArrayList<>();
    private final List<Card> pendingDragnetTargets = new ArrayList<>();
    private final List<Card> pendingHachaReleaseChoices = new ArrayList<>();
    private final List<Card> pendingDamiselasTop = new ArrayList<>();
    private final List<Card> pendingDamiselasOrdered = new ArrayList<>();
    private final List<Card> pendingPeregrinoTop = new ArrayList<>();
    private int decoradorSlotIndex = -1;
    private boolean awaitingViolinistChoice = false;
    private int horseshoeSlotIndex = -1;
    private int horseshoeDieIndex = -1;
    private boolean awaitingHorseshoeValue = false;
    private boolean awaitingBoxerDecision = false;
    private int boxerMovesRemaining = 0;
    private int boxerSlotIndex = -1;
    private boolean awaitingPulpoChoice = false;
    private int pulpoSlotIndex = -1;
    private int pulpoPlacedValue = 0;
    private boolean pendingGameOver = false;
    private String pendingGameOverMessage = null;
    private Card pendingSpiderCrabCard = null;
    private int spiderCrabSlotIndex = -1;
    private final java.util.Deque<Integer> pendingRevealChain = new java.util.ArrayDeque<>();
    private boolean processingRevealChain = false;
    private final java.util.Map<Integer, Integer> bottleTargets = new java.util.HashMap<>();
    private final java.util.Map<Integer, Integer> glassBottleTargets = new java.util.HashMap<>();
    private Card pendingReleaseCard = null;
    private int pendingPezBorronSlot = -1;
    private int pendingSepiaSlot = -1;
    private boolean pendingFletanActive = false;
    private int pendingFletanSlot = -1;
    private boolean awaitingSepiaChoice = false;
    private boolean awaitingDragnetReleaseChoice = false;
    private boolean awaitingHachaReleaseChoice = false;
    private int pendingHachaReleaseCount = 0;
    private int pendingDamiselasSlot = -1;
    private boolean awaitingDamiselasChoice = false;
    private int pendingPeregrinoSlot = -1;
    private boolean awaitingPeregrinoChoice = false;
    private Card pendingPeregrinoTopChoice = null;
    private int pendingHumpbackSlot = -1;
    private boolean awaitingHumpbackDirection = false;
    private final java.util.List<Die> pendingLocoDice = new java.util.ArrayList<>();
    private Die pendingLocoDie = null;
    private AbilityActivation pendingAbilityConfirmation = null;
    private final java.util.Deque<AbilityActivation> pendingAbilityQueue = new java.util.ArrayDeque<>();

    private enum AbilityTrigger {
        REVEAL,
        CAPTURE
    }

    public static class AbilityConfirmation {
        private final Card card;
        private final String detail;

        private AbilityConfirmation(Card card, String detail) {
            this.card = card;
            this.detail = detail;
        }

        public Card getCard() {
            return card;
        }

        public String getDetail() {
            return detail;
        }
    }

    private static class AbilityActivation {
        private final AbilityTrigger trigger;
        private final int slotIndex;
        private final int placedValue;
        private final List<Die> diceOnCard;
        private final Card card;
        private final String detail;

        private AbilityActivation(AbilityTrigger trigger, int slotIndex, int placedValue, List<Die> diceOnCard, Card card, String detail) {
            this.trigger = trigger;
            this.slotIndex = slotIndex;
            this.placedValue = placedValue;
            this.diceOnCard = diceOnCard;
            this.card = card;
            this.detail = detail;
        }
    }

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
        BOXER_FROM,
        BOXER_TO,
        MANTIS_TARGET,
        BOTTLE_TARGET,
        ARENQUE_DESTINATION,
        BLUE_WHALE_PLACE,
        ESTURION_PLACE,
        SPIDER_CRAB_CHOOSE_CARD,
        SPIDER_CRAB_CHOOSE_SLOT,
        KOI_TARGET,
        RELEASE_CHOOSE_SLOT,
        DECORADOR_CHOOSE_SLOT,
        HORSESHOE_DIE,
        PEZ_BORRON_TARGET,
        FLETAN_HIDE,
        GLASS_BOTTLE_TARGET,
        PEZ_LOBO_TARGET,
        TRUCHA_ARCOIRIS_FLIP,
        PEZ_LEON_TARGET,
        BARCO_PESQUERO_TARGET,
        TIGER_SHARK_TARGET,
        DRAGNET_RELEASE,
        LOCO_TARGET,
        SEPIA_CAPTURE
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

    public enum CurrentDirection { UP, DOWN, LEFT, RIGHT }
    private final Deque<CurrentDirection> pendingCurrentAnimations = new ArrayDeque<>();

    public GameState() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new BoardSlot();
        }
    }

    public void newGame() {
        newGame(null);
    }

    public void newGame(List<DieType> startingReserve) {
        newGame(startingReserve, null);
    }

    public void newGame(List<DieType> startingReserve, Map<CardId, Integer> captureCounts) {
        newGame(startingReserve, captureCounts, null);
    }

    public void newGame(List<DieType> startingReserve, Map<CardId, Integer> captureCounts, List<Card> selectedDeck) {
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
        awaitingPezLoboDecision = false;
        pezLoboSlotIndex = -1;
        awaitingMantisDecision = false;
        awaitingMantisLostDieChoice = false;
        awaitingLangostaRecovery = false;
        mantisSlotIndex = -1;
        mantisRerolledDie = null;
        awaitingValueAdjustment = false;
        adjustmentSlotIndex = -1;
        adjustmentDieIndex = -1;
        adjustmentAmount = 0;
        adjustmentSource = null;
        clearPezVelaState();
        recentlyRerolledSlots.clear();
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
        pendingCurrentAnimations.clear();
        pendingPulpoOptions.clear();
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        awaitingArenqueChoice = false;
        arenqueSlotIndex = -1;
        arenquePlacementSlots = 0;
        pendingDecoradorOptions.clear();
        pendingDecoradorCard = null;
        awaitingDecoradorChoice = false;
        decoradorSlotIndex = -1;
        awaitingViolinistChoice = false;
        pendingPezBorronTargets.clear();
        pendingPezBorronSlot = -1;
        pendingSepiaOptions.clear();
        pendingSepiaSlot = -1;
        pendingFletanActive = false;
        pendingFletanSlot = -1;
        pendingDragnetTargets.clear();
        pendingHachaReleaseChoices.clear();
        pendingHachaReleaseCount = 0;
        awaitingSepiaChoice = false;
        awaitingDragnetReleaseChoice = false;
        awaitingHachaReleaseChoice = false;
        pendingDamiselasTop.clear();
        pendingDamiselasOrdered.clear();
        pendingDamiselasSlot = -1;
        awaitingDamiselasChoice = false;
        pendingPeregrinoTop.clear();
        pendingPeregrinoSlot = -1;
        awaitingPeregrinoChoice = false;
        pendingPeregrinoTopChoice = null;
        pendingHumpbackSlot = -1;
        awaitingHumpbackDirection = false;
        clearHorseshoeState();
        clearBoxerState();
        awaitingPulpoChoice = false;
        pulpoSlotIndex = -1;
        pulpoPlacedValue = 0;
        pendingGameOver = false;
        pendingGameOverMessage = null;
        pendingRevealChain.clear();
        processingRevealChain = false;
        bottleTargets.clear();
        glassBottleTargets.clear();

        if (startingReserve == null || startingReserve.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                reserve.add(DieType.D6);
            }
        } else {
            reserve.addAll(startingReserve);
        }

        List<Card> allCards = selectedDeck != null
                ? GameUtils.buildDeckFromSelection(rng, selectedDeck, captureCounts)
                : GameUtils.buildDeck(rng, captureCounts);
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

    public boolean isAwaitingPezLoboDecision() {
        return awaitingPezLoboDecision;
    }

    public boolean isAwaitingMantisDecision() {
        return awaitingMantisDecision;
    }

    public boolean isAwaitingMantisLostDieChoice() {
        return awaitingMantisLostDieChoice;
    }

    public Die getMantisRerolledDie() {
        return mantisRerolledDie;
    }

    public boolean isAwaitingLangostaRecovery() {
        return awaitingLangostaRecovery;
    }

    public boolean isAwaitingDecoradorChoice() {
        return awaitingDecoradorChoice;
    }

    public boolean isAwaitingViolinistChoice() {
        return awaitingViolinistChoice;
    }

    public boolean isAwaitingHorseshoeValue() {
        return awaitingHorseshoeValue;
    }

    public boolean isAwaitingBoxerDecision() {
        return awaitingBoxerDecision;
    }

    public boolean isAwaitingAbilityConfirmation() {
        return pendingAbilityConfirmation != null;
    }

    public AbilityConfirmation getPendingAbilityConfirmation() {
        if (pendingAbilityConfirmation == null) return null;
        return new AbilityConfirmation(pendingAbilityConfirmation.card, pendingAbilityConfirmation.detail);
    }

    public String confirmAbilityActivation() {
        if (pendingAbilityConfirmation == null) return "";
        AbilityActivation activation = pendingAbilityConfirmation;
        pendingAbilityConfirmation = null;
        String result = executeAbilityActivation(activation);
        if (!pendingAbilityQueue.isEmpty()) {
            pendingAbilityConfirmation = pendingAbilityQueue.poll();
        }
        return result == null || result.isEmpty() ? "Habilidad activada." : result;
    }

    public boolean hasPendingTurnResolutions() {
        return isAwaitingDieLoss()
                || awaitingAtunDecision
                || awaitingBlueCrabDecision
                || awaitingBlowfishDecision
                || awaitingPezLoboDecision
                || awaitingMantisDecision
                || awaitingMantisLostDieChoice
                || awaitingLangostaRecovery
                || awaitingPezVelaDecision
                || awaitingPezVelaResultChoice
                || awaitingLanternChoice
                || isAwaitingBoardSelection()
                || awaitingArenqueChoice
                || awaitingDecoradorChoice
                || awaitingViolinistChoice
                || awaitingHorseshoeValue
                || awaitingBoxerDecision
                || awaitingPulpoChoice
                || awaitingValueAdjustment
                || awaitingGhostShrimpDecision
                || awaitingSepiaChoice
                || awaitingDragnetReleaseChoice
                || awaitingHachaReleaseChoice
                || awaitingDamiselasChoice
                || awaitingPeregrinoChoice
                || awaitingHumpbackDirection
                || awaitingCancelConfirmation
                || pendingAbilityConfirmation != null
                || !pendingAbilityQueue.isEmpty()
                || pendingSelection != PendingSelection.NONE
                || !pendingSelectionQueue.isEmpty();
        // OJO: recentlyRevealedCards NO debe bloquear el game over (es UI, no resolución).
    }


    public String resolvePendingGameOverIfReady() {
        if (pendingGameOver && !hasPendingTurnResolutions()) {
            gameOver = true;
            pendingGameOver = false;
            return pendingGameOverMessage;
        }
        return null;
    }

    public String startReleaseFromCapture(Card capturedCard) {
        if (hasPendingTurnResolutions() || selectedDie != null) {
            return "No puedes liberar ahora: termina primero las resoluciones pendientes.";
        }

        if (capturedCard == null) {
            return "No se puede liberar: carta inválida.";
        }
        if (!captures.contains(capturedCard)) {
            return "No se puede liberar: esa carta no está en capturas.";
        }

        // Si ya hay una selección pendiente, lo encolamos (tu sistema ya soporta cola).
        pendingReleaseCard = capturedCard;

        return queueableSelection(
                PendingSelection.RELEASE_CHOOSE_SLOT,
                -1,
                0,
                "Liberación: toca una carta BOCA ABAJO de la zona de pesca para reemplazarla."
        );
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

    public List<Card> getPendingArenqueCards() {
        return new ArrayList<>(pendingArenquePool);
    }

    public List<String> getPendingDecoradorNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingDecoradorOptions) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingDecoradorCards() {
        return new ArrayList<>(pendingDecoradorOptions);
    }

    public boolean isAwaitingSepiaChoice() {
        return awaitingSepiaChoice;
    }

    public List<String> getPendingSepiaNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingSepiaOptions) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingSepiaCards() {
        return new ArrayList<>(pendingSepiaOptions);
    }

    public boolean isAwaitingDragnetReleaseChoice() {
        return awaitingDragnetReleaseChoice;
    }

    public List<String> getPendingDragnetNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingDragnetTargets) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingDragnetCards() {
        return new ArrayList<>(pendingDragnetTargets);
    }

    public boolean isAwaitingHachaReleaseChoice() {
        return awaitingHachaReleaseChoice;
    }

    public List<String> getPendingHachaReleaseNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingHachaReleaseChoices) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingHachaReleaseCards() {
        return new ArrayList<>(pendingHachaReleaseChoices);
    }

    public boolean isAwaitingDamiselasChoice() {
        return awaitingDamiselasChoice;
    }

    public List<String> getPendingDamiselasNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingDamiselasTop) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingDamiselasCards() {
        return new ArrayList<>(pendingDamiselasTop);
    }

    public boolean isAwaitingPeregrinoChoice() {
        return awaitingPeregrinoChoice;
    }

    public boolean isAwaitingPeregrinoBottomChoice() {
        return awaitingPeregrinoChoice && pendingPeregrinoTopChoice != null;
    }

    public List<String> getPendingPeregrinoNames() {
        List<String> names = new ArrayList<>();
        for (Card c : pendingPeregrinoTop) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getPendingPeregrinoCards() {
        return new ArrayList<>(pendingPeregrinoTop);
    }

    public boolean isAwaitingHumpbackDirection() {
        return awaitingHumpbackDirection;
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

    public List<Card> getPendingPulpoCards() {
        return new ArrayList<>(pendingPulpoOptions);
    }

    public int getHorseshoeDieSides() {
        if (horseshoeSlotIndex < 0 || horseshoeSlotIndex >= board.length) return 0;
        BoardSlot slot = board[horseshoeSlotIndex];
        if (slot.getDice().isEmpty() || horseshoeDieIndex < 0 || horseshoeDieIndex >= slot.getDice().size()) {
            return 0;
        }
        return slot.getDice().get(horseshoeDieIndex).getType().getSides();
    }

    public DieType getHorseshoeDieType() {
        if (horseshoeSlotIndex < 0 || horseshoeSlotIndex >= board.length) return null;
        BoardSlot slot = board[horseshoeSlotIndex];
        if (slot.getDice().isEmpty() || horseshoeDieIndex < 0 || horseshoeDieIndex >= slot.getDice().size()) {
            return null;
        }
        return slot.getDice().get(horseshoeDieIndex).getType();
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

    public List<Integer> consumeRecentlyRerolledSlots() {
        List<Integer> copy = new ArrayList<>(recentlyRerolledSlots);
        recentlyRerolledSlots.clear();
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
            case RELEASE_CHOOSE_SLOT:
                for (int i = 0; i < board.length; i++) {
                    BoardSlot s = board[i];
                    if (s.getCard() != null && !s.isFaceUp()) {
                        highlight.add(i); // todas las boca abajo válidas
                    }
                }
                break;
            case PEZ_BORRON_TARGET:
                for (int i = 0; i < board.length; i++) {
                    BoardSlot s = board[i];
                    if (s.getCard() != null && !s.isFaceUp() && s.getDice().size() < 2) {
                        highlight.add(i);
                    }
                }
                break;
            case FLETAN_HIDE:
                for (int i = 0; i < board.length; i++) {
                    BoardSlot s = board[i];
                    if (s.getCard() != null && s.isFaceUp()
                            && (s.getCard().getType() == CardType.PEZ || s.getCard().getType() == CardType.PEZ_GRANDE)) {
                        highlight.add(i);
                    }
                }
                break;
            case GLASS_BOTTLE_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot adj = board[idx];
                    if (adj.getCard() != null && adj.isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case PEZ_LOBO_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot adj = board[idx];
                    if (adj.getCard() != null && adj.isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case TRUCHA_ARCOIRIS_FLIP:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot adj = board[idx];
                    if (adj.getCard() != null && !adj.isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case PEZ_LEON_TARGET:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) {
                        highlight.add(i);
                    }
                }
                break;
            case BARCO_PESQUERO_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot adj = board[idx];
                    if (adj.getCard() != null && adj.isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;
            case TIGER_SHARK_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot adj = board[idx];
                    if (adj.getCard() != null && adj.isFaceUp()) {
                        highlight.add(idx);
                    }
                }
                break;

            case KOI_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot t = board[idx];
                    if (t.getCard() != null && t.isFaceUp() && t.getDice().size() == 1) {
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
            case BOXER_FROM:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (!board[idx].getDice().isEmpty()) {
                        highlight.add(idx);
                    }
                }
                break;
            case BOXER_TO:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (idx != pendingSelectionAux && board[idx].getCard() != null
                            && board[idx].getDice().size() < 2) {
                        highlight.add(idx);
                    }
                }
                break;
            case MANTIS_TARGET:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) {
                        highlight.add(i);
                    }
                }
                break;
            case BOTTLE_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    if (board[idx].getCard() != null && board[idx].isFaceUp()
                            && board[idx].getCard().getType() == CardType.PEZ) {
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
            case LOCO_TARGET:
                for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
                    BoardSlot target = board[idx];
                    if (target.getCard() != null && target.getDice().size() < 2) {
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
            case ESTURION_PLACE:
                for (int i = 0; i < board.length; i++) {
                    if (board[i].getCard() != null && board[i].getDice().size() < 2) {
                        highlight.add(i);
                    }
                }
                break;
            case SPIDER_CRAB_CHOOSE_SLOT:
                for (int i = 0; i < board.length; i++) {
                    BoardSlot s = board[i];
                    if (s.getCard() != null && !s.isFaceUp() && s.getDice().isEmpty()) {
                        highlight.add(i);
                    }
                }
                break;
            case DECORADOR_CHOOSE_SLOT:
                for (int i = 0; i < board.length; i++) {
                    BoardSlot s = board[i];
                    if (s.getCard() != null && !s.isFaceUp() && s.getDice().isEmpty()) {
                        highlight.add(i);
                    }
                }
                break;
            case HORSESHOE_DIE:
                for (int i = 0; i < board.length; i++) {
                    if (!board[i].getDice().isEmpty()) {
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
    // === UI helpers ===
    private final java.util.List<Integer> remoraBorderSlots = new java.util.ArrayList<>();

    public java.util.List<Integer> getRemoraBorderSlots() {
        return remoraBorderSlots;
    }

    public void setRemoraBorderSlots(java.util.List<Integer> slots) {
        remoraBorderSlots.clear();
        if (slots != null) remoraBorderSlots.addAll(slots);
    }

    public java.util.List<Integer> computeRemoraBorderSlots() {
        java.util.List<Integer> slots = new java.util.ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (shouldShowRemoraBorder(i)) {
                slots.add(i);
            }
        }
        setRemoraBorderSlots(slots);
        return getRemoraBorderSlots();
    }
    // === Bota Vieja (UI) ===
// Devuelve slots que actualmente tienen penalización (-1 a la suma) por al menos 1 Bota Vieja adyacente boca arriba.
    public java.util.List<Integer> computeBotaViejaPenaltySlots() {
        java.util.List<Integer> affected = new java.util.ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getDice() == null || s.getDice().isEmpty()) continue;

            boolean hasPenalty = false;
            for (Integer adj : adjacentIndices(i, true)) { // true = incluye diagonales (consistente con GameUtils hoy)
                BoardSlot a = board[adj];
                if (a.getCard() != null
                        && a.isFaceUp()
                        && a.getCard().getId() == CardId.BOTA_VIEJA) {
                    hasPenalty = true;
                    break;
                }
            }

            if (hasPenalty) {
                affected.add(i);
            }
        }
        return affected;
    }

    // === Auto Hundido (UI) ===
    // Devuelve slots que reciben bonus (+1 a la suma) por al menos 1 Auto Hundido adyacente boca arriba.
    public java.util.List<Integer> computeAutoHundidoBonusSlots() {
        java.util.List<Integer> affected = new java.util.ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getDice() == null || s.getDice().isEmpty()) continue;

            boolean hasBonus = false;
            for (Integer adj : adjacentIndices(i, true)) {
                BoardSlot a = board[adj];
                if (a.getCard() != null
                        && a.isFaceUp()
                        && a.getCard().getId() == CardId.AUTO_HUNDIDO) {
                    hasBonus = true;
                    break;
                }
            }

            if (hasBonus) {
                affected.add(i);
            }
        }
        return affected;
    }

    // === Pez Betta (UI) ===
    // Marca visualmente las filas permitidas mientras haya un Pez betta boca arriba.
    public java.util.List<Integer> computeBettaRowSlots() {
        java.util.Set<Integer> rows = new java.util.HashSet<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.PEZ_BETTA) {
                rows.add(i / 3);
            }
        }
        if (rows.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<Integer> affected = new java.util.ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && rows.contains(i / 3)) {
                affected.add(i);
            }
        }
        return affected;
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
        markRevealed(slotIndex);
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
    public boolean isAwaitingSpiderCrabCardChoice() {
        return pendingSelection == PendingSelection.SPIDER_CRAB_CHOOSE_CARD;
    }
    public String cancelSpiderCrab() {
        if (pendingSelection == PendingSelection.SPIDER_CRAB_CHOOSE_CARD ||
                pendingSelection == PendingSelection.SPIDER_CRAB_CHOOSE_SLOT) {

            pendingSpiderCrabCard = null;
            spiderCrabSlotIndex = -1;
            clearPendingSelection();
            recomputeBottleAdjustments();
            return "Cangrejo araña: acción cancelada.";
        }
        return "No hay acción del Cangrejo araña para cancelar.";
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
            case BOXER_FROM:
                result = chooseBoxerOrigin(slotIndex);
                break;
            case BOXER_TO:
                result = chooseBoxerDestination(slotIndex);
                break;
            case MANTIS_TARGET:
                result = replaceWithMantisDie(slotIndex);
                break;
            case BOTTLE_TARGET:
                result = chooseBottleTarget(slotIndex);
                break;
            case GLASS_BOTTLE_TARGET:
                result = chooseGlassBottleTarget(slotIndex);
                break;
            case ARENQUE_DESTINATION:
                result = placeArenqueFish(slotIndex);
                break;
            case BLUE_WHALE_PLACE:
            case ESTURION_PLACE:
                result = placeBlueWhaleDie(slotIndex);
                break;
            case SPIDER_CRAB_CHOOSE_SLOT:
                result = placeSpiderCrabRevivedCard(slotIndex);
                break;
            case KOI_TARGET:
                result = resolveKoiSwap(pendingSelectionActor, slotIndex);
                break;
            case RELEASE_CHOOSE_SLOT:
                result = resolveReleaseIntoSlot(slotIndex);
                break;
            case DECORADOR_CHOOSE_SLOT:
                result = placeDecoradorObject(slotIndex);
                break;
            case HORSESHOE_DIE:
                result = chooseHorseshoeDie(slotIndex);
                break;
            case PEZ_BORRON_TARGET:
                result = resolvePezBorronMove(slotIndex);
                break;
            case FLETAN_HIDE:
                result = resolveFletanHide(slotIndex);
                break;
            case PEZ_LOBO_TARGET:
                result = resolvePezLoboDiscard(slotIndex);
                break;
            case TRUCHA_ARCOIRIS_FLIP:
                result = resolveTruchaArcoirisFlip(slotIndex);
                break;
            case PEZ_LEON_TARGET:
                result = resolvePezLeonBoost(slotIndex);
                break;
            case BARCO_PESQUERO_TARGET:
                result = resolveFishingBoatDiscard(slotIndex);
                break;
            case TIGER_SHARK_TARGET:
                result = resolveTigerSharkDevour(slotIndex);
                break;
            case LOCO_TARGET:
                result = chooseLocoTarget(slotIndex);
                break;

            default:
                result = "No hay acciones pendientes.";
        }
        // Si al resolver esta selección se abrió otra resolución pendiente, NO encadenar nada todavía.
        if (hasPendingTurnResolutions()) {
            return result;
        }

        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            result = result.isEmpty() ? revealLog : result + " " + revealLog;
        }

// ✅ Safety net: resolver cartas que quedaron con 2 dados por efectos
        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            result = result.isEmpty() ? endTurn : result + " " + endTurn;
        }

        return result;
    }

    private String resolveReleaseIntoSlot(int slotIndex) {
        if (pendingReleaseCard == null) {
            pendingSelection = PendingSelection.NONE;
            return "Liberación cancelada: no hay carta seleccionada.";
        }

        BoardSlot slot = board[slotIndex];

        if (slot.getCard() == null) {
            return "Debes elegir una carta válida del tablero.";
        }

        // Debe ser BOCA ABAJO
        if (slot.isFaceUp()) {
            return "Debes elegir una carta BOCA ABAJO para reemplazar.";
        }

        // 1) Si el slot tenía dados, vuelven a la reserva
        if (!slot.getDice().isEmpty()) {
            for (Die d : new ArrayList<>(slot.getDice())) {
                reserve.add(d.getType());
            }
            slot.clearDice();
        }

        // 2) La carta que estaba en el tablero vuelve al mazo (cardumen)
        Card replaced = slot.getCard();
        if (replaced != null) {
            deck.addLast(replaced); // vuelve al mazo
        }

        // 3) Poner la carta liberada en ese slot BOCA ABAJO
        slot.setCard(pendingReleaseCard);
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus()); // resetea estados del slot

        // 4) Sacarla de capturas (pierdes su puntaje automáticamente porque score se calcula desde captures)
        captures.remove(pendingReleaseCard);

        String name = pendingReleaseCard.getName();
        clearReleaseState();

        // Recalcula cualquier ajuste dependiente del tablero (tú ya lo haces en otras acciones)
        recomputeBottleAdjustments();

        String base = "Liberaste " + name + ". Reemplazo realizado.";
        if (pendingHachaReleaseCount > 0) {
            awaitingHachaReleaseChoice = true;
            pendingHachaReleaseChoices.clear();
            pendingHachaReleaseChoices.addAll(captures);
            return base + " Elige otra carta para liberar (" + pendingHachaReleaseCount + " restante).";
        }
        return base;
    }

    private void clearReleaseState() {
        pendingReleaseCard = null;
        pendingSelection = PendingSelection.NONE;
        pendingSelectionActor = -1;
        pendingSelectionAux = -1;
    }


    private String resolveKoiSwap(int koiSlotIndex, int targetIndex) {
        if (!adjacentIndices(koiSlotIndex, true).contains(targetIndex)) {
            return "Koi: debes elegir una carta adyacente.";
        }

        BoardSlot koi = board[koiSlotIndex];
        BoardSlot target = board[targetIndex];

        if (koi.getDice().isEmpty()) {
            clearPendingSelection();
            return "Koi: ya no tiene dado para intercambiar.";
        }

        if (target.getCard() == null || !target.isFaceUp() || target.getDice().size() != 1) {
            return "Koi: elige una carta boca arriba adyacente con exactamente 1 dado.";
        }

        Die fromKoi = koi.removeDie(koi.getDice().size() - 1);
        Die fromTarget = target.removeDie(0);

        koi.addDie(fromTarget);
        target.addDie(fromKoi);

        clearPendingSelection();
        recomputeBottleAdjustments();

        return "Koi intercambió un dado con una carta adyacente.";
    }

    private String resolveFishingBoatDiscard(int slotIndex) {
        if (pendingSelection != PendingSelection.BARCO_PESQUERO_TARGET) {
            return "No hay selección pendiente del Barco pesquero.";
        }
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return "Barco pesquero: debes elegir una carta adyacente.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || !target.isFaceUp()) {
            return "Barco pesquero: selecciona una carta boca arriba.";
        }
        clearPendingSelection();
        return discardCardAsFailed(slotIndex, "Barco pesquero descartó una carta adyacente.");
    }

    private String placeSpiderCrabRevivedCard(int slotIndex) {
        if (pendingSelection != PendingSelection.SPIDER_CRAB_CHOOSE_SLOT) {
            return "No hay colocación pendiente del Cangrejo araña.";
        }
        if (pendingSpiderCrabCard == null) {
            clearPendingSelection();
            return "No hay carta recuperada para colocar.";
        }
        if (slotIndex < 0 || slotIndex >= board.length) {
            return "Selecciona una casilla válida.";
        }

        BoardSlot slot = board[slotIndex];

        // Debe existir carta y estar boca abajo
        if (slot.getCard() == null || slot.isFaceUp()) {
            return "Debes elegir una carta boca abajo para reemplazar.";
        }

        // Para evitar inconsistencias: la carta objetivo debe estar sin dados
        if (!slot.getDice().isEmpty()) {
            return "Debes elegir una carta boca abajo sin dados para reemplazar.";
        }

        // REEMPLAZO: la carta anterior vuelve al mazo (no desaparece)
        Card replaced = slot.getCard();
        deck.push(replaced);
        shuffleDeck();

        // Colocar la recuperada boca abajo
        slot.setCard(pendingSpiderCrabCard);
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        slot.clearDice(); // por seguridad, aunque ya estaba vacía

        pendingSpiderCrabCard = null;
        spiderCrabSlotIndex = -1;
        clearPendingSelection();

        recomputeBottleAdjustments();
        return "Cangrejo araña colocó la carta recuperada boca abajo, reemplazando una carta boca abajo y devolviendo la reemplazada al mazo.";
    }

    public String chooseDecoradorCard(int index) {
        if (!awaitingDecoradorChoice) {
            return "No hay selección pendiente del Cangrejo decorador.";
        }
        if (index < 0 || index >= pendingDecoradorOptions.size()) {
            return "Debes elegir un objeto válido del mazo.";
        }
        pendingDecoradorCard = pendingDecoradorOptions.get(index);
        if (!deck.remove(pendingDecoradorCard)) {
            clearDecoradorState();
            shuffleDeck();
            return "El objeto elegido ya no está disponible en el mazo.";
        }
        pendingDecoradorOptions.clear();
        awaitingDecoradorChoice = false;
        return queueableSelection(
                PendingSelection.DECORADOR_CHOOSE_SLOT,
                decoradorSlotIndex,
                "Cangrejo decorador: elige una carta boca abajo sin dados para reemplazar.");
    }

    public String cancelDecoradorAbility() {
        if (!awaitingDecoradorChoice) {
            return "No hay acción del Cangrejo decorador para cancelar.";
        }
        clearDecoradorState();
        return "Cangrejo decorador: acción cancelada.";
    }

    private String placeDecoradorObject(int slotIndex) {
        if (pendingSelection != PendingSelection.DECORADOR_CHOOSE_SLOT) {
            return "No hay colocación pendiente del Cangrejo decorador.";
        }
        if (pendingDecoradorCard == null) {
            clearPendingSelection();
            return "No hay objeto seleccionado para colocar.";
        }
        if (slotIndex < 0 || slotIndex >= board.length) {
            return "Selecciona una casilla válida.";
        }
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null || slot.isFaceUp()) {
            return "Debes elegir una carta boca abajo para reemplazar.";
        }
        if (!slot.getDice().isEmpty()) {
            return "Debes elegir una carta boca abajo sin dados.";
        }
        Card replaced = slot.getCard();
        deck.push(replaced);
        shuffleDeck();
        slot.setCard(pendingDecoradorCard);
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        slot.clearDice();
        clearDecoradorState();
        clearPendingSelection();
        recomputeBottleAdjustments();
        return "Cangrejo decorador reemplazó una carta boca abajo con un objeto del mazo.";
    }

    private void clearDecoradorState() {
        pendingDecoradorOptions.clear();
        pendingDecoradorCard = null;
        awaitingDecoradorChoice = false;
        decoradorSlotIndex = -1;
    }

    public String chooseViolinistCard(int index) {
        if (!awaitingViolinistChoice) {
            return "No hay selección pendiente del Cangrejo violinista.";
        }
        if (index < 0 || index >= failedDiscards.size()) {
            return "Debes elegir una carta descartada válida.";
        }
        Card chosen = failedDiscards.remove(index);
        captures.add(chosen);
        awaitingViolinistChoice = false;
        return "Cangrejo violinista capturó directamente " + chosen.getName() + ".";
    }

    public String cancelViolinistAbility() {
        if (!awaitingViolinistChoice) {
            return "No hay acción del Cangrejo violinista para cancelar.";
        }
        awaitingViolinistChoice = false;
        return "Cangrejo violinista: acción cancelada.";
    }

    private String chooseHorseshoeDie(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "Elige una carta con un dado para ajustar.";
        }
        horseshoeSlotIndex = slotIndex;
        horseshoeDieIndex = slot.getDice().size() - 1;
        awaitingHorseshoeValue = true;
        suspendPendingSelection();
        return "Cangrejo herradura: elige el nuevo valor del dado.";
    }

    public String chooseHorseshoeValue(int value) {
        if (!awaitingHorseshoeValue) {
            return "No hay ajuste pendiente del Cangrejo herradura.";
        }
        if (horseshoeSlotIndex < 0 || horseshoeSlotIndex >= board.length) {
            clearHorseshoeState();
            return "El dado a ajustar ya no está disponible.";
        }
        BoardSlot slot = board[horseshoeSlotIndex];
        if (slot.getDice().isEmpty() || horseshoeDieIndex < 0 || horseshoeDieIndex >= slot.getDice().size()) {
            clearHorseshoeState();
            return "El dado a ajustar ya no está disponible.";
        }
        Die die = slot.getDice().get(horseshoeDieIndex);
        int sides = die.getType().getSides();
        if (value < 1 || value > sides) {
            return "Elige un valor dentro del rango del dado.";
        }
        slot.setDie(horseshoeDieIndex, new Die(die.getType(), value));
        String msg = "Cangrejo herradura ajustó el dado a " + value + ".";
        clearHorseshoeState();
        advancePendingSelectionQueue();

        if (hasPendingTurnResolutions()) {
            return msg;
        }

        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
        }

        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }

        return msg;
    }

    private void clearHorseshoeState() {
        horseshoeSlotIndex = -1;
        horseshoeDieIndex = -1;
        awaitingHorseshoeValue = false;
    }

    private void clearBoxerState() {
        awaitingBoxerDecision = false;
        boxerMovesRemaining = 0;
        boxerSlotIndex = -1;
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
        String message = (reroll ? "Atún relanzó el dado " : "Atún conserva el dado ")
                + "(" + finalDie.getLabel() + "). Elige una carta para reposicionarlo.";
        return queueableSelection(PendingSelection.ATUN_DESTINATION, atunSlotIndex, message);
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

            String msg = "Omitiste la habilidad de la Jaiba azul.";

            // ✅ Reanudar cadena de revelaciones
            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            // ✅ Safety net
            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;
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

            String msg = "Omitiste la habilidad del Pez globo.";

            // ✅ Reanudar cadena de revelaciones
            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            // ✅ Safety net
            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;
        }

        awaitingBlowfishDecision = false;
        String msg = queueableSelection(
                PendingSelection.BLOWFISH,
                blowfishSlotIndex,
                "Pez globo: elige un dado para inflarlo a su valor máximo.");
        blowfishSlotIndex = -1;
        return msg;
    }

    public String choosePezLoboUse(boolean useAbility) {
        if (!awaitingPezLoboDecision) {
            return "No hay decisión pendiente del Pez Lobo.";
        }
        if (pezLoboSlotIndex < 0 || pezLoboSlotIndex >= board.length) {
            awaitingPezLoboDecision = false;
            pezLoboSlotIndex = -1;
            return "El Pez Lobo ya no está disponible.";
        }
        awaitingPezLoboDecision = false;
        if (!useAbility) {
            pezLoboSlotIndex = -1;

            String msg = "Omitiste la habilidad del Pez Lobo.";

            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;
        }

        String msg = startPezLoboDiscard(pezLoboSlotIndex);
        pezLoboSlotIndex = -1;
        if (pendingSelection == PendingSelection.NONE && pendingSelectionQueue.isEmpty()) {
            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }
        }
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

            String msg = "Pez vela conserva el " + current.getLabel() + ".";

            // ✅ Reanudar cadena de revelaciones
            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            // ✅ Safety net: resolver cartas que quedaron con 2 dados por efectos
            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;
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

// ✅ Si esto abrió una resolución pendiente, NO encadenar nada todavía.
        if (hasPendingTurnResolutions()) {
            return msg;
        }

// ✅ Reanudar cadena de revelaciones (Mero gigante / Pez volador / etc.)
        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
        }

// ✅ Safety net
        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }

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

        pendingDieLossSlot = null;
        pendingLossTriggerValue = 0;

        String result = "La pesca falló y perdiste " + lost.getLabel();
        String msg = checkDefeatOrContinue(result);

// Si se abrió otra resolución pendiente, no seguimos.
        if (hasPendingTurnResolutions()) {
            return msg;
        }

        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }
        return msg;

    }

    public String rollFromReserve(DieType type) {
        clearTransientVisualMarks();
        if (gameOver) {
            return "La partida ha terminado";
        }
        if (awaitingSepiaChoice) {
            return "Resuelve primero la captura de la Sepia.";
        }
        if (awaitingDragnetReleaseChoice) {
            return "Resuelve primero la liberación de la Red de arrastre.";
        }
        if (awaitingHachaReleaseChoice) {
            return "Libera las cartas pendientes antes de lanzar otro dado.";
        }
        if (awaitingDamiselasChoice) {
            return "Resuelve el ordenamiento de Damiselas antes de lanzar otro dado.";
        }
        if (awaitingPeregrinoChoice) {
            return "Resuelve la selección del Tiburón Peregrino antes de lanzar otro dado.";
        }
        if (awaitingHumpbackDirection) {
            return "Elige la dirección de la marea antes de lanzar otro dado.";
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
        if (awaitingMantisDecision || awaitingMantisLostDieChoice) {
            return "Resuelve la habilidad del Langostino mantis antes de continuar.";
        }
        if (awaitingPezLoboDecision) {
            return "Decide primero si activarás la habilidad del Pez Lobo.";
        }
        if (awaitingLangostaRecovery) {
            return "Resuelve la recuperación de la Langosta espinosa antes de continuar.";
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
        if (awaitingDecoradorChoice) {
            return "Elige el objeto del Cangrejo decorador antes de continuar.";
        }
        if (awaitingViolinistChoice) {
            return "Elige la carta del Cangrejo violinista antes de continuar.";
        }
        if (awaitingPulpoChoice) {
            return "Elige la carta para reemplazar al Pulpo antes de lanzar otro dado.";
        }
        if (awaitingValueAdjustment) {
            return "Resuelve el ajuste pendiente antes de lanzar otro dado.";
        }
        if (awaitingHorseshoeValue) {
            return "Resuelve el ajuste del Cangrejo herradura antes de lanzar otro dado.";
        }
        if (awaitingBoxerDecision) {
            return "Decide si moverás otro dado con el Cangrejo boxeador.";
        }
        if (awaitingGhostShrimpDecision) {
            return "Decide primero si intercambiar las cartas vistas por el Camarón fantasma.";
        }
        if ((pendingSelection == PendingSelection.BLUE_WHALE_PLACE
                || pendingSelection == PendingSelection.ESTURION_PLACE)
                && !pendingBallenaDice.isEmpty()) {
            return pendingSelection == PendingSelection.ESTURION_PLACE
                    ? "Coloca los dados pendientes del Esturión antes de continuar."
                    : "Coloca los dados pendientes de la Ballena azul antes de continuar.";
        }
        if (selectedDie != null) {
            return "Coloca el dado ya lanzado antes de lanzar otro.";
        }
        if (!reserve.remove(type)) {
            return "No hay más dados " + type.getLabel();
        }
        selectedDie = Die.roll(type, rng);
        String base = "Lanzaste " + selectedDie.getLabel();
        String boat = triggerFishingBoatOnRoll(selectedDie.getValue());
        if (!boat.isEmpty()) {
            base += " " + boat;
        }
        return base;
    }

    public String placeSelectedDie(int slotIndex) {
        lastDiePlaced = false;
        if (selectedDie == null) {
            return "No has lanzado ningún dado.";
        }
        if (gameOver) {
            return "La partida ha terminado";
        }
        if (awaitingSepiaChoice) {
            return "Resuelve primero la captura de la Sepia.";
        }
        if (awaitingDragnetReleaseChoice) {
            return "Resuelve primero la liberación de la Red de arrastre.";
        }
        if (awaitingHachaReleaseChoice) {
            return "Libera las cartas pendientes antes de colocar otro dado.";
        }
        if (awaitingDamiselasChoice) {
            return "Resuelve el ordenamiento de Damiselas antes de colocar otro dado.";
        }
        if (awaitingPeregrinoChoice) {
            return "Resuelve la selección del Tiburón Peregrino antes de colocar otro dado.";
        }
        if (awaitingHumpbackDirection) {
            return "Elige la dirección de la marea antes de colocar otro dado.";
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
        if (awaitingPezLoboDecision) {
            return "Decide primero si usarás la habilidad del Pez Lobo.";
        }
        if (awaitingMantisDecision || awaitingMantisLostDieChoice) {
            return "Resuelve primero la habilidad del Langostino Mantis.";
        }
        if (awaitingLangostaRecovery) {
            return "Resuelve primero la recuperación de la Langosta espinosa.";
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
        if (awaitingDecoradorChoice) {
            return "Elige el objeto del Cangrejo decorador antes de colocar dados.";
        }
        if (awaitingViolinistChoice) {
            return "Elige la carta del Cangrejo violinista antes de colocar dados.";
        }
        if (awaitingPulpoChoice) {
            return "Elige primero la carta que reemplazará al Pulpo.";
        }
        if (awaitingValueAdjustment) {
            return "Resuelve el ajuste pendiente antes de colocar dados.";
        }
        if (awaitingHorseshoeValue) {
            return "Resuelve el ajuste del Cangrejo herradura antes de colocar dados.";
        }
        if (awaitingBoxerDecision) {
            return "Decide si moverás otro dado con el Cangrejo boxeador.";
        }
        if (awaitingGhostShrimpDecision) {
            return "Decide si intercambiar las cartas vistas por el Camarón fantasma antes de continuar.";
        }
        if ((pendingSelection == PendingSelection.BLUE_WHALE_PLACE
                || pendingSelection == PendingSelection.ESTURION_PLACE)
                && !pendingBallenaDice.isEmpty()) {
            return pendingSelection == PendingSelection.ESTURION_PLACE
                    ? "Primero coloca los dados pendientes del Esturión."
                    : "Primero coloca los dados pendientes de la Ballena azul.";
        }
        if (!isBettaRowAllowed(slotIndex)) {
            return "Pez betta: solo puedes colocar dados en su fila.";
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
        lastDiePlaced = true;
        int placedValue = selectedDie.getValue();
        String cocoLoss = applyCoconutCrabLoss(slotIndex, placedValue);
        selectedDie = null;
        if (forcedSlotIndex != null && slotIndex == forcedSlotIndex) {
            forcedSlotIndex = null;
        }

        StringBuilder extraLog = new StringBuilder();
        if (!cocoLoss.isEmpty()) {
            extraLog.append(" ").append(cocoLoss);
        }
        if (!slot.isFaceUp()) {
            if (canRevealSlot(slotIndex)) {
                slot.setFaceUp(true);
                slot.getStatus().calamarForcedFaceDown = false;
                markRevealed(slotIndex);
                String reveal = handleOnReveal(slotIndex, placedValue);
                if (!reveal.isEmpty()) {
                    extraLog.append(" ").append(reveal);
                }
            } else {
                extraLog.append(" Derrame de petróleo impide revelar la carta.");
            }
        }
        String pistolLog = tryPistolShrimpReposition(slotIndex);
        if (!pistolLog.isEmpty()) {
            extraLog.append(" ").append(pistolLog);
        }
        String bottleLog = applyBottleEffectsOnPlacement(slotIndex);
        if (!bottleLog.isEmpty()) {
            extraLog.append(" ").append(bottleLog);
        }

        if (slot.getDice().size() < 2) {
            String msg = "Necesitas otro dado para intentar la pesca.";
            String corrientes = buildCurrentsLog(placedValue);
            if (!corrientes.isEmpty()) {
                msg += " " + corrientes;
            }
            return checkDefeatOrContinue(msg + extraLog);
        }

        return resolveFishingOutcome(slotIndex, placedValue, extraLog.toString(), true);
    }

    public boolean consumeLastDiePlaced() {
        boolean placed = lastDiePlaced;
        lastDiePlaced = false;
        return placed;
    }

    private String addDieToSlot(int slotIndex, Die die) {
        return addDieToSlotInternal(slotIndex, die, true);
    }

    private String addDieToSlotInternal(int slotIndex, Die die, boolean allowPistolEffect) {
        BoardSlot target = board[slotIndex];
        target.addDie(die);
        String coconutLog = applyCoconutCrabLoss(slotIndex, die.getValue());
        String revealLog = "";
        boolean revealed = false;
        if (target.getCard() != null && !target.isFaceUp()) {
            if (canRevealSlot(slotIndex)) {
                target.setFaceUp(true);
                target.getStatus().calamarForcedFaceDown = false;
                markRevealed(slotIndex);
                String reveal = handleOnReveal(slotIndex, die.getValue());
                revealLog = reveal == null ? "" : reveal;
                revealed = true;
            } else {
                revealLog = "Derrame de petróleo impide revelar la carta.";
            }
        }
        if (revealed) {
            recomputeBottleAdjustments();
        }
        if (!coconutLog.isEmpty()) {
            if (!revealLog.isEmpty()) {
                revealLog = coconutLog + " " + revealLog;
            } else {
                revealLog = coconutLog;
            }
        }
        if (allowPistolEffect) {
            String pistolLog = tryPistolShrimpReposition(slotIndex);
            if (!pistolLog.isEmpty()) {
                return revealLog.isEmpty() ? pistolLog : revealLog + " " + pistolLog;
            }
        }
        String bottleLog = applyBottleEffectsOnPlacement(slotIndex);
        if (!bottleLog.isEmpty()) {
            return revealLog.isEmpty() ? bottleLog : revealLog + " " + bottleLog;
        }
        if (!coconutLog.isEmpty()) {
            return revealLog;
        }
        if (target.getCard() == null || target.isFaceUp()) {
            String outcome = target.getDice().size() < 2
                    ? ""
                    : resolveFishingOutcome(slotIndex, die.getValue(), "", false);
            if (revealLog.isEmpty()) {
                return outcome;
            }
            if (outcome.isEmpty()) {
                return revealLog;
            }
            return revealLog + " " + outcome;
        }
        return revealLog;
    }

    private String applyCoconutCrabLoss(int slotIndex, int placedValue) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null || slot.getCard().getId() != CardId.JAIBA_GIGANTE_DE_COCO) {
            return "";
        }
        if (placedValue >= 7) {
            return "";
        }
        if (!slot.getDice().isEmpty()) {
            Die lost = slot.removeDie(slot.getDice().size() - 1);
            lostDice.add(lost);
            return "Jaiba gigante de coco: el dado se perdió automáticamente.";
        }
        return "";
    }

    private String tryPistolShrimpReposition(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null || slot.getCard().getId() != CardId.CAMARON_PISTOLA) {
            return "";
        }
        if (slot.getDice().isEmpty()) {
            return "";
        }
        if (!rng.nextBoolean()) {
            return "";
        }

        Die moved = slot.removeDie(slot.getDice().size() - 1);
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (i == slotIndex) continue;
            BoardSlot target = board[i];
            if (target.getCard() != null && target.getDice().size() < 2) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            slot.addDie(moved);
            return "Camarón pistola no pudo reposicionar el dado.";
        }

        int targetIndex = candidates.get(rng.nextInt(candidates.size()));
        String reveal = addDieToSlotInternal(targetIndex, moved, false);
        return reveal.isEmpty()
                ? "Camarón pistola reposicionó un dado."
                : "Camarón pistola reposicionó un dado. " + reveal;
    }

    private String resolveFishingOutcome(int slotIndex, int triggerValue, String extraLog, boolean applyCurrents) {
        BoardSlot slot = board[slotIndex];

        if (slot.getDice().size() < 2) {
            return extraLog == null ? "" : extraLog;
        }

        StringBuilder log = new StringBuilder(extraLog == null ? "" : extraLog);

        // 1) Resolver captura/fallo ANTES de corrientes (para que slotIndex sea consistente)
        String coreResult;
        if (slot.getCard().getCondition().isSatisfied(slotIndex, this)) {
            String onCaptureLog = capture(slotIndex);
            coreResult = "¡Captura exitosa!" + onCaptureLog;
        } else if (slot.getStatus().protectedOnce) {
            coreResult = handleProtectedFailure(slotIndex);
        } else if (isDelfinProtectionActive(slotIndex)) {
            coreResult = handleDelfinProtection(slotIndex);
        } else if (isHookActive()) {
            markHookPenaltyUsed();
            coreResult = handleFailedCatchImmediate(slotIndex, true);
        } else {
            pendingDieLossSlot = slotIndex;
            pendingLossTriggerValue = triggerValue;
            coreResult = "La pesca falló. Elige qué dado perder.";
        }

        // 2) Ahora sí, aplicar corrientes (pueden mover el tablero y remapear estados pendientes)
        if (applyCurrents) {
            String corrientes = buildCurrentsLog(triggerValue);
            if (!corrientes.isEmpty()) {
                if (log.length() > 0) log.append(" ");
                log.append(corrientes);
            }
        }

        String morenaAutoCancel = autoCancelInvalidMorenaSelection();
        if (!morenaAutoCancel.isEmpty()) {
            if (log.length() > 0) log.append(" ");
            log.append(morenaAutoCancel);
        }

        String boxerAutoCancel = autoCancelInvalidBoxerSelection();
        if (!boxerAutoCancel.isEmpty()) {
            if (log.length() > 0) log.append(" ");
            log.append(boxerAutoCancel);
        }

        String percebesAutoCancel = autoCancelInvalidPercebesSelection();
        if (!percebesAutoCancel.isEmpty()) {
            if (log.length() > 0) log.append(" ");
            log.append(percebesAutoCancel);
        }

        // 3) Unir mensajes
        String result = coreResult;
        if (log.length() > 0) result += " " + log;

        return checkDefeatOrContinue(result.trim());
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
            if (!slot.isFaceUp() && !canRevealSlot(slotIndex)) {
                return;
            }
            slot.setFaceUp(!slot.isFaceUp());
        }
    }

    public int getScore() {
        int sum = 0;
        int crustaceos = 0, peces = 0, pecesGrandes = 0, objetos = 0;
        int krillCount = 0, sardinaCount = 0, tiburonMartilloCount = 0, limpiadorCount = 0, tiburonBallenaCount = 0;
        int copepodoCount = 0, congrioCount = 0, fosaAbisalCount = 0;
        for (Card c : captures) {
            sum += c.getPoints();
            switch (c.getType()) {
                case CRUSTACEO: crustaceos++; break;
                case PEZ: peces++; break;
                case PEZ_GRANDE: pecesGrandes++; break;
                case OBJETO: objetos++; break;
            }
            if (c.getId() == CardId.KRILL) krillCount++;
            if (c.getId() == CardId.COPEPODO_BRILLANTE) copepodoCount++;
            if (c.getId() == CardId.SARDINA) sardinaCount++;
            if (c.getId() == CardId.CONGRIO) congrioCount++;
            if (c.getId() == CardId.TIBURON_MARTILLO) tiburonMartilloCount++;
            if (c.getId() == CardId.LIMPIADOR_MARINO) limpiadorCount++;
            if (c.getId() == CardId.TIBURON_BALLENA) tiburonBallenaCount++;
            if (c.getId() == CardId.FOSA_ABISAL) fosaAbisalCount++;
        }

        int crustaceosFallados = 0;
        int pecesFallados = 0;
        int objetosFallados = 0;
        for (Card c : failedDiscards) {
            if (c.getType() == CardType.CRUSTACEO) {
                crustaceosFallados++;
            } else if (c.getType() == CardType.PEZ) {
                pecesFallados++;
            } else if (c.getType() == CardType.OBJETO) {
                objetosFallados++;
            }
        }

        sum += krillCount * crustaceos;
        sum += copepodoCount * crustaceosFallados;
        sum += sardinaCount * peces;
        sum += congrioCount * pecesFallados;
        sum += tiburonMartilloCount * pecesGrandes * 2;
        sum += limpiadorCount * objetos * 2;
        sum += fosaAbisalCount * objetosFallados;
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

    private String discardCardAsFailed(int slotIndex, String reason) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) {
            return "";
        }
        List<Die> dice = new ArrayList<>(slot.getDice());
        List<Die> toLose = new ArrayList<>();
        List<Die> toSave = new ArrayList<>();
        if (!dice.isEmpty()) {
            dice.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
            toLose.add(dice.remove(0));
            toSave.addAll(dice);
        }
        if (!toLose.isEmpty()) {
            lostDice.addAll(toLose);
        }
        for (Die d : toSave) {
            reserve.add(d.getType());
        }
        failedDiscards.add(slot.getCard());
        slot.clearDice();
        slot.setCard(deck.isEmpty() ? null : deck.pop());
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        recomputeBottleAdjustments();
        String base = reason == null || reason.isEmpty() ? "Carta descartada." : reason;
        if (toLose.isEmpty()) {
            return base;
        }
        return base + " Perdiste " + toLose.get(0).getLabel() + ".";
    }

    private String handleProtectedFailure(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        slot.getStatus().protectedOnce = false;
        slot.getStatus().protectedBySlot = -1;
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
        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            base = base.isEmpty() ? revealLog : base + " " + revealLog;
        }
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
        CurrentDirection deepDirection = getDeepCurrentDirection(placedValue);
        boolean triggered = placedValue == 1 || deepDirection != null;
        if (triggered && isHumpbackActive()) {
            awaitingHumpbackDirection = true;
            pendingHumpbackSlot = findHumpbackSlot();
            return "Ballena jorobada: elige la dirección de la marea.";
        }
        if (placedValue == 1) {
            enqueueCurrentAnimation(CurrentDirection.UP);
        }
        if (deepDirection != null) {
            enqueueCurrentAnimation(deepDirection);
        }
        return "";
    }

    private String applyCurrent(CurrentDirection direction) {
        if (isCarpaDoradaActive()) {
            return applyDiceOnlyCurrent(direction);
        }
        List<Card> toShuffle = new ArrayList<>();
        List<Die> lostFromBoard = new ArrayList<>();
        Integer newForcedSlot = forcedSlotIndex;
        BoardSlot[] newBoard = new BoardSlot[9];
        int[] indexMap = new int[9];
        Arrays.fill(indexMap, -1);
        for (int i = 0; i < 9; i++) newBoard[i] = new BoardSlot();
        java.util.Set<Integer> protectedColumns = getPezPiedraColumns();

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int idx = r * 3 + c;
                BoardSlot src = board[idx];
                int targetR = r, targetC = c;
                boolean protectedColumn = protectedColumns.contains(c);
                switch (direction) {
                    case UP: targetR = r - 1; break;
                    case DOWN: targetR = r + 1; break;
                    case LEFT: targetC = c - 1; break;
                    case RIGHT: targetC = c + 1; break;
                }
                if (protectedColumn || (protectedColumns.contains(targetC) && targetC != c)) {
                    targetR = r;
                    targetC = c;
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
        if (!lostFromBoard.isEmpty()) {
            baseLog += " y perdiste " + formatDiceList(lostFromBoard) + ".";
        }
        String fletan = triggerFletanAfterCurrent();
        if (!fletan.isEmpty()) {
            baseLog = baseLog.isEmpty() ? fletan : baseLog + " " + fletan;
        }
        return baseLog;
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

    private String triggerFletanAfterCurrent() {
        int fletanSlot = -1;
        for (int i = 0; i < board.length; i++) {
            BoardSlot slot = board[i];
            if (slot.getCard() != null && slot.isFaceUp() && slot.getCard().getId() == CardId.FLETAN) {
                fletanSlot = i;
                break;
            }
        }
        if (fletanSlot < 0) {
            return "";
        }
        boolean hasTarget = false;
        for (int i = 0; i < board.length; i++) {
            if (i == fletanSlot) continue;
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp()
                    && (s.getCard().getType() == CardType.PEZ || s.getCard().getType() == CardType.PEZ_GRANDE)) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return hideFletanAndRecover(fletanSlot, "Fletan no encontró peces para ocultar.");
        }
        pendingFletanSlot = fletanSlot;
        pendingFletanActive = true;
        return queueableSelection(
                PendingSelection.FLETAN_HIDE,
                fletanSlot,
                "Fletan: elige un pez boca arriba para ocultarlo.");
    }

    private String resolveFletanHide(int slotIndex) {
        if (!pendingFletanActive || pendingFletanSlot < 0) {
            clearPendingSelection();
            return "Fletan: no hay acción pendiente.";
        }
        BoardSlot target = board[slotIndex];
        if (slotIndex == pendingFletanSlot) {
            return "Fletan: no puedes elegir al propio Fletan.";
        }
        if (target.getCard() == null || !target.isFaceUp()
                || (target.getCard().getType() != CardType.PEZ && target.getCard().getType() != CardType.PEZ_GRANDE)) {
            return "Fletan: selecciona un pez boca arriba.";
        }
        for (Die d : new ArrayList<>(target.getDice())) {
            reserve.add(d.getType());
        }
        target.clearDice();
        target.setFaceUp(false);

        String log = "Fletan ocultó " + target.getCard().getName() + " y recuperó sus dados.";
        String fletanLog = hideFletanAndRecover(pendingFletanSlot, "");
        clearPendingSelection();
        return fletanLog.isEmpty() ? log : log + " " + fletanLog;
    }

    private String hideFletanAndRecover(int fletanSlot, String prefix) {
        BoardSlot fletan = board[fletanSlot];
        for (Die d : new ArrayList<>(fletan.getDice())) {
            reserve.add(d.getType());
        }
        fletan.clearDice();
        fletan.setFaceUp(false);
        pendingFletanActive = false;
        pendingFletanSlot = -1;
        String base = "Fletan se ocultó y recuperó sus dados.";
        if (prefix == null || prefix.isEmpty()) {
            return base;
        }
        return prefix + " " + base;
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

        pezLoboSlotIndex = remapIndex(pezLoboSlotIndex, indexMap);
        if (awaitingPezLoboDecision && pezLoboSlotIndex < 0) {
            awaitingPezLoboDecision = false;
        }

        adjustmentSlotIndex = remapIndex(adjustmentSlotIndex, indexMap);
        if (awaitingValueAdjustment && adjustmentSlotIndex < 0) {
            awaitingValueAdjustment = false;
            adjustmentDieIndex = -1;
            adjustmentAmount = 0;
            adjustmentSource = null;
        }
        horseshoeSlotIndex = remapIndex(horseshoeSlotIndex, indexMap);
        if (awaitingHorseshoeValue && horseshoeSlotIndex < 0) {
            clearHorseshoeState();
        }

        boxerSlotIndex = remapIndex(boxerSlotIndex, indexMap);
        if (awaitingBoxerDecision && boxerSlotIndex < 0) {
            clearBoxerState();
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
        // ✅ Remap pending reveal chain indices after currents
        if (!pendingRevealChain.isEmpty()) {
            java.util.Deque<Integer> remapped = new java.util.ArrayDeque<>();
            for (Integer idx : pendingRevealChain) {
                int m = remapIndex(idx, indexMap);
                if (m >= 0) remapped.add(m);
            }
            pendingRevealChain.clear();
            pendingRevealChain.addAll(remapped);
        }

        pendingAbilityConfirmation = remapAbilityActivation(pendingAbilityConfirmation, indexMap);
        if (!pendingAbilityQueue.isEmpty()) {
            java.util.Deque<AbilityActivation> remapped = new java.util.ArrayDeque<>();
            for (AbilityActivation activation : pendingAbilityQueue) {
                AbilityActivation remappedActivation = remapAbilityActivation(activation, indexMap);
                if (remappedActivation != null) {
                    remapped.add(remappedActivation);
                }
            }
            pendingAbilityQueue.clear();
            pendingAbilityQueue.addAll(remapped);
        }

    }

    private AbilityActivation remapAbilityActivation(AbilityActivation activation, int[] indexMap) {
        if (activation == null) return null;
        int remapped = remapIndex(activation.slotIndex, indexMap);
        if (remapped < 0) return null;
        return new AbilityActivation(
                activation.trigger,
                remapped,
                activation.placedValue,
                activation.diceOnCard,
                activation.card,
                activation.detail
        );
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

    private CurrentDirection getDeepCurrentDirection(int placedValue) {
        for (BoardSlot slot : board) {
            if (!slot.isFaceUp() || slot.getCard() == null || slot.getCard().getId() != CardId.CORRIENTES_PROFUNDAS) {
                continue;
            }
            for (Die d : slot.getDice()) {
                if (d.getValue() == placedValue) {
                    return placedValue % 2 == 0 ? CurrentDirection.RIGHT : CurrentDirection.LEFT;
                }
            }
        }
        return null;
    }

    private boolean isDeepCurrentTriggered(int placedValue) {
        for (BoardSlot slot : board) {
            if (!slot.isFaceUp() || slot.getCard() == null || slot.getCard().getId() != CardId.CORRIENTES_PROFUNDAS) {
                continue;
            }
            for (Die d : slot.getDice()) {
                if (d.getValue() == placedValue) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHumpbackActive() {
        return findHumpbackSlot() >= 0;
    }

    private int findHumpbackSlot() {
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.BALLENA_JOROBADA) {
                return i;
            }
        }
        return -1;
    }

    public String chooseHumpbackDirection(String direction) {
        if (!awaitingHumpbackDirection) {
            return "No hay dirección pendiente de la Ballena jorobada.";
        }
        CurrentDirection dir;
        switch (direction) {
            case "UP":
                dir = CurrentDirection.UP;
                break;
            case "DOWN":
                dir = CurrentDirection.DOWN;
                break;
            case "LEFT":
                dir = CurrentDirection.LEFT;
                break;
            case "RIGHT":
                dir = CurrentDirection.RIGHT;
                break;
            default:
                return "Dirección inválida para la marea.";
        }
        awaitingHumpbackDirection = false;
        pendingHumpbackSlot = -1;
        enqueueCurrentAnimation(dir);
        return "Ballena jorobada: la marea se está formando.";
    }

    private String triggerFishingBoatOnRoll(int rolledValue) {
        for (int i = 0; i < board.length; i++) {
            BoardSlot slot = board[i];
            if (slot.getCard() == null || !slot.isFaceUp() || slot.getCard().getId() != CardId.BARCO_PESQUERO) {
                continue;
            }
            for (Die d : slot.getDice()) {
                if (d.getValue() == rolledValue) {
                    List<Integer> targets = new ArrayList<>();
                    for (Integer idx : adjacentIndices(i, true)) {
                        BoardSlot adj = board[idx];
                        if (adj.getCard() != null && adj.isFaceUp()) {
                            targets.add(idx);
                        }
                    }
                    if (targets.isEmpty()) {
                        return "Barco pesquero no encontró cartas adyacentes.";
                    }
                    if (targets.size() == 1) {
                        return discardCardAsFailed(targets.get(0), "Barco pesquero descartó una carta adyacente.");
                    }
                    return queueableSelection(
                            PendingSelection.BARCO_PESQUERO_TARGET,
                            i,
                            "Barco pesquero: elige una carta adyacente boca arriba para descartarla.");
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

    private boolean isOilSpillActive() {
        for (BoardSlot s : board) {
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.DERRAME_PETROLEO) {
                return true;
            }
        }
        return false;
    }

    private boolean canRevealSlot(int slotIndex) {
        if (!isOilSpillActive()) {
            return true;
        }
        return false;
    }

    private boolean isBettaRowAllowed(int slotIndex) {
        boolean hasBetta = false;
        int row = slotIndex / 3;
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.PEZ_BETTA) {
                hasBetta = true;
                if (i / 3 == row) {
                    return true;
                }
            }
        }
        return !hasBetta;
    }

    private boolean isCarpaDoradaActive() {
        for (BoardSlot s : board) {
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.CARPA_DORADA) {
                return true;
            }
        }
        return false;
    }

    private java.util.Set<Integer> getPezPiedraColumns() {
        java.util.Set<Integer> cols = new java.util.HashSet<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() == CardId.PEZ_PIEDRA) {
                cols.add(i % 3);
            }
        }
        return cols;
    }

    private String applyDiceOnlyCurrent(CurrentDirection direction) {
        List<Die> lost = new ArrayList<>();
        List<List<Die>> incoming = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            incoming.add(new ArrayList<>());
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int idx = r * 3 + c;
                BoardSlot src = board[idx];
                List<Die> dice = new ArrayList<>(src.getDice());
                src.clearDice();
                int targetR = r, targetC = c;
                switch (direction) {
                    case UP: targetR = r - 1; break;
                    case DOWN: targetR = r + 1; break;
                    case LEFT: targetC = c - 1; break;
                    case RIGHT: targetC = c + 1; break;
                }
                if (targetR < 0 || targetR > 2 || targetC < 0 || targetC > 2) {
                    lost.addAll(dice);
                    continue;
                }
                int targetIdx = targetR * 3 + targetC;
                for (Die d : dice) {
                    if (incoming.get(targetIdx).size() >= 2) {
                        lost.add(d);
                    } else {
                        incoming.get(targetIdx).add(d);
                    }
                }
            }
        }

        for (int i = 0; i < board.length; i++) {
            board[i].getDice().addAll(incoming.get(i));
        }

        if (!lost.isEmpty()) {
            lostDice.addAll(lost);
        }

        String base = "Carpa dorada: la marea movió solo los dados";
        if (!lost.isEmpty()) {
            base += " y perdiste " + formatDiceList(lost) + ".";
        } else {
            base += ".";
        }
        String fletan = triggerFletanAfterCurrent();
        if (!fletan.isEmpty()) {
            base = base + " " + fletan;
        }
        return base;
    }

    private String applyBottleEffectsOnPlacement(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= board.length) {
            return "";
        }
        BoardSlot target = board[slotIndex];
        if (target.getDice().isEmpty()) {
            return "";
        }
        int dieIndex = target.getDice().size() - 1;
        Die original = target.getDice().get(dieIndex);
        int value = original.getValue();
        int sides = original.getType().getSides();
        boolean modified = false;
        StringBuilder log = new StringBuilder();

        if (isBottleTargetActive(slotIndex)) {
            int newValue = Math.min(sides, value + 3);
            if (newValue != value) {
                target.setDie(dieIndex, new Die(original.getType(), newValue));
                value = newValue;
                modified = true;
                log.append("Botella de Plástico ajustó el dado a ").append(newValue).append(".");
            }
        }

        if (isGlassBottleTargetActive(slotIndex)) {
            int newValue = Math.max(1, value - 3);
            if (newValue != value) {
                target.setDie(dieIndex, new Die(original.getType(), newValue));
                if (log.length() > 0) log.append(" ");
                log.append("Botella de vidrio ajustó el dado a ").append(newValue).append(".");
                modified = true;
            }
        }

        String lampreaLog = applyLampreaAdjustmentOnPlacement(slotIndex);
        if (!lampreaLog.isEmpty()) {
            if (log.length() > 0) log.append(" ");
            log.append(lampreaLog);
            modified = true;
        }

        return modified ? log.toString() : "";
    }

    private void enqueueCurrentAnimation(CurrentDirection direction) {
        if (direction == null) {
            return;
        }
        pendingCurrentAnimations.add(direction);
    }

    public List<CurrentDirection> getPendingCurrentDirections() {
        if (pendingCurrentAnimations.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return new ArrayList<>(pendingCurrentAnimations);
    }

    public String applyPendingCurrentAnimations() {
        if (pendingCurrentAnimations.isEmpty()) {
            return "";
        }
        StringBuilder log = new StringBuilder();
        while (!pendingCurrentAnimations.isEmpty()) {
            CurrentDirection direction = pendingCurrentAnimations.poll();
            String result = applyCurrent(direction);
            if (!result.isEmpty()) {
                if (log.length() > 0) log.append(" ");
                log.append(result);
            }
        }
        return log.toString();
    }

    private boolean isBottleTargetActive(int slotIndex) {
        for (Integer target : bottleTargets.values()) {
            if (target == slotIndex) {
                return true;
            }
        }
        return false;
    }

    private boolean isGlassBottleTargetActive(int slotIndex) {
        for (Integer target : glassBottleTargets.values()) {
            if (target == slotIndex) {
                return true;
            }
        }
        return false;
    }

    private String applyLampreaAdjustmentOnPlacement(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "";
        }
        if (slot.getStatus() == null || slot.getStatus().attachedRemoras.isEmpty()) {
            return "";
        }
        boolean hasLamprea = false;
        for (Card c : slot.getStatus().attachedRemoras) {
            if (c.getId() == CardId.LAMPREA) {
                hasLamprea = true;
                break;
            }
        }
        if (!hasLamprea) {
            return "";
        }
        int idx = slot.getDice().size() - 1;
        return startValueAdjustment(
                slotIndex,
                idx,
                1,
                CardId.LAMPREA,
                "Lamprea: elige si sumar o restar 1 al dado colocado.");
    }

    private boolean isHookActive() {
        for (BoardSlot s : board) {
            if (s.isFaceUp() && s.getCard() != null && s.getCard().getId() == CardId.ANZUELO_ROTO && !s.getStatus().hookPenaltyUsed) {
                return true;
            }
        }
        return false;
    }

    private Integer findAdjacentDelfin(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()
                    && adj.getCard().getId() == CardId.DELFIN
                    && !adj.getStatus().delfinProtectionUsed) {
                return idx;
            }
        }
        return null;
    }

    private boolean isDelfinProtectionActive(int slotIndex) {
        return findAdjacentDelfin(slotIndex) != null;
    }

    private String handleDelfinProtection(int slotIndex) {
        Integer delfinIndex = findAdjacentDelfin(slotIndex);
        if (delfinIndex == null) {
            return handleFailedCatchImmediate(slotIndex, false);
        }
        BoardSlot delfin = board[delfinIndex];
        delfin.getStatus().delfinProtectionUsed = true;
        BoardSlot slot = board[slotIndex];
        for (Die d : new ArrayList<>(slot.getDice())) {
            reserve.add(d.getType());
        }
        failedDiscards.add(slot.getCard());
        slot.clearDice();
        slot.setFaceUp(false);
        slot.setStatus(new SlotStatus());
        return "Delfín evitó el descarte: la carta vuelve boca abajo y recuperas los dados.";
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
            case ESTURION_PLACE:
                clearBallenaState();
                break;
            case ARENQUE_DESTINATION:
                clearArenqueState();
                break;
            case MANTIS_TARGET:
                clearMantisState();
                break;
            case DECORADOR_CHOOSE_SLOT:
                if (pendingDecoradorCard != null) {
                    deck.push(pendingDecoradorCard);
                    shuffleDeck();
                }
                clearDecoradorState();
                break;
            case HORSESHOE_DIE:
                clearHorseshoeState();
                break;
            case PEZ_BORRON_TARGET:
                pendingPezBorronSlot = -1;
                break;
            case FLETAN_HIDE:
                pendingFletanActive = false;
                pendingFletanSlot = -1;
                break;
            case GLASS_BOTTLE_TARGET:
                break;
            case PEZ_LOBO_TARGET:
                break;
            case TRUCHA_ARCOIRIS_FLIP:
                break;
            case PEZ_LEON_TARGET:
                break;
            case BARCO_PESQUERO_TARGET:
                break;
            case LOCO_TARGET:
                clearLocoState(true);
                break;
            default:
                break;
        }
    }

    private void clearPercebesState() {
        for (Die die : pendingPercebesDice) {
            reserve.add(die.getType());
        }
        pendingPercebesDice.clear();
        pendingPercebesTargets.clear();
    }

    private void clearBallenaState() {
        for (Die die : pendingBallenaDice) {
            reserve.add(die.getType());
        }
        pendingBallenaDice.clear();
        pendingBallenaTotal = 0;
    }

    private void clearArenqueState() {
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        awaitingArenqueChoice = false;
        arenqueSlotIndex = -1;
        arenquePlacementSlots = 0;
    }

    private void clearMantisState() {
        awaitingMantisDecision = false;
        awaitingMantisLostDieChoice = false;
        mantisSlotIndex = -1;
        mantisRerolledDie = null;
    }

    private void clearLocoState(boolean returnToReserve) {
        if (returnToReserve) {
            if (pendingLocoDie != null) {
                reserve.add(pendingLocoDie.getType());
            }
            for (Die die : pendingLocoDice) {
                reserve.add(die.getType());
            }
        }
        pendingLocoDice.clear();
        pendingLocoDie = null;
        pendingAbilityConfirmation = null;
        pendingAbilityQueue.clear();
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

    private boolean hasBlockingRevealState() {
        return isAwaitingDieLoss()
                || awaitingAtunDecision
                || awaitingBlueCrabDecision
                || awaitingBlowfishDecision
                || awaitingPezVelaDecision
                || awaitingPezVelaResultChoice
                || awaitingLanternChoice
                || isAwaitingBoardSelection()
                || awaitingArenqueChoice
                || awaitingDecoradorChoice
                || awaitingViolinistChoice
                || awaitingHorseshoeValue
                || awaitingBoxerDecision
                || awaitingPulpoChoice
                || awaitingValueAdjustment
                || awaitingLangostaRecovery
                || awaitingGhostShrimpDecision
                || awaitingCancelConfirmation
                || pendingAbilityConfirmation != null
                || !pendingAbilityQueue.isEmpty()
                || pendingSelection != PendingSelection.NONE
                || !pendingSelectionQueue.isEmpty();
    }

    private String continueRevealChain(String currentLog) {
        StringBuilder log = new StringBuilder(currentLog == null ? "" : currentLog);
        if (processingRevealChain) return log.toString();
        processingRevealChain = true;
        while (!pendingRevealChain.isEmpty() && !hasBlockingRevealState()) {
            int idx = pendingRevealChain.poll();
            String reveal = revealFromChain(idx);
            if (!reveal.isEmpty()) {
                if (log.length() > 0) log.append(" ");
                log.append(reveal);
            }
        }
        processingRevealChain = false;
        return log.toString();
    }

    private String revealFromChain(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= board.length) return "";
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) return "";
        if (!target.isFaceUp()) {
            if (!canRevealSlot(slotIndex)) {
                return "Derrame de petróleo impide revelar la carta.";
            }
            target.setFaceUp(true);
            markRevealed(slotIndex);
        }
        return handleOnReveal(slotIndex, 0);
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
        if (!canRevealSlot(slotIndex)) {
            return "Derrame de petróleo impide revelar la carta.";
        }
        target.setFaceUp(true);
        markRevealed(slotIndex);
        String result = handleOnReveal(slotIndex, 0);
        return result;
    }

    private String enqueueAbilityConfirmation(AbilityTrigger trigger, int slotIndex, int placedValue, List<Die> diceOnCard) {
        Card card = board[slotIndex].getCard();
        if (card == null) return "";
        String detail = buildAbilityDetail(card);
        AbilityActivation activation = new AbilityActivation(trigger, slotIndex, placedValue, diceOnCard, card, detail);
        if (pendingAbilityConfirmation == null) {
            pendingAbilityConfirmation = activation;
            return "Habilidad activada: " + card.getName() + ".";
        }
        pendingAbilityQueue.add(activation);
        return "Habilidad activada: " + card.getName() + ". Queda en espera.";
    }

    private String buildAbilityDetail(Card card) {
        if (card == null) return "";
        String detail = card.getOnCatch();
        if (detail == null || detail.isEmpty()) {
            detail = card.getBonus();
        }
        if (detail == null || detail.isEmpty()) {
            detail = card.getOnFail();
        }
        return detail == null || detail.isEmpty() ? "Habilidad especial lista para activarse." : detail;
    }

    private String executeAbilityActivation(AbilityActivation activation) {
        if (activation == null) return "";
        if (activation.trigger == AbilityTrigger.REVEAL) {
            return executeRevealAbility(activation.slotIndex, activation.placedValue);
        }
        if (activation.trigger == AbilityTrigger.CAPTURE) {
            return executeCaptureAbility(activation.card, activation.slotIndex, activation.diceOnCard);
        }
        return "";
    }

    private boolean shouldConfirmRevealAbility(CardId id) {
        if (id == null) return false;
        switch (id) {
            case CANGREJO_ROJO:
            case CANGREJO_BOXEADOR:
            case JAIBA_AZUL:
            case LANGOSTINO_MANTIS:
            case CAMARON_FANTASMA:
            case ATUN:
            case PEZ_GLOBO:
            case MORENA:
            case CANGREJO_ERMITANO:
            case CANGREJO_DECORADOR:
            case CENTOLLA:
            case NAUTILUS:
            case CANGREJO_HERRADURA:
            case CANGREJO_ARANA:
            case CANGREJO_VIOLINISTA:
            case BOTELLA_PLASTICO:
            case BOTELLA_DE_VIDRIO:
            case BOTA_VIEJA:
            case AUTO_HUNDIDO:
            case MICRO_PLASTICOS:
            case DERRAME_PETROLEO:
            case BARCO_PESQUERO:
            case CORRIENTES_PROFUNDAS:
            case PEZ_PAYASO:
            case PEZ_LINTERNA:
            case KOI:
            case PEZ_BETTA:
            case TRUCHA_ARCOIRIS:
            case PEZ_PIEDRA:
            case PEZ_LEON:
            case PEZ_DRAGON_AZUL:
            case PEZ_HACHA_ABISAL:
            case CARPA_DORADA:
            case FLETAN:
            case PEZ_LOBO:
            case PEZ_BORRON:
            case SEPIA:
            case DAMISELAS:
            case LAMPREA:
            case PEZ_VELA:
            case PIRANA:
            case PEZ_FANTASMA:
            case PULPO:
            case CALAMAR_GIGANTE:
            case MANTA_GIGANTE:
            case ARENQUE:
            case REMORA:
            case BALLENA_AZUL:
            case TIBURON_BLANCO:
            case TIBURON_TIGRE:
            case DELFIN:
            case TIBURON_PEREGRINO:
            case NARVAL:
            case ORCA:
            case ANGUILA_ELECTRICA:
            case CACHALOTE:
            case ESTURION:
            case BALLENA_JOROBADA:
            case MERO_GIGANTE:
            case PEZ_LUNA:
                return true;
            default:
                return false;
        }
    }

    private boolean shouldConfirmCaptureAbility(CardId id) {
        if (id == null) return false;
        switch (id) {
            case LANGOSTA_ESPINOSA:
            case BOGAVANTE:
            case RED_ENREDADA:
            case RED_DE_ARRASTRE:
            case LATA_OXIDADA:
            case PERCEBES:
            case LOCO:
            case CABALLITO_DE_MAR:
            case PEZ_PIPA:
            case SALMON:
            case PEZ_VOLADOR:
                return true;
            default:
                return false;
        }
    }

    private String triggerOilSpill() {
        int flipped = 0;
        for (BoardSlot s : board) {
            if (s.getCard() != null && s.isFaceUp() && s.getCard().getId() != CardId.DERRAME_PETROLEO) {
                s.setFaceUp(false);
                s.getStatus().oilSpillLock = true;
                flipped++;
            }
        }
        recomputeBottleAdjustments();
        return flipped == 0 ? "Derrame de petróleo: no había cartas boca arriba." :
                "Derrame de petróleo volteó " + flipped + " carta(s) boca abajo.";
    }

    private String invertAllCardsFromMicroPlastics() {
        int flippedUp = 0;
        int flippedDown = 0;
        boolean oilActive = isOilSpillActive();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() == null) continue;
            if (s.getCard().getId() == CardId.MICRO_PLASTICOS) continue;
            if (s.isFaceUp()) {
                s.setFaceUp(false);
                flippedDown++;
            } else if (!oilActive) {
                s.setFaceUp(true);
                markRevealed(i);
                pendingRevealChain.add(i);
                flippedUp++;
            }
        }
        recomputeBottleAdjustments();
        String base = "Micro plásticos volteó " + flippedUp + " carta(s) boca arriba y " + flippedDown + " boca abajo.";
        String chain = continueRevealChain("");
        if (!chain.isEmpty()) {
            base += " " + chain;
        }
        if (!pendingRevealChain.isEmpty()) {
            base += " Revelaciones adicionales en espera.";
        }
        return base;
    }

    private String handleOnReveal(int slotIndex, int placedValue) {
        BoardSlot slot = board[slotIndex];
        if (slot.getCard() == null) return "";
        if (shouldConfirmRevealAbility(slot.getCard().getId())) {
            return enqueueAbilityConfirmation(AbilityTrigger.REVEAL, slotIndex, placedValue, null);
        }
        return executeRevealAbility(slotIndex, placedValue);
    }

    private String executeRevealAbility(int slotIndex, int placedValue) {
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
            case CANGREJO_BOXEADOR:
                result = startBoxerCrabMove(slotIndex);
                break;
            case JAIBA_AZUL:
                awaitingBlueCrabDecision = true;
                blueCrabSlotIndex = slotIndex;
                result = "Jaiba azul: ¿quieres ajustar un dado ±1?";
                break;
            case LANGOSTINO_MANTIS:
                result = startMantisDecision(slotIndex);
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
            case CANGREJO_DECORADOR:
                result = startDecoradorSelection(slotIndex);
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
            case CANGREJO_HERRADURA:
                result = startHorseshoeAdjustment(slotIndex);
                break;
            case CANGREJO_ARANA:
                result = startSpiderCrabRevive(slotIndex);
                break;
            case CANGREJO_VIOLINISTA:
                result = startViolinistCapture();
                break;

            case BOTELLA_PLASTICO:
                result = startBottleTargetSelection(slotIndex);
                break;
            case BOTELLA_DE_VIDRIO:
                result = startGlassBottleTargetSelection(slotIndex);
                break;
            case BOTA_VIEJA:
                result = "Bota vieja: −1 a la suma de adyacentes.";
                break;
            case AUTO_HUNDIDO:
                result = "Auto hundido: +1 a la suma de adyacentes.";
                break;
            case MICRO_PLASTICOS:
                result = invertAllCardsFromMicroPlastics();
                break;
            case DERRAME_PETROLEO:
                result = triggerOilSpill();
                break;
            case BARCO_PESQUERO:
                result = "Barco pesquero listo: si igualas su dado, eliminará una carta adyacente.";
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
                result = startKoiSwapSelection(slotIndex);
                break;
            case PEZ_BETTA:
                result = "Pez betta: solo podrás colocar dados en su fila.";
                break;
            case TRUCHA_ARCOIRIS:
                result = startTruchaArcoiris(slotIndex);
                break;
            case PEZ_PIEDRA:
                result = "Pez piedra: su columna no será afectada por la marea.";
                break;
            case PEZ_LEON:
                result = startPezLeon(slotIndex);
                break;
            case PEZ_DRAGON_AZUL:
                result = returnHighDice();
                break;
            case PEZ_HACHA_ABISAL:
                result = startHachaAbisalRelease();
                break;
            case CARPA_DORADA:
                result = "Carpa dorada: la marea solo afectará a los dados.";
                break;
            case FLETAN:
                result = "Fletan espera la próxima marea.";
                break;
            case PEZ_LOBO:
                result = startPezLoboDecision(slotIndex);
                break;
            case PEZ_BORRON:
                result = startPezBorronMove(slotIndex);
                break;
            case SEPIA:
                result = startSepiaCapture(slotIndex, placedValue);
                break;
            case DAMISELAS:
                result = startDamiselasReorder(slotIndex);
                break;
            case LAMPREA:
                result = attachLampreaToBigFish(slotIndex);
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
            case TIBURON_TIGRE:
                result = startTigerSharkSelection(slotIndex);
                break;
            case DELFIN:
                result = startDelfinProtection(slotIndex);
                break;
            case TIBURON_PEREGRINO:
                result = startPeregrinoPeek(slotIndex);
                break;
            case NARVAL:
                result = returnAdjacentFaceUpToDeck(slotIndex);
                break;
            case ORCA:
                result = flipAdjacentFaceUpCardsDownAndRecover(slotIndex);
                break;
            case ANGUILA_ELECTRICA:
                result = rerollAdjacentDiceWithRecovery(slotIndex);
                break;
            case CACHALOTE:
                result = startCachaloteReposition(slotIndex);
                break;
            case ESTURION:
                result = startEsturionRoll(slotIndex);
                break;
            case BALLENA_JOROBADA:
                result = "Ballena jorobada: podrás elegir la dirección de la marea.";
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
        String oysters = triggerAdjacentOysters(slotIndex, !result.isEmpty());
        if (!oysters.isEmpty()) {
            result = result.isEmpty() ? oysters : result + " " + oysters;
        }
        return result;
    }
    private String startKoiSwapSelection(int slotIndex) {
        BoardSlot origin = board[slotIndex];
        if (origin.getDice().isEmpty()) {
            return ""; // KOI sin dado: no puede hacer nada
        }

        if (!hasKoiSwapTarget(slotIndex)) {

            return "Koi: no hay cartas adyacentes boca arriba con exactamente 1 dado para intercambiar.";
        }

        return queueableSelection(
                PendingSelection.KOI_TARGET,
                slotIndex,
                "Koi: elige una carta adyacente boca arriba con 1 dado para intercambiar un dado."
        );
    }
    private boolean hasKoiSwapTarget(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot t = board[idx];
            if (t.getCard() != null && t.isFaceUp() && t.getDice().size() == 1) {
                return true;
            }
        }
        return false;
    }

    private String startSpiderCrabRevive(int slotIndex) {
        if (failedDiscards.isEmpty()) {
            return "No hay cartas descartadas por fallo para recuperar.";
        }

        boolean hasFaceDownNoDice = false;
        for (BoardSlot s : board) {
            if (s.getCard() != null && !s.isFaceUp() && s.getDice().isEmpty()) {
                hasFaceDownNoDice = true;
                break;
            }
        }
        if (!hasFaceDownNoDice) {
            return "No hay cartas boca abajo sin dados para reemplazar con el Cangrejo araña.";
        }
        spiderCrabSlotIndex = slotIndex;

        return queueableSelection(
                PendingSelection.SPIDER_CRAB_CHOOSE_CARD,
                slotIndex,
                -1,
                "Cangrejo araña: elige una carta descartada por fallo para recuperar."
        );

    }

    public List<String> getFailedDiscardNames() {
        List<String> names = new ArrayList<>();
        for (Card c : failedDiscards) {
            names.add(c.getName());
        }
        return names;
    }

    public List<Card> getFailedDiscardCards() {
        return new ArrayList<>(failedDiscards);
    }

    public String chooseSpiderCrabCard(int index) {
        if (pendingSelection != PendingSelection.SPIDER_CRAB_CHOOSE_CARD) {
            return "No hay selección pendiente del Cangrejo araña.";
        }
        if (index < 0 || index >= failedDiscards.size()) {
            return "Debes elegir una carta descartada válida.";
        }

        pendingSpiderCrabCard = failedDiscards.remove(index);
        pendingSelection = PendingSelection.SPIDER_CRAB_CHOOSE_SLOT;
        return "Elige una carta boca abajo para reemplazar por " + pendingSpiderCrabCard.getName() + ".";
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

    private String startBottleTargetSelection(int slotIndex) {
        boolean hasOption = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.PEZ) {
                hasOption = true;
                break;
            }
        }
        if (!hasOption) {
            return "Botella de Plástico: no hay peces pequeños boca arriba adyacentes para marcar.";
        }
        return queueableSelection(
                PendingSelection.BOTTLE_TARGET,
                slotIndex,
                "Botella de Plástico: elige un pez pequeño adyacente boca arriba.");
    }

    private String startGlassBottleTargetSelection(int slotIndex) {
        boolean hasOption = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                hasOption = true;
                break;
            }
        }
        if (!hasOption) {
            return "Botella de vidrio: no hay cartas boca arriba adyacentes para marcar.";
        }
        return queueableSelection(
                PendingSelection.GLASS_BOTTLE_TARGET,
                slotIndex,
                "Botella de vidrio: elige una carta adyacente boca arriba.");
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

        // ✅ Si ya no existe ningún dado adyacente, NO podemos completar la habilidad.
        // Esto evita el "bucle" de selección eterna.
        boolean anyAdjacentWithDice = false;
        for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
            if (!board[idx].getDice().isEmpty()) {
                anyAdjacentWithDice = true;
                break;
            }
        }
        if (!anyAdjacentWithDice) {
            clearPendingSelection();
            recomputeBottleAdjustments();
            return "Cangrejo rojo: no hay dados adyacentes para mover (se omite la habilidad).";
        }

        // Validación normal
        if (!isAdjacentToActor(slotIndex) || board[slotIndex].getDice().isEmpty()) {
            return "Elige una carta adyacente con dado para mover.";
        }

        pendingSelectionAux = slotIndex;
        pendingSelection = PendingSelection.RED_CRAB_TO;
        return "Selecciona la carta adyacente destino (máx. 2 dados).";
    }

    private String offerCancelAbility(String abilityName) {
        awaitingCancelConfirmation = true;
        pendingCancelMessage = abilityName + ": esa opción no es válida. ¿Quieres cancelar la habilidad?";
        return pendingCancelMessage;
    }

    private boolean awaitingCancelConfirmation = false;
    private String pendingCancelMessage = null;

    public boolean isAwaitingCancelConfirmation() {
        return awaitingCancelConfirmation;
    }

    public String getPendingCancelMessage() {
        return pendingCancelMessage;
    }
    public String resolveCancelConfirmation(boolean cancel) {
        awaitingCancelConfirmation = false;
        String ability = pendingCancelMessage;
        pendingCancelMessage = null;

        if (cancel) {
            clearPendingSelection();
            return ability + ": habilidad cancelada.";
        }

        return ability + ": continúa seleccionando.";
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

    private String chooseBottleTarget(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige un pez pequeño adyacente boca arriba.";
        }

        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || !target.isFaceUp() || target.getCard().getType() != CardType.PEZ) {
            return "Debes seleccionar un pez pequeño boca arriba.";
        }

        // Guardamos vínculo (solo para que puedas seguir dibujando la línea si quieres)
        bottleTargets.put(pendingSelectionActor, slotIndex);

        if (target.getDice().isEmpty()) {
            // No modificamos nada ahora, solo dejamos el efecto activo para futuras colocaciones
            clearPendingSelection();
            return "Botella de Plástico: objetivo marcado. Cuando coloques dados sobre este pez, se modificarán (+3 con tope).";
        }

        // Evita aplicar 2 veces sobre el mismo objetivo
        if (target.getStatus() != null && target.getStatus().bottleDieBonus > 0) {
            clearPendingSelection();
            return "Ese pez ya fue modificado por la Botella de Plástico.";
        }

        // APLICAR CAMBIO REAL A LOS DADOS (visible porque cambia la imagen del dado)
        StringBuilder detail = new StringBuilder();
        for (int i = 0; i < target.getDice().size(); i++) {
            Die d = target.getDice().get(i);
            int oldV = d.getValue();
            int sides = d.getType().getSides();

            int newV = oldV + 3;
            if (newV > sides) newV = sides;
            if (newV < 1) newV = 1;

            target.setDie(i, new Die(d.getType(), newV));

            if (detail.length() > 0) detail.append(", ");
            detail.append(d.getLabel()).append(" ").append(oldV).append("→").append(newV);
        }

        // Marcador VISUAL para halo/estado (ya no se usará para sumar a la pesca)
        if (target.getStatus() != null) {
            target.getStatus().bottleDieBonus = 3; // “flag” visual
        }

        clearPendingSelection();
        return "Botella de Plástico modificó los dados de " + target.getCard().getName() + " (+3, con tope). [" + detail + "]";
    }

    private String chooseGlassBottleTarget(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "Elige una carta adyacente boca arriba.";
        }

        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || !target.isFaceUp()) {
            return "Debes seleccionar una carta boca arriba.";
        }

        glassBottleTargets.put(pendingSelectionActor, slotIndex);

        if (target.getDice().isEmpty()) {
            if (target.getStatus() != null) {
                target.getStatus().glassBottlePenalty = true;
            }
            clearPendingSelection();
            return "Botella de vidrio: objetivo marcado. Los dados futuros se ajustarán −3.";
        }

        StringBuilder detail = new StringBuilder();
        for (int i = 0; i < target.getDice().size(); i++) {
            Die d = target.getDice().get(i);
            int oldV = d.getValue();
            int newV = Math.max(1, oldV - 3);
            target.setDie(i, new Die(d.getType(), newV));
            if (detail.length() > 0) detail.append(", ");
            detail.append(d.getLabel()).append(" ").append(oldV).append("→").append(newV);
        }

        if (target.getStatus() != null) {
            target.getStatus().glassBottlePenalty = true;
        }

        clearPendingSelection();
        return "Botella de vidrio modificó los dados de " + target.getCard().getName() + " (−3). [" + detail + "]";
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

    private String startTruchaArcoiris(int slotIndex) {
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "Trucha Arcoíris: no hay cartas boca abajo adyacentes.";
        }
        return queueableSelection(
                PendingSelection.TRUCHA_ARCOIRIS_FLIP,
                slotIndex,
                "Trucha Arcoíris: elige una carta boca abajo adyacente para revelar.");
    }

    private String resolveTruchaArcoirisFlip(int slotIndex) {
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return "Trucha Arcoíris: elige una carta adyacente.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) {
            return "Trucha Arcoíris: la carta debe estar boca abajo.";
        }
        String reveal = revealAndTrigger(slotIndex);
        StringBuilder log = new StringBuilder("Trucha Arcoíris reveló " + target.getCard().getName() + ".");
        if (!reveal.isEmpty()) {
            log.append(" ").append(reveal);
        }
        if (target.getCard().getType() == CardType.PEZ && !lostDice.isEmpty() && target.getDice().size() < 2) {
            Die recovered = lostDice.remove(lostDice.size() - 1);
            target.addDie(recovered);
            log.append(" Colocaste un dado perdido sobre ese pez pequeño.");
        }
        clearPendingSelection();
        recomputeBottleAdjustments();
        return log.toString();
    }

    private String startPezLeon(int slotIndex) {
        boolean hasDie = false;
        for (BoardSlot s : board) {
            if (!s.getDice().isEmpty()) {
                hasDie = true;
                break;
            }
        }
        if (!hasDie) {
            return "Pez León: no hay dados para multiplicar.";
        }
        return queueableSelection(
                PendingSelection.PEZ_LEON_TARGET,
                slotIndex,
                "Pez León: elige una carta con dados para duplicar uno.");
    }

    private String resolvePezLeonBoost(int slotIndex) {
        BoardSlot target = board[slotIndex];
        if (target.getDice().isEmpty()) {
            return "Pez León: selecciona una carta con dados.";
        }
        int idx = target.getDice().size() - 1;
        Die die = target.getDice().get(idx);
        int newValue = Math.min(die.getType().getSides(), die.getValue() * 2);
        target.setDie(idx, new Die(die.getType(), newValue));
        clearPendingSelection();
        return "Pez León duplicó el dado a " + newValue + ".";
    }

    private String returnHighDice() {
        int returned = 0;
        for (BoardSlot slot : board) {
            if (slot.getDice().isEmpty()) continue;
            List<Die> kept = new ArrayList<>();
            for (Die d : slot.getDice()) {
                if (d.getValue() >= 6) {
                    reserve.add(d.getType());
                    returned++;
                } else {
                    kept.add(d);
                }
            }
            slot.clearDice();
            for (Die d : kept) {
                slot.addDie(d);
            }
        }
        return returned == 0 ? "Pez Dragón azul: no había dados ≥ 6." : "Pez Dragón azul devolvió " + returned + " dado(s) a la reserva.";
    }

    private String startPezLoboDecision(int slotIndex) {
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "Pez Lobo: no hay cartas boca arriba adyacentes para descartar.";
        }
        awaitingPezLoboDecision = true;
        pezLoboSlotIndex = slotIndex;
        return "Pez Lobo: ¿quieres descartar una carta adyacente boca arriba?";
    }

    private String startPezLoboDiscard(int slotIndex) {
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "Pez Lobo: no hay cartas boca arriba adyacentes para descartar.";
        }
        return queueableSelection(
                PendingSelection.PEZ_LOBO_TARGET,
                slotIndex,
                "Pez Lobo: elige una carta adyacente boca arriba para descartarla.");
    }

    private String resolvePezLoboDiscard(int slotIndex) {
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return offerCancelAbility("Pez Lobo");
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || !target.isFaceUp()) {
            return offerCancelAbility("Pez Lobo");
        }
        Card removed = target.getCard();
        for (Die d : new ArrayList<>(target.getDice())) {
            reserve.add(d.getType());
        }
        target.clearDice();
        failedDiscards.add(removed);
        target.setCard(deck.isEmpty() ? null : deck.pop());
        target.setFaceUp(false);
        target.setStatus(new SlotStatus());

        BoardSlot wolfSlot = board[pendingSelectionActor];
        if (wolfSlot.getCard() != null && wolfSlot.getCard().getId() == CardId.PEZ_LOBO) {
            for (Die d : new ArrayList<>(wolfSlot.getDice())) {
                reserve.add(d.getType());
            }
            wolfSlot.clearDice();
            deck.addLast(wolfSlot.getCard());
            wolfSlot.setCard(deck.isEmpty() ? null : deck.pop());
            wolfSlot.setFaceUp(false);
            wolfSlot.setStatus(new SlotStatus());
        }
        clearPendingSelection();
        recomputeBottleAdjustments();
        return "Pez Lobo descartó " + removed.getName() + " y regresó al mazo.";
    }

    private String startPezBorronMove(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        if (slot.getDice().isEmpty()) {
            return "Pez borrón: no hay dado para mover.";
        }
        pendingPezBorronSlot = slotIndex;
        return queueableSelection(
                PendingSelection.PEZ_BORRON_TARGET,
                slotIndex,
                "Pez borrón: elige una carta boca abajo para mover el dado.");
    }

    private String resolvePezBorronMove(int slotIndex) {
        if (pendingPezBorronSlot < 0 || pendingPezBorronSlot >= board.length) {
            clearPendingSelection();
            return "Pez borrón: la carta ya no está disponible.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.isFaceUp()) {
            return "Pez borrón: elige una carta boca abajo.";
        }
        if (target.getDice().size() >= 2) {
            return "Pez borrón: la carta destino ya tiene 2 dados.";
        }
        BoardSlot origin = board[pendingPezBorronSlot];
        if (origin.getDice().isEmpty()) {
            clearPendingSelection();
            return "Pez borrón: no hay dado para mover.";
        }
        Die moved = origin.removeDie(origin.getDice().size() - 1);
        target.addDie(moved);
        clearPendingSelection();
        String outcome = target.getDice().size() == 2
                ? resolveFishingOutcome(slotIndex, moved.getValue(), "", false)
                : "";
        if (!outcome.isEmpty()) {
            return "Pez borrón movió el dado sin revelar. " + outcome;
        }
        return "Pez borrón movió el dado sin revelar la carta.";
    }

    private String startSepiaCapture(int slotIndex, int placedValue) {
        if (placedValue % 2 == 0) {
            return "";
        }
        if (deck.isEmpty()) {
            return "Sepia: el mazo está vacío.";
        }
        pendingSepiaOptions.clear();
        pendingSepiaSlot = slotIndex;
        int count = Math.min(3, deck.size());
        for (int i = 0; i < count; i++) {
            pendingSepiaOptions.add(deck.pop());
        }
        awaitingSepiaChoice = true;
        return "Sepia: elige una carta para capturar.";
    }

    public String chooseSepiaCapture(int index) {
        if (!awaitingSepiaChoice) {
            return "No hay captura pendiente de la Sepia.";
        }
        if (pendingSepiaOptions.isEmpty()) {
            awaitingSepiaChoice = false;
            return "Sepia: no hay cartas disponibles.";
        }
        if (index < 0 || index >= pendingSepiaOptions.size()) {
            return "Sepia: selección inválida.";
        }
        Card chosen = pendingSepiaOptions.remove(index);
        captures.add(chosen);
        for (Card c : pendingSepiaOptions) {
            deck.addLast(c);
        }
        pendingSepiaOptions.clear();
        awaitingSepiaChoice = false;

        boolean reshuffle = false;
        if (pendingSepiaSlot >= 0 && pendingSepiaSlot < board.length) {
            BoardSlot sepiaSlot = board[pendingSepiaSlot];
            if (sepiaSlot.getCard() != null && sepiaSlot.getCard().getId() == CardId.SEPIA) {
                for (Die d : new ArrayList<>(sepiaSlot.getDice())) {
                    reserve.add(d.getType());
                }
                sepiaSlot.clearDice();
                deck.addLast(sepiaSlot.getCard());
                reshuffle = true;
                sepiaSlot.setFaceUp(false);
                sepiaSlot.setStatus(new SlotStatus());
            }
        }
        if (reshuffle) {
            shuffleDeck();
            if (pendingSepiaSlot >= 0 && pendingSepiaSlot < board.length) {
                BoardSlot sepiaSlot = board[pendingSepiaSlot];
                sepiaSlot.setCard(deck.isEmpty() ? null : deck.pop());
            }
        }
        pendingSepiaSlot = -1;
        recomputeBottleAdjustments();
        return "Sepia capturó " + chosen.getName() + " y regresó al mazo.";
    }

    private String startDamiselasReorder(int slotIndex) {
        if (deck.isEmpty()) {
            return "Damiselas: el mazo está vacío.";
        }
        pendingDamiselasTop.clear();
        pendingDamiselasOrdered.clear();
        pendingDamiselasSlot = slotIndex;
        int count = Math.min(6, deck.size());
        for (int i = 0; i < count; i++) {
            pendingDamiselasTop.add(deck.pop());
        }
        awaitingDamiselasChoice = true;
        return "Damiselas: elige el orden de las cartas.";
    }

    public String chooseDamiselasOrder(int index) {
        if (!awaitingDamiselasChoice) {
            return "No hay orden pendiente de Damiselas.";
        }
        if (pendingDamiselasTop.isEmpty()) {
            awaitingDamiselasChoice = false;
            return "Damiselas: no hay cartas para ordenar.";
        }
        if (index < 0 || index >= pendingDamiselasTop.size()) {
            return "Damiselas: selección inválida.";
        }
        Card chosen = pendingDamiselasTop.remove(index);
        pendingDamiselasOrdered.add(chosen);
        if (!pendingDamiselasTop.isEmpty()) {
            return "Damiselas: elige la siguiente carta.";
        }
        for (int i = pendingDamiselasOrdered.size() - 1; i >= 0; i--) {
            deck.push(pendingDamiselasOrdered.get(i));
        }
        pendingDamiselasOrdered.clear();
        awaitingDamiselasChoice = false;
        return "Damiselas reordenó las cartas del mazo.";
    }

    private String startHachaAbisalRelease() {
        if (captures.isEmpty()) {
            return "Pez Hacha Abisal: no hay capturas para liberar.";
        }
        pendingHachaReleaseChoices.clear();
        pendingHachaReleaseChoices.addAll(captures);
        pendingHachaReleaseCount = Math.min(2, captures.size());
        awaitingHachaReleaseChoice = true;
        return "Pez Hacha Abisal: elige una carta capturada para liberar (" + pendingHachaReleaseCount + ").";
    }

    public String chooseHachaRelease(int index) {
        if (!awaitingHachaReleaseChoice) {
            return "No hay liberaciones pendientes.";
        }
        if (pendingHachaReleaseChoices.isEmpty()) {
            awaitingHachaReleaseChoice = false;
            return "No hay cartas disponibles para liberar.";
        }
        if (index < 0 || index >= pendingHachaReleaseChoices.size()) {
            return "Selección inválida para liberar.";
        }
        Card chosen = pendingHachaReleaseChoices.remove(index);
        pendingHachaReleaseCount = Math.max(0, pendingHachaReleaseCount - 1);
        awaitingHachaReleaseChoice = false;
        String msg = startReleaseFromCapture(chosen);
        pendingHachaReleaseChoices.clear();
        return "Pez Hacha Abisal: " + msg;
    }

    private String handleDragnetCapture(int slotIndex) {
        int captured = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                captures.add(adj.getCard());
                adj.clearDice();
                adj.setCard(deck.isEmpty() ? null : deck.pop());
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                captured++;
                if (captured >= 2) break;
            }
        }
        recomputeBottleAdjustments();
        if (!captures.isEmpty()) {
            pendingDragnetTargets.clear();
            pendingDragnetTargets.addAll(captures);
            awaitingDragnetReleaseChoice = true;
            return "Red de arrastre capturó " + captured + " carta(s). Elige una carta para liberar.";
        }
        return "Red de arrastre no encontró cartas boca abajo adyacentes.";
    }

    public String chooseDragnetRelease(int index) {
        if (!awaitingDragnetReleaseChoice) {
            return "No hay liberación pendiente de la Red de arrastre.";
        }
        if (pendingDragnetTargets.isEmpty()) {
            awaitingDragnetReleaseChoice = false;
            return "No hay cartas para liberar.";
        }
        if (index < 0 || index >= pendingDragnetTargets.size()) {
            return "Selección inválida.";
        }
        Card chosen = pendingDragnetTargets.remove(index);
        awaitingDragnetReleaseChoice = false;
        pendingDragnetTargets.clear();
        return "Red de arrastre: " + startReleaseFromCapture(chosen);
    }

    private String recoverIfD12Used(List<Die> diceOnCard) {
        boolean used = false;
        if (diceOnCard == null) {
            return "";
        }
        for (Die d : diceOnCard) {
            if (d.getType() == DieType.D12) {
                used = true;
                break;
            }
        }
        if (used && !lostDice.isEmpty()) {
            Die recovered = lostDice.remove(lostDice.size() - 1);
            reserve.add(recovered.getType());
            return "Pez pipa recuperó un dado perdido.";
        }
        return "";
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
        String msg;
        if (swap) {
            swapSlots(ghostShrimpFirstChoice, ghostShrimpSecondChoice);
            recomputeBottleAdjustments();
            clearGhostShrimpState();
            msg = "Intercambiaste las cartas vistas por el Camarón fantasma.";
        } else {
            clearGhostShrimpState();
            msg = "Decidiste mantener las cartas en su lugar.";
        }

// ✅ Reanudar cadena de revelaciones (Mero gigante / etc.)
        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
        }

// ✅ Safety net: resolver cartas que quedaron con 2 dados por efectos
        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }

        return msg;

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
        copy.bottleDieBonus = status.bottleDieBonus;
        copy.attachedRemoras = new ArrayList<>(status.attachedRemoras);
        copy.hookPenaltyUsed = status.hookPenaltyUsed;
        copy.langostaRecovered = status.langostaRecovered;
        copy.protectedBySlot = status.protectedBySlot;
        copy.lastTriggeredBySlot = status.lastTriggeredBySlot;
        copy.oilSpillLock = status.oilSpillLock;
        copy.autoHundidoBonus = status.autoHundidoBonus;
        copy.glassBottlePenalty = status.glassBottlePenalty;
        copy.delfinProtectionUsed = status.delfinProtectionUsed;
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
            return offerCancelAbility("Pez globo");
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
        return queueableSelection(
                PendingSelection.MORENA_FROM,
                slotIndex,
                "Morena: elige una carta adyacente con dado para mover.");
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

    private String autoCancelInvalidMorenaSelection() {
        if (pendingSelection != PendingSelection.MORENA_FROM && pendingSelection != PendingSelection.MORENA_TO) {
            return "";
        }
        if (pendingSelectionActor < 0 || pendingSelectionActor >= board.length) {
            clearPendingSelection();
            return "Morena: acción cancelada (carta no disponible).";
        }
        BoardSlot morenaSlot = board[pendingSelectionActor];
        if (morenaSlot.getCard() == null || morenaSlot.getCard().getId() != CardId.MORENA) {
            clearPendingSelection();
            return "Morena: acción cancelada (carta no disponible).";
        }

        boolean hasOrigin = false;
        boolean hasTarget = false;
        for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
            BoardSlot adj = board[idx];
            if (!adj.getDice().isEmpty()) hasOrigin = true;
            if (adj.getCard() != null && adj.getDice().size() < 2) hasTarget = true;
        }

        if (pendingSelection == PendingSelection.MORENA_FROM) {
            if (!hasOrigin || !hasTarget) {
                clearPendingSelection();
                return "Morena: no hay dados válidos para mover.";
            }
            return "";
        }

        BoardSlot origin = pendingSelectionAux >= 0 && pendingSelectionAux < board.length
                ? board[pendingSelectionAux]
                : null;
        if (origin == null || origin.getDice().isEmpty()) {
            clearPendingSelection();
            return "Morena: no hay dados para mover.";
        }

        boolean hasDestination = false;
        for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
            if (idx == pendingSelectionAux) continue;
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.getDice().size() < 2) {
                hasDestination = true;
                break;
            }
        }
        if (!hasDestination) {
            clearPendingSelection();
            return "Morena: no hay destino válido para el dado.";
        }
        return "";
    }

    private String autoCancelInvalidBoxerSelection() {
        if (pendingSelection != PendingSelection.BOXER_FROM && pendingSelection != PendingSelection.BOXER_TO) {
            return "";
        }
        if (pendingSelectionActor < 0 || pendingSelectionActor >= board.length) {
            clearBoxerState();
            clearPendingSelection();
            return "Cangrejo boxeador: acción cancelada (carta no disponible).";
        }
        BoardSlot boxerSlot = board[pendingSelectionActor];
        if (boxerSlot.getCard() == null || boxerSlot.getCard().getId() != CardId.CANGREJO_BOXEADOR) {
            clearBoxerState();
            clearPendingSelection();
            return "Cangrejo boxeador: acción cancelada (carta no disponible).";
        }

        if (pendingSelection == PendingSelection.BOXER_FROM) {
            if (!hasValidBoxerMove(pendingSelectionActor)) {
                clearBoxerState();
                clearPendingSelection();
                return "Cangrejo boxeador: no hay movimientos válidos.";
            }
            return "";
        }

        BoardSlot origin = pendingSelectionAux >= 0 && pendingSelectionAux < board.length
                ? board[pendingSelectionAux]
                : null;
        if (origin == null || origin.getDice().isEmpty()) {
            clearBoxerState();
            clearPendingSelection();
            return "Cangrejo boxeador: no hay dados para mover.";
        }

        boolean hasDestination = false;
        for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
            if (idx == pendingSelectionAux) continue;
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.getDice().size() < 2) {
                hasDestination = true;
                break;
            }
        }
        if (!hasDestination) {
            clearBoxerState();
            clearPendingSelection();
            return "Cangrejo boxeador: no hay destino válido para el dado.";
        }
        return "";
    }

    private String autoCancelInvalidPercebesSelection() {
        if (pendingSelection != PendingSelection.PERCEBES_MOVE) {
            return "";
        }
        if (pendingSelectionActor < 0 || pendingSelectionActor >= board.length) {
            return cancelPercebesMove("Percebes: acción cancelada (carta no disponible).");
        }
        BoardSlot percebesSlot = board[pendingSelectionActor];
        if (percebesSlot.getCard() == null || percebesSlot.getCard().getId() != CardId.PERCEBES) {
            return cancelPercebesMove("Percebes: acción cancelada (carta no disponible).");
        }
        if (pendingPercebesDice.isEmpty()) {
            clearPendingSelection();
            return "Percebes: no hay dados por mover.";
        }

        boolean hasDestination = false;
        for (Integer idx : adjacentIndices(pendingSelectionActor, true)) {
            if (pendingPercebesTargets.contains(idx)) continue;
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.getDice().size() < 2) {
                hasDestination = true;
                break;
            }
        }
        if (!hasDestination) {
            return cancelPercebesMove("Percebes: no hay destino válido para mover los dados.");
        }
        return "";
    }

    private String cancelPercebesMove(String message) {
        for (Die die : pendingPercebesDice) {
            reserve.add(die.getType());
        }
        clearPercebesState();
        clearPendingSelection();
        return message;
    }

    private String startBoxerCrabMove(int slotIndex) {
        if (!hasValidBoxerMove(slotIndex)) {
            clearBoxerState();
            return "Cangrejo boxeador: no hay movimientos válidos.";
        }
        boxerMovesRemaining = 2;
        boxerSlotIndex = slotIndex;
        awaitingBoxerDecision = false;
        return queueableSelection(
                PendingSelection.BOXER_FROM,
                slotIndex,
                "Cangrejo boxeador: elige una carta adyacente con dados."
        );
    }

    private String chooseBoxerOrigin(int slotIndex) {
        if (!isAdjacentToActor(slotIndex) || board[slotIndex].getDice().isEmpty()) {
            return "Elige una carta adyacente con dado.";
        }
        pendingSelectionAux = slotIndex;
        pendingSelection = PendingSelection.BOXER_TO;
        return "Selecciona una carta adyacente destino (máx. 2 dados).";
    }

    private String chooseBoxerDestination(int slotIndex) {
        if (!isAdjacentToActor(slotIndex)) {
            return "La carta destino debe ser adyacente al cangrejo boxeador.";
        }
        if (slotIndex == pendingSelectionAux) {
            return "Elige una carta distinta como destino.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null || target.getDice().size() >= 2) {
            return "La carta destino debe tener espacio para un dado.";
        }
        BoardSlot origin = board[pendingSelectionAux];
        if (origin.getDice().isEmpty()) {
            clearBoxerState();
            clearPendingSelection();
            return "No hay dados para mover.";
        }
        Die moving = origin.removeDie(origin.getDice().size() - 1);
        String reveal = addDieToSlot(slotIndex, moving);
        suspendPendingSelection();
        boxerMovesRemaining = Math.max(0, boxerMovesRemaining - 1);

        String msg = reveal.isEmpty()
                ? "Cangrejo boxeador movió 1 dado entre cartas adyacentes."
                : "Cangrejo boxeador movió 1 dado entre cartas adyacentes. " + reveal;

        if (boxerMovesRemaining > 0 && hasValidBoxerMove(boxerSlotIndex)) {
            awaitingBoxerDecision = true;
            return msg;
        }

        clearBoxerState();
        advancePendingSelectionQueue();
        return msg;
    }

    public String chooseBoxerContinue(boolean use) {
        if (!awaitingBoxerDecision) {
            return "No hay decisión pendiente del Cangrejo boxeador.";
        }
        awaitingBoxerDecision = false;
        if (!use) {
            clearBoxerState();
            return "Cangrejo boxeador: habilidad finalizada.";
        }
        if (!hasValidBoxerMove(boxerSlotIndex)) {
            clearBoxerState();
            return "Cangrejo boxeador: no hay movimientos válidos.";
        }
        return queueableSelection(
                PendingSelection.BOXER_FROM,
                boxerSlotIndex,
                "Cangrejo boxeador: elige una carta adyacente con dados."
        );
    }

    private boolean hasValidBoxerMove(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= board.length) {
            return false;
        }
        List<Integer> adjacents = adjacentIndices(slotIndex, true);
        for (Integer originIdx : adjacents) {
            BoardSlot origin = board[originIdx];
            if (origin.getDice().isEmpty()) continue;
            for (Integer destIdx : adjacents) {
                if (destIdx.equals(originIdx)) continue;
                BoardSlot target = board[destIdx];
                if (target.getCard() != null && target.getDice().size() < 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private void suspendPendingSelection() {
        pendingSelection = PendingSelection.NONE;
        pendingSelectionActor = -1;
        pendingSelectionAux = -1;
    }

    private void advancePendingSelectionQueue() {
        if (pendingSelection != PendingSelection.NONE) {
            return;
        }
        PendingSelectionState next = pendingSelectionQueue.poll();
        if (next != null) {
            pendingSelection = next.selection;
            pendingSelectionActor = next.actor;
            pendingSelectionAux = next.aux;
        }
    }

    private String startMantisDecision(int slotIndex) {
        if (lostDice.isEmpty()) {
            return "Langostino mantis: no hay dados perdidos para relanzar.";
        }
        boolean hasTarget = false;
        for (BoardSlot s : board) {
            if (!s.getDice().isEmpty()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "Langostino mantis: no hay dados en la zona de pesca.";
        }
        awaitingMantisDecision = true;
        mantisSlotIndex = slotIndex;
        return "Langostino mantis: ¿quieres relanzar un dado perdido?";
    }

    public String chooseMantisReroll(boolean use) {
        if (!awaitingMantisDecision) {
            return "No hay decisión pendiente del Langostino mantis.";
        }
        awaitingMantisDecision = false;
        if (!use) {
            mantisSlotIndex = -1;
            return "Langostino mantis: habilidad omitida.";
        }
        if (lostDice.isEmpty()) {
            mantisSlotIndex = -1;
            return "Langostino mantis: no hay dados perdidos para relanzar.";
        }
        awaitingMantisLostDieChoice = true;
        return "Elige un dado perdido para relanzar.";
    }

    public String chooseMantisLostDie(int index) {
        if (!awaitingMantisLostDieChoice) {
            return "No hay selección pendiente del Langostino mantis.";
        }
        if (index < 0 || index >= lostDice.size()) {
            return "Debes elegir un dado perdido válido.";
        }
        Die chosen = lostDice.remove(index);
        mantisRerolledDie = Die.roll(chosen.getType(), rng);
        awaitingMantisLostDieChoice = false;
        String message = "Langostino mantis lanzó un " + mantisRerolledDie.getLabel()
                + ". Elige una carta con dado para reemplazarlo.";
        return queueableSelection(PendingSelection.MANTIS_TARGET, mantisSlotIndex, message);
    }

    public String chooseLangostaRecoveredDie(int index) {
        if (!awaitingLangostaRecovery) {
            return "No hay recuperación pendiente de la Langosta espinosa.";
        }
        if (index < 0 || index >= lostDice.size()) {
            return "Debes elegir un dado perdido válido.";
        }
        Die recovered = lostDice.remove(index);
        reserve.add(recovered.getType());
        awaitingLangostaRecovery = false;
        String msg = "Langosta espinosa recuperó " + recovered.getLabel() + ".";

        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
        }

        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }
        return msg;
    }

    private String replaceWithMantisDie(int slotIndex) {
        if (mantisRerolledDie == null) {
            clearPendingSelection();
            clearMantisState();
            return "No hay dado del Langostino mantis para colocar.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) {
            return offerCancelAbility("Langostino mantis");
        }
        if (target.getDice().isEmpty()) {
            return offerCancelAbility("Langostino mantis");
        }
        Die replaced = target.removeDie(target.getDice().size() - 1);
        lostDice.add(replaced);
        String reveal = addDieToSlot(slotIndex, mantisRerolledDie);
        clearPendingSelection();
        clearMantisState();
        if (reveal.isEmpty()) {
            return "Langostino mantis reemplazó un dado en la zona de pesca.";
        }
        return "Langostino mantis reemplazó un dado en la zona de pesca. " + reveal;
    }

    private String replaceAdjacentObject(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.OBJETO) {
                failedDiscards.add(adj.getCard());

                // ✅ DEVOLVER DADOS A RESERVA (como en capture)
                for (Die d : new ArrayList<>(adj.getDice())) {
                    reserve.add(d.getType());
                }
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

    private String startDecoradorSelection(int slotIndex) {
        boolean hasTarget = false;
        for (BoardSlot s : board) {
            if (s.getCard() != null && !s.isFaceUp() && s.getDice().isEmpty()) {
                hasTarget = true;
                break;
            }
        }
        if (!hasTarget) {
            return "Cangrejo decorador: no hay cartas boca abajo sin dados para reemplazar.";
        }
        pendingDecoradorOptions.clear();
        for (Card c : deck) {
            if (c.getType() == CardType.OBJETO) {
                pendingDecoradorOptions.add(c);
            }
        }
        if (pendingDecoradorOptions.isEmpty()) {
            return "Cangrejo decorador: no hay objetos en el mazo.";
        }
        awaitingDecoradorChoice = true;
        decoradorSlotIndex = slotIndex;
        return "Cangrejo decorador: elige un objeto del mazo.";
    }

    private String startHorseshoeAdjustment(int slotIndex) {
        boolean hasDice = false;
        for (BoardSlot s : board) {
            if (!s.getDice().isEmpty()) {
                hasDice = true;
                break;
            }
        }
        if (!hasDice) {
            return "Cangrejo herradura: no hay dados en la zona de pesca.";
        }
        return queueableSelection(
                PendingSelection.HORSESHOE_DIE,
                slotIndex,
                "Cangrejo herradura: elige un dado para ajustar a cualquier valor.");
    }

    private String startViolinistCapture() {
        if (failedDiscards.isEmpty()) {
            return "No hay cartas descartadas por fallo para capturar.";
        }
        awaitingViolinistChoice = true;
        return "Cangrejo violinista: elige una carta descartada para capturarla.";
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
        if (adjustmentSource == CardId.LOCO) {
            if (pendingLocoDie == null) {
                clearValueAdjustmentState();
                return "No hay dado del Loco para ajustar.";
            }
            if (adjustmentSlotIndex < 0 || adjustmentSlotIndex >= board.length) {
                clearValueAdjustmentState();
                return "La carta destino del Loco ya no está disponible.";
            }
            BoardSlot slot = board[adjustmentSlotIndex];
            if (slot.getCard() == null || slot.getDice().size() >= 2) {
                clearValueAdjustmentState();
                return "La carta destino del Loco ya no está disponible.";
            }
            int delta = increase ? adjustmentAmount : -adjustmentAmount;
            int newVal = pendingLocoDie.getValue() + delta;
            if (newVal < 1 || newVal > pendingLocoDie.getType().getSides()) {
                return "No puedes ajustar el dado más allá de sus caras. Elige la otra opción.";
            }
            Die adjusted = new Die(pendingLocoDie.getType(), newVal);
            String reveal = addDieToSlotInternal(adjustmentSlotIndex, adjusted, false);
            String base = "Loco movió un dado a " + slot.getCard().getName() + " (" + newVal + ").";

            pendingLocoDie = null;
            awaitingValueAdjustment = false;
            clearValueAdjustmentState();

            String msg = reveal.isEmpty() ? base : base + " " + reveal;

            if (!pendingLocoDice.isEmpty()) {
                if (!hasLocoTargets(pendingSelectionActor)) {
                    for (Die die : pendingLocoDice) {
                        reserve.add(die.getType());
                    }
                    pendingLocoDice.clear();
                    clearPendingSelection();
                    msg += " Loco no encontró más cartas adyacentes con espacio; los dados restantes regresan a la reserva.";
                } else {
                    return msg + " Elige otra carta adyacente.";
                }
            }

            if (pendingSelection == PendingSelection.LOCO_TARGET) {
                clearPendingSelection();
            }

            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;
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
        String actor;
        if (adjustmentSource == CardId.NAUTILUS) {
            actor = "Nautilus";
        } else if (adjustmentSource == CardId.LAMPREA) {
            actor = "Lamprea";
        } else {
            actor = "Jaiba azul";
        }
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

            String msg = result + " No hay un segundo dado disponible.";

// ✅ Reanudar cadena de revelaciones
            String revealLog = continueRevealChain("");
            if (!revealLog.isEmpty()) {
                msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
            }

// ✅ Safety net
            String endTurn = resolveAllReadySlots();
            if (!endTurn.isEmpty()) {
                msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
            }

            return msg;

        }

        if (fromNautilus && pendingSelection == PendingSelection.NAUTILUS_SECOND) {
            clearPendingSelection();
        } else if (adjustmentSource == CardId.JAIBA_AZUL) {
            clearPendingSelection();
        }

        clearValueAdjustmentState();

// ✅ Reanudar cadena de revelaciones
        String msg = result;
        String revealLog = continueRevealChain("");
        if (!revealLog.isEmpty()) {
            msg = msg.isEmpty() ? revealLog : msg + " " + revealLog;
        }

// ✅ Safety net
        String endTurn = resolveAllReadySlots();
        if (!endTurn.isEmpty()) {
            msg = msg.isEmpty() ? endTurn : msg + " " + endTurn;
        }

        return msg;

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
        target.getStatus().protectedBySlot = pendingSelectionActor; // <-- clave para dibujar vínculo

        clearPendingSelection();
        return "Pez payaso protege la carta seleccionada.";
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
        for (Die d : preservedDice) {
            if (adj.getDice().size() < 2) {
                adj.addDie(d);
            } else {
                reserve.add(d.getType());
            }
        }
        String base = "Piraña descartó a " + removed.getName() + " sin perder sus dados.";
        if (pendingSelection == PendingSelection.PIRANA_TARGET) {
            clearPendingSelection();
        }
        return base;
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

    private String recoverAnyLostDie() {
        if (lostDice.isEmpty()) {
            return "";
        }
        Die recovered = lostDice.remove(lostDice.size() - 1);
        reserve.add(recovered.getType());
        return "Recuperaste un dado perdido.";
    }

    private String startArenqueSelection(int slotIndex) {
        int availableSlots = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                availableSlots++;
            }
        }
        if (availableSlots == 0) {
            return "Arenque: no hay cartas boca abajo adyacentes para reemplazar.";
        }

        List<Card> remaining = new ArrayList<>(deck);
        deck.clear();
        pendingArenquePool.clear();
        pendingArenqueChosen.clear();
        arenqueSlotIndex = slotIndex;
        arenquePlacementSlots = availableSlots;
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
            arenquePlacementSlots = 0;
            return "No hay peces pequeños en el mazo.";
        }
        awaitingArenqueChoice = true;
        return availableSlots == 1
                ? "Arenque: elige 1 pez pequeño del mazo para colocarlo adyacente."
                : "Arenque: elige hasta 2 peces pequeños del mazo.";
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
            if (unique.size() >= arenquePlacementSlots) break;
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
        int remainingSlots = Math.min(arenquePlacementSlots, pendingArenqueChosen.size());
        String message = remainingSlots == 1
                ? "Coloca el pez pequeño elegido adyacente al Arenque."
                : "Coloca los 2 peces pequeños adyacentes al Arenque.";
        return queueableSelection(PendingSelection.ARENQUE_DESTINATION, arenqueSlotIndex, message);
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
        if (target.getCard() == null || target.isFaceUp()) {
            return "Debes elegir una carta boca abajo adyacente al Arenque.";
        }
        if (!target.getDice().isEmpty()) {
            return "Debes elegir una casilla sin dados.";
        }
        deck.push(target.getCard());
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
        // Esto se usa por otras mecánicas visuales (corrientes, etc.)
        for (BoardSlot slot : board) {
            if (slot.getStatus() != null) {
                slot.getStatus().sumConditionShift = 0;
                slot.getStatus().autoHundidoBonus = false;
                slot.getStatus().glassBottlePenalty = false;
                // OJO: NO reseteamos bottleDieBonus.
                // Ahora bottleDieBonus es un "flag" visual permanente del cambio ya aplicado.
            }
        }

        // Solo valida y limpia vínculos inválidos para no dibujar líneas fantasmas
        List<Integer> invalid = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : bottleTargets.entrySet()) {
            int bottleIndex = entry.getKey();
            int targetIndex = entry.getValue();
            if (!isBottleLinkActive(bottleIndex, targetIndex)) {
                invalid.add(bottleIndex);
            }
        }
        for (Integer key : invalid) {
            bottleTargets.remove(key);
        }

        List<Integer> invalidGlass = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : glassBottleTargets.entrySet()) {
            int bottleIndex = entry.getKey();
            int targetIndex = entry.getValue();
            if (!isGlassBottleLinkActive(bottleIndex, targetIndex)) {
                invalidGlass.add(bottleIndex);
            }
        }
        for (Integer key : invalidGlass) {
            glassBottleTargets.remove(key);
        }

        for (Map.Entry<Integer, Integer> entry : glassBottleTargets.entrySet()) {
            int targetIndex = entry.getValue();
            if (targetIndex >= 0 && targetIndex < board.length) {
                BoardSlot slot = board[targetIndex];
                if (slot.getStatus() != null) {
                    slot.getStatus().glassBottlePenalty = true;
                }
            }
        }
    }


    private boolean isBottleLinkActive(int bottleIndex, int targetIndex) {
        if (bottleIndex < 0 || bottleIndex >= board.length || targetIndex < 0 || targetIndex >= board.length) {
            return false;
        }
        if (!adjacentIndices(bottleIndex, true).contains(targetIndex)) return false;
        BoardSlot bottle = board[bottleIndex];
        BoardSlot target = board[targetIndex];
        return bottle.getCard() != null && bottle.isFaceUp()
                && bottle.getCard().getId() == CardId.BOTELLA_PLASTICO
                && target.getCard() != null && target.isFaceUp()
                && target.getCard().getType() == CardType.PEZ;
    }

    private boolean isGlassBottleLinkActive(int bottleIndex, int targetIndex) {
        if (bottleIndex < 0 || bottleIndex >= board.length || targetIndex < 0 || targetIndex >= board.length) {
            return false;
        }
        if (!adjacentIndices(bottleIndex, true).contains(targetIndex)) return false;
        BoardSlot bottle = board[bottleIndex];
        BoardSlot target = board[targetIndex];
        return bottle.getCard() != null && bottle.isFaceUp()
                && bottle.getCard().getId() == CardId.BOTELLA_DE_VIDRIO
                && target.getCard() != null && target.isFaceUp();
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
            if (adj.getStatus() != null) {
                adj.getStatus().lastTriggeredBySlot = triggeredSlotIndex;
            }

            if (log.length() > 0) log.append(" ");
            log.append("Almejas lanzó un ").append(rolled.getLabel()).append(".");

            // ✅ FIX: si ahora tiene 2 dados, debe resolverse captura/fallo
            if (adj.getDice().size() >= 2) {
                String outcome = resolveFishingOutcome(idx, rolled.getValue(), "", false); // sin corrientes
                if (outcome != null && !outcome.isEmpty()) {
                    log.append(" ").append(outcome);
                }
            }
        }

        return log.toString();
    }

    private String triggerAdjacentOysters(int triggeredSlotIndex, boolean abilityTriggered) {
        if (!abilityTriggered) {
            return "";
        }
        StringBuilder log = new StringBuilder();
        for (Integer idx : adjacentIndices(triggeredSlotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() == null || !adj.isFaceUp() || adj.getCard().getId() != CardId.OSTRAS) {
                continue;
            }
            if (lostDice.isEmpty()) {
                continue;
            }
            List<Integer> candidates = new ArrayList<>();
            for (int i = 0; i < board.length; i++) {
                BoardSlot target = board[i];
                if (target.getCard() != null && !target.isFaceUp() && target.getDice().size() < 2) {
                    candidates.add(i);
                }
            }
            if (candidates.isEmpty()) {
                continue;
            }
            Die recovered = lostDice.remove(lostDice.size() - 1);
            Die rolled = Die.roll(recovered.getType(), rng);
            int targetIndex = candidates.get(rng.nextInt(candidates.size()));
            String reveal = addDieToSlotInternal(targetIndex, rolled, false);
            if (log.length() > 0) log.append(" ");
            String base = "Ostras lanzó un " + rolled.getLabel() + " y lo colocó al azar.";
            log.append(reveal.isEmpty() ? base : base + " " + reveal);
        }
        return log.toString();
    }

    private String resolveAllReadySlots() {
        // No intentes resolver si el juego está esperando decisiones del jugador
        if (hasPendingTurnResolutions() || selectedDie != null) {
            return "";
        }

        StringBuilder log = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() == null) continue;
            if (!s.isFaceUp()) continue;
            if (s.getDice().size() < 2) continue;

            // Resolver SIN corrientes (esto no es "colocar dado", es "estado listo")
            int triggerValue = s.getDice().get(s.getDice().size() - 1).getValue();
            String outcome = resolveFishingOutcome(i, triggerValue, "", false);

            if (outcome != null && !outcome.isEmpty()) {
                if (log.length() > 0) log.append(" ");
                log.append(outcome);
            }

            // Si esto abrió una decisión pendiente (p.ej. falló y pide elegir dado),
            // corta aquí para no encadenar más cosas.
            if (hasPendingTurnResolutions()) {
                break;
            }
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

    private String attachLampreaToBigFish(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        List<Integer> candidates = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getType() == CardType.PEZ_GRANDE) {
                candidates.add(idx);
            }
        }
        if (candidates.isEmpty()) {
            return "No hay peces grandes boca arriba adyacentes para la lamprea.";
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
        return "Lamprea se adhirió a un pez grande.";
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

        // IMPORTANTE: aunque exista solo 1 opción, igual debe seleccionarse manualmente.
        return queueableSelection(
                PendingSelection.WHITE_SHARK_TARGET,
                slotIndex,
                "Tiburón blanco: elige una carta adyacente boca arriba para devorarla.");
    }

    private String startTigerSharkSelection(int slotIndex) {
        List<Integer> targets = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                targets.add(idx);
            }
        }
        if (targets.isEmpty()) {
            return "No hay cartas boca arriba adyacentes para el tiburón tigre.";
        }
        return queueableSelection(
                PendingSelection.TIGER_SHARK_TARGET,
                slotIndex,
                "Tiburón tigre: elige una carta adyacente boca arriba para eliminarla.");
    }

    private String resolveTigerSharkDevour(int targetIndex) {
        if (!adjacentIndices(pendingSelectionActor, true).contains(targetIndex)) {
            return "Elige una carta adyacente al tiburón tigre.";
        }
        BoardSlot adj = board[targetIndex];
        if (adj.getCard() == null || !adj.isFaceUp()) {
            return "Debes elegir una carta boca arriba para eliminar.";
        }
        for (Die d : new ArrayList<>(adj.getDice())) {
            reserve.add(d.getType());
        }
        failedDiscards.add(adj.getCard());
        adj.clearDice();
        adj.setCard(deck.isEmpty() ? null : deck.pop());
        adj.setFaceUp(false);
        adj.setStatus(new SlotStatus());
        clearPendingSelection();
        return "Tiburón tigre eliminó una carta adyacente y recuperaste sus dados.";
    }

    private String startDelfinProtection(int slotIndex) {
        BoardSlot slot = board[slotIndex];
        slot.getStatus().delfinProtectionUsed = false;
        return "Delfín protegerá el próximo fallo adyacente.";
    }

    private String startPeregrinoPeek(int slotIndex) {
        if (deck.isEmpty()) {
            return "Tiburón Peregrino: el mazo está vacío.";
        }
        pendingPeregrinoTop.clear();
        pendingPeregrinoSlot = slotIndex;
        pendingPeregrinoTopChoice = null;
        int count = Math.min(5, deck.size());
        for (int i = 0; i < count; i++) {
            pendingPeregrinoTop.add(deck.pop());
        }
        awaitingPeregrinoChoice = true;
        return "Tiburón Peregrino: elige una carta para dejar arriba.";
    }

    public String choosePeregrinoTop(int index) {
        if (!awaitingPeregrinoChoice) {
            return "No hay selección pendiente del Tiburón Peregrino.";
        }
        if (pendingPeregrinoTop.isEmpty()) {
            awaitingPeregrinoChoice = false;
            return "Tiburón Peregrino: no hay cartas disponibles.";
        }
        if (index < 0 || index >= pendingPeregrinoTop.size()) {
            return "Selección inválida.";
        }
        pendingPeregrinoTopChoice = pendingPeregrinoTop.remove(index);
        return "Tiburón Peregrino: elige una carta para poner al fondo.";
    }

    public String choosePeregrinoBottom(int index) {
        if (!awaitingPeregrinoChoice || pendingPeregrinoTopChoice == null) {
            return "No hay elección pendiente del Tiburón Peregrino.";
        }
        if (pendingPeregrinoTop.isEmpty()) {
            deck.push(pendingPeregrinoTopChoice);
            pendingPeregrinoTopChoice = null;
            awaitingPeregrinoChoice = false;
            return "Tiburón Peregrino reorganizó el mazo.";
        }
        if (index < 0 || index >= pendingPeregrinoTop.size()) {
            return "Selección inválida.";
        }
        Card bottom = pendingPeregrinoTop.remove(index);
        for (Card c : pendingPeregrinoTop) {
            deck.push(c);
        }
        deck.addLast(bottom);
        deck.push(pendingPeregrinoTopChoice);
        pendingPeregrinoTop.clear();
        pendingPeregrinoTopChoice = null;
        awaitingPeregrinoChoice = false;
        return "Tiburón Peregrino reorganizó el mazo.";
    }

    private String returnAdjacentFaceUpToDeck(int slotIndex) {
        int returned = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                for (Die d : new ArrayList<>(adj.getDice())) {
                    reserve.add(d.getType());
                }
                adj.clearDice();
                deck.addLast(adj.getCard());
                adj.setCard(null);
                adj.setFaceUp(false);
                adj.setStatus(new SlotStatus());
                returned++;
            }
        }
        shuffleDeck();
        return returned == 0 ? "Narval: no había cartas boca arriba adyacentes." :
                "Narval regresó " + returned + " carta(s) al mazo.";
    }

    private String flipAdjacentFaceUpCardsDownAndRecover(int slotIndex) {
        int flipped = 0;
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                for (Die d : new ArrayList<>(adj.getDice())) {
                    reserve.add(d.getType());
                }
                adj.clearDice();
                adj.setFaceUp(false);
                flipped++;
            }
        }
        recomputeBottleAdjustments();
        return flipped == 0 ? "Orca: no había cartas boca arriba adyacentes." :
                "Orca volteó " + flipped + " carta(s) boca abajo y recuperó sus dados.";
    }

    private String rerollAdjacentDiceWithRecovery(int slotIndex) {
        boolean recovered = false;
        int rerolled = 0;
        recentlyRerolledSlots.clear();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getDice().isEmpty()) continue;
            if (!recentlyRerolledSlots.contains(idx)) {
                recentlyRerolledSlots.add(idx);
            }
            for (int i = 0; i < adj.getDice().size(); i++) {
                Die d = adj.getDice().get(i);
                Die rolled = Die.roll(d.getType(), rng);
                adj.setDie(i, rolled);
                rerolled++;
                if (rolled.getValue() == rolled.getType().getSides()) {
                    recovered = true;
                }
            }
        }
        if (recovered && !lostDice.isEmpty()) {
            Die rec = lostDice.remove(lostDice.size() - 1);
            reserve.add(rec.getType());
        }
        if (rerolled == 0) {
            return "Anguila eléctrica: no había dados adyacentes para relanzar.";
        }
        return recovered
                ? "Anguila eléctrica relanzó dados y recuperaste un dado perdido."
                : "Anguila eléctrica relanzó dados adyacentes.";
    }

    private String startCachaloteReposition(int slotIndex) {
        List<Die> pool = new ArrayList<>();
        for (BoardSlot s : board) {
            if (!s.getDice().isEmpty()) {
                pool.addAll(s.getDice());
                s.clearDice();
            }
        }
        if (pool.isEmpty()) {
            return "Cachalote: no hay dados para reposicionar.";
        }
        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() != null && s.isFaceUp() && s.getDice().isEmpty()) {
                targets.add(i);
            }
        }
        int placed = 0;
        for (Die die : pool) {
            if (targets.isEmpty()) {
                reserve.add(die.getType());
                continue;
            }
            int targetIdx = targets.get(0);
            BoardSlot target = board[targetIdx];
            target.addDie(die);
            placed++;
            targets.remove(0);
        }
        String base = "Cachalote reposicionó " + placed + " dado(s) en cartas boca arriba sin dados.";
        String resolved = resolveAllReadySlots();
        return resolved.isEmpty() ? base : base + " " + resolved;
    }

    private String startEsturionRoll(int slotIndex) {
        if (reserve.isEmpty()) {
            return "Esturión: no hay dados en reserva.";
        }
        if (reserve.size() == 1) {
            return "Esturión: no hay suficientes dados en reserva para lanzar.";
        }
        List<DieType> toRoll = new ArrayList<>(reserve);
        DieType saved = toRoll.remove(toRoll.size() - 1);
        reserve.clear();
        reserve.add(saved);
        List<Die> rolled = new ArrayList<>();
        for (DieType type : toRoll) {
            rolled.add(Die.roll(type, rng));
        }
        if (rolled.isEmpty()) {
            return "Esturión: no hay dados para lanzar.";
        }
        pendingBallenaDice.clear();
        pendingBallenaDice.addAll(rolled);
        pendingBallenaTotal = pendingBallenaDice.size();
        return queueableSelection(
                PendingSelection.ESTURION_PLACE,
                slotIndex,
                "Esturión: coloca cada dado en la zona de pesca. Comienza con "
                        + pendingBallenaDice.get(0).getLabel() + ".");
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
        String baseLog = reveal.length() == 0
                ? "Tiburón blanco devoró una carta adyacente."
                : "Tiburón blanco devoró una carta adyacente. " + reveal;

        int lastValue = shark.getDice().isEmpty()
                ? 0
                : shark.getDice().get(shark.getDice().size() - 1).getValue();
        if (shark.getDice().size() >= 2) {
            String outcome = resolveFishingOutcome(sharkSlot, lastValue, "", false);
            if (!outcome.isEmpty()) {
                return baseLog + " " + outcome;
            }
        }
        return baseLog;
    }

    private String flipAdjacentCardsDown(int slotIndex) {
        List<Integer> toReveal = new ArrayList<>();
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot adj = board[idx];
            if (adj.getCard() != null && !adj.isFaceUp()) {
                toReveal.add(idx);
            }
        }
        if (toReveal.isEmpty()) return "";
        pendingRevealChain.addAll(toReveal);
        int before = pendingRevealChain.size();
        String chain = continueRevealChain("");
        int processed = before - pendingRevealChain.size();
        String base = processed == 0 ? "" : "Mero gigante volteó " + processed + " carta(s).";
        if (!chain.isEmpty()) {
            base = base.isEmpty() ? chain : base + " " + chain;
        }
        if (!pendingRevealChain.isEmpty()) {
            base = base.isEmpty() ? "Revelaciones adicionales en espera." : base + " Revelaciones adicionales en espera.";
        }
        return base;
    }

    private String handleOnCapture(int slotIndex, List<Die> diceOnCard) {
        Card captured = board[slotIndex].getCard();
        if (captured == null) return "";
        if (shouldConfirmCaptureAbility(captured.getId())) {
            List<Die> snapshot = diceOnCard == null ? null : new ArrayList<>(diceOnCard);
            return enqueueAbilityConfirmation(AbilityTrigger.CAPTURE, slotIndex, 0, snapshot);
        }
        return executeCaptureAbility(captured, slotIndex, diceOnCard);
    }

    private String executeCaptureAbility(Card captured, int slotIndex, List<Die> diceOnCard) {
        if (captured == null) return "";
        String remoraLog = collectAttachedRemoras(slotIndex);
        String result;
        switch (captured.getId()) {
            case LANGOSTA_ESPINOSA:
                result = recoverIfD8Used(slotIndex, diceOnCard) + remoraLog;
                break;
            case BOGAVANTE:
                result = recoverAnyLostDie() + remoraLog;
                break;
            case RED_ENREDADA:
                captureAdjacentFaceDown(slotIndex);
                result = "Red enredada arrastra una carta adyacente." + remoraLog;
                break;
            case RED_DE_ARRASTRE:
                result = handleDragnetCapture(slotIndex) + remoraLog;
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
                result = startPercebesMove(slotIndex, diceOnCard) + remoraLog;
                break;
            case LOCO:
                result = moveLocoDice(slotIndex, diceOnCard) + remoraLog;
                break;
            case CABALLITO_DE_MAR:
                result = recoverSpecificDie(DieType.D4) + remoraLog;
                break;
            case PEZ_PIPA:
                result = recoverIfD12Used(diceOnCard) + remoraLog;
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
        String oysters = triggerAdjacentOysters(slotIndex, !result.isEmpty());
        if (!oysters.isEmpty()) {
            result = result.isEmpty() ? oysters : result + " " + oysters;
        }
        return result;
    }

    private String recoverIfD8Used(int slotIndex, List<Die> diceOnCard) {
        if (diceOnCard == null) {
            return "";
        }
        if (board[slotIndex].getStatus().langostaRecovered) {
            return "";
        }
        boolean usedD8 = false;
        for (Die d : diceOnCard) {
            if (d.getType() == DieType.D8) {
                usedD8 = true;
                break;
            }
        }
        if (!usedD8 || lostDice.isEmpty()) {
            return "";
        }
        awaitingLangostaRecovery = true;
        return "Langosta espinosa: elige un dado perdido para recuperar.";
    }

    private String startPercebesMove(int slotIndex, List<Die> diceOnCard) {
        List<Die> dice = diceOnCard == null ? new ArrayList<>() : new ArrayList<>(diceOnCard);
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
        return queueableSelection(
                PendingSelection.PERCEBES_MOVE,
                slotIndex,
                "Percebes: elige una carta adyacente para mover un dado.");
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

    private String moveLocoDice(int slotIndex, List<Die> diceOnCard) {
        if (diceOnCard == null || diceOnCard.isEmpty()) {
            return "";
        }
        board[slotIndex].clearDice();
        clearLocoState(false);
        pendingLocoDice.addAll(diceOnCard);
        if (!hasLocoTargets(slotIndex)) {
            for (Die d : pendingLocoDice) {
                reserve.add(d.getType());
            }
            clearLocoState(false);
            return "Loco no encontró cartas adyacentes con espacio; los dados regresan a la reserva.";
        }
        return queueableSelection(
                PendingSelection.LOCO_TARGET,
                slotIndex,
                "Loco: elige una carta adyacente para mover un dado.");
    }

    private String chooseLocoTarget(int slotIndex) {
        if (!adjacentIndices(pendingSelectionActor, true).contains(slotIndex)) {
            return "Elige una carta adyacente a Loco.";
        }
        BoardSlot target = board[slotIndex];
        if (target.getCard() == null) {
            return "Esa casilla no tiene carta.";
        }
        if (target.getDice().size() >= 2) {
            return "La carta seleccionada ya tiene 2 dados.";
        }
        if (pendingLocoDice.isEmpty()) {
            clearPendingSelection();
            clearLocoState(false);
            return "No hay dados por mover.";
        }
        pendingLocoDie = pendingLocoDice.remove(0);
        return startValueAdjustment(
                slotIndex,
                -1,
                1,
                CardId.LOCO,
                "Loco: elige si sumar o restar 1 al dado.");
    }

    private boolean hasLocoTargets(int slotIndex) {
        for (Integer idx : adjacentIndices(slotIndex, true)) {
            BoardSlot target = board[idx];
            if (target.getCard() != null && target.getDice().size() < 2) {
                return true;
            }
        }
        return false;
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
        return queueableSelection(
                PendingSelection.SALMON_FLIP,
                -1,
                "Salmón: selecciona una carta boca abajo para revelarla.");
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
        String actorName = pendingSelection == PendingSelection.ESTURION_PLACE ? "Esturión" : "Ballena azul";
        String completion = pendingSelection == PendingSelection.ESTURION_PLACE
                ? "colocó todos los dados en la zona de pesca."
                : "reposicionó todos los dados en el tablero.";
        if (pendingBallenaDice.isEmpty()) {
            clearPendingSelection();
            return reveal.isEmpty()
                    ? actorName + " " + completion
                    : actorName + " " + completion + " " + reveal;
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
    private void clearTransientVisualMarks() {
        for (BoardSlot s : board) {
            if (s.getStatus() != null) {
                s.getStatus().lastTriggeredBySlot = -1;
            }
        }
    }

    public List<Link> getBoardLinks() {
        recomputeBottleAdjustments();
        List<Link> links = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            BoardSlot s = board[i];
            if (s.getCard() == null || !s.isFaceUp()) continue;

            CardId id = s.getCard().getId();

            switch (id) {
                case PEZ_PAYASO:
                    // buscar cartas que estén protegidas por ESTE payaso (tracked)
                    for (Integer adj : adjacentIndices(i, true)) {
                        BoardSlot t = board[adj];
                        if (t.getCard() != null && t.isFaceUp()
                                && t.getStatus() != null
                                && t.getStatus().protectedOnce
                                && t.getStatus().protectedBySlot == i) {
                            links.add(new Link(i, adj, LinkType.PAYASO_PROTEGE));
                        }
                    }
                    break;

                case ALMEJAS:
                    if (s.getStatus() != null && s.getStatus().lastTriggeredBySlot >= 0) {
                        links.add(new Link(s.getStatus().lastTriggeredBySlot, i, LinkType.ALMEJAS_REACCION));
                    }
                    break;

                case BOTA_VIEJA:
                    for (Integer adj : adjacentIndices(i, true)) {
                        BoardSlot t = board[adj];
                        if (t.getCard() != null) {
                            links.add(new Link(i, adj, LinkType.BOTA_VIEJA_PENALIZA));
                        }
                    }
                    break;

                case BOTELLA_PLASTICO: {
                    // Solo dibujar enlace hacia el pez marcado (si existe y sigue siendo válido)
                    Integer target = bottleTargets.get(i);
                    if (target != null && isBottleLinkActive(i, target)) {
                        links.add(new Link(i, target, LinkType.BOTELLA_PLASTICO_AJUSTA));
                    }
                    break;
                }


                default:
                    break;
            }
        }

        return links;
    }
    public boolean shouldShowRemoraBorder(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= board.length) return false;
        BoardSlot s = board[slotIndex];
        return s.getCard() != null
                && s.isFaceUp()
                && s.getStatus() != null
                && s.getStatus().attachedRemoras != null
                && !s.getStatus().attachedRemoras.isEmpty();
    }


}
