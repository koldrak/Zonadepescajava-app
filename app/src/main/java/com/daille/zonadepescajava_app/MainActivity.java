package com.daille.zonadepescajava_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
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
        String result = gameState.placeSelectedDie(position);
        refreshUi(result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlotLongPressed(int position) {
        gameState.toggleFace(position);
        refreshUi("Has volteado la carta");
    }
}
