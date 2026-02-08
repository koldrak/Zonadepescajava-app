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
        setupSettingsSections();
        refreshUi(getString(R.string.log_game_start));
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

    private void setupSettingsSections() {
        binding.userSectionHeader.setOnClickListener(v -> toggleSection(binding.userSectionContent));
        binding.languageSectionHeader.setOnClickListener(v -> toggleSection(binding.languageSectionContent));
        binding.audioSectionHeader.setOnClickListener(v -> toggleSection(binding.audioSectionContent));
        binding.tutorialSectionHeader.setOnClickListener(v -> toggleSection(binding.tutorialSectionContent));
    }

    private void toggleSection(View sectionContent) {
        int visibility = sectionContent.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        sectionContent.setVisibility(visibility);
    }

    private void onRoll(DieType type) {
        String msg = gameState.rollFromReserve(type);
        refreshUi(msg);
    }

    private void refreshUi(String log) {
        adapter.update(Arrays.asList(gameState.getBoard()));
        binding.score.setText(getString(R.string.score_format, gameState.getScore()));
        binding.deckInfo.setText(getString(R.string.deck_info_format, gameState.getDeckSize()));
        binding.captures.setText(getString(R.string.captures_format, gameState.getCaptures().size()));

        binding.selection.setText(gameState.getSelectedDie() == null
                ? getString(R.string.selection_prompt)
                : getString(R.string.die_ready_format, gameState.getSelectedDie().getLabel()));

        int[] reserveCounts = buildReserveCounts();
        binding.reserve.setText(getString(
                R.string.reserve_format,
                reserveCounts[0],
                reserveCounts[1],
                reserveCounts[2],
                reserveCounts[3]
        ));
        binding.lost.setText(getString(R.string.lost_format, gameState.getLostDice().size()));
        binding.log.setText(log);
    }

    private int[] buildReserveCounts() {
        int d4 = 0, d6 = 0, d8 = 0, d12 = 0;
        for (DieType t : gameState.getReserve()) {
            switch (t) {
                case D4: d4++; break;
                case D6: d6++; break;
                case D8: d8++; break;
                case D12: d12++; break;
            }
        }
        return new int[] { d4, d6, d8, d12 };
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
        refreshUi(getString(R.string.log_card_flipped));
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
