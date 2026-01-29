package com.daille.zonadepescajava_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.BaseAdapter;
import android.app.Dialog;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.data.AudioSettings;
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
import com.daille.zonadepescajava_app.model.ShopPrices;
import com.daille.zonadepescajava_app.ui.BoardLinksDecoration;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;
import com.daille.zonadepescajava_app.ui.AcquiredCardsAdapter;
import com.daille.zonadepescajava_app.ui.BiteTeethView;
import com.daille.zonadepescajava_app.ui.CardFullscreenDialog;
import com.daille.zonadepescajava_app.ui.CardImageResolver;
import com.daille.zonadepescajava_app.ui.CardPackOpenDialog;
import com.daille.zonadepescajava_app.ui.CollectionCardAdapter;
import com.daille.zonadepescajava_app.ui.DeckSelectionAdapter;
import com.daille.zonadepescajava_app.ui.DiceImageResolver;
import com.daille.zonadepescajava_app.ui.TideParticlesView;
import com.google.android.material.card.MaterialCardView;
import android.view.animation.DecelerateInterpolator;

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
import com.daille.zonadepescajava_app.data.RankingApiClient;

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
    private ArrayAdapter<String> rankingAdapter;
    private CollectionCardAdapter collectionCardAdapter;
    private DeckSelectionAdapter deckSelectionAdapter;
    private DeckSelectionAdapter cardSellAdapter;
    private final List<String> deckPresetNames = new ArrayList<>();
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
    private int cardSellPoints = 0;
    private CardType deckSelectionFilterType;
    private CardType collectionsFilterType;
    private final Map<String, Bitmap> packImageCache = new java.util.HashMap<>();
    private final List<Card> acquiredCopiesInMatch = new ArrayList<>();
    private MediaPlayer ambientPlayer;
    private int ambientResId = 0;
    private SoundPool soundPool;
    private int buttonSoundId;
    private int rollSoundId;
    private int splashSoundId;
    private int tideSoundId;
    private int captureSoundId;
    private int packOpenSoundId;
    private int whaleSoundId;
    private int orcaSoundId;
    private final Set<Integer> loadedSoundIds = new java.util.HashSet<>();
    private final Set<Integer> pendingSoundIds = new java.util.HashSet<>();
    private Vibrator vibrator;
    private float musicVolume = 1f;
    private float sfxVolume = 1f;
    private float buttonVolume = 0.25f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private boolean buttonEnabled = true;
    private List<CountryOption> rankingCountryOptions = Collections.emptyList();

    private static final String TUTORIAL_PREFS = "tutorial_preferences";
    private static final String TUTORIAL_DICE_DONE_KEY = "tutorial_dice_done";
    private static final String TUTORIAL_DECK_DONE_KEY = "tutorial_deck_done";
    private static final String TUTORIAL_GAME_LOOP_DONE_KEY = "tutorial_game_loop_done";
    private static final String TUTORIAL_RELEASE_DONE_KEY = "tutorial_release_done";
    private static final String TUTORIAL_TIDE_DONE_KEY = "tutorial_tide_done";
    private SharedPreferences tutorialPreferences;
    private TutorialType activeTutorial;
    private final List<TutorialStep> tutorialSteps = new ArrayList<>();
    private int tutorialStepIndex = -1;
    private final List<View> tutorialHighlightedViews = new ArrayList<>();
    private final List<ObjectAnimator> tutorialHighlightAnimators = new ArrayList<>();
    private boolean pendingTideTutorial;

    private static final String PACK_RANDOM_ASSET = "sobresorpresa.png";
    private static final String PACK_CRUSTACEO_ASSET = "sobrecrustaceos.png";
    private static final String PACK_SMALL_FISH_ASSET = "sobrepecespequeños.png";
    private static final String PACK_BIG_FISH_ASSET = "sobrepecesgrandes.png";
    private static final String PACK_OBJECT_ASSET = "sobreobjetos.png";
    private static final int DICE_SELECTION_COLUMNS = 4;
    private static final int MIN_DICE_CAPACITY = 6;
    private static final int MAX_DICE_CAPACITY = 10;
    private static final int MIN_DECK_CARDS = 30;
    private static final int CARD_SELL_MULTIPLIER = 6;
    private static final int MAX_DECK_CARDS = 40;

    private enum TutorialType {
        DICE_SELECTION,
        DECK_SELECTION,
        GAME_LOOP,
        CARD_RELEASE,
        TIDE
    }

    private static class TutorialStep {
        private final int titleResId;
        private final int messageResId;
        private final boolean showNextButton;
        private final int nextButtonTextResId;
        private final List<View> allowedViews;

        TutorialStep(int titleResId, int messageResId, View... allowedViews) {
            this.titleResId = titleResId;
            this.messageResId = messageResId;
            this.showNextButton = false;
            this.nextButtonTextResId = 0;
            this.allowedViews = Arrays.asList(allowedViews);
        }

        TutorialStep(int titleResId, int messageResId, int nextButtonTextResId, View... allowedViews) {
            this.titleResId = titleResId;
            this.messageResId = messageResId;
            this.showNextButton = true;
            this.nextButtonTextResId = nextButtonTextResId;
            this.allowedViews = Arrays.asList(allowedViews);
        }
    }


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
        tutorialPreferences = getSharedPreferences(TUTORIAL_PREFS, MODE_PRIVATE);
        setupScoreRecordsList();
        setupMenuButtons();
        setupDiceSelectionUi();
        setupDeckSelectionPanel();
        setupDiceShopPanel();
        setupCardSellPanel();
        setupTideAnimationOverlay();
        setupCollectionsPanel();
        setupSettingsPanel();
        setupRankingPanel();
        setupTutorialOverlay();
        setupAudio();
        setupHaptics();
        loadAudioSettings();

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

    @Override
    public void onBackPressed() {
        if (isPanelVisible(binding.deckSelectionPanel.getRoot())) {
            showDiceSelectionPanel();
            return;
        }
        if (isPanelVisible(binding.cardSellPanel.getRoot())) {
            showDiceShopPanel();
            return;
        }
        if (isPanelVisible(binding.diceSelectionPanel.getRoot())
                || isPanelVisible(binding.diceShopPanel.getRoot())
                || isPanelVisible(binding.collectionsPanel.getRoot())
                || isPanelVisible(binding.rankingPanel.getRoot())
                || isPanelVisible(binding.settingsPanel.getRoot())
                || isPanelVisible(binding.gamePanel.getRoot())) {
            showStartMenu();
            return;
        }
        super.onBackPressed();
    }

    private static boolean isPanelVisible(View panel) {
        return panel.getVisibility() == View.VISIBLE;
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
        setSoundButtonClickListener(binding.startMenu.startNewGame, this::showDiceSelectionPanel);
        setSoundButtonClickListener(binding.startMenu.openDiceShop, this::showDiceShopPanel);
        setButtonClickListener(binding.diceSelectionPanel.openDeckSelection, this::showDeckSelectionPanel);
        setButtonClickListener(binding.diceSelectionPanel.confirmDiceSelection, () -> {
            List<DieType> startingReserve = extractSelectedDice();
            if (startingReserve.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos 1 dado para iniciar.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (activeTutorial == TutorialType.DICE_SELECTION && tutorialStepIndex == 2) {
                advanceTutorialStep();
            }
            Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
            if (selectedDeck == null || selectedDeck.size() < MIN_DECK_CARDS || selectedDeck.size() > MAX_DECK_CARDS) {
                List<Card> autoDeck = GameUtils.buildRandomDeckSelection(
                        new java.util.Random(),
                        GameUtils.getSelectableCards(ownedCounts),
                        ownedCounts,
                        MIN_DECK_CARDS,
                        MAX_DECK_CARDS
                );
                selectedDeck = autoDeck;
            }
            viewModel.startNewGame(startingReserve, ownedCounts, selectedDeck);
            gameState = viewModel.getGameState();
            endScoringShown = false;
            acquiredCopiesInMatch.clear();
            setupBoard();
            snapshotBoardState();
            showGameLayout();
            refreshUi("Juego iniciado. Lanza un dado y toca una carta.");
            binding.gamePanel.getRoot().post(() -> maybeStartTutorial(TutorialType.GAME_LOOP));
        });
        setSoundButtonClickListener(binding.startMenu.openSettings, this::showSettingsPanel);
        setSoundButtonClickListener(binding.startMenu.openCollections, this::showCollectionsPanel);
        setSoundButtonClickListener(binding.startMenu.openRanking, this::showRankingPanel);
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
        binding.collectionsPanel.collectionsFilterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            collectionsFilterType = resolveCardFilterType(checkedId,
                    R.id.collectionsFilterAll,
                    R.id.collectionsFilterOrange,
                    R.id.collectionsFilterGreen,
                    R.id.collectionsFilterBlue,
                    R.id.collectionsFilterBlack);
            refreshCollections();
        });
        binding.collectionsPanel.collectionsFilterGroup.check(R.id.collectionsFilterAll);
        setButtonClickListener(binding.collectionsPanel.closeCollections, this::showStartMenu);
    }

    private void setupRankingPanel() {
        rankingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.rankingPanel.rankingList.setAdapter(rankingAdapter);
        binding.rankingPanel.rankingList.setEmptyView(binding.rankingPanel.rankingEmpty);
        setButtonClickListener(binding.rankingPanel.rankingBack, this::showStartMenu);
    }

    private void setupDeckSelectionPanel() {
        deckSelectionAdapter = new DeckSelectionAdapter(this, this::updateDeckSelectionScore);
        binding.deckSelectionPanel.deckSelectionRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.deckSelectionPanel.deckSelectionRecycler.setAdapter(deckSelectionAdapter);
        binding.deckSelectionPanel.deckSelectionFilterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            deckSelectionFilterType = resolveCardFilterType(checkedId,
                    R.id.deckSelectionFilterAll,
                    R.id.deckSelectionFilterOrange,
                    R.id.deckSelectionFilterGreen,
                    R.id.deckSelectionFilterBlue,
                    R.id.deckSelectionFilterBlack);
            applyDeckSelectionFilter();
        });
        binding.deckSelectionPanel.deckSelectionFilterGroup.check(R.id.deckSelectionFilterAll);
        setButtonClickListener(binding.deckSelectionPanel.deckSelectionBack, this::showDiceSelectionPanel);
        setButtonClickListener(binding.deckSelectionPanel.deckSelectionConfirm, () -> {
            if (deckSelectionAdapter == null) {
                return;
            }
            List<Card> selected = deckSelectionAdapter.getSelectedDeck();
            selectedDeck = new ArrayList<>(selected);
            if (selected.size() < MIN_DECK_CARDS || selected.size() > MAX_DECK_CARDS) {
                Toast.makeText(this, "Selecciona entre 30 y 40 cartas para el mazo.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDeck = new ArrayList<>(selected);
            showDiceSelectionPanel();
        });
        setButtonClickListener(binding.deckSelectionPanel.deckSelectionSave, this::showDeckSaveDialog);
        setButtonClickListener(binding.deckSelectionPanel.deckSelectionLoad,
                () -> showDeckPresetSelectionDialog(R.string.deck_selection_load_title, this::loadDeckPreset));
        setButtonClickListener(binding.deckSelectionPanel.deckSelectionDelete,
                () -> showDeckPresetSelectionDialog(R.string.deck_selection_delete_title, this::confirmDeleteDeckPreset));
    }

    private interface DeckPresetSelectionHandler {
        void onPresetSelected(String name);
    }

    private void showDeckSaveDialog() {
        if (deckSelectionAdapter == null) {
            return;
        }
        Map<CardId, Integer> selection = new EnumMap<>(CardId.class);
        selection.putAll(deckSelectionAdapter.getSelectionCounts());
        if (selection.isEmpty()) {
            Toast.makeText(this, "Selecciona cartas antes de guardar el mazo.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.deck_selection_name_hint);
        nameInput.setSingleLine(true);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.deck_selection_create_title)
                .setView(nameInput)
                .setPositiveButton(R.string.deck_selection_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "Ingresa un nombre para el mazo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<CardId, Integer> currentSelection = new EnumMap<>(CardId.class);
                currentSelection.putAll(deckSelectionAdapter.getSelectionCounts());
                if (currentSelection.isEmpty()) {
                    Toast.makeText(this, "Selecciona cartas antes de guardar el mazo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                scoreDatabaseHelper.saveDeckPreset(name, currentSelection);
                refreshDeckPresetList();
                Toast.makeText(this, "Mazo guardado.", Toast.LENGTH_SHORT).show();
                if (activeTutorial == TutorialType.DECK_SELECTION && tutorialStepIndex == 1) {
                    advanceTutorialStep();
                }
                dialog.dismiss();
            });
        });
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void showDeckPresetSelectionDialog(int titleResId, DeckPresetSelectionHandler handler) {
        refreshDeckPresetList();
        if (deckPresetNames.isEmpty()) {
            Toast.makeText(this, "No hay mazos guardados.", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence[] items = deckPresetNames.toArray(new CharSequence[0]);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setItems(items, (dlg, which) -> handler.onPresetSelected(deckPresetNames.get(which)))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void loadDeckPreset(String name) {
        Map<CardId, Integer> preset = scoreDatabaseHelper.getDeckPreset(name);
        if (preset.isEmpty()) {
            Toast.makeText(this, "No se encontró el mazo seleccionado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (deckSelectionAdapter != null) {
            deckSelectionAdapter.setSelectionCounts(preset);
        }
        Toast.makeText(this, "Mazo cargado.", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteDeckPreset(String name) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.deck_selection_delete_title)
                .setMessage("¿Eliminar el mazo guardado \"" + name + "\"?")
                .setPositiveButton(R.string.deck_selection_delete, (dlg, which) -> {
                    boolean removed = scoreDatabaseHelper.deleteDeckPreset(name);
                    refreshDeckPresetList();
                    if (removed) {
                        Toast.makeText(this, "Mazo eliminado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el mazo.", Toast.LENGTH_SHORT).show();
                    }
                    if (activeTutorial == TutorialType.DECK_SELECTION && tutorialStepIndex == 2) {
                        advanceTutorialStep();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void setupCardSellPanel() {
        cardSellAdapter = new DeckSelectionAdapter(this, this::updateCardSellTotals, true, CARD_SELL_MULTIPLIER);
        binding.cardSellPanel.cardSellRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.cardSellPanel.cardSellRecycler.setAdapter(cardSellAdapter);
        setButtonClickListener(binding.cardSellPanel.cardSellBack, this::showDiceShopPanel);
        setButtonClickListener(binding.cardSellPanel.cardSellConfirm, this::sellSelectedCards);
    }

    private void setupDiceShopPanel() {
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBack, this::showStartMenu);
        setSoundButtonClickListener(binding.diceShopPanel.diceShopSellCards, this::showCardSellPanel);
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD4,
                () -> attemptDicePurchase(DieType.D4, ShopPrices.D4_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD6,
                () -> attemptDicePurchase(DieType.D6, ShopPrices.D6_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD8,
                () -> attemptDicePurchase(DieType.D8, ShopPrices.D8_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD10,
                () -> attemptDicePurchase(DieType.D10, ShopPrices.D10_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD12,
                () -> attemptDicePurchase(DieType.D12, ShopPrices.D12_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceShopBuyD20,
                () -> attemptDicePurchase(DieType.D20, ShopPrices.D20_COST));
        setSoundButtonClickListener(binding.diceShopPanel.diceCapacityBuy, this::attemptDiceCapacityUpgrade);
        updateDiceShopDicePreviews();
        updateCardPackPreviews();
        setSoundButtonClickListener(binding.diceShopPanel.cardPackRandomBuy, () ->
                attemptCardPackPurchase(ShopPrices.PACK_RANDOM_COST, null, PACK_RANDOM_ASSET));
        setSoundButtonClickListener(binding.diceShopPanel.cardPackCrustaceoBuy, () ->
                attemptCardPackPurchase(
                        ShopPrices.PACK_CRUSTACEO_COST, CardType.CRUSTACEO, PACK_CRUSTACEO_ASSET));
        setSoundButtonClickListener(binding.diceShopPanel.cardPackSmallFishBuy, () ->
                attemptCardPackPurchase(
                        ShopPrices.PACK_SMALL_FISH_COST, CardType.PEZ, PACK_SMALL_FISH_ASSET));
        setSoundButtonClickListener(binding.diceShopPanel.cardPackBigFishBuy, () ->
                attemptCardPackPurchase(
                        ShopPrices.PACK_BIG_FISH_COST, CardType.PEZ_GRANDE, PACK_BIG_FISH_ASSET));
        setSoundButtonClickListener(binding.diceShopPanel.cardPackObjectBuy, () ->
                attemptCardPackPurchase(
                        ShopPrices.PACK_OBJECT_COST, CardType.OBJETO, PACK_OBJECT_ASSET));
    }

    private void updateDiceShopDicePreviews() {
        binding.diceShopPanel.diceShopD4Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D4));
        binding.diceShopPanel.diceShopD6Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D6));
        binding.diceShopPanel.diceShopD8Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D8));
        binding.diceShopPanel.diceShopD10Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D10));
        binding.diceShopPanel.diceShopD12Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D12));
        binding.diceShopPanel.diceShopD20Image.setImageBitmap(
                diceImageResolver.getTypePreview(DieType.D20));
    }

    private void refreshCollections() {
        Map<CardId, Integer> counts = scoreDatabaseHelper.getCaptureCounts();
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        List<CollectionCardAdapter.CollectionEntry> entries = new ArrayList<>();
        List<Card> cards = new ArrayList<>(GameUtils.createAllCards());
        cards.sort((first, second) -> {
            int byPoints = Integer.compare(second.getPoints(), first.getPoints());
            if (byPoints != 0) {
                return byPoints;
            }
            return first.getName().compareToIgnoreCase(second.getName());
        });
        for (Card card : cards) {
            if (collectionsFilterType != null && card.getType() != collectionsFilterType) {
                continue;
            }
            int count = 0;
            if (counts.containsKey(card.getId())) {
                count = counts.get(card.getId());
            }
            entries.add(new CollectionCardAdapter.CollectionEntry(card, count));
        }
        collectionCardAdapter.submitList(entries, ownedCounts);
    }

    private void refreshScoreRecords() {
        // ===== 1) TOP PERSONAL (local) =====
        List<ScoreRecord> records = scoreDatabaseHelper.getTopScores(5);
        List<String> labels = new ArrayList<>();

        labels.add("🏠 TOP PERSONAL");
        if (records == null || records.isEmpty()) {
            labels.add("— Sin registros todavía —");
        } else {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            for (int i = 0; i < records.size(); i++) {
                ScoreRecord r = records.get(i);
                String date = dateFormat.format(new Date(r.getCreatedAt()));
                labels.add(String.format(Locale.getDefault(),
                        "#%d • %d puntos (%s)", i + 1, r.getScore(), date));
            }
        }

        // ===== 2) Separador TOP GLOBAL =====
        labels.add(""); // espacio visual
        labels.add("🌐 TOP GLOBAL (online)");

        // Mensaje inicial (se reemplaza si hay internet y llega data)
        if (!RankingApiClient.hasInternet(this)) {
            labels.add("— Sin conexión —");
            scoreRecordsAdapter.clear();
            scoreRecordsAdapter.addAll(labels);
            scoreRecordsAdapter.notifyDataSetChanged();
            binding.startMenu.scoreRecordsList.post(() ->
                    setListViewHeightBasedOnChildren(binding.startMenu.scoreRecordsList)
            );
            return;
        } else {
            labels.add("Cargando...");
            scoreRecordsAdapter.clear();
            scoreRecordsAdapter.addAll(labels);
            scoreRecordsAdapter.notifyDataSetChanged();
            binding.startMenu.scoreRecordsList.post(() ->
                    setListViewHeightBasedOnChildren(binding.startMenu.scoreRecordsList)
            );
        }

        // ===== 3) Pedir TOP GLOBAL (10) =====
        RankingApiClient.fetchTopAsync(10, (top, err) -> {
            // Volvemos a construir la lista completa: local + global real
            List<String> merged = new ArrayList<>();

            // (A) Local otra vez (para mantenerlo estable)
            merged.add("🏠 TOP PERSONAL");
            if (records == null || records.isEmpty()) {
                merged.add("— Sin registros todavía —");
            } else {
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
                for (int i = 0; i < records.size(); i++) {
                    ScoreRecord r = records.get(i);
                    String date = dateFormat.format(new Date(r.getCreatedAt()));
                    merged.add(String.format(Locale.getDefault(),
                            "#%d • %d puntos (%s)", i + 1, r.getScore(), date));
                }
            }

            merged.add("");
            merged.add("🌐 TOP GLOBAL (online)");

            // (B) Global
            if (err != null || top == null || top.isEmpty()) {
                merged.add("— No disponible —");
            } else {
                for (int i = 0; i < top.size(); i++) {
                    RankingApiClient.RemoteScore r = top.get(i);

                    // r.fecha viene como "YYYY-MM-DD" desde tu Worker
                    merged.add(String.format(Locale.getDefault(),
                            "#%d • %s %s — %d (%s)",
                            i + 1,
                            r.nombre,
                            countryCodeToFlag(r.pais),
                            r.puntaje,
                            r.fecha
                    ));
                }
            }

            scoreRecordsAdapter.clear();
            scoreRecordsAdapter.addAll(merged);
            scoreRecordsAdapter.notifyDataSetChanged();
            binding.startMenu.scoreRecordsList.post(() ->
                    setListViewHeightBasedOnChildren(binding.startMenu.scoreRecordsList)
            );
        });



        scoreRecordsAdapter.clear();
        scoreRecordsAdapter.addAll(labels);
        scoreRecordsAdapter.notifyDataSetChanged();

        // ✅ CLAVE: ListView dentro de ScrollView => fijar altura
        binding.startMenu.scoreRecordsList.post(() ->
                setListViewHeightBasedOnChildren(binding.startMenu.scoreRecordsList)
        );
    }

    private void refreshRankingList() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.ranking_title) + " (TOP 900)");

        if (!RankingApiClient.hasInternet(this)) {
            labels.add(getString(R.string.ranking_no_connection));
            rankingAdapter.clear();
            rankingAdapter.addAll(labels);
            rankingAdapter.notifyDataSetChanged();
            return;
        }

        labels.add(getString(R.string.ranking_loading));
        rankingAdapter.clear();
        rankingAdapter.addAll(labels);
        rankingAdapter.notifyDataSetChanged();

        RankingApiClient.fetchTopAsync(900, (top, err) -> {
            List<String> merged = new ArrayList<>();
            merged.add(getString(R.string.ranking_title) + " (TOP 900)");

            if (err != null || top == null || top.isEmpty()) {
                merged.add(getString(R.string.ranking_unavailable));
            } else {
                for (int i = 0; i < top.size(); i++) {
                    RankingApiClient.RemoteScore r = top.get(i);
                    merged.add(String.format(Locale.getDefault(),
                            "#%d • %s %s — %d (%s)",
                            i + 1,
                            r.nombre,
                            countryCodeToFlag(r.pais),
                            r.puntaje,
                            r.fecha
                    ));
                }
            }

            rankingAdapter.clear();
            rankingAdapter.addAll(merged);
            rankingAdapter.notifyDataSetChanged();
        });
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


    private boolean persistFinalScore(int finalScore) {
        if (!viewModel.isFinalScoreRecorded()) {
            int previousBest = scoreDatabaseHelper.getHighestScore();
            scoreDatabaseHelper.saveScore(finalScore);

            // ✅ NUEVO: intenta subir al ranking online (solo si hay internet)
            submitScoreOnlineIfPossible(finalScore);

            viewModel.markFinalScoreRecorded();
            refreshScoreRecords();
            return finalScore > previousBest;
        }
        return false;
    }

    // ===== RANKING ONLINE =====
    private static final String PREF_RANKING = "ranking_prefs";
    private static final String KEY_PLAYER_NAME = "player_name";
    private static final String KEY_PLAYER_COUNTRY = "player_country";

    private void submitScoreOnlineIfPossible(int finalScore) {
        if (!RankingApiClient.hasInternet(this)) return;

        android.content.SharedPreferences sp = getSharedPreferences(PREF_RANKING, MODE_PRIVATE);
        String nombre = sp.getString(KEY_PLAYER_NAME, null);
        String pais = sp.getString(KEY_PLAYER_COUNTRY, "CL");

        if (nombre == null || nombre.trim().isEmpty()) {
            promptPlayerProfileThenSubmit(finalScore);
            return;
        }

        nombre = normalizeNombre(nombre);
        pais = normalizePais(pais);

        String fecha = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());

        RankingApiClient.submitScoreAsync(nombre, pais, finalScore, fecha, (ok, err) -> {
            // silencioso: si quieres, aquí puedes mostrar Toast cuando falle
            // if (!ok && err != null) { ... }
        });
    }

    private void promptPlayerProfileThenSubmit(int finalScore) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        android.widget.EditText etNombre = new android.widget.EditText(this);
        etNombre.setHint("Nombre (máx 7)");
        etNombre.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(7)});
        String storedName = getSharedPreferences(PREF_RANKING, MODE_PRIVATE)
                .getString(KEY_PLAYER_NAME, "");
        etNombre.setText(storedName == null ? "" : storedName);
        layout.addView(etNombre);

        List<CountryOption> countryOptions = buildCountryOptions();
        android.widget.Spinner spPais = new android.widget.Spinner(this);
        android.content.SharedPreferences sp = getSharedPreferences(PREF_RANKING, MODE_PRIVATE);
        String storedPais = sp.getString(KEY_PLAYER_COUNTRY, "CL");
        configureCountrySpinner(spPais, countryOptions, storedPais);
        layout.addView(spPais);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ranking online")
                .setMessage("Se usará para publicar tu puntaje (solo cuando tengas internet).")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nombre = normalizeNombre(etNombre.getText().toString());
                    int index = spPais.getSelectedItemPosition();
                    String selectedPais = countryOptions.get(index).code;
                    String pais = normalizePais(selectedPais);

                    getSharedPreferences(PREF_RANKING, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PLAYER_NAME, nombre)
                            .putString(KEY_PLAYER_COUNTRY, pais)
                            .apply();

                    // ahora sí, subimos
                    submitScoreOnlineIfPossible(finalScore);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static String normalizeNombre(String raw) {
        if (raw == null) return "PLAYER";
        String s = raw.trim().toUpperCase(java.util.Locale.ROOT);
        if (s.isEmpty()) s = "PLAYER";
        if (s.length() > 7) s = s.substring(0, 7);
        return s;
    }

    private static String normalizePais(String raw) {
        if (raw == null) return "CL";
        String s = raw.trim().toUpperCase(java.util.Locale.ROOT);
        if (s.length() != 2) s = "CL";
        return s;
    }

    private static String countryCodeToFlag(String raw) {
        String code = normalizePais(raw);
        if (code.length() != 2) return "🏳️";
        int first = Character.codePointAt(code, 0) - 'A' + 0x1F1E6;
        int second = Character.codePointAt(code, 1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    private static List<CountryOption> buildCountryOptions() {
        String[] codes = java.util.Locale.getISOCountries();
        List<CountryOption> options = new ArrayList<>();
        for (String code : codes) {
            java.util.Locale locale = new java.util.Locale("", code);
            String name = locale.getDisplayCountry(java.util.Locale.getDefault());
            if (name == null || name.trim().isEmpty()) {
                name = code;
            }
            options.add(new CountryOption(code, code + " - " + name));
        }
        options.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
        return options;
    }

    private void configureCountrySpinner(android.widget.Spinner spinner, List<CountryOption> countryOptions, String selectedCode) {
        List<String> labels = new ArrayList<>();
        for (CountryOption option : countryOptions) {
            labels.add(option.label);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int selection = indexForCountryCode(countryOptions, selectedCode);
        spinner.setSelection(selection);
    }

    private static int indexForCountryCode(List<CountryOption> options, String code) {
        if (code == null) return 0;
        String normalized = normalizePais(code);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).code.equalsIgnoreCase(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private static class CountryOption {
        private final String code;
        private final String label;

        private CountryOption(String code, String label) {
            this.code = code;
            this.label = label;
        }
    }


    private void showStartMenu() {
        cancelTutorialOverlay();
        resetDiceSelection();
        selectedDeck = new ArrayList<>();
        deckSelectionPoints = 0;
        cardSellPoints = 0;
        binding.startMenu.getRoot().setVisibility(View.VISIBLE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
        refreshScoreRecords(); // ✅ asegura recarga al mostrar menú
    }

    private void showDeckSelectionPanel() {
        cancelTutorialOverlay();
        refreshDeckSelectionList();
        refreshDeckPresetList();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.VISIBLE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
        binding.deckSelectionPanel.getRoot().post(() -> maybeStartTutorial(TutorialType.DECK_SELECTION));
    }

    private void showDiceSelectionPanel() {
        cancelTutorialOverlay();
        resetDiceSelection();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.VISIBLE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
        binding.diceSelectionPanel.getRoot().post(() -> maybeStartTutorial(TutorialType.DICE_SELECTION));
    }

    private void showDiceShopPanel() {
        cancelTutorialOverlay();
        refreshDiceShopUi();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.VISIBLE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("market");
    }

    private void showCardSellPanel() {
        cancelTutorialOverlay();
        refreshCardSellPanel();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.VISIBLE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("market");
    }

    private void showGameLayout() {
        cancelTutorialOverlay();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.VISIBLE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
    }

    private void showCollectionsPanel() {
        cancelTutorialOverlay();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.VISIBLE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
        refreshCollections();
    }

    private void showSettingsPanel() {
        cancelTutorialOverlay();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.VISIBLE);
        binding.rankingPanel.getRoot().setVisibility(View.GONE);
        updateAmbientMusic("ambientalplaya");
        updateTutorialSwitches();
        refreshRankingProfileSettings();
    }

    private void showRankingPanel() {
        cancelTutorialOverlay();
        binding.startMenu.getRoot().setVisibility(View.GONE);
        binding.deckSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceSelectionPanel.getRoot().setVisibility(View.GONE);
        binding.diceShopPanel.getRoot().setVisibility(View.GONE);
        binding.cardSellPanel.getRoot().setVisibility(View.GONE);
        binding.gamePanel.getRoot().setVisibility(View.GONE);
        binding.collectionsPanel.getRoot().setVisibility(View.GONE);
        binding.settingsPanel.getRoot().setVisibility(View.GONE);
        binding.rankingPanel.getRoot().setVisibility(View.VISIBLE);
        updateAmbientMusic("ambientalplaya");
        refreshRankingList();
    }

    private void refreshDeckSelectionList() {
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        List<Card> selectable = GameUtils.getSelectableCards(ownedCounts);
        selectable.sort((first, second) -> {
            int byPoints = Integer.compare(second.getPoints(), first.getPoints());
            if (byPoints != 0) {
                return byPoints;
            }
            return first.getName().compareToIgnoreCase(second.getName());
        });
        deckSelectionCounts.clear();
        selectedDeck = new ArrayList<>();
        deckSelectionPoints = 0;
        if (deckSelectionAdapter != null) {
            deckSelectionAdapter.submitList(selectable);
            deckSelectionAdapter.setInventoryCounts(ownedCounts);
            applyDeckSelectionFilter();
        }
        updateDeckSelectionScore();
    }

    private void applyDeckSelectionFilter() {
        if (deckSelectionAdapter != null) {
            deckSelectionAdapter.setFilter(deckSelectionFilterType);
        }
    }

    private CardType resolveCardFilterType(int checkedId, int allId, int orangeId,
                                           int greenId, int blueId, int blackId) {
        if (checkedId == orangeId) {
            return CardType.CRUSTACEO;
        }
        if (checkedId == greenId) {
            return CardType.PEZ_GRANDE;
        }
        if (checkedId == blueId) {
            return CardType.PEZ;
        }
        if (checkedId == blackId) {
            return CardType.OBJETO;
        }
        if (checkedId == allId) {
            return null;
        }
        return null;
    }

    private void refreshDeckPresetList() {
        List<String> presets = scoreDatabaseHelper.getDeckPresetNames();
        deckPresetNames.clear();
        deckPresetNames.addAll(presets);
    }

    private void updateDeckSelectionScore() {
        int totalPoints = 0;
        int totalCards = 0;
        if (deckSelectionAdapter != null) {
            List<Card> selected = deckSelectionAdapter.getSelectedDeck();
            for (Card card : selected) {
                totalPoints += card.getPoints();
            }
            totalCards = selected.size();
            deckSelectionCounts.clear();
            deckSelectionCounts.putAll(deckSelectionAdapter.getSelectionCounts());
        }
        deckSelectionPoints = totalPoints;
        binding.deckSelectionPanel.deckSelectionScore.setText(
                getString(R.string.deck_selection_score_format, deckSelectionPoints));
        binding.deckSelectionPanel.deckSelectionCount.setText(
                getString(R.string.deck_selection_count_format, totalCards));
        if (activeTutorial == TutorialType.DECK_SELECTION && tutorialStepIndex == 0 && totalCards > 0) {
            advanceTutorialStep();
        }
    }

    private void refreshCardSellPanel() {
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        List<Card> cards = new ArrayList<>();
        for (Card card : GameUtils.createAllCards()) {
            if (ownedCounts.getOrDefault(card.getId(), 0) > 0) {
                cards.add(card);
            }
        }
        cards.sort((first, second) -> {
            int byPoints = Integer.compare(second.getPoints(), first.getPoints());
            if (byPoints != 0) {
                return byPoints;
            }
            return first.getName().compareToIgnoreCase(second.getName());
        });
        cardSellPoints = 0;
        if (cardSellAdapter != null) {
            cardSellAdapter.submitList(cards);
            cardSellAdapter.setInventoryCounts(ownedCounts);
        }
        updateCardSellTotals();
    }

    private void updateCardSellTotals() {
        int totalPoints = 0;
        if (cardSellAdapter != null) {
            List<Card> selected = cardSellAdapter.getSelectedDeck();
            for (Card card : selected) {
                totalPoints += card.getPoints() * CARD_SELL_MULTIPLIER;
            }
        }
        cardSellPoints = totalPoints;
        binding.cardSellPanel.cardSellPoints.setText(
                getString(R.string.card_sell_points_format, cardSellPoints));
    }

    private void sellSelectedCards() {
        if (cardSellAdapter == null) {
            return;
        }
        Map<CardId, Integer> selections = cardSellAdapter.getSelectionCounts();
        if (selections == null || selections.isEmpty()) {
            Toast.makeText(this, "Selecciona cartas para vender.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<CardId, Integer> ownedCounts = scoreDatabaseHelper.getCardInventoryCounts();
        int totalOwned = 0;
        for (Integer count : ownedCounts.values()) {
            totalOwned += count == null ? 0 : count;
        }
        int totalSelected = 0;
        for (Integer count : selections.values()) {
            totalSelected += count == null ? 0 : count;
        }
        if (totalOwned - totalSelected < MIN_DECK_CARDS) {
            Toast.makeText(this, getString(R.string.card_sell_minimum_warning), Toast.LENGTH_SHORT).show();
            return;
        }
        Map<CardId, Integer> pointsMap = new EnumMap<>(CardId.class);
        for (Card card : GameUtils.createAllCards()) {
            pointsMap.put(card.getId(), card.getPoints());
        }
        int totalPoints = 0;
        for (Map.Entry<CardId, Integer> entry : selections.entrySet()) {
            CardId id = entry.getKey();
            int count = entry.getValue() == null ? 0 : entry.getValue();
            if (id == null || count <= 0) {
                continue;
            }
            int cardPoints = pointsMap.getOrDefault(id, 0);
            totalPoints += cardPoints * CARD_SELL_MULTIPLIER * count;
            scoreDatabaseHelper.removeCardCopies(id, count);
        }
        if (totalPoints > 0) {
            scoreDatabaseHelper.addBonusPoints(totalPoints);
            Toast.makeText(this, "Venta completada: +" + totalPoints + " puntos.", Toast.LENGTH_SHORT).show();
        }
        showDiceShopPanel();
    }

    private void refreshDiceShopUi() {
        int availablePoints = scoreDatabaseHelper.getAvailablePoints();
        binding.diceShopPanel.diceShopPoints.setText(
                getString(R.string.dice_shop_points_format, availablePoints));
        binding.diceShopPanel.diceShopD4Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D4_COST));
        binding.diceShopPanel.diceShopD6Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D6_COST));
        binding.diceShopPanel.diceShopD8Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D8_COST));
        binding.diceShopPanel.diceShopD10Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D10_COST));
        binding.diceShopPanel.diceShopD12Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D12_COST));
        binding.diceShopPanel.diceShopD20Price.setText(
                getString(R.string.dice_shop_price_format, ShopPrices.D20_COST));
        binding.diceShopPanel.cardPackRandomPrice.setText(
                getString(R.string.card_pack_price_format, ShopPrices.PACK_RANDOM_COST));
        binding.diceShopPanel.cardPackCrustaceoPrice.setText(
                getString(R.string.card_pack_price_format, ShopPrices.PACK_CRUSTACEO_COST));
        binding.diceShopPanel.cardPackSmallFishPrice.setText(
                getString(R.string.card_pack_price_format, ShopPrices.PACK_SMALL_FISH_COST));
        binding.diceShopPanel.cardPackBigFishPrice.setText(
                getString(R.string.card_pack_price_format, ShopPrices.PACK_BIG_FISH_COST));
        binding.diceShopPanel.cardPackObjectPrice.setText(
                getString(R.string.card_pack_price_format, ShopPrices.PACK_OBJECT_COST));
        int ownedD4 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D4.name());
        int ownedD6 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D6.name());
        int ownedD8 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D8.name());
        int ownedD10 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D10.name());
        int ownedD12 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D12.name());
        int ownedD20 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D20.name());
        binding.diceShopPanel.diceShopD4Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD4));
        binding.diceShopPanel.diceShopD6Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD6));
        binding.diceShopPanel.diceShopD8Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD8));
        binding.diceShopPanel.diceShopD10Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD10));
        binding.diceShopPanel.diceShopD12Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD12));
        binding.diceShopPanel.diceShopD20Owned.setText(
                getString(R.string.dice_shop_owned_format, ownedD20));
        updateDiceCapacityShopUi();
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

    private void attemptDiceCapacityUpgrade() {
        int currentCapacity = getDiceSelectionCapacity();
        if (currentCapacity >= MAX_DICE_CAPACITY) {
            Toast.makeText(this, getString(R.string.dice_capacity_maxed), Toast.LENGTH_SHORT).show();
            return;
        }
        int nextCapacity = currentCapacity + 1;
        int cost = ShopPrices.getDiceCapacityUpgradeCost(nextCapacity);
        int available = scoreDatabaseHelper.getAvailablePoints();
        if (available < cost) {
            Toast.makeText(this, "No tienes suficientes puntos para ampliar la capacidad.", Toast.LENGTH_SHORT).show();
            return;
        }
        scoreDatabaseHelper.setDiceCapacity(nextCapacity);
        scoreDatabaseHelper.addSpentPoints(cost);
        refreshDiceShopUi();
        updateDiceSelectionCapacityText();
    }

    private void updateDiceCapacityShopUi() {
        int currentCapacity = getDiceSelectionCapacity();
        binding.diceShopPanel.diceCapacityCurrent.setText(
                getString(R.string.dice_capacity_current_format, currentCapacity));
        if (currentCapacity >= MAX_DICE_CAPACITY) {
            binding.diceShopPanel.diceCapacityNext.setText(R.string.dice_capacity_maxed);
            binding.diceShopPanel.diceCapacityBuy.setEnabled(false);
            return;
        }
        int nextCapacity = currentCapacity + 1;
        int cost = ShopPrices.getDiceCapacityUpgradeCost(nextCapacity);
        binding.diceShopPanel.diceCapacityNext.setText(
                getString(R.string.dice_capacity_next_format, nextCapacity, cost));
        binding.diceShopPanel.diceCapacityBuy.setEnabled(true);
    }

    private void attemptCardPackPurchase(int cost, CardType filterType, String packAsset) {
        int available = scoreDatabaseHelper.getAvailablePoints();
        if (available < cost) {
            Toast.makeText(this, "No tienes suficientes puntos para comprar este paquete.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Card> awarded = drawPackRewards(filterType);
        if (awarded.isEmpty()) {
            Toast.makeText(this, "No hay cartas disponibles en este paquete.", Toast.LENGTH_SHORT).show();
            return;
        }
        scoreDatabaseHelper.addSpentPoints(cost);
        refreshDiceShopUi();
        showCardPackRewards(awarded, packAsset, this::showDiceShopPanel);
    }

    private void showCardPackRewards(List<Card> cards, String packAsset, Runnable onClose) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        playPackOpenSound();
        Bitmap packImage = loadPackAsset(packAsset);
        CardPackOpenDialog.show(this, packImage, cards, cardImageResolver, onClose);
    }

    private List<Card> drawPackRewards(CardType filterType) {
        List<Card> pool = filterType == null
                ? GameUtils.createAllCards()
                : GameUtils.getCardsByType(filterType);
        if (pool.isEmpty()) {
            return new ArrayList<>();
        }
        List<Card> awarded = new ArrayList<>();
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            Card card = GameUtils.drawWeightedByPoints(rng, pool);
            if (card != null) {
                awarded.add(card);
                scoreDatabaseHelper.addCardCopies(card.getId(), 1);
            }
        }
        return awarded;
    }

    private void awardRecordBreakPack() {
        Toast.makeText(this, getString(R.string.record_break_reward_message), Toast.LENGTH_LONG).show();
        int packIndex = new java.util.Random().nextInt(5);
        CardType filterType;
        String packAsset;
        switch (packIndex) {
            case 0:
                filterType = null;
                packAsset = PACK_RANDOM_ASSET;
                break;
            case 1:
                filterType = CardType.CRUSTACEO;
                packAsset = PACK_CRUSTACEO_ASSET;
                break;
            case 2:
                filterType = CardType.PEZ;
                packAsset = PACK_SMALL_FISH_ASSET;
                break;
            case 3:
                filterType = CardType.PEZ_GRANDE;
                packAsset = PACK_BIG_FISH_ASSET;
                break;
            default:
                filterType = CardType.OBJETO;
                packAsset = PACK_OBJECT_ASSET;
                break;
        }
        List<Card> awarded = drawPackRewards(filterType);
        if (awarded.isEmpty()) {
            showStartMenu();
            return;
        }
        showCardPackRewards(awarded, packAsset, this::showStartMenu);
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
        setButtonClickListener(binding.settingsPanel.settingsBack, this::showStartMenu);
        binding.settingsPanel.settingsMusicToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicEnabled = !isChecked;
            applyAudioSettings();
            saveAudioSettings();
        });
        binding.settingsPanel.settingsSfxToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sfxEnabled = !isChecked;
            saveAudioSettings();
        });
        binding.settingsPanel.settingsButtonToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonEnabled = !isChecked;
            saveAudioSettings();
        });
        binding.settingsPanel.settingsMusicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                musicVolume = Math.max(0f, Math.min(1f, progress / 100f));
                applyAudioSettings();
                saveAudioSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        binding.settingsPanel.settingsSfxVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sfxVolume = Math.max(0f, Math.min(1f, progress / 100f));
                saveAudioSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        binding.settingsPanel.settingsButtonVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                buttonVolume = Math.max(0f, Math.min(1f, progress / 100f));
                saveAudioSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setupRankingProfileSettings();
    }

    private void setupRankingProfileSettings() {
        rankingCountryOptions = buildCountryOptions();
        SharedPreferences sp = getSharedPreferences(PREF_RANKING, MODE_PRIVATE);
        String storedCountry = sp.getString(KEY_PLAYER_COUNTRY, "CL");
        configureCountrySpinner(binding.settingsPanel.settingsRankingCountrySpinner, rankingCountryOptions, storedCountry);
        binding.settingsPanel.settingsRankingSave.setOnClickListener(v -> saveRankingProfileSettings());
    }

    private void refreshRankingProfileSettings() {
        SharedPreferences sp = getSharedPreferences(PREF_RANKING, MODE_PRIVATE);
        String storedName = sp.getString(KEY_PLAYER_NAME, "");
        String storedCountry = sp.getString(KEY_PLAYER_COUNTRY, "CL");
        binding.settingsPanel.settingsRankingNameInput.setText(storedName == null ? "" : storedName);
        int selection = indexForCountryCode(rankingCountryOptions, storedCountry);
        binding.settingsPanel.settingsRankingCountrySpinner.setSelection(selection);
    }

    private void saveRankingProfileSettings() {
        String nombre = normalizeNombre(binding.settingsPanel.settingsRankingNameInput.getText().toString());
        int index = binding.settingsPanel.settingsRankingCountrySpinner.getSelectedItemPosition();
        if (index < 0 || index >= rankingCountryOptions.size()) {
            index = 0;
        }
        String selectedPais = rankingCountryOptions.get(index).code;
        String pais = normalizePais(selectedPais);
        getSharedPreferences(PREF_RANKING, MODE_PRIVATE)
                .edit()
                .putString(KEY_PLAYER_NAME, nombre)
                .putString(KEY_PLAYER_COUNTRY, pais)
                .apply();
        binding.settingsPanel.settingsRankingNameInput.setText(nombre);
    }

    private void setupTutorialOverlay() {
        binding.tutorialOverlay.getRoot().setVisibility(View.GONE);
        binding.tutorialOverlay.tutorialNextButton.setOnClickListener(v -> {
            if (activeTutorial == null) {
                return;
            }
            advanceTutorialStep();
        });
        binding.tutorialOverlay.getRoot().setOnTouchListener((view, event) -> {
            if (activeTutorial == null) {
                return false;
            }
            if (activeTutorial == TutorialType.TIDE && event.getAction() == MotionEvent.ACTION_UP) {
                advanceTutorialStep();
                return true;
            }
            View target = getActiveTutorialTouchTarget(event.getRawX(), event.getRawY());
            if (target == null) {
                return true;
            }
            int[] location = new int[2];
            target.getLocationOnScreen(location);
            float rawX = event.getRawX();
            float rawY = event.getRawY();
            boolean inside =
                    rawX >= location[0]
                            && rawX <= location[0] + target.getWidth()
                            && rawY >= location[1]
                            && rawY <= location[1] + target.getHeight();
            if (!inside) {
                return true;
            }
            MotionEvent transformed = MotionEvent.obtain(event);
            transformed.setLocation(rawX - location[0], rawY - location[1]);
            boolean handled = target.dispatchTouchEvent(transformed);
            transformed.recycle();
            return handled;
        });
    }

    private void maybeStartTutorial(TutorialType type) {
        if (!isTutorialCompleted(type)) {
            startTutorial(type);
        }
    }

    private void startTutorial(TutorialType type) {
        activeTutorial = type;
        tutorialSteps.clear();
        tutorialStepIndex = 0;
        if (type == TutorialType.DICE_SELECTION) {
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_dice_step1_title,
                    R.string.tutorial_dice_step1_message,
                    binding.diceSelectionPanel.diceWarehouseGrid,
                    binding.diceSelectionPanel.diceSelectionGrid));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_dice_step2_title,
                    R.string.tutorial_dice_step2_message,
                    binding.diceSelectionPanel.diceSelectionGrid));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_dice_step3_title,
                    R.string.tutorial_dice_step3_message,
                    binding.diceSelectionPanel.confirmDiceSelection,
                    binding.diceSelectionPanel.diceWarehouseGrid,
                    binding.diceSelectionPanel.diceSelectionGrid));
        } else if (type == TutorialType.DECK_SELECTION) {
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_deck_step1_title,
                    R.string.tutorial_deck_step1_message,
                    binding.deckSelectionPanel.deckSelectionRecycler));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_deck_step2_title,
                    R.string.tutorial_deck_step2_message,
                    binding.deckSelectionPanel.deckSelectionRecycler,
                    binding.deckSelectionPanel.deckSelectionSave));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_deck_step3_title,
                    R.string.tutorial_deck_step3_message,
                    binding.deckSelectionPanel.deckSelectionLoad,
                    binding.deckSelectionPanel.deckSelectionDelete));
        } else if (type == TutorialType.GAME_LOOP) {
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_game_loop_step1_title,
                    R.string.tutorial_game_loop_step1_message,
                    R.string.tutorial_accept,
                    binding.tutorialOverlay.tutorialNextButton));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_game_loop_step2_title,
                    R.string.tutorial_game_loop_step2_message,
                    binding.gamePanel.reserveDiceContainer));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_game_loop_step3_title,
                    R.string.tutorial_game_loop_step3_message,
                    binding.gamePanel.boardRecycler));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_game_loop_step4_title,
                    R.string.tutorial_game_loop_step4_message,
                    binding.gamePanel.boardRecycler));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_game_loop_step5_title,
                    R.string.tutorial_game_loop_step5_message,
                    binding.gamePanel.reserveDiceContainer,
                    binding.gamePanel.boardRecycler));
        } else if (type == TutorialType.CARD_RELEASE) {
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_release_step1_title,
                    R.string.tutorial_release_step1_message,
                    binding.gamePanel.captureScroll));
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_release_step2_title,
                    R.string.tutorial_release_step2_message,
                    binding.gamePanel.boardRecycler));
        } else if (type == TutorialType.TIDE) {
            tutorialSteps.add(new TutorialStep(
                    R.string.tutorial_tide_step1_title,
                    R.string.tutorial_tide_step1_message));
        }
        updateTutorialStep();
    }

    private void updateTutorialStep() {
        if (activeTutorial == null || tutorialStepIndex < 0 || tutorialStepIndex >= tutorialSteps.size()) {
            cancelTutorialOverlay();
            return;
        }
        TutorialStep step = tutorialSteps.get(tutorialStepIndex);
        binding.tutorialOverlay.tutorialStepLabel.setText(
                getString(R.string.tutorial_step_format, tutorialStepIndex + 1, tutorialSteps.size()));
        binding.tutorialOverlay.tutorialTitle.setText(step.titleResId);
        binding.tutorialOverlay.tutorialMessage.setText(step.messageResId);
        if (step.showNextButton) {
            binding.tutorialOverlay.tutorialNextButton.setVisibility(View.VISIBLE);
            binding.tutorialOverlay.tutorialNextButton.setEnabled(true);
            if (step.nextButtonTextResId != 0) {
                binding.tutorialOverlay.tutorialNextButton.setText(step.nextButtonTextResId);
            } else {
                binding.tutorialOverlay.tutorialNextButton.setText(R.string.tutorial_next);
            }
        } else {
            binding.tutorialOverlay.tutorialNextButton.setVisibility(View.GONE);
            binding.tutorialOverlay.tutorialNextButton.setEnabled(false);
        }
        binding.tutorialOverlay.getRoot().setVisibility(View.VISIBLE);
        resetTutorialCardPosition();
        binding.tutorialOverlay.getRoot().post(() -> {
            adjustTutorialCardPosition();
            highlightTutorialTargets();
        });
    }

    private void advanceTutorialStep() {
        if (activeTutorial == null) {
            return;
        }
        if (tutorialStepIndex < tutorialSteps.size() - 1) {
            tutorialStepIndex++;
            updateTutorialStep();
        } else {
            completeTutorial(activeTutorial);
        }
    }

    private void completeTutorial(TutorialType type) {
        setTutorialCompleted(type, true);
        cancelTutorialOverlay();
    }

    private void cancelTutorialOverlay() {
        activeTutorial = null;
        tutorialSteps.clear();
        tutorialStepIndex = -1;
        binding.tutorialOverlay.getRoot().setVisibility(View.GONE);
        clearTutorialHighlights();
        maybeStartPendingTideTutorial();
    }

    private boolean isTutorialCompleted(TutorialType type) {
        if (tutorialPreferences == null) {
            return false;
        }
        return tutorialPreferences.getBoolean(getTutorialPreferenceKey(type), false);
    }

    private void setTutorialCompleted(TutorialType type, boolean completed) {
        if (tutorialPreferences == null) {
            return;
        }
        tutorialPreferences.edit().putBoolean(getTutorialPreferenceKey(type), completed).apply();
    }

    private String getTutorialPreferenceKey(TutorialType type) {
        switch (type) {
            case DICE_SELECTION:
                return TUTORIAL_DICE_DONE_KEY;
            case DECK_SELECTION:
                return TUTORIAL_DECK_DONE_KEY;
            case GAME_LOOP:
                return TUTORIAL_GAME_LOOP_DONE_KEY;
            case CARD_RELEASE:
                return TUTORIAL_RELEASE_DONE_KEY;
            case TIDE:
                return TUTORIAL_TIDE_DONE_KEY;
            default:
                return TUTORIAL_DICE_DONE_KEY;
        }
    }

    private View getActiveTutorialTouchTarget(float rawX, float rawY) {
        List<View> allowedViews = getActiveTutorialAllowedViews();
        if (allowedViews.isEmpty()) {
            return null;
        }
        for (View view : allowedViews) {
            if (view == null || view.getVisibility() != View.VISIBLE) {
                continue;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            boolean inside =
                    rawX >= location[0]
                            && rawX <= location[0] + view.getWidth()
                            && rawY >= location[1]
                            && rawY <= location[1] + view.getHeight();
            if (inside) {
                return view;
            }
        }
        return null;
    }

    private List<View> getActiveTutorialAllowedViews() {
        if (activeTutorial == null || tutorialStepIndex < 0 || tutorialStepIndex >= tutorialSteps.size()) {
            return Collections.emptyList();
        }
        List<View> allowed = new ArrayList<>();
        TutorialStep step = tutorialSteps.get(tutorialStepIndex);
        if (step.allowedViews != null) {
            allowed.addAll(step.allowedViews);
        }
        allowed.add(binding.tutorialOverlay.tutorialCard);
        return allowed;
    }

    private void highlightTutorialTargets() {
        clearTutorialHighlights();
        if (activeTutorial == null || tutorialStepIndex < 0 || tutorialStepIndex >= tutorialSteps.size()) {
            return;
        }
        TutorialStep step = tutorialSteps.get(tutorialStepIndex);
        if (step.allowedViews == null) {
            return;
        }
        for (View view : step.allowedViews) {
            if (view == null) {
                continue;
            }
            view.setScaleX(1f);
            view.setScaleY(1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.03f);
            scaleX.setDuration(600);
            scaleX.setRepeatCount(ObjectAnimator.INFINITE);
            scaleX.setRepeatMode(ObjectAnimator.REVERSE);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.03f);
            scaleY.setDuration(600);
            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
            scaleY.setRepeatMode(ObjectAnimator.REVERSE);
            scaleX.start();
            scaleY.start();
            tutorialHighlightAnimators.add(scaleX);
            tutorialHighlightAnimators.add(scaleY);
            tutorialHighlightedViews.add(view);
        }
        binding.tutorialOverlay.getRoot().post(this::updateTutorialScrim);
    }

    private void resetTutorialCardPosition() {
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) binding.tutorialOverlay.tutorialCard.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        int margin = dpToPx(20);
        params.topMargin = margin;
        params.bottomMargin = margin;
        binding.tutorialOverlay.tutorialCard.setLayoutParams(params);
    }

    private void adjustTutorialCardPosition() {
        if (activeTutorial == null) {
            return;
        }
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) binding.tutorialOverlay.tutorialCard.getLayoutParams();
        android.graphics.Rect cardRect = getViewRectOnScreen(binding.tutorialOverlay.tutorialCard);
        boolean overlaps = false;
        TutorialStep step = tutorialSteps.get(tutorialStepIndex);
        if (step != null && step.allowedViews != null) {
            for (View view : step.allowedViews) {
                if (view == null || view.getVisibility() != View.VISIBLE) {
                    continue;
                }
                android.graphics.Rect targetRect = getViewRectOnScreen(view);
                if (android.graphics.Rect.intersects(cardRect, targetRect)) {
                    overlaps = true;
                    break;
                }
            }
        }
        params.gravity = overlaps ? Gravity.TOP : Gravity.BOTTOM;
        int margin = dpToPx(20);
        params.topMargin = margin;
        params.bottomMargin = margin;
        binding.tutorialOverlay.tutorialCard.setLayoutParams(params);
    }

    private android.graphics.Rect getViewRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new android.graphics.Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight()
        );
    }

    private void clearTutorialHighlights() {
        for (ObjectAnimator animator : tutorialHighlightAnimators) {
            animator.cancel();
        }
        tutorialHighlightAnimators.clear();
        for (View view : tutorialHighlightedViews) {
            if (view != null) {
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        }
        tutorialHighlightedViews.clear();
        binding.tutorialOverlay.tutorialScrim.setHighlightRects(Collections.emptyList());
    }

    private void updateTutorialScrim() {
        List<View> allowedViews = getActiveTutorialAllowedViews();
        if (allowedViews.isEmpty()) {
            binding.tutorialOverlay.tutorialScrim.setHighlightRects(Collections.emptyList());
            return;
        }
        List<android.graphics.RectF> rects = new ArrayList<>();
        int[] scrimLocation = new int[2];
        binding.tutorialOverlay.tutorialScrim.getLocationOnScreen(scrimLocation);
        for (View view : allowedViews) {
            if (view == null || view.getVisibility() != View.VISIBLE) {
                continue;
            }
            int[] viewLocation = new int[2];
            view.getLocationOnScreen(viewLocation);
            float left = viewLocation[0] - scrimLocation[0];
            float top = viewLocation[1] - scrimLocation[1];
            float right = left + view.getWidth();
            float bottom = top + view.getHeight();
            rects.add(new android.graphics.RectF(left, top, right, bottom));
        }
        binding.tutorialOverlay.tutorialScrim.setHighlightRects(rects);
    }

    private void updateTutorialSwitches() {
        boolean diceEnabled = !isTutorialCompleted(TutorialType.DICE_SELECTION);
        binding.settingsPanel.settingsTutorialDiceToggle.setOnCheckedChangeListener(null);
        binding.settingsPanel.settingsTutorialDiceToggle.setChecked(diceEnabled);
        binding.settingsPanel.settingsTutorialDiceToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTutorialCompleted(TutorialType.DICE_SELECTION, !isChecked);
            Toast.makeText(this,
                    isChecked ? "Tutorial de dados reactivado." : "Tutorial de dados desactivado.",
                    Toast.LENGTH_SHORT).show();
        });

        boolean deckEnabled = !isTutorialCompleted(TutorialType.DECK_SELECTION);
        binding.settingsPanel.settingsTutorialDeckToggle.setOnCheckedChangeListener(null);
        binding.settingsPanel.settingsTutorialDeckToggle.setChecked(deckEnabled);
        binding.settingsPanel.settingsTutorialDeckToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTutorialCompleted(TutorialType.DECK_SELECTION, !isChecked);
            Toast.makeText(this,
                    isChecked ? "Tutorial de mazos reactivado." : "Tutorial de mazos desactivado.",
                    Toast.LENGTH_SHORT).show();
        });

        boolean gameLoopEnabled = !isTutorialCompleted(TutorialType.GAME_LOOP);
        binding.settingsPanel.settingsTutorialGameLoopToggle.setOnCheckedChangeListener(null);
        binding.settingsPanel.settingsTutorialGameLoopToggle.setChecked(gameLoopEnabled);
        binding.settingsPanel.settingsTutorialGameLoopToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTutorialCompleted(TutorialType.GAME_LOOP, !isChecked);
            Toast.makeText(this,
                    isChecked ? "Tutorial de juego reactivado." : "Tutorial de juego desactivado.",
                    Toast.LENGTH_SHORT).show();
        });

        boolean releaseEnabled = !isTutorialCompleted(TutorialType.CARD_RELEASE);
        binding.settingsPanel.settingsTutorialReleaseToggle.setOnCheckedChangeListener(null);
        binding.settingsPanel.settingsTutorialReleaseToggle.setChecked(releaseEnabled);
        binding.settingsPanel.settingsTutorialReleaseToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTutorialCompleted(TutorialType.CARD_RELEASE, !isChecked);
            Toast.makeText(this,
                    isChecked ? "Tutorial de liberar cartas reactivado."
                            : "Tutorial de liberar cartas desactivado.",
                    Toast.LENGTH_SHORT).show();
        });

        boolean tideEnabled = !isTutorialCompleted(TutorialType.TIDE);
        binding.settingsPanel.settingsTutorialTideToggle.setOnCheckedChangeListener(null);
        binding.settingsPanel.settingsTutorialTideToggle.setChecked(tideEnabled);
        binding.settingsPanel.settingsTutorialTideToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTutorialCompleted(TutorialType.TIDE, !isChecked);
            Toast.makeText(this,
                    isChecked ? "Tutorial de mareas reactivado."
                            : "Tutorial de mareas desactivado.",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void maybeStartTideTutorial() {
        if (isTutorialCompleted(TutorialType.TIDE) || activeTutorial == TutorialType.TIDE) {
            return;
        }
        if (activeTutorial == null) {
            startTutorial(TutorialType.TIDE);
            pendingTideTutorial = false;
            return;
        }
        pendingTideTutorial = true;
    }

    private void maybeStartPendingTideTutorial() {
        if (!pendingTideTutorial || activeTutorial != null || isTutorialCompleted(TutorialType.TIDE)) {
            return;
        }
        pendingTideTutorial = false;
        startTutorial(TutorialType.TIDE);
    }

    private void setupAudio() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attributes)
                .build();
        loadedSoundIds.clear();
        pendingSoundIds.clear();
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                loadedSoundIds.add(sampleId);
                if (pendingSoundIds.remove(sampleId)) {
                    playSound(sampleId);
                }
            }
        });
        buttonSoundId = loadSound("boton");
        rollSoundId = loadSound("cana");
        splashSoundId = loadSound("splash");
        tideSoundId = loadSound("marea");
        captureSoundId = loadSound("capturar");
        packOpenSoundId = loadSound("sobre");
        whaleSoundId = loadSound("ballena");
        orcaSoundId = loadSound("orca");
    }

    private void setupHaptics() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = getSystemService(VibratorManager.class);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
    }

    private void loadAudioSettings() {
        AudioSettings settings = scoreDatabaseHelper.getAudioSettings();
        musicVolume = settings.getMusicVolume();
        sfxVolume = settings.getSfxVolume();
        buttonVolume = settings.getButtonVolume();
        musicEnabled = settings.isMusicEnabled();
        sfxEnabled = settings.isSfxEnabled();
        buttonEnabled = settings.isButtonEnabled();
        updateAudioSettingsUi();
        applyAudioSettings();
    }

    private void updateAudioSettingsUi() {
        binding.settingsPanel.settingsMusicVolume.setProgress(Math.round(musicVolume * 100f));
        binding.settingsPanel.settingsSfxVolume.setProgress(Math.round(sfxVolume * 100f));
        binding.settingsPanel.settingsButtonVolume.setProgress(Math.round(buttonVolume * 100f));
        binding.settingsPanel.settingsMusicToggle.setChecked(!musicEnabled);
        binding.settingsPanel.settingsSfxToggle.setChecked(!sfxEnabled);
        binding.settingsPanel.settingsButtonToggle.setChecked(!buttonEnabled);
    }

    private void applyAudioSettings() {
        if (!musicEnabled) {
            stopAmbientMusic();
        } else if (ambientPlayer != null) {
            ambientPlayer.setVolume(musicVolume, musicVolume);
        }
    }

    private void saveAudioSettings() {
        scoreDatabaseHelper.saveAudioSettings(
                new AudioSettings(musicVolume, sfxVolume, buttonVolume,
                        musicEnabled, sfxEnabled, buttonEnabled));
    }

    private int loadSound(String name) {
        int resId = getRawSoundId(name);
        if (resId == 0 || soundPool == null) {
            return 0;
        }
        return soundPool.load(this, resId, 1);
    }

    private int getRawSoundId(String name) {
        return getResources().getIdentifier(name, "raw", getPackageName());
    }

    private void playButtonSound(View view) {
        if (view == null || !isButtonView(view)) {
            return;
        }
        playSound(buttonSoundId, buttonVolume, buttonEnabled);
        vibrateButton();
    }

    private void vibrateButton() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(30);
        }
    }

    private void playRollSound() {
        playSound(rollSoundId);
    }

    private void playSplashSound() {
        playSound(splashSoundId);
    }

    private void playTideSound() {
        playSound(tideSoundId);
    }

    private void playCaptureSound() {
        playSound(captureSoundId);
    }

    private void playPackOpenSound() {
        playSound(packOpenSoundId);
    }

    private void playSound(int soundId) {
        playSound(soundId, sfxVolume, sfxEnabled);
    }

    private void playSound(int soundId, float volume, boolean enabled) {
        if (soundPool == null || soundId == 0 || !enabled) {
            return;
        }
        if (!loadedSoundIds.contains(soundId)) {
            pendingSoundIds.add(soundId);
            return;
        }
        soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    private void updateAmbientMusic(String trackName) {
        if (!musicEnabled) {
            stopAmbientMusic();
            return;
        }
        int resId = getRawSoundId(trackName);
        if (resId == 0) {
            stopAmbientMusic();
            return;
        }
        if (ambientResId == resId && ambientPlayer != null) {
            if (!ambientPlayer.isPlaying()) {
                ambientPlayer.start();
            }
            return;
        }
        stopAmbientMusic();
        ambientResId = resId;
        ambientPlayer = MediaPlayer.create(this, resId);
        if (ambientPlayer != null) {
            ambientPlayer.setLooping(true);
            ambientPlayer.setVolume(musicVolume, musicVolume);
            ambientPlayer.start();
        }
    }

    private void stopAmbientMusic() {
        if (ambientPlayer != null) {
            ambientPlayer.stop();
            ambientPlayer.release();
            ambientPlayer = null;
        }
        ambientResId = 0;
    }

    private void pauseAmbientMusic() {
        if (ambientPlayer != null && ambientPlayer.isPlaying()) {
            ambientPlayer.pause();
        }
    }

    private void resumeAmbientMusic() {
        if (musicEnabled && ambientPlayer != null && ambientResId != 0 && !ambientPlayer.isPlaying()) {
            ambientPlayer.start();
        }
    }

    private void releaseAudio() {
        stopAmbientMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        loadedSoundIds.clear();
        pendingSoundIds.clear();
    }

    private void setButtonClickListener(View view, Runnable action) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> {
            if (isButtonView(v)) {
                playButtonSound(v);
            }
            if (action != null) {
                action.run();
            }
        });
    }

    private void setSoundButtonClickListener(View view, Runnable action) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> {
            if (isButtonView(v)) {
                playButtonSound(v);
            }
            if (action != null) {
                action.run();
            }
        });
    }

    private void attachDialogButtonSounds(AlertDialog dialog) {
        if (dialog == null) {
            return;
        }
        dialog.setOnShowListener(dlg -> {
            attachButtonSound(dialog.getButton(AlertDialog.BUTTON_POSITIVE));
            attachButtonSound(dialog.getButton(AlertDialog.BUTTON_NEGATIVE));
            attachButtonSound(dialog.getButton(AlertDialog.BUTTON_NEUTRAL));
        });
    }

    private void attachButtonSound(View button) {
        if (button == null) {
            return;
        }
        if (!isButtonView(button)) {
            return;
        }
        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                playButtonSound(v);
            }
            return false;
        });
    }

    private boolean isButtonView(View view) {
        return view instanceof Button || view instanceof ImageButton;
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
        maybeStartTideTutorial();
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
        binding.gamePanel.fishingAnimationOverlay.post(() -> {
            playTideSound();
            tideParticlesView.playSequence(directions, () -> {
            String currentsLog = gameState.applyPendingCurrentAnimations();
            String combinedLog = combineLogs(baseMessage, currentsLog);
            refreshUi(combinedLog, onComplete);
            });
        });
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
        updateDiceSelectionCapacityText();
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
        int size = calculateDieSize(binding.diceSelectionPanel.diceWarehouseZone);
        int spacing = dpToPx(8);
        for (Map.Entry<DieType, Integer> entry : inventory.entrySet()) {
            DieType type = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                ImageView dieView = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
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
        updateDiceGridColumns();
    }

    private void updateDiceGridColumns() {
        applyDiceGridSizing(binding.diceSelectionPanel.diceSelectionGrid,
                binding.diceSelectionPanel.diceSelectionZone);
        applyDiceGridSizing(binding.diceSelectionPanel.diceWarehouseGrid,
                binding.diceSelectionPanel.diceWarehouseZone);
    }

    private void applyDiceGridSizing(GridLayout grid, View container) {
        int size = calculateDieSize(container);
        int spacing = dpToPx(8);
        grid.setColumnCount(DICE_SELECTION_COLUMNS);
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

    private int calculateDieSize(View container) {
        int availableWidth = container.getWidth();
        if (availableWidth == 0) {
            availableWidth = container.getMeasuredWidth();
        }
        if (availableWidth == 0 && container instanceof ViewGroup) {
            availableWidth = ((ViewGroup) container).getWidth();
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceWarehouseGrid.getMeasuredWidth();
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceWarehouseGrid.getWidth();
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceSelectionGrid.getMeasuredWidth();
        }
        if (availableWidth == 0) {
            availableWidth = binding.diceSelectionPanel.diceSelectionGrid.getWidth();
        }
        if (availableWidth == 0) {
            return dpToPx(70);
        }
        availableWidth = Math.max(0,
                availableWidth - container.getPaddingLeft() - container.getPaddingRight());
        int spacing = dpToPx(8);
        int spacingTotal = spacing * 2 * DICE_SELECTION_COLUMNS;
        int calculated = (availableWidth - spacingTotal) / DICE_SELECTION_COLUMNS;
        return Math.max(dpToPx(32), calculated);
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
        int maxDice = getDiceSelectionCapacity();
        if (!inSelection && countDiceInContainer(binding.diceSelectionPanel.diceSelectionGrid) >= maxDice) {
            Toast.makeText(this, getString(R.string.dice_selection_limit_warning, maxDice), Toast.LENGTH_SHORT).show();
            return;
        }
        if (activeTutorial == TutorialType.DICE_SELECTION) {
            if (tutorialStepIndex == 0 && !inSelection) {
                advanceTutorialStep();
            } else if (tutorialStepIndex == 1 && inSelection) {
                advanceTutorialStep();
            }
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
        updateDiceSelectionCapacityText();
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
        int maxDice = getDiceSelectionCapacity();
        binding.diceSelectionPanel.diceSelectionCounter.setText(
                getString(R.string.dice_selection_counter_format, selected, maxDice));
        binding.diceSelectionPanel.diceSelectionLabel.setVisibility(
                selected == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateDiceSelectionCapacityText() {
        int maxDice = getDiceSelectionCapacity();
        binding.diceSelectionPanel.diceSelectionInstruction.setText(
                getString(R.string.dice_selection_instruction_format, maxDice));
        binding.diceSelectionPanel.diceSelectionCounter.setText(
                getString(R.string.dice_selection_counter_format, 0, maxDice));
    }

    private int getDiceSelectionCapacity() {
        int storedCapacity = scoreDatabaseHelper.getDiceCapacity();
        int clamped = Math.max(MIN_DICE_CAPACITY, Math.min(MAX_DICE_CAPACITY, storedCapacity));
        if (clamped != storedCapacity) {
            scoreDatabaseHelper.setDiceCapacity(clamped);
        }
        return clamped;
    }

    private Map<DieType, Integer> buildDiceInventory() {
        Map<DieType, Integer> inventory = new EnumMap<>(DieType.class);
        int purchasedD4 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D4.name());
        int purchasedD6 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D6.name());
        int purchasedD8 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D8.name());
        int purchasedD10 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D10.name());
        int purchasedD12 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D12.name());
        int purchasedD20 = scoreDatabaseHelper.getPurchasedDiceCount(DieType.D20.name());
        inventory.put(DieType.D4, 1 + purchasedD4);
        inventory.put(DieType.D6, 2 + purchasedD6);
        inventory.put(DieType.D8, 1 + purchasedD8);
        inventory.put(DieType.D10, 1 + purchasedD10);
        inventory.put(DieType.D12, 1 + purchasedD12);
        inventory.put(DieType.D20, purchasedD20);
        return inventory;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String buildReserveText() {
        int d4 = 0, d6 = 0, d8 = 0, d10 = 0, d12 = 0, d20 = 0;
        for (DieType t : gameState.getReserve()) {
            switch (t) {
                case D4: d4++; break;
                case D6: d6++; break;
                case D8: d8++; break;
                case D10: d10++; break;
                case D12: d12++; break;
                case D20: d20++; break;
            }
        }
        return String.format(Locale.getDefault(),
                "D4 x%d • D6 x%d • D8 x%d • D10 x%d • D12 x%d • D20 x%d",
                d4, d6, d8, d10, d12, d20);
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
        if (gameState.isAwaitingD20CriticalValue()) {
            promptD20CriticalValue();
            return;
        }
        if (gameState.isAwaitingCachaloteValue()) {
            promptCachaloteValue();
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
            if (gameState.shouldPlayBiteAnimation(position)) {
                animateBiteOnSlot(position, () -> handleGameResult(gameState.handleBoardSelection(position)));
            } else {
                handleGameResult(gameState.handleBoardSelection(position));
            }
            if (activeTutorial == TutorialType.CARD_RELEASE && tutorialStepIndex == 1) {
                advanceTutorialStep();
            }
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
        if (activeTutorial == TutorialType.GAME_LOOP && tutorialStepIndex == 3) {
            advanceTutorialStep();
        }
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
        playRevealSound(card);
        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        CardFullscreenDialog.show(this, image, null, () -> showRevealedCardsSequential(revealed, onComplete));
    }

    private void playRevealSound(Card card) {
        if (card == null) {
            return;
        }
        CardId id = card.getId();
        if (id == null) {
            return;
        }
        switch (id) {
            case BALLENA_AZUL:
            case BALLENA_JOROBADA:
                playSound(whaleSoundId);
                break;
            case ORCA:
                playSound(orcaSoundId);
                break;
            default:
                break;
        }
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
        boolean brokeRecord = persistFinalScore(finalScore);
        String overlay = bonus != 0
                ? "Bonos: " + (bonus > 0 ? "+" : "") + bonus + "\nTotal: " + finalScore
                : "Total final: " + finalScore;
        Bitmap image = cardImageResolver.getCardBack();
        CardFullscreenDialog.show(this, image, overlay, () -> {
            Runnable finish = () -> {
                if (brokeRecord) {
                    awardRecordBreakPack();
                } else {
                    showStartMenu();
                }
            };
            showAcquiredCardsDialogIfNeeded(() -> showGlobalRankingDialogIfNeeded(finalScore, finish));
        });
    }

    private void showGlobalRankingDialogIfNeeded(int finalScore, Runnable onComplete) {
        if (!RankingApiClient.hasInternet(this)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        RankingApiClient.fetchTopAsync(900, (top, err) -> {
            Integer rank = null;
            if (err == null) {
                if (top != null && !top.isEmpty()) {
                    for (int i = 0; i < top.size(); i++) {
                        if (finalScore >= top.get(i).puntaje) {
                            rank = i + 1;
                            break;
                        }
                    }
                    if (rank == null && top.size() < 900) {
                        rank = top.size() + 1;
                    }
                } else if (top != null && top.isEmpty()) {
                    rank = 1;
                }
            }

            if (rank == null) {
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            String overlay = getString(R.string.global_ranking_congrats, rank);
            Bitmap image = cardImageResolver.getCardBack();
            CardFullscreenDialog.show(this, image, overlay, onComplete);
        });
    }

    private void handleReserveDieTap(DieType type) {
        if (isRevealingCard) {
            Toast.makeText(this, "Toca la carta para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }
        startRollingAnimation(type);
        String msg = gameState.rollFromReserve(type);
        handleGameResult(msg);
        if (activeTutorial == TutorialType.GAME_LOOP && tutorialStepIndex == 1
                && gameState.getSelectedDie() != null) {
            advanceTutorialStep();
        }
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
        boolean hasNewCapture = false;
        for (Card card : currentCaptures) {
            if (!lastCaptures.contains(card)) {
                hasNewCapture = true;
                int totalCaptures = scoreDatabaseHelper.incrementCaptureCount(card.getId());
                int baseTarget = card != null ? card.getPoints() : 0;
                if (GameUtils.isCaptureRewardThreshold(totalCaptures, baseTarget)) {
                    scoreDatabaseHelper.addCardCopies(card.getId(), 1);
                    acquiredCopiesInMatch.add(card);
                }
            }
        }
        if (hasNewCapture) {
            playCaptureSound();
        }
        if (activeTutorial == null
                && !isTutorialCompleted(TutorialType.CARD_RELEASE)
                && gameState.getCaptures().size() >= 5) {
            binding.gamePanel.getRoot().post(() -> maybeStartTutorial(TutorialType.CARD_RELEASE));
        }
    }

    private void showAcquiredCardsDialogIfNeeded(Runnable onComplete) {
        if (acquiredCopiesInMatch.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_acquired_cards);
        RecyclerView recyclerView = dialog.findViewById(R.id.acquiredCardsRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        AcquiredCardsAdapter adapter = new AcquiredCardsAdapter(this);
        adapter.submitList(new ArrayList<>(acquiredCopiesInMatch));
        recyclerView.setAdapter(adapter);
        Button continueButton = dialog.findViewById(R.id.acquiredCardsContinue);
        continueButton.setOnClickListener(v -> {
            dialog.dismiss();
            acquiredCopiesInMatch.clear();
            if (onComplete != null) {
                onComplete.run();
            }
        });
        dialog.setOnCancelListener(cancel -> {
            acquiredCopiesInMatch.clear();
            if (onComplete != null) {
                onComplete.run();
            }
        });
        dialog.show();
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

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Liberar pez")
                            .setMessage("¿Quieres liberar este pez?")
                            .setPositiveButton("Sí", (dialogInterface, which) -> {
                                String msg = gameState.startReleaseFromCapture(card);
                                handleGameResult(msg); // refresca UI + toast + prompts
                                if (activeTutorial == TutorialType.CARD_RELEASE && tutorialStepIndex == 0) {
                                    advanceTutorialStep();
                                }
                            })
                            .setNegativeButton("No", null)
                            .create();
                    attachDialogButtonSounds(dialog);
                    dialog.show();
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Elige qué dado perder")
                .setAdapter(adapter, (dialogInterface, which) -> {
                    String msg = gameState.chooseDieToLose(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }


    private void promptCancelAbility() {
        if (!gameState.isAwaitingCancelConfirmation()) return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cancelar habilidad")
                .setMessage(gameState.getPendingCancelMessage() +
                        "\n¿Deseas cancelar la habilidad?")
                .setPositiveButton("Cancelar", (dialogInterface, which) -> {
                    String msg = gameState.resolveCancelConfirmation(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Seguir intentando", (dialogInterface, which) -> {
                    String msg = gameState.resolveCancelConfirmation(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptAtunDecision() {
        if (!gameState.isAwaitingAtunDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Habilidad del Atún")
                .setMessage("¿Quieres relanzar el dado recién lanzado?")
                .setPositiveButton("Relanzar", (dialogInterface, which) -> {
                    String msg = gameState.chooseAtunReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialogInterface, which) -> {
                    String msg = gameState.chooseAtunReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptBlueCrabDecision() {
        if (!gameState.isAwaitingBlueCrabDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Jaiba azul")
                .setMessage("¿Quieres activar la habilidad para ajustar un dado ±1?")
                .setPositiveButton("Usar", (dialogInterface, which) -> {
                    String msg = gameState.chooseBlueCrabUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialogInterface, which) -> {
                    String msg = gameState.chooseBlueCrabUse(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptBlowfishDecision() {
        if (!gameState.isAwaitingBlowfishDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pez globo")
                .setMessage("¿Quieres inflar un dado al máximo?")
                .setPositiveButton("Usar", (dialogInterface, which) -> {
                    String msg = gameState.chooseBlowfishUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialogInterface, which) -> {
                    String msg = gameState.chooseBlowfishUse(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptPezLoboDecision() {
        if (!gameState.isAwaitingPezLoboDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pez Lobo")
                .setMessage("¿Quieres descartar una carta adyacente boca arriba?")
                .setPositiveButton("Usar", (dialogInterface, which) -> {
                    String msg = gameState.choosePezLoboUse(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialogInterface, which) -> {
                    String msg = gameState.choosePezLoboUse(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptMantisDecision() {
        if (!gameState.isAwaitingMantisDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Langostino mantis")
                .setMessage("¿Quieres relanzar un dado perdido?")
                .setPositiveButton("Usar", (dialogInterface, which) -> {
                    String msg = gameState.chooseMantisReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialogInterface, which) -> {
                    String msg = gameState.chooseMantisReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptBoxerDecision() {
        if (!gameState.isAwaitingBoxerDecision()) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cangrejo boxeador")
                .setMessage("¿Quieres mover otro dado adyacente?")
                .setPositiveButton("Mover", (dialogInterface, which) -> {
                    String msg = gameState.chooseBoxerContinue(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Omitir", (dialogInterface, which) -> {
                    String msg = gameState.chooseBoxerContinue(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Langostino mantis")
                .setAdapter(adapter, (dialogInterface, which) -> {
                    Die chosen = options.get(which);
                    startRollingAnimation(chosen.getType());
                    String msg = gameState.chooseMantisLostDie(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptLangostaRecoveryChoice() {
        if (!gameState.isAwaitingLangostaRecovery()) return;
        List<Die> options = new ArrayList<>(gameState.getLostDice());
        if (options.isEmpty()) {
            handleGameResult("Langosta espinosa: no hay dados perdidos para recuperar.");
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Langosta espinosa")
                .setAdapter(adapter, (dialogInterface, which) -> {
                    String msg = gameState.chooseLangostaRecoveredDie(which);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ajuste de " + creature)
                .setMessage("¿Quieres sumar o restar " + amount + " al dado seleccionado?")
                .setPositiveButton("Sumar", (dialogInterface, which) -> {
                    String msg = gameState.chooseValueAdjustment(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Restar", (dialogInterface, which) -> {
                    String msg = gameState.chooseValueAdjustment(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptGhostShrimpDecision() {
        if (!gameState.isAwaitingGhostShrimpDecision()) return;
        String seen = gameState.getGhostShrimpPeekNames();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Camarón fantasma")
                .setMessage("Viste: " + seen + ". ¿Intercambiar sus posiciones?")
                .setPositiveButton("Intercambiar", (dialogInterface, which) -> {
                    String msg = gameState.resolveGhostShrimpSwap(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialogInterface, which) -> {
                    String msg = gameState.resolveGhostShrimpSwap(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
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
        attachDialogButtonSounds(dialog);

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
        attachDialogButtonSounds(dialog);

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

    private void promptMorsaChoice() {
        if (!gameState.isAwaitingMorsaChoice()) return;
        List<Card> cards = gameState.getPendingMorsaCards();
        if (cards.isEmpty()) return;
        showSingleCardChoiceDialog(
                "Morsa",
                cards,
                gameState::chooseMorsaReplacement,
                null
        );
    }

    private void promptLeonMarinoChoice() {
        if (!gameState.isAwaitingLeonMarinoChoice()) return;
        List<Card> cards = gameState.getPendingLeonMarinoCards();
        if (cards.isEmpty()) return;
        showMultiCardChoiceDialog(
                "León Marino: elige hasta 2 cartas verdes",
                cards,
                2,
                gameState::chooseLeonMarinoCapture,
                () -> gameState.chooseLeonMarinoCapture(java.util.Collections.emptyList())
        );
    }

    private void promptPezVelaDecision() {
        if (!gameState.isAwaitingPezVelaDecision()) return;
        String current = gameState.getPezVelaOriginalDie() != null
                ? gameState.getPezVelaOriginalDie().getLabel()
                : "actual";
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Habilidad del Pez Vela")
                .setMessage("Resultado actual: " + current + ". ¿Relanzar?")
                .setPositiveButton("Relanzar", (dialogInterface, which) -> {
                    String msg = gameState.choosePezVelaReroll(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Conservar", (dialogInterface, which) -> {
                    String msg = gameState.choosePezVelaReroll(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
    }

    private void promptPezVelaResultChoice() {
        if (!gameState.isAwaitingPezVelaResultChoice()) return;
        String previous = gameState.getPezVelaOriginalDie() != null
                ? gameState.getPezVelaOriginalDie().getLabel()
                : "previo";
        String rerolled = gameState.getPezVelaRerolledDie() != null
                ? gameState.getPezVelaRerolledDie().getLabel()
                : "nuevo";
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Habilidad del Pez Vela")
                .setMessage("Elige qué resultado conservar")
                .setPositiveButton("Nuevo (" + rerolled + ")", (dialogInterface, which) -> {
                    String msg = gameState.choosePezVelaResult(true);
                    handleGameResult(msg);
                })
                .setNegativeButton("Anterior (" + previous + ")", (dialogInterface, which) -> {
                    String msg = gameState.choosePezVelaResult(false);
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
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
        List<GameState.CurrentDirection> directions = gameState.getAllowedHumpbackDirections();
        CharSequence[] items = new CharSequence[directions.size()];
        for (int i = 0; i < directions.size(); i++) {
            switch (directions.get(i)) {
                case UP:
                    items[i] = "Arriba";
                    break;
                case DOWN:
                    items[i] = "Abajo";
                    break;
                case LEFT:
                    items[i] = "Izquierda";
                    break;
                default:
                    items[i] = "Derecha";
                    break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Ballena jorobada")
                .setItems(items, (dialog, which) -> {
                    GameState.CurrentDirection selected = directions.get(which);
                    String msg = gameState.chooseHumpbackDirection(selected.name());
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .show();
    }

    private void startRollingAnimation(DieType type) {
        if (animationHandler == null) return;
        playRollSound();

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
        if (gameState.isAwaitingAbilityConfirmation()) { promptAbilityConfirmation(); return; }
        if (gameState.isAwaitingValueAdjustment()) { promptValueAdjustmentChoice(); return; }
        if (gameState.isAwaitingBlueCrabDecision()) { promptBlueCrabDecision(); return; }
        if (gameState.isAwaitingBlowfishDecision()) { promptBlowfishDecision(); return; }
        if (gameState.isAwaitingPezLoboDecision()) { promptPezLoboDecision(); return; }
        if (gameState.isAwaitingMantisDecision()) { promptMantisDecision(); return; }
        if (gameState.isAwaitingMantisLostDieChoice()) { promptMantisLostDieChoice(); return; }
        if (gameState.isAwaitingLangostaRecovery()) { promptLangostaRecoveryChoice(); return; }
        if (gameState.isAwaitingPezVelaDecision()) { promptPezVelaDecision(); return; }
        if (gameState.isAwaitingPezVelaResultChoice()) { promptPezVelaResultChoice(); return; }
        if (gameState.isAwaitingGhostShrimpDecision()) { promptGhostShrimpDecision(); return; }
        if (gameState.isAwaitingPulpoChoice()) { promptPulpoChoice(); return; }
        if (gameState.isAwaitingMorsaChoice()) { promptMorsaChoice(); return; }
        if (gameState.isAwaitingLeonMarinoChoice()) { promptLeonMarinoChoice(); return; }
        if (gameState.isAwaitingArenqueChoice()) { promptArenqueChoice(); return; }
        if (gameState.isAwaitingDecoradorChoice()) { promptDecoradorChoice(); return; }
        if (gameState.isAwaitingHermitChoice()) { promptHermitChoice(); return; }
        if (gameState.isAwaitingViolinistChoice()) { promptViolinistChoice(); return; }
        if (gameState.isAwaitingFocaMoteadaValue()) { promptFocaMoteadaValue(); return; }
        if (gameState.isAwaitingHorseshoeValue()) { promptHorseshoeValue(); return; }
        if (gameState.isAwaitingD20CriticalValue()) { promptD20CriticalValue(); return; }
        if (gameState.isAwaitingCachaloteValue()) { promptCachaloteValue(); return; }
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

    private void promptAbilityConfirmation() {
        GameState.AbilityConfirmation confirmation = gameState.getPendingAbilityConfirmation();
        if (confirmation == null || confirmation.getCard() == null) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_ability_confirmation, null);
        ImageView cardImage = view.findViewById(R.id.abilityConfirmationImage);
        TextView cardName = view.findViewById(R.id.abilityConfirmationName);
        TextView detail = view.findViewById(R.id.abilityConfirmationDetail);

        Bitmap image = cardImageResolver.getImageFor(confirmation.getCard(), true);
        if (image != null) {
            cardImage.setImageBitmap(image);
        }
        cardName.setText(confirmation.getCard().getName());
        detail.setText(confirmation.getDetail());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ability_activation_title))
                .setView(view)
                .setPositiveButton(getString(R.string.ability_activation_accept), (dialogInterface, which) -> {
                    String msg = gameState.confirmAbilityActivation();
                    handleGameResult(msg);
                })
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);
        dialog.show();
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
            handleGameResult("Cangrejo decorador: no hay cartas negras disponibles.");
            return;
        }
        showSingleCardChoiceDialog(
                "Cangrejo decorador",
                cards,
                gameState::chooseDecoradorCard,
                gameState::cancelDecoradorAbility
        );
    }

    private void promptHermitChoice() {
        List<Card> cards = new ArrayList<>(gameState.getPendingHermitCards());
        if (cards.isEmpty()) {
            handleGameResult("Cangrejo ermitaño: no hay cartas disponibles en el mazo.");
            return;
        }
        showSingleCardChoiceDialog(
                "Cangrejo ermitaño",
                cards,
                gameState::chooseHermitReplacementCard,
                null
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
        attachDialogButtonSounds(dialog);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = gameState.chooseHorseshoeValue(values.get(position));
            dialog.dismiss();
            handleGameResult(msg);
        });

        dialog.show();
    }

    private void promptFocaMoteadaValue() {
        int sides = gameState.getFocaMoteadaDieSides();
        DieType dieType = gameState.getFocaMoteadaDieType();
        if (sides <= 0 || dieType == null) {
            handleGameResult("Foca moteada: no hay dado válido para ajustar.");
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
                .setTitle("Foca moteada")
                .setView(gridView)
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = gameState.chooseFocaMoteadaValue(values.get(position));
            dialog.dismiss();
            handleGameResult(msg);
        });

        dialog.show();
    }

    private void promptD20CriticalValue() {
        int sides = gameState.getD20CriticalDieSides();
        DieType dieType = gameState.getD20CriticalDieType();
        if (sides <= 0 || dieType == null) {
            handleGameResult("D20 crítico: no hay dado válido para ajustar.");
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
                .setTitle("D20 crítico")
                .setView(gridView)
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = gameState.chooseD20CriticalValue(values.get(position));
            dialog.dismiss();
            handleGameResult(msg);
        });

        dialog.show();
    }

    private void promptCachaloteValue() {
        int sides = gameState.getCachaloteDieSides();
        DieType dieType = gameState.getCachaloteDieType();
        if (sides <= 0 || dieType == null) {
            handleGameResult("Cachalote: no hay dado válido para ajustar.");
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
                .setTitle("Cachalote")
                .setView(gridView)
                .setCancelable(false)
                .create();
        attachDialogButtonSounds(dialog);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String msg = gameState.chooseCachaloteValue(values.get(position));
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

    private void animateBiteOnSlot(int slotIndex, Runnable onComplete) {
        FrameLayout overlay = binding.animationOverlay;
        if (overlay == null || binding.gamePanel.boardRecycler.getLayoutManager() == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        RecyclerView.LayoutManager layoutManager = binding.gamePanel.boardRecycler.getLayoutManager();
        View slotView = layoutManager.findViewByPosition(slotIndex);
        if (slotView == null || overlay.getWidth() == 0 || overlay.getHeight() == 0) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        int[] overlayLoc = new int[2];
        int[] slotLoc = new int[2];
        overlay.getLocationOnScreen(overlayLoc);
        slotView.getLocationOnScreen(slotLoc);

        float slotX = slotLoc[0] - overlayLoc[0];
        float slotY = slotLoc[1] - overlayLoc[1];
        float slotWidth = slotView.getWidth();
        float slotHeight = slotView.getHeight();

        int teethHeight = Math.max(dpToPx(16), (int) (slotHeight * 0.22f));

        BiteTeethView topTeeth = new BiteTeethView(this);
        topTeeth.setDirection(BiteTeethView.Direction.DOWN);
        BiteTeethView bottomTeeth = new BiteTeethView(this);
        bottomTeeth.setDirection(BiteTeethView.Direction.UP);

        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams((int) slotWidth, teethHeight);
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams((int) slotWidth, teethHeight);
        overlay.addView(topTeeth, topParams);
        overlay.addView(bottomTeeth, bottomParams);

        topTeeth.setX(slotX);
        topTeeth.setY(slotY - teethHeight);
        bottomTeeth.setX(slotX);
        bottomTeeth.setY(slotY + slotHeight);

        float targetTopY = slotY + (slotHeight * 0.5f) - teethHeight;
        float targetBottomY = slotY + (slotHeight * 0.5f);

        ObjectAnimator topAnimator = ObjectAnimator.ofFloat(topTeeth, View.Y, topTeeth.getY(), targetTopY);
        ObjectAnimator bottomAnimator = ObjectAnimator.ofFloat(bottomTeeth, View.Y, bottomTeeth.getY(), targetBottomY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(topAnimator, bottomAnimator);
        set.setDuration(240L);
        set.setInterpolator(new DecelerateInterpolator(1.4f));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.removeView(topTeeth);
                overlay.removeView(bottomTeeth);
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                overlay.removeView(topTeeth);
                overlay.removeView(bottomTeeth);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        set.start();
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
        boolean hadSelectedDie = gameState.getSelectedDie() != null;
        BoardSlot slot = gameState.getBoard()[position];
        int diceBefore = slot != null ? slot.getDice().size() : 0;
        String result = gameState.placeSelectedDie(position);
        boolean placed = hadSelectedDie && gameState.consumeLastDiePlaced();
        if (placed) {
            playSplashSound();
        }
        handleGameResult(result);
        if (activeTutorial == TutorialType.GAME_LOOP) {
            if (tutorialStepIndex == 2 && placed) {
                advanceTutorialStep();
            } else if (tutorialStepIndex == 4 && placed && diceBefore == 1) {
                advanceTutorialStep();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAmbientMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeAmbientMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scoreDatabaseHelper != null) {
            scoreDatabaseHelper.close();
        }
        releaseAudio();
    }
}
