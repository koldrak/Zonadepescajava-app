package com.daille.zonadepescajava_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.data.ScoreDatabaseHelper;
import com.daille.zonadepescajava_app.data.ScoreRecord;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.Die;
import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;
import com.daille.zonadepescajava_app.model.GameUtils;
import com.daille.zonadepescajava_app.ui.BoardLinksDecoration;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;
import com.daille.zonadepescajava_app.ui.CardFullscreenDialog;
import com.daille.zonadepescajava_app.ui.CardImageResolver;
import com.daille.zonadepescajava_app.ui.CollectionCardAdapter;
import com.daille.zonadepescajava_app.ui.DiceImageResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements BoardSlotAdapter.OnSlotInteractionListener {

    private ActivityMainBinding binding;
    private GameViewModel viewModel;
    private GameState gameState;
    private BoardSlotAdapter adapter;
    private CardImageResolver cardImageResolver;
    private DiceImageResolver diceImageResolver;
    private Handler animationHandler;
    private Runnable rollingRunnable;
    private boolean endScoringShown = false;
    private boolean isRevealingCard = false;
    private ScoreDatabaseHelper scoreDatabaseHelper;
    private ArrayAdapter<String> scoreRecordsAdapter;
    private CollectionCardAdapter collectionCardAdapter;
    private final List<ImageView> diceTokens = new ArrayList<>();
    private final Card[] lastBoardCards = new Card[9];
    private BoardLinksDecoration boardLinksDecoration;
    private final List<Card> lastCaptures = new ArrayList<>();
    private float lastReserveTapCenterX = Float.NaN;
    private float lastReserveTapCenterY = Float.NaN;
    private long lastReserveTapAtMs = 0L;


    private static class CaptureAnimationRequest {
        private final Card card;
        private final int slotIndex;

        CaptureAnimationRequest(Card card, int slotIndex) {
            this.card = card;
            this.slotIndex = slotIndex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameState = viewModel.getGameState();
        endScoringShown = viewModel.isFinalScoreRecorded();
        cardImageResolver = new CardImageResolver(this);
        diceImageResolver = new DiceImageResolver(this);
        animationHandler = new Handler(Looper.getMainLooper());
        scoreDatabaseHelper = new ScoreDatabaseHelper(this);
        setupScoreRecordsList();
        setupMenuButtons();
        setupDiceSelectionUi();
        setupCollectionsPanel();

        if (viewModel.isInitialized()) {
            setupBoard();
            snapshotBoardState();
            if (viewModel.isFinalScoreRecorded()) {
                showStartMenu();
            } else {
                showGameLayout();
            }
            refreshUi("Partida restaurada tras cambio de orientación.");
        } else {
            showStartMenu();
        }
    }

    private void setupBoard() {
        adapter = new BoardSlotAdapter(this, Arrays.asList(gameState.getBoard()), this);
        binding.gamePanel.boardRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.gamePanel.boardRecycler.setAdapter(adapter);

        // ✅ Instalar decoration UNA vez
        if (boardLinksDecoration == null) {
            boardLinksDecoration = new BoardLinksDecoration(this);
            binding.gamePanel.boardRecycler.addItemDecoration(boardLinksDecoration);
        }
    }



    private void setupMenuButtons() {
        binding.startMenu.startNewGame.setOnClickListener(v -> {
            showDiceSelectionPanel();
        });
        binding.diceSelectionPanel.confirmDiceSelection.setOnClickListener(v -> {
            List<DieType> startingReserve = extractSelectedDice();
            if (startingReserve.size() != 7) {
                Toast.makeText(this, "Selecciona exactamente 7 dados antes de iniciar.", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<CardId, Integer> captureCounts = scoreDatabaseHelper.getCaptureCounts();
            viewModel.startNewGame(startingReserve, captureCounts);
            gameState = viewModel.getGameState();
            endScoringShown = false;
            setupBoard();
            snapshotBoardState();
            showGameLayout();
            refreshUi("Juego iniciado. Lanza un dado y toca una carta.");
        });
        binding.startMenu.openSettings.setOnClickListener(v ->
                Toast.makeText(this, "Configuraciones próximamente.", Toast.LENGTH_SHORT).show());
        binding.startMenu.openCollections.setOnClickListener(v -> showCollectionsPanel());
    }

    private void setupScoreRecordsList() {
        scoreRecordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.startMenu.scoreRecordsList.setAdapter(scoreRecordsAdapter);
        binding.startMenu.scoreRecordsList.setEmptyView(binding.startMenu.scoreRecordsEmpty);
        refreshScoreRecords();
    }

    private void setupCollectionsPanel() {
        collectionCardAdapter = new CollectionCardAdapter(this);
        binding.collectionsPanel.collectionsRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.collectionsPanel.collectionsRecycler.setAdapter(collectionCardAdapter);
        binding.collectionsPanel.closeCollections.setOnClickListener(v -> showStartMenu());
    }

    private void refreshCollections() {
        Map<CardId, Integer> counts = scoreDatabaseHelper.getCaptureCounts();
        List<CollectionCardAdapter.CollectionEntry> entries = new ArrayList<>();
        for (Card card : GameUtils.createAllCards()) {
            int count = 0;
            if (counts.containsKey(card.getId())) {
                count = counts.get(card.getId());
            }
            entries.add(new CollectionCardAdapter.CollectionEntry(card, count));
        }
        collectionCardAdapter.submitList(entries, counts);
    }

    private void refreshScoreRecords() {
        List<ScoreRecord> records = scoreDatabaseHelper.getTopScores(10);
        List<String> labels = new ArrayList<>();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        for (int i = 0; i < records.size(); i++) {
            ScoreRecord record = records.get(i);
            String date = dateFormat.format(new Date(record.getCreatedAt()));
            labels.add(String.format(Locale.getDefault(), "#%d • %d puntos (%s)", i + 1, record.getScore(), date));
        }

        scoreRecordsAdapter.clear();
        scoreRecordsAdapter.addAll(labels);
        scoreRecordsAdapter.notifyDataSetChanged();

        // ✅ CLAVE: ListView dentro de ScrollView => fijar altura
        binding.startMenu.scoreRecordsList.post(() ->
                setListViewHeightBasedOnChildren(binding.startMenu.scoreRecordsList)
        );
    }
    private static void setListViewHeightBasedOnChildren(android.widget.ListView listView) {
        android.widget.ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        int widthSpec = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View item = listAdapter.getView(i, null, listView);
            item.measure(widthSpec, View.MeasureSpec.UNSPECIFIED);
            totalHeight += item.getMeasuredHeight();
        }

        int dividers = listView.getDividerHeight() * Math.max(0, listAdapter.getCount() - 1);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + dividers;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    private void persistFinalScore(int finalScore) {
        if (!viewModel.isFinalScoreRecorded()) {
            scoreDatabaseHelper.saveScore(finalScore);
            viewModel.markFinalScoreRecorded();
            refreshScoreRecords();
        }
    }

    private void showStartMenu() {
        resetDiceSelection();
        binding.startMenu.getRoot().setVisibility(View.VISIBLE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        refreshScoreRecords(); // ✅ asegura recarga al mostrar menú
    }

    private void showDiceSelectionPanel() {
        resetDiceSelection();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.VISIBLE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showGameLayout() {
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.VISIBLE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showCollectionsPanel() {
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.VISIBLE);
        refreshCollections();
    }

    private void refreshUi(String log) {
        refreshUi(log, null);
    }

    private void refreshUi(String log, Runnable afterReveals) {
        List<CaptureAnimationRequest> captureAnimations = collectCaptureAnimations();
        List<ReleaseAnimationRequest> releaseAnimations = collectReleaseAnimations();
        List<Integer> refillSlots = collectRefillSlots();

        adapter.update(
                Arrays.asList(gameState.getBoard()),
                gameState.getHighlightSlots(),
                gameState.computeRemoraBorderSlots(),
                gameState.computeBotaViejaPenaltySlots()
        );


// ✅ Pasa links al decoration (necesitas un getter en GameState)
        if (boardLinksDecoration != null) {
            boardLinksDecoration.setLinks(gameState.getBoardLinks());
            binding.gamePanel.boardRecycler.invalidateItemDecorations();
            binding.gamePanel.boardRecycler.postInvalidateOnAnimation();
        }

        binding.gamePanel.score.setText(String.format(Locale.getDefault(), "Puntaje: %d", gameState.getScore()));
        binding.gamePanel.deckInfo.setText(String.format(Locale.getDefault(), "Mazo restante: %d", gameState.getDeckSize()));
        binding.gamePanel.captures.setText(String.format(Locale.getDefault(), "Capturas: %d", gameState.getCaptures().size()));

        binding.gamePanel.selection.setText(gameState.getSelectedDie() == null
                ? "Selecciona un dado de la reserva"
                : "Dado preparado: " + gameState.getSelectedDie().getLabel());

        updateSelectedDiePreview();
        renderDiceCollection(binding.gamePanel.reserveDiceContainer, gameState.getReserve(), true);
        renderDiceCollection(binding.gamePanel.lostDiceContainer, gameState.getLostDice(), false);

        binding.gamePanel.lost.setText(String.format(Locale.getDefault(), "Perdidos: %d", gameState.getLostDice().size()));
        String pendingGameOver = gameState.resolvePendingGameOverIfReady();
        if (pendingGameOver != null) {
            log = pendingGameOver;
        }
        binding.gamePanel.log.setText(log);
        renderCapturedCards();
        binding.getRoot().post(() -> {
            hideRefillSlots(refillSlots);
            runCaptureAnimationQueue(new ArrayList<>(captureAnimations),
                    () -> runReleaseAnimationQueue(new ArrayList<>(releaseAnimations),
                            () -> runRefillAnimationQueue(new ArrayList<>(refillSlots), null)
                    )
            );
        });

        recordNewCaptures();
        snapshotBoardState();
        List<Card> revealed = gameState.consumeRecentlyRevealedCards();
        if (revealed.isEmpty()) {
            if (afterReveals != null) {
                afterReveals.run();
            } else {
                // ✅ CLAVE: aunque nadie haya pasado afterReveals,
                // revisa prompts igual (pérdida de dado incluida).
                triggerPendingPrompts();
            }
            checkForFinalScoring(); // si no quieres game over, luego lo sacas, pero esto no afecta al prompt
            return;
        }
        showRevealedCardsSequential(new ArrayList<>(revealed), afterReveals);

    }

    private void setupDiceSelectionUi() {
        binding.diceSelectionPanel.diceSelectionZone.setOnDragListener(createDragListener());
        binding.diceSelectionPanel.diceWarehouseZone.setOnDragListener(createDragListener());
        createDiceTokens();
        binding.diceSelectionPanel.diceWarehouseZone.post(this::layoutDiceInWarehouse);
    }

    private void createDiceTokens() {
        if (!diceTokens.isEmpty()) {
            return;
        }
        List<DieType> availableTypes = Arrays.asList(DieType.D4, DieType.D6, DieType.D8, DieType.D12);
        for (DieType type : availableTypes) {
            for (int i = 0; i < 3; i++) {
                ImageView dieView = new ImageView(this);
                dieView.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(70), dpToPx(70)));
                dieView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                dieView.setTag(type);
                Bitmap preview = diceImageResolver.getTypePreview(type);
                dieView.setImageBitmap(preview);
                dieView.setOnTouchListener(this::handleDiceTouch);
                diceTokens.add(dieView);
                binding.diceSelectionPanel.diceWarehouseZone.addView(dieView);
            }
        }
        updateSelectionCounter();
    }

    private boolean handleDiceTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ClipData data = ClipData.newPlainText("die", ((DieType) view.getTag()).name());
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDragAndDrop(data, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    private View.OnDragListener createDragListener() {
        return (v, event) -> {
            View draggedView = (View) event.getLocalState();
            if (!(draggedView instanceof ImageView)) {
                return false;
            }
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    FrameLayout target = (FrameLayout) v;
                    boolean isSelectionZone = target == binding.diceSelectionPanel.diceSelectionZone;
                    boolean alreadyInTarget = draggedView.getParent() == target;
                    if (isSelectionZone && !alreadyInTarget && countDiceInContainer(target) >= 7) {
                        Toast.makeText(this, "La zona de selección solo admite 7 dados.", Toast.LENGTH_SHORT).show();
                        draggedView.setVisibility(View.VISIBLE);
                        return true;
                    }
                    moveDieToContainer((ImageView) draggedView, target, event.getX(), event.getY());
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        draggedView.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return true;
            }
        };
    }

    private void moveDieToContainer(ImageView dieView, FrameLayout container, float x, float y) {
        ViewGroup parent = (ViewGroup) dieView.getParent();
        if (parent != null) {
            parent.removeView(dieView);
        }
        container.addView(dieView);
        container.post(() -> {
            float targetX = x - dieView.getWidth() / 2f;
            float targetY = y - dieView.getHeight() / 2f;
            targetX = Math.max(0, Math.min(targetX, container.getWidth() - dieView.getWidth()));
            targetY = Math.max(0, Math.min(targetY, container.getHeight() - dieView.getHeight()));
            dieView.setX(targetX);
            dieView.setY(targetY);
            dieView.setVisibility(View.VISIBLE);
        });
        updateSelectionCounter();
    }

    private void layoutDiceInWarehouse() {
        int containerWidth = binding.diceSelectionPanel.diceWarehouseZone.getWidth();
        if (containerWidth == 0) {
            binding.diceSelectionPanel.diceWarehouseZone.post(this::layoutDiceInWarehouse);
            return;
        }

        int dieSize = dpToPx(70);
        int spacing = dpToPx(12);
        int perRow = Math.max(1, (containerWidth - spacing) / (dieSize + spacing));

        for (int i = 0; i < diceTokens.size(); i++) {
            ImageView die = diceTokens.get(i);
            float centerX = spacing + (i % perRow) * (dieSize + spacing) + dieSize / 2f;
            float centerY = spacing + (i / perRow) * (dieSize + spacing) + dieSize / 2f;
            moveDieToContainer(die, binding.diceSelectionPanel.diceWarehouseZone, centerX, centerY);
        }
    }

    private void resetDiceSelection() {
        binding.diceSelectionPanel.diceSelectionZone.post(this::layoutDiceInWarehouse);
    }

    private int countDiceInContainer(FrameLayout container) {
        int count = 0;
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i) instanceof ImageView) {
                count++;
            }
        }
        return count;
    }

    private List<DieType> extractSelectedDice() {
        if (diceTokens.isEmpty()) {
            return Collections.emptyList();
        }
        List<DieType> selected = new ArrayList<>();
        for (int i = 0; i < binding.diceSelectionPanel.diceSelectionZone.getChildCount(); i++) {
            View child = binding.diceSelectionPanel.diceSelectionZone.getChildAt(i);
            if (child instanceof ImageView) {
                selected.add((DieType) child.getTag());
            }
        }
        return selected;
    }

    private void updateSelectionCounter() {
        int selected = countDiceInContainer(binding.diceSelectionPanel.diceSelectionZone);
        binding.diceSelectionPanel.diceSelectionCounter.setText(String.format(Locale.getDefault(), "Dados seleccionados: %d/7", selected));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String buildReserveText() {
        int d4 = 0, d6 = 0, d8 = 0, d12 = 0;
        for (DieType t : gameState.getReserve()) {
            switch (t) {
                case D4: d4++; break;
                case D6: d6++; break;
                case D8: d8++; break;
                case D12: d12++; break;
            }
        }
        return String.format(Locale.getDefault(), "D4 x%d • D6 x%d • D8 x%d • D12 x%d", d4, d6, d8, d12);
    }

    @Override
    public void onSlotTapped(int position) {
        if (isRevealingCard) {
            Toast.makeText(this, "Toca la carta para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gameState.isAwaitingValueAdjustment()) {
            promptValueAdjustmentChoice();
            return;
        }
        if (gameState.isAwaitingBlueCrabDecision()) {
            promptBlueCrabDecision();
            return;
        }
        if (gameState.isAwaitingBlowfishDecision()) {
            promptBlowfishDecision();
            return;
        }
        if (gameState.isAwaitingGhostShrimpDecision()) {
            promptGhostShrimpDecision();
            return;
        }
        if (gameState.isAwaitingPulpoChoice()) {
            promptPulpoChoice();
            return;
        }
        if (gameState.isAwaitingSpiderCrabCardChoice()) {
            promptSpiderCrabCardChoice();
            return;
        }
        if (gameState.isAwaitingDieLoss()) {
            promptDieLossChoice();
            return;
        }

        if (gameState.isAwaitingBoardSelection()) {
            handleGameResult(gameState.handleBoardSelection(position));
        } else if (gameState.isAwaitingLanternChoice()) {
            handleGameResult(gameState.chooseLanternTarget(position));
        } else {
            animatePlacement(position);
        }
    }

    @Override
    public void onSlotLongPressed(int position) {
        if (isRevealingCard) {
            return;
        }
        BoardSlot slot = gameState.getBoard()[position];
        android.graphics.Bitmap image = cardImageResolver.getImageFor(slot.getCard(), slot.isFaceUp());
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        CardFullscreenDialog.show(this, image);
    }

    private void updateSelectedDiePreview() {
        if (gameState.getSelectedDie() == null) {
            binding.gamePanel.selectedDieImage.setVisibility(View.GONE);
            return;
        }

        Bitmap face = diceImageResolver.getFace(gameState.getSelectedDie());
        if (face != null) {
            binding.gamePanel.selectedDieImage.setVisibility(View.VISIBLE);
            binding.gamePanel.selectedDieImage.setImageBitmap(face);
        } else {
            binding.gamePanel.selectedDieImage.setVisibility(View.GONE);
        }
    }

    private void showRevealedCardsSequential(List<Card> revealed, Runnable onComplete) {
        if (revealed.isEmpty()) {
            finishRevealSequence(onComplete);
            return;
        }
        isRevealingCard = true;
        Card card = revealed.remove(0);
        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        CardFullscreenDialog.show(this, image, null, () -> showRevealedCardsSequential(revealed, onComplete));
    }

    private void finishRevealSequence(Runnable onComplete) {
        isRevealingCard = false;

        if (onComplete != null) {
            onComplete.run();
        } else {
            // ✅ Si nadie pasó onComplete, igual revisa prompts.
            triggerPendingPrompts();
        }

        checkForFinalScoring(); // si no quieres game over, luego lo quitas
    }


    private void checkForFinalScoring() {
        String pending = gameState.resolvePendingGameOverIfReady();
        if (pending != null) {
            binding.gamePanel.log.setText(pending);
        }
        if (!gameState.isGameOver() || endScoringShown) {
            return;
        }
        endScoringShown = true;
        showGameLayout();
        showCaptureScoringSequence();
    }

    private void showCaptureScoringSequence() {
        List<Card> captures = new ArrayList<>(gameState.getCaptures());
        int running = 0;
        List<Integer> cumulative = new ArrayList<>();
        for (Card card : captures) {
            running += card.getPoints();
            cumulative.add(running);
        }
        int finalScore = gameState.getScore();
        continueCaptureScoring(0, captures, cumulative, running, finalScore);
    }

    private void continueCaptureScoring(int index, List<Card> captures,
                                        List<Integer> cumulative, int baseTotal, int finalScore) {
        if (index >= captures.size()) {
            showBonusScoreDialog(baseTotal, finalScore);
            return;
        }
        Card card = captures.get(index);
        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        int cardPoints = card.getPoints();
        int cumulativeScore = cumulative.get(index);
        String overlay = (cardPoints >= 0 ? "+" : "") + cardPoints + " → " + cumulativeScore;
        CardFullscreenDialog.show(this, image, overlay,
                () -> continueCaptureScoring(index + 1, captures, cumulative, baseTotal, finalScore));
    }

    private void showBonusScoreDialog(int baseTotal, int finalScore) {
        int bonus = finalScore - baseTotal;
        persistFinalScore(finalScore);
        String overlay = bonus != 0
                ? "Bonos: " + (bonus > 0 ? "+" : "") + bonus + "\nTotal: " + finalScore
                : "Total final: " + finalScore;
        Bitmap image = cardImageResolver.getCardBack();
        CardFullscreenDialog.show(this, image, overlay, this::showStartMenu);
    }

    private void handleReserveDieTap(DieType type) {
        if (isRevealingCard) {
            Toast.makeText(this, "Toca la carta para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }
        startRollingAnimation(type);
        String msg = gameState.rollFromReserve(type);
        handleGameResult(msg);
    }

    private void renderDiceCollection(View container, Iterable<?> dice, boolean allowReserveTap) {
        if (!(container instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) container;
        group.removeAllViews();

        for (Object item : dice) {
            Bitmap bmp = null;
            String contentDescription = "";
            ImageView chip = new ImageView(this);
            chip.setLayoutParams(new ViewGroup.MarginLayoutParams(96, 96));
            ((ViewGroup.MarginLayoutParams) chip.getLayoutParams()).setMargins(4, 0, 4, 0);

            if (item instanceof DieType) {
                DieType type = (DieType) item;
                bmp = diceImageResolver.getTypePreview(type);
                contentDescription = type.getLabel();
                if (allowReserveTap) {
                    chip.setOnClickListener(v -> {
                        int[] loc = new int[2];
                        v.getLocationOnScreen(loc);

                        lastReserveTapCenterX = loc[0] + (v.getWidth() / 2f);
                        lastReserveTapCenterY = loc[1] + (v.getHeight() / 2f);
                        lastReserveTapAtMs = android.os.SystemClock.uptimeMillis();

                        handleReserveDieTap(type);
                    });

                }
            } else if (item instanceof com.daille.zonadepescajava_app.model.Die) {
                com.daille.zonadepescajava_app.model.Die die = (com.daille.zonadepescajava_app.model.Die) item;
                bmp = diceImageResolver.getFace(die);
                contentDescription = die.getLabel();
            }
            if (bmp != null) {
                chip.setImageBitmap(bmp);
            }
            chip.setContentDescription(contentDescription);
            group.addView(chip);
        }
    }

    private List<CaptureAnimationRequest> collectCaptureAnimations() {
        List<CaptureAnimationRequest> events = new ArrayList<>();
        BoardSlot[] board = gameState.getBoard();
        for (int i = 0; i < board.length; i++) {
            Card previous = lastBoardCards[i];
            Card current = board[i].getCard();
            if (previous != null && previous != current && gameState.getCaptures().contains(previous)) {
                events.add(new CaptureAnimationRequest(previous, i));
            }
        }
        return events;
    }

    private List<ReleaseAnimationRequest> collectReleaseAnimations() {
        List<ReleaseAnimationRequest> events = new ArrayList<>();

        // 1) ¿Qué carta estaba en capturas y ya no está? (salió de capturas)
        List<Card> currentCaptures = gameState.getCaptures();
        Card removedFromCaptures = null;
        for (Card c : lastCaptures) {
            if (!currentCaptures.contains(c)) {
                removedFromCaptures = c;
                break;
            }
        }
        if (removedFromCaptures == null) return events;

        // 2) ¿En qué slot del tablero apareció esa carta?
        BoardSlot[] board = gameState.getBoard();
        for (int i = 0; i < board.length; i++) {
            Card previous = lastBoardCards[i];
            Card current = board[i].getCard();

            if (current == removedFromCaptures) {
                // En ese slot, "previous" fue la carta que volvió al mazo
                if (previous != null) {
                    events.add(new ReleaseAnimationRequest(removedFromCaptures, previous, i));
                }
                break;
            }
        }
        return events;
    }


    private List<Integer> collectRefillSlots() {
        List<Integer> slots = new ArrayList<>();
        BoardSlot[] board = gameState.getBoard();

        for (int i = 0; i < board.length; i++) {
            Card previous = lastBoardCards[i];
            Card current = board[i].getCard();

            // Reposición real: entra una carta nueva que NO venía del tablero y NO venía de capturas
            boolean cameFromCaptures = lastCaptures.contains(current);

            if (current != null && current != previous && !wasCardOnBoard(current) && !cameFromCaptures) {
                slots.add(i);
            }
        }
        return slots;
    }

    private void runReleaseAnimationQueue(List<ReleaseAnimationRequest> events, Runnable onComplete) {
        if (events == null || events.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ReleaseAnimationRequest ev = events.remove(0);
        animateRelease(ev, () -> runReleaseAnimationQueue(events, onComplete));
    }
    private void animateRelease(ReleaseAnimationRequest ev, Runnable onComplete) {
        animateSlotToDeck(ev.slotIndex, ev.returnedToDeck, () ->
                animateCapturesToSlot(ev.slotIndex, ev.releasedFromCaptures, onComplete)
        );
    }
    private void animateSlotToDeck(int slotIndex, Card card, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        View deckView = binding.gamePanel.cardumenDeckImage;
        RecyclerView.LayoutManager lm = binding.gamePanel.boardRecycler.getLayoutManager();

        if (overlay == null || deckView == null || lm == null || card == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        View source = lm.findViewByPosition(slotIndex);
        if (source == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) image = cardImageResolver.getCardBack();

        int width = source.getWidth();
        int height = source.getHeight();
        if (width == 0 || height == 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ImageView floating = createFloatingCard(image, width, height);

        int[] overlayLocation = new int[2];
        overlay.getLocationOnScreen(overlayLocation);

        int[] sourceLocation = new int[2];
        source.getLocationOnScreen(sourceLocation);

        int[] deckLocation = new int[2];
        deckView.getLocationOnScreen(deckLocation);

        float startX = sourceLocation[0] - overlayLocation[0];
        float startY = sourceLocation[1] - overlayLocation[1];

        float endX = deckLocation[0] - overlayLocation[0] + deckView.getWidth() / 2f - width / 2f;
        float endY = deckLocation[1] - overlayLocation[1] + deckView.getHeight() / 2f - height / 2f;

        floating.setX(startX);
        floating.setY(startY);
        overlay.addView(floating);

        floating.animate()
                .x(endX)
                .y(endY)
                .setDuration(350)
                .withEndAction(() -> {
                    overlay.removeView(floating);
                    if (onComplete != null) onComplete.run();
                })
                .start();
    }
    private void animateCapturesToSlot(int slotIndex, Card card, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        View capturesView = binding.gamePanel.captureScroll; // origen aproximado
        RecyclerView.LayoutManager lm = binding.gamePanel.boardRecycler.getLayoutManager();

        if (overlay == null || capturesView == null || lm == null || card == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        View target = lm.findViewByPosition(slotIndex);
        if (target == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        float originalAlpha = target.getAlpha();
        target.setAlpha(0f);

        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) image = cardImageResolver.getCardBack();

        int width = target.getWidth();
        int height = target.getHeight();
        if (width == 0 || height == 0) {
            target.setAlpha(originalAlpha > 0f ? originalAlpha : 1f);
            if (onComplete != null) onComplete.run();
            return;
        }

        ImageView floating = createFloatingCard(image, width, height);

        int[] overlayLocation = new int[2];
        overlay.getLocationOnScreen(overlayLocation);

        int[] capturesLocation = new int[2];
        capturesView.getLocationOnScreen(capturesLocation);

        int[] targetLocation = new int[2];
        target.getLocationOnScreen(targetLocation);

        // start: desde la zona de capturas (similar a tu animateCardToCaptureZone pero al revés)
        float startX = capturesLocation[0] - overlayLocation[0] + dpToPx(8);
        float startY = capturesLocation[1] - overlayLocation[1] + dpToPx(4);

        float endX = targetLocation[0] - overlayLocation[0];
        float endY = targetLocation[1] - overlayLocation[1];

        floating.setX(startX);
        floating.setY(startY);
        overlay.addView(floating);

        floating.animate()
                .x(endX)
                .y(endY)
                .setDuration(400)
                .withEndAction(() -> {
                    overlay.removeView(floating);
                    target.setAlpha(originalAlpha > 0f ? originalAlpha : 1f);
                    if (onComplete != null) onComplete.run();
                })
                .start();
    }


    private boolean wasCardOnBoard(Card card) {
        if (card == null) return false;
        for (Card previous : lastBoardCards) {
            if (previous == card) return true;
        }
        return false;
    }

    private void snapshotBoardState() {
        BoardSlot[] board = gameState.getBoard();
        for (int i = 0; i < board.length; i++) {
            lastBoardCards[i] = board[i].getCard();
        }
        // NUEVO: snapshot de capturas
        lastCaptures.clear();
        lastCaptures.addAll(gameState.getCaptures());
    }

    private void recordNewCaptures() {
        List<Card> currentCaptures = gameState.getCaptures();
        for (Card card : currentCaptures) {
            if (!lastCaptures.contains(card)) {
                scoreDatabaseHelper.incrementCaptureCount(card.getId());
            }
        }
    }

    private void renderCapturedCards() {
        ViewGroup container = binding.gamePanel.captureCardsContainer;
        container.removeAllViews();

        // Espera a que la zona de capturas tenga tamaño real
        binding.gamePanel.captureScroll.post(() -> {
            int zoneH = binding.gamePanel.captureScroll.getHeight();
            if (zoneH <= 0) return;

            int margin = dpToPx(6);

            // Queremos que la carta use, por ejemplo, 80% del alto de la zona.
            float heightFactor = 0.80f;

            // Resta un poco por padding interno (lo tienes en el contenedor: paddingVertical=4dp)
            int innerPadding = dpToPx(8);
            int cardHeight = Math.round((zoneH - innerPadding) * heightFactor);

            // Mantén proporción 120x170 (tu proporción actual)
            int cardWidth = Math.round(cardHeight * (120f / 170f));

            for (Card card : gameState.getCaptures()) {
                Bitmap image = cardImageResolver.getImageFor(card, true);
                if (image == null) image = cardImageResolver.getCardBack();

                ImageView cardView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
                params.setMargins(margin, 0, margin, 0);
                cardView.setLayoutParams(params);

                // Elige cómo se ajusta la imagen dentro del rectángulo:
                // - CENTER_CROP: llena y puede recortar un poco
                // - FIT_CENTER: se ve completa (recomendado si no quieres recortes)
                cardView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                cardView.setImageBitmap(image);
                cardView.setContentDescription(card != null ? card.getName() : getString(R.string.card_image_content_description));

                // ✅ CLICK NORMAL = LIBERAR PEZ
                cardView.setOnClickListener(v -> {
                    // Si hay revelaciones/prompt activos, mejor bloquear para no romper flujos.
                    if (isRevealingCard) {
                        Toast.makeText(this, "Toca la carta para continuar.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Liberar pez")
                            .setMessage("¿Quieres liberar este pez?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                String msg = gameState.startReleaseFromCapture(card);
                                handleGameResult(msg); // refresca UI + toast + prompts
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

                // 👇 CLICK LARGO = CARTA EN GRANDE
                cardView.setOnLongClickListener(v -> {
                    Bitmap fullImage = cardImageResolver.getImageFor(card, true);
                    if (fullImage == null) {
                        fullImage = cardImageResolver.getCardBack();
                    }
                    CardFullscreenDialog.show(this, fullImage);
                    return true;
                });

                container.addView(cardView);
            }
        });
    }


    private void runCaptureAnimationQueue(List<CaptureAnimationRequest> queue, Runnable onComplete) {
        if (queue == null || queue.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        CaptureAnimationRequest next = queue.remove(0);
        View source = binding.gamePanel.boardRecycler.getLayoutManager() != null
                ? binding.gamePanel.boardRecycler.getLayoutManager().findViewByPosition(next.slotIndex)
                : null;
        if (source == null) {
            runCaptureAnimationQueue(queue, onComplete);
            return;
        }
        animateCardToCaptureZone(source, next.card, () -> runCaptureAnimationQueue(queue, onComplete));
    }

    private void animateCardToCaptureZone(View sourceView, Card card, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        if (overlay == null || card == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        int width = sourceView.getWidth();
        int height = sourceView.getHeight();
        if (width == 0 || height == 0) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        ImageView floating = createFloatingCard(image, width, height);
        int[] overlayLocation = new int[2];
        overlay.getLocationOnScreen(overlayLocation);
        int[] sourceLocation = new int[2];
        sourceView.getLocationOnScreen(sourceLocation);
        floating.setX(sourceLocation[0] - overlayLocation[0]);
        floating.setY(sourceLocation[1] - overlayLocation[1]);
        overlay.addView(floating);

        int[] targetLocation = new int[2];
        binding.gamePanel.captureScroll.getLocationOnScreen(targetLocation);
        float targetX = targetLocation[0] - overlayLocation[0] + dpToPx(8);
        float targetY = targetLocation[1] - overlayLocation[1] + dpToPx(4);

        floating.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(400)
                .withEndAction(() -> {
                    overlay.removeView(floating);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    private void runRefillAnimationQueue(List<Integer> slots, Runnable onComplete) {
        if (slots == null || slots.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Integer slotIndex = slots.remove(0);
        animateDeckToSlot(slotIndex, () -> runRefillAnimationQueue(slots, onComplete));
    }

    private void animateDeckToSlot(int slotIndex, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        View deckView = binding.gamePanel.cardumenDeckImage;
        if (overlay == null || deckView == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        View target = binding.gamePanel.boardRecycler.getLayoutManager() != null
                ? binding.gamePanel.boardRecycler.getLayoutManager().findViewByPosition(slotIndex)
                : null;
        if (target == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        float originalAlpha = target.getAlpha();
        target.setAlpha(0f);

        Bitmap image = cardImageResolver.getCardBack();
        int width = target.getWidth();
        int height = target.getHeight();
        if (width == 0 || height == 0) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        ImageView floating = createFloatingCard(image, width, height);
        int[] overlayLocation = new int[2];
        overlay.getLocationOnScreen(overlayLocation);
        int[] deckLocation = new int[2];
        deckView.getLocationOnScreen(deckLocation);
        int[] targetLocation = new int[2];
        target.getLocationOnScreen(targetLocation);

        float startX = deckLocation[0] - overlayLocation[0] + deckView.getWidth() / 2f - width / 2f;
        float startY = deckLocation[1] - overlayLocation[1] + deckView.getHeight() / 2f - height / 2f;
        float endX = targetLocation[0] - overlayLocation[0];
        float endY = targetLocation[1] - overlayLocation[1];

        floating.setX(startX);
        floating.setY(startY);
        overlay.addView(floating);

        floating.animate()
                .x(endX)
                .y(endY)
                .setDuration(350)
                .withEndAction(() -> {
                    overlay.removeView(floating);
                    target.setAlpha(originalAlpha > 0f ? originalAlpha : 1f);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    private void hideRefillSlots(List<Integer> slots) {
        if (slots == null || slots.isEmpty()) return;
        RecyclerView.LayoutManager layoutManager = binding.gamePanel.boardRecycler.getLayoutManager();
        if (layoutManager == null) return;
        for (Integer idx : slots) {
            if (idx == null) continue;
            View target = layoutManager.findViewByPosition(idx);
            if (target != null) {
                target.setAlpha(0f);
            }
        }
    }

    private ImageView createFloatingCard(Bitmap image, int width, int height) {
        ImageView floating = new ImageView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        floating.setLayoutParams(params);
        floating.setScaleType(ImageView.ScaleType.CENTER_CROP);
        floating.setImageBitmap(image);
        floating.setElevation(8f);
        return floating;
    }

    private void promptDieLossChoice() {
        if (!gameState.isAwaitingDieLoss()) return;

        final java.util.List<Die> options = gameState.getPendingDiceChoices();
        if (options == null || options.isEmpty()) {
            Toast.makeText(this, "ERROR: Falta lista de dados para perder (pendingDiceChoices vacío).", Toast.LENGTH_LONG).show();
            return;
        }

        // Adapter que muestra la CARA del dado (ej: D43.png, D87.png) usando DiceImageResolver
        android.widget.ListAdapter adapter = new android.widget.BaseAdapter() {
            @Override
            public int getCount() {
                return options.size();
            }

            @Override
            public Object getItem(int position) {
                return options.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                final int paddingH = dpToPx(14);
                final int paddingV = dpToPx(10);
                final int imgSize = dpToPx(56);

                android.widget.LinearLayout row;
                android.widget.ImageView img;
                android.widget.TextView fallbackText;

                if (convertView instanceof android.widget.LinearLayout) {
                    row = (android.widget.LinearLayout) convertView;
                    img = (android.widget.ImageView) row.getChildAt(0);
                    fallbackText = (android.widget.TextView) row.getChildAt(1);
                } else {
                    row = new android.widget.LinearLayout(MainActivity.this);
                    row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    row.setPadding(paddingH, paddingV, paddingH, paddingV);
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    img = new android.widget.ImageView(MainActivity.this);
                    android.widget.LinearLayout.LayoutParams imgParams =
                            new android.widget.LinearLayout.LayoutParams(imgSize, imgSize);
                    img.setLayoutParams(imgParams);
                    img.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);

                    // Fallback SOLO si no existe la imagen (por ejemplo, PNG faltante)
                    fallbackText = new android.widget.TextView(MainActivity.this);
                    android.widget.LinearLayout.LayoutParams textParams =
                            new android.widget.LinearLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                    textParams.leftMargin = dpToPx(12);
                    fallbackText.setLayoutParams(textParams);

                    row.addView(img);
                    row.addView(fallbackText);
                }

                Die die = options.get(position);
                android.graphics.Bitmap face = diceImageResolver.getFace(die);

                if (face != null) {
                    img.setImageBitmap(face);
                    // Pediste que NO se muestre como texto, así que lo ocultamos cuando hay imagen
                    fallbackText.setVisibility(android.view.View.GONE);
                } else {
                    // Si por algún motivo falta el asset, al menos que el usuario pueda elegir igual
                    img.setImageBitmap(null);
                    fallbackText.setVisibility(android.view.View.VISIBLE);
                    fallbackText.setText(die.getLabel());
                }

                return row;
            }
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Elige qué dado perder")
                .setAdapter(adapter, (dialog, which) -> {
                    String msg = gameState.chooseDieToLose(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }


    private void promptCancelAbility() {
        if (!gameState.isAwaitingCancelConfirmation()) return;

        new AlertDialog.Builder(this)
                .setTitle("Cancelar habilidad")
                .setMessage(gameState.getPendingCancelMessage() +
                        "\n¿Deseas cancelar la habilidad?")
                .setPositiveButton("Cancelar", (d, w) -> {
                    String msg = gameState.resolveCancelConfirmation(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Seguir intentando", (d, w) -> {
                    String msg = gameState.resolveCancelConfirmation(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptAtunDecision() {
        if (!gameState.isAwaitingAtunDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Habilidad del Atún")
                .setMessage("¿Quieres relanzar el dado recién lanzado?")
                .setPositiveButton("Relanzar", (dialog, which) -> {
                    String msg = gameState.chooseAtunReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialog, which) -> {
                    String msg = gameState.chooseAtunReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptBlueCrabDecision() {
        if (!gameState.isAwaitingBlueCrabDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Jaiba azul")
                .setMessage("¿Quieres activar la habilidad para ajustar un dado ±1?")
                .setPositiveButton("Usar", (dialog, which) -> {
                    String msg = gameState.chooseBlueCrabUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialog, which) -> {
                    String msg = gameState.chooseBlueCrabUse(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptBlowfishDecision() {
        if (!gameState.isAwaitingBlowfishDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Pez globo")
                .setMessage("¿Quieres inflar un dado al máximo?")
                .setPositiveButton("Usar", (dialog, which) -> {
                    String msg = gameState.chooseBlowfishUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialog, which) -> {
                    String msg = gameState.chooseBlowfishUse(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptMantisDecision() {
        if (!gameState.isAwaitingMantisDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Langostino mantis")
                .setMessage("¿Quieres relanzar un dado perdido?")
                .setPositiveButton("Usar", (dialog, which) -> {
                    String msg = gameState.chooseMantisReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialog, which) -> {
                    String msg = gameState.chooseMantisReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptMantisLostDieChoice() {
        if (!gameState.isAwaitingMantisLostDieChoice()) return;
        List<Die> options = new ArrayList<>(gameState.getLostDice());
        if (options.isEmpty()) {
            handleGameResult("Langostino mantis: no hay dados perdidos para relanzar.");
            return;
        }

        ArrayAdapter<Die> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout row;
                ImageView img;
                TextView fallbackText;

                if (convertView == null) {
                    row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    img = new ImageView(MainActivity.this);
                    LinearLayout.LayoutParams imgParams =
                            new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
                    img.setLayoutParams(imgParams);
                    img.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    fallbackText = new TextView(MainActivity.this);
                    LinearLayout.LayoutParams textParams =
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                    textParams.leftMargin = dpToPx(12);
                    fallbackText.setLayoutParams(textParams);

                    row.addView(img);
                    row.addView(fallbackText);
                } else {
                    row = (LinearLayout) convertView;
                    img = (ImageView) row.getChildAt(0);
                    fallbackText = (TextView) row.getChildAt(1);
                }

                Die die = options.get(position);
                Bitmap face = diceImageResolver.getFace(die);

                if (face != null) {
                    img.setImageBitmap(face);
                    fallbackText.setVisibility(View.GONE);
                } else {
                    img.setImageBitmap(null);
                    fallbackText.setVisibility(View.VISIBLE);
                    fallbackText.setText(die.getLabel());
                }

                return row;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Langostino mantis")
                .setAdapter(adapter, (dialog, which) -> {
                    String msg = gameState.chooseMantisLostDie(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptValueAdjustmentChoice() {
        if (!gameState.isAwaitingValueAdjustment()) return;
        int amount = gameState.getPendingAdjustmentAmount();
        String creature = gameState.getPendingAdjustmentSource() == CardId.NAUTILUS
                ? "Nautilus"
                : "Jaiba azul";
        new AlertDialog.Builder(this)
                .setTitle("Ajuste de " + creature)
                .setMessage("¿Quieres sumar o restar " + amount + " al dado seleccionado?")
                .setPositiveButton("Sumar", (dialog, which) -> {
                    String msg = gameState.chooseValueAdjustment(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Restar", (dialog, which) -> {
                    String msg = gameState.chooseValueAdjustment(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptGhostShrimpDecision() {
        if (!gameState.isAwaitingGhostShrimpDecision()) return;
        String seen = gameState.getGhostShrimpPeekNames();
        new AlertDialog.Builder(this)
                .setTitle("Camarón fantasma")
                .setMessage("Viste: " + seen + ". ¿Intercambiar sus posiciones?")
                .setPositiveButton("Intercambiar", (dialog, which) -> {
                    String msg = gameState.resolveGhostShrimpSwap(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialog, which) -> {
                    String msg = gameState.resolveGhostShrimpSwap(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptPulpoChoice() {
        if (!gameState.isAwaitingPulpoChoice()) return;
        List<String> names = gameState.getPendingPulpoNames();
        if (names.isEmpty()) return;
        CharSequence[] items = names.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("Pulpo")
                .setItems(items, (dialog, which) -> {
                    String msg = gameState.choosePulpoReplacement(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptPezVelaDecision() {
        if (!gameState.isAwaitingPezVelaDecision()) return;
        String current = gameState.getPezVelaOriginalDie() != null
                ? gameState.getPezVelaOriginalDie().getLabel()
                : "actual";
        new AlertDialog.Builder(this)
                .setTitle("Habilidad del Pez Vela")
                .setMessage("Resultado actual: " + current + ". ¿Relanzar?")
                .setPositiveButton("Relanzar", (dialog, which) -> {
                    String msg = gameState.choosePezVelaReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialog, which) -> {
                    String msg = gameState.choosePezVelaReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptPezVelaResultChoice() {
        if (!gameState.isAwaitingPezVelaResultChoice()) return;
        String previous = gameState.getPezVelaOriginalDie() != null
                ? gameState.getPezVelaOriginalDie().getLabel()
                : "previo";
        String rerolled = gameState.getPezVelaRerolledDie() != null
                ? gameState.getPezVelaRerolledDie().getLabel()
                : "nuevo";
        new AlertDialog.Builder(this)
                .setTitle("Habilidad del Pez Vela")
                .setMessage("Elige qué resultado conservar")
                .setPositiveButton("Nuevo (" + rerolled + ")", (dialog, which) -> {
                    String msg = gameState.choosePezVelaResult(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Anterior (" + previous + ")", (dialog, which) -> {
                    String msg = gameState.choosePezVelaResult(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptArenqueChoice() {
        if (!gameState.isAwaitingArenqueChoice()) return;
        List<String> names = gameState.getPendingArenqueNames();
        if (names.isEmpty()) return;
        boolean[] checked = new boolean[names.size()];
        CharSequence[] items = names.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("Elige hasta 2 peces pequeños")
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    List<Integer> selected = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) selected.add(i);
                    }
                    String msg = gameState.chooseArenqueFish(selected);
                    handleGameResult(msg);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    String msg = gameState.chooseArenqueFish(java.util.Collections.emptyList());
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void startRollingAnimation(DieType type) {
        if (animationHandler == null) return;

        // Cancela runnable anterior (si estaba en cola)
        if (rollingRunnable != null) {
            animationHandler.removeCallbacks(rollingRunnable);
        }

        // ✅ Retry solo 1 vez (para arreglar el primer lanzamiento cuando aún no hay medidas)
        final boolean[] retried = {false};

        rollingRunnable = new Runnable() {
            @Override
            public void run() {
                // Cancela animación anterior (si estaba corriendo)
                Object previous = binding.animationOverlay.getTag();
                if (previous instanceof android.animation.Animator) {
                    ((android.animation.Animator) previous).cancel();
                }

                final FrameLayout overlay = binding.animationOverlay;
                final View reserveView = binding.gamePanel.reserveDiceContainer;
                final ImageView target = binding.gamePanel.selectedDieImage;

                if (overlay == null || reserveView == null || target == null) {
                    updateSelectedDiePreview();
                    return;
                }

                // Limpia “dados voladores” anteriores
                for (int i = overlay.getChildCount() - 1; i >= 0; i--) {
                    View child = overlay.getChildAt(i);
                    Object tag = child.getTag();
                    if ("ROLLING_DIE_FLYING".equals(tag)) {
                        child.animate().cancel();
                        overlay.removeViewAt(i);
                    }
                }

                // ✅ Asegura que el target esté visible para que pueda medirse en el próximo layout pass
                target.setVisibility(View.VISIBLE);

                // ✅ Si aún no está medido, reintenta 1 vez en el próximo frame
                if (overlay.getWidth() == 0 || overlay.getHeight() == 0
                        || reserveView.getWidth() == 0 || reserveView.getHeight() == 0
                        || target.getWidth() == 0 || target.getHeight() == 0) {

                    if (!retried[0]) {
                        retried[0] = true;
                        overlay.post(this);
                        return;
                    }

                    // Fallback legacy (si aun así no hay medidas)
                    final Runnable legacy = new Runnable() {
                        int frames = 12;

                        @Override
                        public void run() {
                            if (frames-- <= 0) {
                                animationHandler.removeCallbacks(this);
                                updateSelectedDiePreview();
                                return;
                            }
                            Bitmap preview = diceImageResolver.randomFace(type);
                            if (preview != null) {
                                binding.gamePanel.selectedDieImage.setVisibility(View.VISIBLE);
                                binding.gamePanel.selectedDieImage.setImageBitmap(preview);
                            }
                            animationHandler.postDelayed(this, 70);
                        }
                    };
                    animationHandler.post(legacy);
                    return;
                }

                overlay.setVisibility(View.VISIBLE);

                // Tamaño del dado volador
                int size = target.getWidth() > 0 ? target.getWidth() : dpToPx(56);
                size = Math.max(size, dpToPx(48));

                // ✅ Context correcto dentro de Runnable
                final ImageView flying = new ImageView(MainActivity.this);
                flying.setTag("ROLLING_DIE_FLYING");
                flying.setScaleType(ImageView.ScaleType.FIT_CENTER);

                Bitmap first = diceImageResolver.randomFace(type);
                if (first != null) flying.setImageBitmap(first);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
                overlay.addView(flying, lp);

                // Coordenadas en pantalla -> overlay
                int[] overlayLoc = new int[2];
                int[] reserveLoc = new int[2];
                int[] targetLoc = new int[2];
                overlay.getLocationOnScreen(overlayLoc);
                reserveView.getLocationOnScreen(reserveLoc);
                target.getLocationOnScreen(targetLoc);

                float overlayX = overlayLoc[0];
                float overlayY = overlayLoc[1];

                // ✅ ORIGEN REAL: chip exacto tocado (guardado en onClick)
                float startX, startY;

// si el tap fue “reciente”, úsalo (evita usar coords viejas)
                boolean haveTap = !Float.isNaN(lastReserveTapCenterX)
                        && (android.os.SystemClock.uptimeMillis() - lastReserveTapAtMs) < 800;

                if (haveTap) {
                    startX = (lastReserveTapCenterX - overlayX) - (size / 2f);
                    startY = (lastReserveTapCenterY - overlayY) - (size / 2f);
                } else {
                    // fallback: centro de la reserva
                    startX = (reserveLoc[0] - overlayX) + (reserveView.getWidth() * 0.5f) - (size / 2f);
                    startY = (reserveLoc[1] - overlayY) + (reserveView.getHeight() * 0.5f) - (size / 2f);
                }


                // Destino: centro del preview
                float endX = (targetLoc[0] - overlayX) + (target.getWidth() / 2f) - (size / 2f);
                float endY = (targetLoc[1] - overlayY) + (target.getHeight() / 2f) - (size / 2f);

                // Control point para arco
                float cx = (startX + endX) / 2f + (dpToPx(18) * ((float) Math.random() * 2f - 1f));
                float cy = Math.min(startY, endY) - dpToPx(160);

                // Target se “materializa” al final
                target.setAlpha(0f);

                flying.setX(startX);
                flying.setY(startY);

                final long[] lastSwap = {0L};
                final long[] swapEveryMs = {22L};
                final float density = getResources().getDisplayMetrics().density;

                android.animation.ValueAnimator va = android.animation.ValueAnimator.ofFloat(0f, 1f);
                va.setDuration(780L);
                va.setInterpolator(new android.view.animation.DecelerateInterpolator(1.25f));

                va.addUpdateListener(anim -> {
                    float t = (float) anim.getAnimatedValue();
                    float u = 1f - t;

                    // Bézier (arco)
                    float x = (u * u * startX) + (2f * u * t * cx) + (t * t * endX);
                    float y = (u * u * startY) + (2f * u * t * cy) + (t * t * endY);

                    // Shake decae
                    float shake = (1f - t) * (2.0f * density);
                    x += (float) (Math.sin(t * 18.0 * Math.PI) * shake);
                    y += (float) (Math.cos(t * 14.0 * Math.PI) * shake);

                    flying.setX(x);
                    flying.setY(y);

                    // Spin 3D decae
                    float energy = 1f - t;
                    flying.setRotation((float) (t * 720f + Math.sin(t * 10.0 * Math.PI) * 120f * energy));
                    flying.setRotationX((float) (Math.cos(t * 8.0 * Math.PI) * 55f * energy));
                    flying.setRotationY((float) (Math.sin(t * 9.0 * Math.PI) * 55f * energy));

                    // Squash & stretch
                    float s = 0.92f + 0.16f * (float) Math.sin(t * Math.PI);
                    flying.setScaleX(s);
                    flying.setScaleY(s);

                    // Swap de caras: rápido al inicio, lento al final
                    long now = android.os.SystemClock.uptimeMillis();
                    if (now - lastSwap[0] >= swapEveryMs[0]) {
                        Bitmap face = diceImageResolver.randomFace(type);
                        if (face != null) flying.setImageBitmap(face);
                        lastSwap[0] = now;
                        swapEveryMs[0] = 22L + (long) (140L * (t * t));
                    }
                });

                va.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                        try { overlay.removeView(flying); } catch (Exception ignore) {}
                        overlay.setTag(null);
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        try { overlay.removeView(flying); } catch (Exception ignore) {}
                        overlay.setTag(null);

                        // Resultado real (ya calculado por rollFromReserve)
                        updateSelectedDiePreview();

                        // “Aterrizaje”
                        target.setAlpha(1f);
                        target.setScaleX(0.92f);
                        target.setScaleY(0.92f);
                        target.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(220L)
                                .setInterpolator(new android.view.animation.OvershootInterpolator(1.15f))
                                .start();
                    }
                });

                overlay.setTag(va);
                va.start();
            }
        };

        animationHandler.post(rollingRunnable);
    }


    private void handleGameResult(String message) {
        refreshUi(message, () -> {
            triggerPendingPrompts();
            checkForFinalScoring(); // ✅ ahora sí: después de decidir si hay prompts
        });
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private static class ReleaseAnimationRequest {
        final Card releasedFromCaptures; // la carta que salió de capturas y entró al tablero
        final Card returnedToDeck;       // la carta que salió del tablero al mazo
        final int slotIndex;             // el slot del tablero donde ocurrió el recambio

        ReleaseAnimationRequest(Card releasedFromCaptures, Card returnedToDeck, int slotIndex) {
            this.releasedFromCaptures = releasedFromCaptures;
            this.returnedToDeck = returnedToDeck;
            this.slotIndex = slotIndex;
        }
    }

    private void triggerPendingPrompts() {
        if (gameState.isAwaitingValueAdjustment()) { promptValueAdjustmentChoice(); return; }
        if (gameState.isAwaitingBlueCrabDecision()) { promptBlueCrabDecision(); return; }
        if (gameState.isAwaitingBlowfishDecision()) { promptBlowfishDecision(); return; }
        if (gameState.isAwaitingMantisDecision()) { promptMantisDecision(); return; }
        if (gameState.isAwaitingMantisLostDieChoice()) { promptMantisLostDieChoice(); return; }
        if (gameState.isAwaitingPezVelaDecision()) { promptPezVelaDecision(); return; }
        if (gameState.isAwaitingPezVelaResultChoice()) { promptPezVelaResultChoice(); return; }
        if (gameState.isAwaitingGhostShrimpDecision()) { promptGhostShrimpDecision(); return; }
        if (gameState.isAwaitingPulpoChoice()) { promptPulpoChoice(); return; }
        if (gameState.isAwaitingArenqueChoice()) { promptArenqueChoice(); return; }
        if (gameState.isAwaitingAtunDecision()) { promptAtunDecision(); return; }
        if (gameState.isAwaitingDieLoss()) { promptDieLossChoice(); return; }
        if (gameState.isAwaitingSpiderCrabCardChoice()) {promptSpiderCrabCardChoice();return;}
        if (gameState.isAwaitingCancelConfirmation()) { promptCancelAbility(); return;
        }

    }

private void promptSpiderCrabCardChoice() {
        List<String> names = new ArrayList<>(gameState.getFailedDiscardNames());
        names.removeIf(s -> s == null || s.trim().isEmpty());

        if (names.isEmpty()) {
            handleGameResult("No hay cartas descartadas por fallo para recuperar.");
            return;
        }

        CharSequence[] items = names.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Cangrejo araña")
                .setItems(items, (d, which) -> {
                    String msg = gameState.chooseSpiderCrabCard(which);
                    handleGameResult(msg);
                })
                .setNegativeButton("Cancelar", (d, which) -> {
                    String msg = gameState.cancelSpiderCrab();
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void animatePlacement(int position) {
        if (gameState.getSelectedDie() == null) {
            completePlacement(position);
            return;
        }

        BoardSlot slot = gameState.getBoard()[position];
        boolean shouldFlip = slot != null && !slot.isFaceUp(); // 👈 solo si está boca abajo

        if (!shouldFlip) {
            completePlacement(position); // no giro si ya estaba boca arriba
            return;
        }

        View cardView = binding.gamePanel.boardRecycler.getLayoutManager() != null
                ? binding.gamePanel.boardRecycler.getLayoutManager().findViewByPosition(position)
                : null;

        if (cardView == null) {
            completePlacement(position);
            return;
        }

        cardView.setHasTransientState(true);

        ObjectAnimator firstHalf = ObjectAnimator.ofFloat(cardView, View.ROTATION_Y, 0f, 90f);
        firstHalf.setDuration(150);

        ObjectAnimator secondHalf = ObjectAnimator.ofFloat(cardView, View.ROTATION_Y, 90f, 0f);
        secondHalf.setDuration(150);

        firstHalf.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardView.setRotationY(90f);
                completePlacement(position);
                cardView.post(secondHalf::start);
            }
        });

        secondHalf.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardView.setRotationY(0f);
                cardView.setHasTransientState(false);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                cardView.setRotationY(0f);
                cardView.setHasTransientState(false);
            }
        });

        firstHalf.start();
    }


    private void completePlacement(int position) {
        String result = gameState.placeSelectedDie(position);
        handleGameResult(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scoreDatabaseHelper != null) {
            scoreDatabaseHelper.close();
        }
    }
}
