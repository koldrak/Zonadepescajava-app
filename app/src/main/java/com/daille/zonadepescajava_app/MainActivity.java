package com.daille.zonadepescajava_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
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
import com.daille.zonadepescajava_app.model.CardType;
import com.daille.zonadepescajava_app.model.Die;
import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;
import com.daille.zonadepescajava_app.model.GameUtils;
import com.daille.zonadepescajava_app.ui.BoardLinksDecoration;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;
import com.daille.zonadepescajava_app.ui.CardFullscreenDialog;
import com.daille.zonadepescajava_app.ui.CardImageResolver;
import com.daille.zonadepescajava_app.ui.CardPackOpenDialog;
import com.daille.zonadepescajava_app.ui.CollectionCardAdapter;
import com.daille.zonadepescajava_app.ui.DeckSelectionAdapter;
import com.daille.zonadepescajava_app.ui.DiceImageResolver;
import com.daille.zonadepescajava_app.ui.TideParticlesView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.text.DateFormat;
import java.util.Date;
import java.io.InputStream;
import java.io.IOException;

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
    private DeckSelectionAdapter deckSelectionAdapter;
    private final List<ImageView> diceTokens = new ArrayList<>();
    private final Card[] lastBoardCards = new Card[9];
    private final List<List<Die>> lastBoardDice = new ArrayList<>();
    private BoardLinksDecoration boardLinksDecoration;
    private final List<Card> lastCaptures = new ArrayList<>();
    private final Map<DieType, Integer> lastReserveCounts = new EnumMap<>(DieType.class);
    private float lastReserveTapCenterX = Float.NaN;
    private float lastReserveTapCenterY = Float.NaN;
    private long lastReserveTapAtMs = 0L;
    private TideParticlesView tideParticlesView;
    private final Map<CardId, Integer> deckSelectionCounts = new EnumMap<>(CardId.class);
    private List<Card> selectedDeck = new ArrayList<>();
    private int deckSelectionPoints = 0;
    private final Map<String, Bitmap> packImageCache = new java.util.HashMap<>();

    private static final String PACK_RANDOM_ASSET = "sobresorpresa.png";
    private static final String PACK_CRUSTACEO_ASSET = "sobrecrustaceos.png";
    private static final String PACK_SMALL_FISH_ASSET = "sobrepecespequeños.png";
    private static final String PACK_BIG_FISH_ASSET = "sobrepecesgrandes.png";
    private static final String PACK_OBJECT_ASSET = "sobreobjetos.png";


    private static class CaptureAnimationRequest {
        private final Card card;
        private final int slotIndex;

        CaptureAnimationRequest(Card card, int slotIndex) {
            this.card = card;
            this.slotIndex = slotIndex;
        }
    }

    private static class ReturnDiceAnimationRequest {
        private final Die die;
        private final int slotIndex;

        ReturnDiceAnimationRequest(Die die, int slotIndex) {
            this.die = die;
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
        setupDeckSelectionPanel();
        setupDiceShopPanel();
        setupTideAnimationOverlay();
        setupCollectionsPanel();
        setupSettingsPanel();

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

    private void setupTideAnimationOverlay() {
        if (tideParticlesView != null) {
            return;
        }
        tideParticlesView = new TideParticlesView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        binding.gamePanel.fishingAnimationOverlay.addView(tideParticlesView, params);
    }



    private void setupMenuButtons() {
        binding.startMenu.startNewGame.setOnClickListener(v -> {
            showDiceSelectionPanel();
        });
        binding.startMenu.openDiceShop.setOnClickListener(v -> showDiceShopPanel());
        binding.diceSelectionPanel.openDeckSelection.setOnClickListener(v -> showDeckSelectionPanel());
        binding.diceSelectionPanel.confirmDiceSelection.setOnClickListener(v -> {
            List<DieType> startingReserve = extractSelectedDice();
            if (startingReserve.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos 1 dado para iniciar.", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
            if (selectedDeck == null || selectedDeck.size() < 9 || deckSelectionPoints < 150 || deckSelectionPoints > 200) {
                List<Card> autoDeck = GameUtils.buildRandomDeckSelection(
                        new java.util.Random(),
                        GameUtils.getSelectableCards(ownedCounts),
                        ownedCounts,
                        150,
                        200,
                        9
                );
                selectedDeck = autoDeck;
            }
            viewModel.startNewGame(startingReserve, ownedCounts, selectedDeck);
            gameState = viewModel.getGameState();
            endScoringShown = false;
            setupBoard();
            snapshotBoardState();
            showGameLayout();
            refreshUi("Juego iniciado. Lanza un dado y toca una carta.");
        });
        binding.startMenu.openSettings.setOnClickListener(v -> showSettingsPanel());
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

    private void setupDeckSelectionPanel() {
        deckSelectionAdapter = new DeckSelectionAdapter(this, this::updateDeckSelectionScore);
        binding.deckSelectionPanel.deckSelectionRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.deckSelectionPanel.deckSelectionRecycler.setAdapter(deckSelectionAdapter);
        binding.deckSelectionPanel.deckSelectionBack.setOnClickListener(v -> showDiceSelectionPanel());
        binding.deckSelectionPanel.deckSelectionConfirm.setOnClickListener(v -> {
            if (deckSelectionAdapter == null) {
                return;
            }
            List<Card> selected = deckSelectionAdapter.getSelectedDeck();
            int totalPoints = deckSelectionPoints;
            if (selected.size() < 9) {
                Toast.makeText(this, "Selecciona al menos 9 cartas para el mazo.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (totalPoints < 150 || totalPoints > 200) {
                Toast.makeText(this, "El puntaje del mazo debe estar entre 150 y 200.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDeck = new ArrayList<>(selected);
            showDiceSelectionPanel();
        });
    }

    private void setupDiceShopPanel() {
        binding.diceShopPanel.diceShopBack.setOnClickListener(v -> showStartMenu());
        binding.diceShopPanel.diceShopBuyD4.setOnClickListener(v -> attemptDicePurchase(DieType.D4, 200));
        binding.diceShopPanel.diceShopBuyD6.setOnClickListener(v -> attemptDicePurchase(DieType.D6, 400));
        binding.diceShopPanel.diceShopBuyD8.setOnClickListener(v -> attemptDicePurchase(DieType.D8, 600));
        binding.diceShopPanel.diceShopBuyD12.setOnClickListener(v -> attemptDicePurchase(DieType.D12, 1000));
        updateDiceShopDicePreviews();
        updateCardPackPreviews();
        binding.diceShopPanel.cardPackRandomBuy.setOnClickListener(v ->
                attemptCardPackPurchase(2000, null, PACK_RANDOM_ASSET));
        binding.diceShopPanel.cardPackCrustaceoBuy.setOnClickListener(v ->
                attemptCardPackPurchase(2500, CardType.CRUSTACEO, PACK_CRUSTACEO_ASSET));
        binding.diceShopPanel.cardPackSmallFishBuy.setOnClickListener(v ->
                attemptCardPackPurchase(2500, CardType.PEZ, PACK_SMALL_FISH_ASSET));
        binding.diceShopPanel.cardPackBigFishBuy.setOnClickListener(v ->
                attemptCardPackPurchase(2500, CardType.PEZ_GRANDE, PACK_BIG_FISH_ASSET));
        binding.diceShopPanel.cardPackObjectBuy.setOnClickListener(v ->
                attemptCardPackPurchase(2500, CardType.OBJETO, PACK_OBJECT_ASSET));
    }

    private void updateDiceShopDicePreviews() {
        binding.diceShopPanel.diceShopD4Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D4));
        binding.diceShopPanel.diceShopD6Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D6));
        binding.diceShopPanel.diceShopD8Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D8));
        binding.diceShopPanel.diceShopD12Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D12));
    }

    private void refreshCollections() {
        Map<CardId, Integer> counts = scoreDatabaseHelper.getCaptureCounts();
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        List<CollectionCardAdapter.CollectionEntry> entries = new ArrayList<>();
        for (Card card : GameUtils.createAllCards()) {
            int count = 0;
            if (counts.containsKey(card.getId())) {
                count = counts.get(card.getId());
            }
            entries.add(new CollectionCardAdapter.CollectionEntry(card, count));
        }
        collectionCardAdapter.submitList(entries, ownedCounts);
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
        selectedDeck = new ArrayList<>();
        deckSelectionPoints = 0;
        binding.startMenu.getRoot().setVisibility(View.VISIBLE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        refreshScoreRecords(); // ✅ asegura recarga al mostrar menú
    }

    private void showDeckSelectionPanel() {
        refreshDeckSelectionList();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.VISIBLE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showDiceSelectionPanel() {
        resetDiceSelection();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.VISIBLE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showDiceShopPanel() {
        refreshDiceShopUi();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.VISIBLE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showGameLayout() {
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.VISIBLE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
    }

    private void showCollectionsPanel() {
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.VISIBLE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        refreshCollections();
    }

    private void showSettingsPanel() {
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.VISIBLE);
    }

    private void refreshDeckSelectionList() {
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        List<Card> selectable = GameUtils.getSelectableCards(ownedCounts);
        deckSelectionCounts.clear();
        selectedDeck = new ArrayList<>();
        deckSelectionPoints = 0;
        if (deckSelectionAdapter != null) {
            deckSelectionAdapter.submitList(selectable);
            deckSelectionAdapter.setInventoryCounts(ownedCounts);
        }
        updateDeckSelectionScore();
    }

    private void updateDeckSelectionScore() {
        int totalPoints = 0;
        if (deckSelectionAdapter != null) {
            List<Card> selected = deckSelectionAdapter.getSelectedDeck();
            for (Card card : selected) {
                totalPoints += card.getPoints();
            }
            deckSelectionCounts.clear();
            deckSelectionCounts.putAll(deckSelectionAdapter.getSelectionCounts());
        }
        deckSelectionPoints = totalPoints;
        binding.deckSelectionPanel.deckSelectionScore.setText(
                getString(R.string.deck_selection_score_format, deckSelectionPoints));
    }

    private void refreshDiceShopUi() {
        int availablePoints = scoreDatabaseHelper.getAvailablePoints();
        binding.diceShopPanel.diceShopPoints.setText(
                getString(R.string.dice_shop_points_format, availablePoints));
        int ownedD4 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D4.name());
        int ownedD6 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D6.name());
        int ownedD8 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D8.name());
        int ownedD12 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D12.name());
        binding.diceShopPanel.diceShopD4Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD4));
        binding.diceShopPanel.diceShopD6Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD6));
        binding.diceShopPanel.diceShopD8Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD8));
        binding.diceShopPanel.diceShopD12Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD12));
    }

    private void attemptDicePurchase(DieType type, int cost) {
        int available = scoreDatabaseHelper.getAvailablePoints();
        if (available < cost) {
            Toast.makeText(this, "No tienes suficientes puntos para comprar este dado.", Toast.LENGTH_SHORT).show();
            return;
        }
        scoreDatabaseHelper.addPurchasedDice(type.name(), 1);
        scoreDatabaseHelper.addSpentPoints(cost);
        refreshDiceShopUi();
    }

    private void attemptCardPackPurchase(int cost, CardType filterType, String packAsset) {
        int available = scoreDatabaseHelper.getAvailablePoints();
        if (available < cost) {
            Toast.makeText(this, "No tienes suficientes puntos para comprar este paquete.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Card> pool = filterType == null
                ? GameUtils.createAllCards()
                : GameUtils.getCardsByType(filterType);
        if (pool.isEmpty()) {
            Toast.makeText(this, "No hay cartas disponibles en este paquete.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Card> awarded = new ArrayList<>();
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            Card card = pool.get(rng.nextInt(pool.size()));
            awarded.add(card);
            scoreDatabaseHelper.addCardCopies(card.getId(), 1);
        }
        scoreDatabaseHelper.addSpentPoints(cost);
        refreshDiceShopUi();
        showCardPackRewards(awarded, packAsset);
    }

    private void showCardPackRewards(List<Card> cards, String packAsset) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        Bitmap packImage = loadPackAsset(packAsset);
        CardPackOpenDialog.show(this, packImage, cards, cardImageResolver);
    }

    private Bitmap loadPackAsset(String assetName) {
        if (assetName == null) {
            return null;
        }
        if (packImageCache.containsKey(assetName)) {
            return packImageCache.get(assetName);
        }
        Bitmap bitmap = null;
        try (InputStream stream = getAssets().open("img/" + assetName)) {
            bitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException ignored) {
        }
        packImageCache.put(assetName, bitmap);
        return bitmap;
    }

    private void updateCardPackPreviews() {
        binding.diceShopPanel.cardPackRandomImage.setImageBitmap(loadPackAsset(PACK_RANDOM_ASSET));
        binding.diceShopPanel.cardPackCrustaceoImage.setImageBitmap(loadPackAsset(PACK_CRUSTACEO_ASSET));
        binding.diceShopPanel.cardPackSmallFishImage.setImageBitmap(loadPackAsset(PACK_SMALL_FISH_ASSET));
        binding.diceShopPanel.cardPackBigFishImage.setImageBitmap(loadPackAsset(PACK_BIG_FISH_ASSET));
        binding.diceShopPanel.cardPackObjectImage.setImageBitmap(loadPackAsset(PACK_OBJECT_ASSET));
    }

    private void setupSettingsPanel() {
        binding.settingsPanel.settingsBack.setOnClickListener(v -> showStartMenu());
        binding.settingsPanel.settingsResetData.setOnClickListener(v -> {
            scoreDatabaseHelper.resetAllData();
            viewModel.resetProgress();
            selectedDeck = new ArrayList<>();
            deckSelectionPoints = 0;
            refreshScoreRecords();
            showStartMenu();
            Toast.makeText(this, "Datos borrados.", Toast.LENGTH_SHORT).show();
        });
        binding.settingsPanel.settingsAddPoints.setOnClickListener(v -> {
            scoreDatabaseHelper.addBonusPoints(1000);
            Toast.makeText(this, "Se agregaron 1000 puntos.", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshUi(String log) {
        refreshUi(log, null);
    }

    private void refreshUi(String log, Runnable afterReveals) {
        List<CaptureAnimationRequest> captureAnimations = collectCaptureAnimations();
        List<ReleaseAnimationRequest> releaseAnimations = collectReleaseAnimations();
        List<Integer> refillSlots = collectRefillSlots();
        List<ReturnDiceAnimationRequest> returnDiceAnimations = collectReturnDiceAnimations();

        adapter.update(
                Arrays.asList(gameState.getBoard()),
                gameState.getHighlightSlots(),
                gameState.computeRemoraBorderSlots(),
                gameState.computeBotaViejaPenaltySlots(),
                gameState.computeAutoHundidoBonusSlots(),
                gameState.computeBettaRowSlots()
        );
        animateRerolledDiceSlots(gameState.consumeRecentlyRerolledSlots());


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
        triggerTideAnimationIfNeeded();
        renderCapturedCards();
        binding.getRoot().post(() -> {
            hideRefillSlots(refillSlots);
            runReturnDiceAnimationQueue(new ArrayList<>(returnDiceAnimations),
                    () -> runCaptureAnimationQueue(new ArrayList<>(captureAnimations),
                            () -> runReleaseAnimationQueue(new ArrayList<>(releaseAnimations),
                                    () -> runRefillAnimationQueue(new ArrayList<>(refillSlots), null)
                            )
                    ));
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

    private void runPendingCurrentsSequence(String baseMessage, Runnable onComplete) {
        if (tideParticlesView == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (gameState.hasPendingTurnResolutions()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        List<GameState.CurrentDirection> currents = gameState.getPendingCurrentDirections();
        if (currents.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        List<TideParticlesView.Direction> directions = new ArrayList<>();
        for (GameState.CurrentDirection current : currents) {
            switch (current) {
                case UP:
                    directions.add(TideParticlesView.Direction.UP);
                    break;
                case DOWN:
                    directions.add(TideParticlesView.Direction.DOWN);
                    break;
                case LEFT:
                    directions.add(TideParticlesView.Direction.LEFT);
                    break;
                case RIGHT:
                    directions.add(TideParticlesView.Direction.RIGHT);
                    break;
            }
        }
        binding.gamePanel.fishingAnimationOverlay.post(() -> tideParticlesView.playSequence(directions, () -> {
            String currentsLog = gameState.applyPendingCurrentAnimations();
            String combinedLog = combineLogs(baseMessage, currentsLog);
            refreshUi(combinedLog, onComplete);
        }));
    }

    private String combineLogs(String base, String extra) {
        if (extra == null || extra.isEmpty()) {
            return base == null ? "" : base;
        }
        if (base == null || base.isEmpty()) {
            return extra;
        }
        return base + " " + extra;
    }

    @Deprecated
    private void triggerTideAnimationIfNeeded() {
        // This method is kept for backward compatibility; tide sequencing now runs
        // after reveal dialogs via runPendingCurrentsSequence in handleGameResult.
    }

    private void setupDiceSelectionUi() {
        refreshDiceTokens();
        updateDiceGridColumns();
        binding.diceSelectionPanel.getRoot().post(this::updateDiceGridColumns);
        binding.diceSelectionPanel.diceSelectionZone.addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if ((right - left) != (oldRight - oldLeft)) {
                        updateDiceGridColumns();
                    }
                });
        binding.diceSelectionPanel.diceWarehouseZone.addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if ((right - left) != (oldRight - oldLeft)) {
                        updateDiceGridColumns();
                    }
                });
    }

    private void refreshDiceTokens() {
        diceTokens.clear();
        clearDiceTokensFromContainer(binding.diceSelectionPanel.diceSelectionGrid);
        clearDiceTokensFromContainer(binding.diceSelectionPanel.diceWarehouseGrid);
        Map<DieType, Integer> inventory = buildDiceInventory();
        for (Map.Entry<DieType, Integer> entry : inventory.entrySet()) {
            DieType type = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                ImageView dieView = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                int size = dpToPx(70);
                int spacing = dpToPx(8);
                params.width = size;
                params.height = size;
                params.setMargins(spacing, spacing, spacing, spacing);
                dieView.setLayoutParams(params);
                dieView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                dieView.setTag(type);
                Bitmap preview = diceImageResolver.getTypePreview(type);
                dieView.setImageBitmap(preview);
                dieView.setOnClickListener(this::handleDieClick);
                diceTokens.add(dieView);
                binding.diceSelectionPanel.diceWarehouseGrid.addView(dieView);
            }
        }
        updateSelectionCounter();
    }

    private void updateDiceGridColumns() {
        int columnCount = calculateDiceColumns();
        resetDiceGridLayoutParams(binding.diceSelectionPanel.diceSelectionGrid);
        resetDiceGridLayoutParams(binding.diceSelectionPanel.diceWarehouseGrid);
        binding.diceSelectionPanel.diceSelectionGrid.setColumnCount(columnCount);
        binding.diceSelectionPanel.diceWarehouseGrid.setColumnCount(columnCount);
    }

    private void resetDiceGridLayoutParams(GridLayout grid) {
        int size = dpToPx(70);
        int spacing = dpToPx(8);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (!(child instanceof ImageView)) {
                continue;
            }
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(spacing, spacing, spacing, spacing);
            child.setLayoutParams(params);
        }
    }

    private int calculateDiceColumns() {
        int availableWidth = binding.diceSelectionPanel.diceWarehouseZone.getWidth();
        int selectionWidth = binding.diceSelectionPanel.diceSelectionZone.getWidth();
        if (availableWidth == 0) {
            availableWidth = selectionWidth;
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceWarehouseGrid.getWidth();
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceSelectionGrid.getWidth();
        }
        if (availableWidth == 0) {
            return 4;
        }
        availableWidth = Math.max(0,
                availableWidth - binding.diceSelectionPanel.diceWarehouseZone.getPaddingLeft()
                        - binding.diceSelectionPanel.diceWarehouseZone.getPaddingRight());
        int dieSize = dpToPx(70);
        int spacing = dpToPx(8);
        int cellSize = dieSize + spacing * 2;
        return Math.max(2, availableWidth / cellSize);
    }

    private void clearDiceTokensFromContainer(ViewGroup container) {
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child instanceof ImageView) {
                container.removeViewAt(i);
            }
        }
    }

    private void handleDieClick(View view) {
        if (!(view instanceof ImageView)) {
            return;
        }
        ImageView dieView = (ImageView) view;
        ViewGroup parent = (ViewGroup) dieView.getParent();
        if (parent == null) {
            return;
        }
        boolean inSelection = parent == binding.diceSelectionPanel.diceSelectionGrid;
        if (!inSelection && countDiceInContainer(binding.diceSelectionPanel.diceSelectionGrid) >= 7) {
            Toast.makeText(this, "La zona de selección solo admite 7 dados.", Toast.LENGTH_SHORT).show();
            return;
        }
        ViewGroup target = inSelection
                ? binding.diceSelectionPanel.diceWarehouseGrid
                : binding.diceSelectionPanel.diceSelectionGrid;
        animateDieTransfer(dieView, parent, target);
    }

    private void animateDieTransfer(ImageView dieView, ViewGroup from, ViewGroup to) {
        ViewGroup root = binding.diceSelectionPanel.getRoot();
        int[] rootLoc = new int[2];
        int[] startLoc = new int[2];
        root.getLocationInWindow(rootLoc);
        dieView.getLocationInWindow(startLoc);
        float startX = startLoc[0] - rootLoc[0];
        float startY = startLoc[1] - rootLoc[1];

        ImageView ghost = new ImageView(this);
        ghost.setImageDrawable(dieView.getDrawable());
        ghost.setScaleType(dieView.getScaleType());
        ghost.measure(
                View.MeasureSpec.makeMeasureSpec(dieView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dieView.getHeight(), View.MeasureSpec.EXACTLY));
        ghost.layout((int) startX, (int) startY,
                (int) startX + dieView.getWidth(),
                (int) startY + dieView.getHeight());
        root.getOverlay().add(ghost);

        from.removeView(dieView);
        to.addView(dieView);
        dieView.setVisibility(View.INVISIBLE);
        updateSelectionCounter();

        root.post(() -> {
            int[] endLoc = new int[2];
            dieView.getLocationInWindow(endLoc);
            float endX = endLoc[0] - rootLoc[0];
            float endY = endLoc[1] - rootLoc[1];
            ghost.animate()
                    .x(endX)
                    .y(endY)
                    .setDuration(220)
                    .withEndAction(() -> {
                        root.getOverlay().remove(ghost);
                        dieView.setVisibility(View.VISIBLE);
                        updateSelectionCounter();
                    })
                    .start();
        });
    }

    private void resetDiceSelection() {
        refreshDiceTokens();
    }

    private int countDiceInContainer(ViewGroup container) {
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
        for (int i = 0; i < binding.diceSelectionPanel.diceSelectionGrid.getChildCount(); i++) {
            View child = binding.diceSelectionPanel.diceSelectionGrid.getChildAt(i);
            if (child instanceof ImageView) {
                selected.add((DieType) child.getTag());
            }
        }
        return selected;
    }

    private void updateSelectionCounter() {
        int selected = countDiceInContainer(binding.diceSelectionPanel.diceSelectionGrid);
        binding.diceSelectionPanel.diceSelectionCounter.setText(
                getString(R.string.dice_selection_counter_format, selected));
        binding.diceSelectionPanel.diceSelectionLabel.setVisibility(
                selected == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private Map<DieType, Integer> buildDiceInventory() {
        Map<DieType, Integer> inventory = new EnumMap<>(DieType.class);
        int purchasedD4 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D4.name());
        int purchasedD6 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D6.name());
        int purchasedD8 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D8.name());
        int purchasedD12 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D12.name());
        inventory.put(DieType.D4, purchasedD4);
        inventory.put(DieType.D6, 6 + purchasedD6);
        inventory.put(DieType.D8, purchasedD8);
        inventory.put(DieType.D12, purchasedD12);
        return inventory;
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
        if (gameState.isAwaitingPezLoboDecision()) {
            promptPezLoboDecision();
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
        if (gameState.isAwaitingDecoradorChoice()) {
            promptDecoradorChoice();
            return;
        }
        if (gameState.isAwaitingViolinistChoice()) {
            promptViolinistChoice();
            return;
        }
        if (gameState.isAwaitingHorseshoeValue()) {
            promptHorseshoeValue();
            return;
        }
        if (gameState.isAwaitingBoxerDecision()) {
            promptBoxerDecision();
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
        Die preview = gameState.getSelectedDie();
        if (preview == null) {
            preview = gameState.getMantisRerolledDie();
        }
        if (preview == null) {
            binding.gamePanel.selectedDieImage.setVisibility(View.GONE);
            return;
        }

        Bitmap face = diceImageResolver.getFace(preview);
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

    private List<ReturnDiceAnimationRequest> collectReturnDiceAnimations() {
        if (lastBoardDice.size() != gameState.getBoard().length || lastReserveCounts.isEmpty()) {
            return new ArrayList<>();
        }
        Map<DieType, Integer> reserveGained = buildReserveCounts();
        for (Map.Entry<DieType, Integer> entry : lastReserveCounts.entrySet()) {
            reserveGained.put(entry.getKey(),
                    reserveGained.getOrDefault(entry.getKey(), 0) - entry.getValue());
        }
        List<ReturnDiceAnimationRequest> events = new ArrayList<>();
        BoardSlot[] board = gameState.getBoard();
        for (int i = 0; i < board.length; i++) {
            List<Die> removed = computeRemovedDice(lastBoardDice.get(i), board[i].getDice());
            for (Die die : removed) {
                int remaining = reserveGained.getOrDefault(die.getType(), 0);
                if (remaining > 0) {
                    events.add(new ReturnDiceAnimationRequest(die, i));
                    reserveGained.put(die.getType(), remaining - 1);
                }
            }
        }
        return events;
    }

    private List<Die> computeRemovedDice(List<Die> before, List<Die> after) {
        List<Die> remaining = new ArrayList<>(before);
        for (Die die : after) {
            int idx = indexOfDie(remaining, die);
            if (idx >= 0) {
                remaining.remove(idx);
            }
        }
        return remaining;
    }

    private int indexOfDie(List<Die> list, Die die) {
        for (int i = 0; i < list.size(); i++) {
            Die candidate = list.get(i);
            if (candidate.getType() == die.getType() && candidate.getValue() == die.getValue()) {
                return i;
            }
        }
        return -1;
    }

    private Map<DieType, Integer> buildReserveCounts() {
        Map<DieType, Integer> counts = new EnumMap<>(DieType.class);
        for (DieType type : gameState.getReserve()) {
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }
        return counts;
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
        lastBoardDice.clear();
        for (BoardSlot slot : board) {
            lastBoardDice.add(new ArrayList<>(slot.getDice()));
        }
        // NUEVO: snapshot de capturas
        lastCaptures.clear();
        lastCaptures.addAll(gameState.getCaptures());
        lastReserveCounts.clear();
        lastReserveCounts.putAll(buildReserveCounts());
    }

    private void recordNewCaptures() {
        List<Card> currentCaptures = gameState.getCaptures();
        for (Card card : currentCaptures) {
            if (!lastCaptures.contains(card)) {
                int totalCaptures = scoreDatabaseHelper.incrementCaptureCount(card.getId());
                if (totalCaptures > 0 && totalCaptures % 3 == 0) {
                    scoreDatabaseHelper.addCardCopies(card.getId(), 1);
                }
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

    private void promptPezLoboDecision() {
        if (!gameState.isAwaitingPezLoboDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Pez Lobo")
                .setMessage("¿Quieres descartar una carta adyacente boca arriba?")
                .setPositiveButton("Usar", (dialog, which) -> {
                    String msg = gameState.choosePezLoboUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialog, which) -> {
                    String msg = gameState.choosePezLoboUse(false);
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

    private void promptBoxerDecision() {
        if (!gameState.isAwaitingBoxerDecision()) return;
        new AlertDialog.Builder(this)
                .setTitle("Cangrejo boxeador")
                .setMessage("¿Quieres mover otro dado adyacente?")
                .setPositiveButton("Mover", (dialog, which) -> {
                    String msg = gameState.chooseBoxerContinue(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialog, which) -> {
                    String msg = gameState.chooseBoxerContinue(false);
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
                    Die chosen = options.get(which);
                    startRollingAnimation(chosen.getType());
                    String msg = gameState.chooseMantisLostDie(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void promptValueAdjustmentChoice() {
        if (!gameState.isAwaitingValueAdjustment()) return;
        int amount = gameState.getPendingAdjustmentAmount();
        String creature;
        if (gameState.getPendingAdjustmentSource() == CardId.NAUTILUS) {
            creature = "Nautilus";
        } else if (gameState.getPendingAdjustmentSource() == CardId.LOCO) {
            creature = "Loco";
        } else {
            creature = "Jaiba azul";
        }
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

    private interface CardSelectionHandler {
        String onSelect(int index);
    }

    private interface CardMultiSelectionHandler {
        String onSelect(List<Integer> indices);
    }

    private interface CardCancelHandler {
        String onCancel();
    }

    private class CardGridAdapter extends BaseAdapter {
        private final List<Card> cards;
        private final Set<Integer> selected;
        private final boolean showSelection;

        CardGridAdapter(List<Card> cards, Set<Integer> selected, boolean showSelection) {
            this.cards = cards;
            this.selected = selected;
            this.showSelection = showSelection;
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Object getItem(int position) {
            return cards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_dialog_card, parent, false);
            }
            MaterialCardView container = view.findViewById(R.id.dialogCardContainer);
            ImageView imageView = view.findViewById(R.id.dialogCardImage);
            Card card = cards.get(position);
            Bitmap image = cardImageResolver.getImageFor(card, true);
            if (image == null) {
                image = cardImageResolver.getCardBack();
            }
            imageView.setImageBitmap(image);
            imageView.setContentDescription(card != null ? card.getName()
                    : getString(R.string.card_image_content_description));

            if (showSelection && selected != null) {
                int strokeWidth = selected.contains(position) ? dpToPx(3) : 0;
                container.setStrokeWidth(strokeWidth);
            } else {
                container.setStrokeWidth(0);
            }

            return view;
        }
    }

    private GridView createCardGridView(int count, BaseAdapter adapter) {
        GridView gridView = new GridView(this);
        int padding = dpToPx(12);
        gridView.setPadding(padding, padding, padding, padding);
        gridView.setHorizontalSpacing(dpToPx(10));
        gridView.setVerticalSpacing(dpToPx(10));
        gridView.setNumColumns(Math.min(3, Math.max(1, count)));
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setAdapter(adapter);
        return gridView;
    }

    private void showSingleCardChoiceDialog(String title, List<Card> cards,
                                            CardSelectionHandler onSelect,
                                            CardCancelHandler onCancel) {
        CardGridAdapter adapter = new CardGridAdapter(cards, null, false);
        GridView gridView = createCardGridView(cards.size(), adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(gridView)
                .setCancelable(false);

        if (onCancel != null) {
            builder.setNegativeButton("Cancelar", (dialog, which) -> {
                String msg = onCancel.onCancel();
                handleGameResult(msg);
            });
        }

        AlertDialog dialog = builder.create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = onSelect.onSelect(position);
            dialog.dismiss();
            handleGameResult(msg);
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Card card = cards.get(position);
            Bitmap fullImage = cardImageResolver.getImageFor(card, true);
            if (fullImage == null) {
                fullImage = cardImageResolver.getCardBack();
            }
            CardFullscreenDialog.show(this, fullImage);
            return true;
        });

        dialog.show();
    }

    private void showMultiCardChoiceDialog(String title, List<Card> cards, int maxSelections,
                                           CardMultiSelectionHandler onConfirm,
                                           CardCancelHandler onCancel) {
        Set<Integer> selected = new LinkedHashSet<>();
        CardGridAdapter adapter = new CardGridAdapter(cards, selected, true);
        GridView gridView = createCardGridView(cards.size(), adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(gridView)
                .setPositiveButton("Aceptar", (dialogInterface, which) -> {
                    List<Integer> indices = new ArrayList<>(selected);
                    Collections.sort(indices);
                    String msg = onConfirm.onSelect(indices);
                    handleGameResult(msg);
                })
                .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                    if (onCancel != null) {
                        String msg = onCancel.onCancel();
                        handleGameResult(msg);
                    }
                })
                .setCancelable(false)
                .create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (selected.contains(position)) {
                selected.remove(position);
            } else {
                if (selected.size() >= maxSelections) {
                    Toast.makeText(this,
                            "Solo puedes elegir hasta " + maxSelections + " cartas.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                selected.add(position);
            }
            adapter.notifyDataSetChanged();
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Card card = cards.get(position);
            Bitmap fullImage = cardImageResolver.getImageFor(card, true);
            if (fullImage == null) {
                fullImage = cardImageResolver.getCardBack();
            }
            CardFullscreenDialog.show(this, fullImage);
            return true;
        });

        dialog.show();
    }

    private void promptPulpoChoice() {
        if (!gameState.isAwaitingPulpoChoice()) return;
        List<Card> cards = gameState.getPendingPulpoCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Pulpo",
                cards,
                gameState::choosePulpoReplacement,
                null
        );
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
        List<Card> cards = gameState.getPendingArenqueCards();
        if (cards.isEmpty()) return;
        showMultiCardChoiceDialog(
                "Elige hasta 2 peces pequeños",
                cards,
                2,
                gameState::chooseArenqueFish,
                () -> gameState.chooseArenqueFish(java.util.Collections.emptyList())
        );
    }

    private void promptSepiaChoice() {
        if (!gameState.isAwaitingSepiaChoice()) return;
        List<Card> cards = gameState.getPendingSepiaCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Sepia",
                cards,
                gameState::chooseSepiaCapture,
                null
        );
    }

    private void promptDragnetReleaseChoice() {
        if (!gameState.isAwaitingDragnetReleaseChoice()) return;
        List<Card> cards = gameState.getPendingDragnetCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Red de arrastre",
                cards,
                gameState::chooseDragnetRelease,
                null
        );
    }

    private void promptHachaReleaseChoice() {
        if (!gameState.isAwaitingHachaReleaseChoice()) return;
        List<Card> cards = gameState.getPendingHachaReleaseCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Pez Hacha Abisal",
                cards,
                gameState::chooseHachaRelease,
                null
        );
    }

    private void promptDamiselasChoice() {
        if (!gameState.isAwaitingDamiselasChoice()) return;
        List<Card> cards = gameState.getPendingDamiselasCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Damiselas: ordena el mazo",
                cards,
                gameState::chooseDamiselasOrder,
                null
        );
    }

    private void promptPeregrinoChoice() {
        if (!gameState.isAwaitingPeregrinoChoice()) return;
        List<Card> cards = gameState.getPendingPeregrinoCards();
        if (cards.isEmpty()) return;
        String title = gameState.isAwaitingPeregrinoBottomChoice()
                ? "Tiburón Peregrino: carta al fondo"
                : "Tiburón Peregrino: carta arriba";
        showSingleCardChoiceDialog(
                title,
                cards,
                index -> gameState.isAwaitingPeregrinoBottomChoice()
                        ? gameState.choosePeregrinoBottom(index)
                        : gameState.choosePeregrinoTop(index),
                null
        );
    }

    private void promptHumpbackDirection() {
        if (!gameState.isAwaitingHumpbackDirection()) return;
        CharSequence[] items = new CharSequence[] {"Arriba", "Abajo", "Izquierda", "Derecha"};
        new AlertDialog.Builder(this)
                .setTitle("Ballena jorobada")
                .setItems(items, (dialog, which) -> {
                    String direction;
                    switch (which) {
                        case 0: direction = "UP"; break;
                        case 1: direction = "DOWN"; break;
                        case 2: direction = "LEFT"; break;
                        default: direction = "RIGHT"; break;
                    }
                    String msg = gameState.chooseHumpbackDirection(direction);
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
        refreshUi(message, () -> runPendingCurrentsSequence(message, () -> {
            triggerPendingPrompts();
            checkForFinalScoring(); // ✅ ahora sí: después de decidir si hay prompts
        }));
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
        if (gameState.isAwaitingPezLoboDecision()) { promptPezLoboDecision(); return; }
        if (gameState.isAwaitingMantisDecision()) { promptMantisDecision(); return; }
        if (gameState.isAwaitingMantisLostDieChoice()) { promptMantisLostDieChoice(); return; }
        if (gameState.isAwaitingPezVelaDecision()) { promptPezVelaDecision(); return; }
        if (gameState.isAwaitingPezVelaResultChoice()) { promptPezVelaResultChoice(); return; }
        if (gameState.isAwaitingGhostShrimpDecision()) { promptGhostShrimpDecision(); return; }
        if (gameState.isAwaitingPulpoChoice()) { promptPulpoChoice(); return; }
        if (gameState.isAwaitingArenqueChoice()) { promptArenqueChoice(); return; }
        if (gameState.isAwaitingDecoradorChoice()) { promptDecoradorChoice(); return; }
        if (gameState.isAwaitingViolinistChoice()) { promptViolinistChoice(); return; }
        if (gameState.isAwaitingHorseshoeValue()) { promptHorseshoeValue(); return; }
        if (gameState.isAwaitingBoxerDecision()) { promptBoxerDecision(); return; }
        if (gameState.isAwaitingSepiaChoice()) { promptSepiaChoice(); return; }
        if (gameState.isAwaitingDragnetReleaseChoice()) { promptDragnetReleaseChoice(); return; }
        if (gameState.isAwaitingHachaReleaseChoice()) { promptHachaReleaseChoice(); return; }
        if (gameState.isAwaitingDamiselasChoice()) { promptDamiselasChoice(); return; }
        if (gameState.isAwaitingPeregrinoChoice()) { promptPeregrinoChoice(); return; }
        if (gameState.isAwaitingHumpbackDirection()) { promptHumpbackDirection(); return; }
        if (gameState.isAwaitingAtunDecision()) { promptAtunDecision(); return; }
        if (gameState.isAwaitingDieLoss()) { promptDieLossChoice(); return; }
        if (gameState.isAwaitingSpiderCrabCardChoice()) {promptSpiderCrabCardChoice();return;}
        if (gameState.isAwaitingCancelConfirmation()) { promptCancelAbility(); return;
        }

    }

    private void animateRerolledDiceSlots(List<Integer> slots) {
        if (slots == null || slots.isEmpty() || animationHandler == null) return;
        binding.gamePanel.boardRecycler.post(() -> {
            RecyclerView.LayoutManager lm = binding.gamePanel.boardRecycler.getLayoutManager();
            if (lm == null) return;
            for (Integer idx : slots) {
                if (idx == null || idx < 0 || idx >= gameState.getBoard().length) continue;
                View slotView = lm.findViewByPosition(idx);
                BoardSlot slot = gameState.getBoard()[idx];
                if (slotView == null || slot.getDice().isEmpty()) continue;

                ImageView dieOne = slotView.findViewById(R.id.dieSlotOne);
                ImageView dieTwo = slotView.findViewById(R.id.dieSlotTwo);
                if (slot.getDice().size() > 0) {
                    animateDieReroll(dieOne, slot.getDice().get(0));
                }
                if (slot.getDice().size() > 1) {
                    animateDieReroll(dieTwo, slot.getDice().get(1));
                }
            }
        });
    }

    private void animateDieReroll(ImageView target, Die die) {
        if (target == null || die == null || animationHandler == null) return;
        final int[] frames = {10};
        final Runnable[] runner = new Runnable[1];
        runner[0] = () -> {
            if (frames[0]-- <= 0) {
                Bitmap finalFace = diceImageResolver.getFace(die.getType(), die.getValue());
                if (finalFace != null) target.setImageBitmap(finalFace);
                return;
            }
            Bitmap random = diceImageResolver.randomFace(die.getType());
            if (random != null) target.setImageBitmap(random);
            animationHandler.postDelayed(runner[0], 45L);
        };
        animationHandler.post(runner[0]);
    }

    private void promptSpiderCrabCardChoice() {
        List<Card> cards = new ArrayList<>(gameState.getFailedDiscardCards());
        if (cards.isEmpty()) {
            handleGameResult("No hay cartas descartadas por fallo para recuperar.");
            return;
        }
        showSingleCardChoiceDialog(
                "Cangrejo araña",
                cards,
                gameState::chooseSpiderCrabCard,
                gameState::cancelSpiderCrab
        );
    }

    private void promptDecoradorChoice() {
        List<Card> cards = new ArrayList<>(gameState.getPendingDecoradorCards());
        if (cards.isEmpty()) {
            handleGameResult("Cangrejo decorador: no hay objetos disponibles.");
            return;
        }
        showSingleCardChoiceDialog(
                "Cangrejo decorador",
                cards,
                gameState::chooseDecoradorCard,
                gameState::cancelDecoradorAbility
        );
    }

    private void promptViolinistChoice() {
        List<Card> cards = new ArrayList<>(gameState.getFailedDiscardCards());
        if (cards.isEmpty()) {
            handleGameResult("No hay cartas descartadas por fallo para capturar.");
            return;
        }
        showSingleCardChoiceDialog(
                "Cangrejo violinista",
                cards,
                gameState::chooseViolinistCard,
                gameState::cancelViolinistAbility
        );
    }

    private void promptHorseshoeValue() {
        int sides = gameState.getHorseshoeDieSides();
        DieType dieType = gameState.getHorseshoeDieType();
        if (sides <= 0 || dieType == null) {
            handleGameResult("Cangrejo herradura: no hay dado válido para ajustar.");
            return;
        }
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= sides; i++) {
            values.add(i);
        }

        GridView gridView = new GridView(this);
        int padding = dpToPx(12);
        gridView.setPadding(padding, padding, padding, padding);
        gridView.setHorizontalSpacing(dpToPx(12));
        gridView.setVerticalSpacing(dpToPx(12));
        gridView.setNumColumns(Math.min(4, sides));
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return values.size();
            }

            @Override
            public Object getItem(int position) {
                return values.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout cell;
                ImageView img;
                TextView fallback;

                if (convertView instanceof LinearLayout) {
                    cell = (LinearLayout) convertView;
                    img = (ImageView) cell.getChildAt(0);
                    fallback = (TextView) cell.getChildAt(1);
                } else {
                    cell = new LinearLayout(MainActivity.this);
                    cell.setOrientation(LinearLayout.VERTICAL);
                    cell.setGravity(Gravity.CENTER);
                    cell.setLayoutParams(new AbsListView.LayoutParams(dpToPx(64), dpToPx(64)));

                    img = new ImageView(MainActivity.this);
                    LinearLayout.LayoutParams imgParams =
                            new LinearLayout.LayoutParams(dpToPx(52), dpToPx(52));
                    img.setLayoutParams(imgParams);
                    img.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    fallback = new TextView(MainActivity.this);
                    fallback.setGravity(Gravity.CENTER);

                    cell.addView(img);
                    cell.addView(fallback);
                }

                int value = values.get(position);
                Bitmap face = diceImageResolver.getFace(dieType, value);
                if (face != null) {
                    img.setImageBitmap(face);
                    fallback.setVisibility(View.GONE);
                } else {
                    img.setImageBitmap(null);
                    fallback.setVisibility(View.VISIBLE);
                    fallback.setText(String.valueOf(value));
                }

                return cell;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cangrejo herradura")
                .setView(gridView)
                .setCancelable(false)
                .create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = gameState.chooseHorseshoeValue(values.get(position));
            dialog.dismiss();
            handleGameResult(msg);
        });

        dialog.show();
    }

    private void animatePlacement(int position) {
        if (gameState.getSelectedDie() == null) {
            completePlacement(position);
            return;
        }

        BoardSlot slot = gameState.getBoard()[position];
        boolean shouldFlip = slot != null && !slot.isFaceUp(); // 👈 solo si está boca abajo

        View cardView = binding.gamePanel.boardRecycler.getLayoutManager() != null
                ? binding.gamePanel.boardRecycler.getLayoutManager().findViewByPosition(position)
                : null;

        if (cardView == null) {
            completePlacement(position);
            return;
        }

        cardView.setHasTransientState(true);
        binding.gamePanel.selectedDieImage.setVisibility(View.INVISIBLE);
        animateDieToSlot(cardView, () -> {
            playPlacementRipple(cardView, null);
            if (shouldFlip) {
                cardView.postDelayed(() -> flipCardWithPlacement(position, cardView), 520L);
            } else {
                completePlacement(position);
                cardView.postDelayed(() -> cardView.setHasTransientState(false), 240L);
            }
        });
    }

    private void animateDieToSlot(View cardView, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        ImageView source = binding.gamePanel.selectedDieImage;
        Die selected = gameState.getSelectedDie();
        if (overlay == null || source == null || cardView == null || selected == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (overlay.getWidth() == 0 || overlay.getHeight() == 0) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Bitmap face = diceImageResolver.getFace(selected);
        if (face == null) {
            face = diceImageResolver.getTypePreview(selected.getType());
        }
        if (face == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        int size = source.getWidth() > 0 ? source.getWidth() : dpToPx(48);
        size = Math.max(size, dpToPx(44));

        ImageView flying = new ImageView(this);
        flying.setImageBitmap(face);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        overlay.addView(flying, params);

        int[] overlayLoc = new int[2];
        int[] sourceLoc = new int[2];
        int[] targetLoc = new int[2];
        overlay.getLocationOnScreen(overlayLoc);
        source.getLocationOnScreen(sourceLoc);
        cardView.getLocationOnScreen(targetLoc);

        float startX = sourceLoc[0] - overlayLoc[0] + (source.getWidth() / 2f) - (size / 2f);
        float startY = sourceLoc[1] - overlayLoc[1] + (source.getHeight() / 2f) - (size / 2f);
        float endX = targetLoc[0] - overlayLoc[0] + (cardView.getWidth() / 2f) - (size / 2f);
        float endY = targetLoc[1] - overlayLoc[1] + (cardView.getHeight() / 2f) - (size / 2f);

        flying.setX(startX);
        flying.setY(startY);
        floatingDieTravel(source, flying, endX, endY, onComplete);
    }

    private void floatingDieTravel(ImageView source, ImageView flying, float endX, float endY,
                                   Runnable onComplete) {
        source.setAlpha(0f);
        flying.animate()
                .x(endX)
                .y(endY)
                .rotationBy(120f)
                .setDuration(320L)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.2f))
                .withEndAction(() -> {
                    try {
                        ((ViewGroup) flying.getParent()).removeView(flying);
                    } catch (Exception ignore) {
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    private void playPlacementRipple(View cardView, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        if (overlay == null || cardView == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        int[] overlayLoc = new int[2];
        int[] cardLoc = new int[2];
        overlay.getLocationOnScreen(overlayLoc);
        cardView.getLocationOnScreen(cardLoc);

        float centerX = cardLoc[0] - overlayLoc[0] + (cardView.getWidth() / 2f);
        float centerY = cardLoc[1] - overlayLoc[1] + (cardView.getHeight() / 2f);

        int baseSize = dpToPx(32);
        int stroke = dpToPx(3);
        long[] delays = new long[] {0L, 300L, 600L};
        long duration = 1600L;
        int lastIndex = delays.length - 1;

        for (int i = 0; i < delays.length; i++) {
            int index = i;
            View ring = new View(this);
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(android.graphics.Color.TRANSPARENT);
            shape.setStroke(stroke, 0x66B3E5FC);
            ring.setBackground(shape);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(baseSize, baseSize);
            ring.setLayoutParams(params);
            ring.setX(centerX - baseSize / 2f);
            ring.setY(centerY - baseSize / 2f);
            ring.setScaleX(0.2f);
            ring.setScaleY(0.2f);
            ring.setAlpha(0.6f);
            overlay.addView(ring);

            ring.animate()
                    .scaleX(4.2f)
                    .scaleY(4.2f)
                    .alpha(0f)
                    .setStartDelay(delays[i])
                    .setDuration(duration)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.1f))
                    .withEndAction(() -> {
                        try {
                            overlay.removeView(ring);
                        } catch (Exception ignore) {
                        }
                        if (index == lastIndex && onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .start();
        }
    }

    private void flipCardWithPlacement(int position, View cardView) {
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

    private void runReturnDiceAnimationQueue(List<ReturnDiceAnimationRequest> queue, Runnable onComplete) {
        if (queue == null || queue.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        ReturnDiceAnimationRequest next = queue.remove(0);
        animateReturnDie(next, () -> runReturnDiceAnimationQueue(queue, onComplete));
    }

    private void animateReturnDie(ReturnDiceAnimationRequest request, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        View reserveView = binding.gamePanel.reserveDiceContainer;
        RecyclerView.LayoutManager lm = binding.gamePanel.boardRecycler.getLayoutManager();

        if (overlay == null || reserveView == null || lm == null || request == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        View source = lm.findViewByPosition(request.slotIndex);
        if (source == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Bitmap face = diceImageResolver.getFace(request.die);
        if (face == null) {
            face = diceImageResolver.getTypePreview(request.die.getType());
        }
        if (face == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        int size = dpToPx(40);
        ImageView floating = new ImageView(this);
        floating.setImageBitmap(face);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        overlay.addView(floating, params);

        int[] overlayLoc = new int[2];
        int[] sourceLoc = new int[2];
        int[] reserveLoc = new int[2];
        overlay.getLocationOnScreen(overlayLoc);
        source.getLocationOnScreen(sourceLoc);
        reserveView.getLocationOnScreen(reserveLoc);

        float startX = sourceLoc[0] - overlayLoc[0] + (source.getWidth() / 2f) - (size / 2f);
        float startY = sourceLoc[1] - overlayLoc[1] + (source.getHeight() / 2f) - (size / 2f);
        float endX = reserveLoc[0] - overlayLoc[0] + (reserveView.getWidth() / 2f) - (size / 2f);
        float endY = reserveLoc[1] - overlayLoc[1] + (reserveView.getHeight() / 2f) - (size / 2f);

        floating.setX(startX);
        floating.setY(startY);
        floating.animate()
                .x(endX)
                .y(endY)
                .rotationBy(180f)
                .setDuration(420L)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    try {
                        overlay.removeView(floating);
                    } catch (Exception ignore) {
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
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
