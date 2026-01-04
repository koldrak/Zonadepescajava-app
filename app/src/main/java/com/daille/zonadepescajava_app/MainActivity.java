package com.daille.zonadepescajava_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.daille.zonadepescajava_app.databinding.ActivityMainBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.Die;
import com.daille.zonadepescajava_app.ui.BoardSlotAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setupBoard();
    }

    private void setupBoard() {
        List<BoardSlot> sample = Arrays.asList(
                new BoardSlot(new Card("Calamar", "Molusco", 4), true,
                        Collections.singletonList(new Die("D6", 4)), false, false, 0),
                new BoardSlot(new Card("Langosta Espinosa", "Crustáceo", 3), true,
                        Arrays.asList(new Die("D8", 2), new Die("D6", 5)), true, false, 1),
                new BoardSlot(new Card("Krill", "Presa", 1), false,
                        Collections.singletonList(new Die("D4", 3)), false, true, 0),
                new BoardSlot(new Card("Tiburón", "Depredador", 5), true,
                        Collections.emptyList(), false, false, -1),
                new BoardSlot(null, true, Collections.emptyList(), false, false, 0),
                new BoardSlot(new Card("Cangrejo ermitaño", "Crustáceo", 2), true,
                        Collections.singletonList(new Die("D6", 1)), false, false, 0)
        );

        binding.boardRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.boardRecycler.setAdapter(new BoardSlotAdapter(sample));
    }
}
