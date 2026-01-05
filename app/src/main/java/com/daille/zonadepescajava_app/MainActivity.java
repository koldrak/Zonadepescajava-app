package com.daille.zonadepescajava_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.data.ScoreDatabaseHelper;
import com.daille.zonadepescajava_app.data.ScoreRecord;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.Die;
import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;
import com.daille.zonadepescajava_app.ui.CardFullscreenDialog;
import com.daille.zonadepescajava_app.ui.CardImageResolver;
import com.daille.zonadepescajava_app.ui.DiceImageResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private ScoreDatabaseHelper scoreDatabaseHelper;
    private ArrayAdapter<String> scoreRecordsAdapter;

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

        if (viewModel.isInitialized()) {
            setupBoard();
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
        binding.boardRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.boardRecycler.setAdapter(adapter);
    }

    private void setupMenuButtons() {
        binding.startNewGame.setOnClickListener(v -> {
            viewModel.startNewGame();
            gameState = viewModel.getGameState();
            endScoringShown = false;
            setupBoard();
            showGameLayout();
            refreshUi("Juego iniciado. Lanza un dado y toca una carta.");
        });
        binding.openSettings.setOnClickListener(v ->
                Toast.makeText(this, "Configuraciones próximamente.", Toast.LENGTH_SHORT).show());
        binding.openCollections.setOnClickListener(v ->
                Toast.makeText(this, "Colecciones disponibles próximamente.", Toast.LENGTH_SHORT).show());
    }

    private void setupScoreRecordsList() {
        scoreRecordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.scoreRecordsList.setAdapter(scoreRecordsAdapter);
        binding.scoreRecordsList.setEmptyView(binding.scoreRecordsEmpty);
        refreshScoreRecords();
    }

    private void refreshScoreRecords() {
        List<ScoreRecord> records = scoreDatabaseHelper.getTopScores(3);
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
    }

    private void persistFinalScore(int finalScore) {
        if (!viewModel.isFinalScoreRecorded()) {
            scoreDatabaseHelper.saveScore(finalScore);
            viewModel.markFinalScoreRecorded();
            refreshScoreRecords();
        }
    }

    private void showStartMenu() {
        binding.startMenu.setVisibility(View.VISIBLE);
        binding.gameContent.setVisibility(View.GONE);
    }

    private void showGameLayout() {
        binding.startMenu.setVisibility(View.GONE);
        binding.gameContent.setVisibility(View.VISIBLE);
    }

    private void refreshUi(String log) {
        refreshUi(log, null);
    }

    private void refreshUi(String log, Runnable afterReveals) {
        adapter.update(Arrays.asList(gameState.getBoard()), gameState.getHighlightSlots());
        binding.score.setText(String.format(Locale.getDefault(), "Puntaje: %d", gameState.getScore()));
        binding.deckInfo.setText(String.format(Locale.getDefault(), "Mazo restante: %d", gameState.getDeckSize()));
        binding.captures.setText(String.format(Locale.getDefault(), "Capturas: %d", gameState.getCaptures().size()));

        binding.selection.setText(gameState.getSelectedDie() == null
                ? "Selecciona un dado de la reserva"
                : "Dado preparado: " + gameState.getSelectedDie().getLabel());

        updateSelectedDiePreview();
        renderDiceCollection(binding.reserveDiceContainer, gameState.getReserve(), true);
        renderDiceCollection(binding.lostDiceContainer, gameState.getLostDice(), false);

        binding.lost.setText(String.format(Locale.getDefault(), "Perdidos: %d", gameState.getLostDice().size()));
        binding.log.setText(log);
        List<Card> revealed = gameState.consumeRecentlyRevealedCards();
        if (revealed.isEmpty()) {
            if (afterReveals != null) {
                afterReveals.run();
            }
            checkForFinalScoring();
            return;
        }
        showRevealedCardsSequential(new ArrayList<>(revealed), afterReveals);
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
        BoardSlot slot = gameState.getBoard()[position];
        android.graphics.Bitmap image = cardImageResolver.getImageFor(slot.getCard(), slot.isFaceUp());
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        CardFullscreenDialog.show(this, image);
    }

    private void updateSelectedDiePreview() {
        if (gameState.getSelectedDie() == null) {
            binding.selectedDieImage.setVisibility(View.GONE);
            return;
        }

        Bitmap face = diceImageResolver.getFace(gameState.getSelectedDie());
        if (face != null) {
            binding.selectedDieImage.setVisibility(View.VISIBLE);
            binding.selectedDieImage.setImageBitmap(face);
        } else {
            binding.selectedDieImage.setVisibility(View.GONE);
        }
    }

    private void showRevealedCardsSequential(List<Card> revealed, Runnable onComplete) {
        if (revealed.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            checkForFinalScoring();
            return;
        }
        Card card = revealed.remove(0);
        Bitmap image = cardImageResolver.getImageFor(card, true);
        if (image == null) {
            image = cardImageResolver.getCardBack();
        }
        CardFullscreenDialog.show(this, image, null, () -> showRevealedCardsSequential(revealed, onComplete));
    }

    private void checkForFinalScoring() {
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
                    chip.setOnClickListener(v -> handleReserveDieTap(type));
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

    private void promptDieLossChoice() {
        if (!gameState.isAwaitingDieLoss()) return;
        java.util.List<Die> options = gameState.getPendingDiceChoices();
        if (options.isEmpty()) return;
        CharSequence[] labels = new CharSequence[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).getLabel();
        }

        new AlertDialog.Builder(this)
                .setTitle("Elige qué dado perder")
                .setItems(labels, (dialog, which) -> {
                    String msg = gameState.chooseDieToLose(which);
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
        if (rollingRunnable != null) {
            animationHandler.removeCallbacks(rollingRunnable);
        }

        rollingRunnable = new Runnable() {
            private int frames = 12;

            @Override
            public void run() {
                if (frames-- <= 0) {
                    animationHandler.removeCallbacks(this);
                    updateSelectedDiePreview();
                    return;
                }
                Bitmap preview = diceImageResolver.randomFace(type);
                if (preview != null) {
                    binding.selectedDieImage.setVisibility(View.VISIBLE);
                    binding.selectedDieImage.setImageBitmap(preview);
                }
                animationHandler.postDelayed(this, 70);
            }
        };

        animationHandler.post(rollingRunnable);
    }

    private void handleGameResult(String message) {
        refreshUi(message, this::triggerPendingPrompts);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void triggerPendingPrompts() {
        if (gameState.isAwaitingValueAdjustment()) {
            promptValueAdjustmentChoice();
        }
        if (gameState.isAwaitingBlueCrabDecision()) {
            promptBlueCrabDecision();
        }
        if (gameState.isAwaitingBlowfishDecision()) {
            promptBlowfishDecision();
        }
        if (gameState.isAwaitingPezVelaDecision()) {
            promptPezVelaDecision();
        }
        if (gameState.isAwaitingPezVelaResultChoice()) {
            promptPezVelaResultChoice();
        }
        if (gameState.isAwaitingGhostShrimpDecision()) {
            promptGhostShrimpDecision();
        }
        if (gameState.isAwaitingPulpoChoice()) {
            promptPulpoChoice();
        }
        if (gameState.isAwaitingArenqueChoice()) {
            promptArenqueChoice();
        }
        if (gameState.isAwaitingAtunDecision()) {
            promptAtunDecision();
        }
        if (gameState.isAwaitingDieLoss()) {
            promptDieLossChoice();
        }
    }

    private void animatePlacement(int position) {
        if (gameState.getSelectedDie() == null) {
            completePlacement(position);
            return;
        }
        View cardView = binding.boardRecycler.getLayoutManager() != null
                ? binding.boardRecycler.getLayoutManager().findViewByPosition(position)
                : null;
        if (cardView == null) {
            completePlacement(position);
            return;
        }
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
