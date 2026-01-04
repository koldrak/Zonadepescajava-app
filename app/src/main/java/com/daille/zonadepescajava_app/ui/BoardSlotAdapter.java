package com.daille.zonadepescajava_app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.databinding.ItemBoardSlotBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Die;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoardSlotAdapter extends RecyclerView.Adapter<BoardSlotAdapter.SlotViewHolder> {

    private final List<BoardSlot> slots = new ArrayList<>();

    public BoardSlotAdapter(List<BoardSlot> data) {
        slots.addAll(data);
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBoardSlotBinding binding = ItemBoardSlotBinding.inflate(inflater, parent, false);
        return new SlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemBoardSlotBinding binding;

        SlotViewHolder(ItemBoardSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BoardSlot slot) {
            if (slot.getCard() == null) {
                binding.cardTitle.setText("Vacío");
                binding.cardType.setVisibility(View.GONE);
                binding.cardPoints.setVisibility(View.GONE);
            } else {
                binding.cardTitle.setText(slot.isFaceUp() ? slot.getCard().getName() : "Boca abajo");
                binding.cardType.setVisibility(View.VISIBLE);
                binding.cardPoints.setVisibility(View.VISIBLE);
                binding.cardType.setText(String.format(Locale.getDefault(), "Tipo: %s", slot.getCard().getType()));
                binding.cardPoints.setText(String.format(Locale.getDefault(), "Pts: %d", slot.getCard().getPoints()));
            }

            binding.dice.setText("Dados: " + buildDiceLabel(slot.getDice()));

            StringBuilder status = new StringBuilder();
            if (slot.isProtectedOnce()) status.append("🛡️ Protegido\n");
            if (slot.isCalamarForcedFaceDown()) status.append("🦑 Forzado por Calamar\n");
            if (slot.getSumConditionShift() != 0) {
                status.append("ΔCondSuma: ").append(slot.getSumConditionShift());
            }

            if (status.length() == 0) {
                status.append("Sin modificadores activos");
            }

            binding.status.setText(status.toString().trim());
        }

        private String buildDiceLabel(List<Die> dice) {
            if (dice == null || dice.isEmpty()) {
                return "-";
            }
            List<String> parts = new ArrayList<>();
            for (Die die : dice) {
                parts.add(die.getLabel());
            }
            return String.join(" | ", parts);
        }
    }
}
