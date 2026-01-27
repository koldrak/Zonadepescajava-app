package com.daille.zonadepescajava_app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.DieType;
import com.daille.zonadepescajava_app.model.GameState;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;

import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BoardSlotAdapter.OnSlotInteractionListener {

    private ActivityMainBinding binding;
    private GameState gameState;
    private BoardSlotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        disableSoundEffects(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        gameState = new GameState();
        setupBoard();
        setupButtons();
        refreshUi("Juego iniciado. Lanza un dado y toca una carta.");
    }

    private void setupBoard() {
        gameState.newGame();
        adapter = new BoardSlotAdapter(Arrays.asList(gameState.getBoard()), this);
        binding.boardRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.boardRecycler.setAdapter(adapter);
    }

    private void disableSoundEffects(View view) {
        view.setSoundEffectsEnabled(false);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                disableSoundEffects(group.getChildAt(i));
            }
        }
    }

    private void setupButtons() {
        binding.rollD4.setOnClickListener(v -> onRoll(DieType.D4));
        binding.rollD6.setOnClickListener(v -> onRoll(DieType.D6));
        binding.rollD8.setOnClickListener(v -> onRoll(DieType.D8));
        binding.rollD12.setOnClickListener(v -> onRoll(DieType.D12));
    }

    private void onRoll(DieType type) {
        String msg = gameState.rollFromReserve(type);
        refreshUi(msg);
    }

    private void refreshUi(String log) {
        adapter.update(Arrays.asList(gameState.getBoard()));
        binding.score.setText(String.format(Locale.getDefault(), "Puntaje: %d", gameState.getScore()));
        binding.deckInfo.setText(String.format(Locale.getDefault(), "Mazo restante: %d", gameState.getDeckSize()));
        binding.captures.setText(String.format(Locale.getDefault(), "Capturas: %d", gameState.getCaptures().size()));

        binding.selection.setText(gameState.getSelectedDie() == null
                ? "Selecciona un dado de la reserva"
                : "Dado preparado: " + gameState.getSelectedDie().getLabel());

        binding.reserve.setText("Reserva: " + buildReserveText());
        binding.lost.setText(String.format(Locale.getDefault(), "Perdidos: %d", gameState.getLostDice().size()));
        binding.log.setText(log);
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
        BoardSlot slot = gameState.getBoard()[position];
        boolean wasFaceUp = slot.isFaceUp();
        String result = gameState.placeSelectedDie(position);
        BoardSlot updatedSlot = gameState.getBoard()[position];
        if (!wasFaceUp && updatedSlot.isFaceUp()) {
            playWhaleSound(updatedSlot.getCard());
        }
        refreshUi(result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlotLongPressed(int position) {
        BoardSlot slot = gameState.getBoard()[position];
        boolean wasFaceUp = slot.isFaceUp();
        gameState.toggleFace(position);
        BoardSlot updatedSlot = gameState.getBoard()[position];
        if (!wasFaceUp && updatedSlot.isFaceUp()) {
            playWhaleSound(updatedSlot.getCard());
        }
        refreshUi("Has volteado la carta");
    }

    private void playWhaleSound(Card card) {
        int soundResId = getWhaleSoundResId(card);
        if (soundResId == 0) {
            return;
        }
        android.media.MediaPlayer player = android.media.MediaPlayer.create(this, soundResId);
        if (player == null) {
            return;
        }
        player.setOnCompletionListener(android.media.MediaPlayer::release);
        player.start();
    }

    private int getWhaleSoundResId(Card card) {
        if (card == null) {
            return 0;
        }
        if (card.getId() == CardId.BALLENA_AZUL) {
            return getRawSoundId("ballena");
        }
        String name = card.getName();
        if (name == null) {
            return 0;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.contains("ballena azul") || normalized.contains("ballena jorobada")) {
            return getRawSoundId("ballena");
        }
        if (normalized.contains("orca")) {
            return getRawSoundId("orca");
        }
        return 0;
    }

    private int getRawSoundId(String rawName) {
        return getResources().getIdentifier(rawName, "raw", getPackageName());
    }
}
