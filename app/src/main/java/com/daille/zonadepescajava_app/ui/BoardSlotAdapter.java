package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.databinding.ItemBoardSlotBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.Die;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoardSlotAdapter extends RecyclerView.Adapter<BoardSlotAdapter.SlotViewHolder> {

    public interface OnSlotInteractionListener {
        void onSlotTapped(int position);

        void onSlotLongPressed(int position);
    }

    private final List<BoardSlot> slots = new ArrayList<>();
    private final OnSlotInteractionListener listener;
    private final CardImageResolver imageResolver;
    private final Context context;

    public BoardSlotAdapter(Context context, List<BoardSlot> data, OnSlotInteractionListener listener) {
        this.context = context;
        slots.addAll(data);
        this.listener = listener;
        this.imageResolver = new CardImageResolver(context);
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBoardSlotBinding binding = ItemBoardSlotBinding.inflate(inflater, parent, false);
        return new SlotViewHolder(binding, listener, imageResolver, context);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public void update(List<BoardSlot> updated) {
        slots.clear();
        slots.addAll(updated);
        notifyDataSetChanged();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemBoardSlotBinding binding;
        private final CardImageResolver imageResolver;
        private final Context context;

        SlotViewHolder(ItemBoardSlotBinding binding, OnSlotInteractionListener listener,
                       CardImageResolver imageResolver, Context context) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageResolver = imageResolver;
            this.context = context;
            binding.getRoot().setOnClickListener(v -> listener.onSlotTapped(getBindingAdapterPosition()));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onSlotLongPressed(getBindingAdapterPosition());
                return true;
            });
        }

        void bind(BoardSlot slot) {
            Card card = slot.getCard();
            Bitmap image = imageResolver.getImageFor(card, slot.isFaceUp());
            if (image != null) {
                binding.cardImage.setImageBitmap(image);
            } else {
                int color = ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.darker_gray);
                binding.cardImage.setImageDrawable(new ColorDrawable(color));
            }

            if (card == null) {
                binding.cardTitle.setText("Vacío");
                binding.cardType.setText("Sin carta");
                binding.cardPoints.setText("");
                binding.cardImage.setContentDescription(context.getString(R.string.card_image_content_description));
            } else {
                binding.cardTitle.setText(slot.isFaceUp() ? card.getName() : "Boca abajo");
                binding.cardType.setText(String.format(Locale.getDefault(), "%s • %s",
                        card.getType().name(), slot.isFaceUp() ? card.getCondition().getClass().getSimpleName() : "?"));
                binding.cardPoints.setText(String.format(Locale.getDefault(), "%d pts", card.getPoints()));
                binding.cardImage.setContentDescription(slot.isFaceUp() ? card.getName() : context.getString(R.string.card_image_content_description));
            }

            binding.dice.setText("Dados: " + buildDiceLabel(slot.getDice()));

            StringBuilder status = new StringBuilder();
            if (slot.getStatus().protectedOnce) status.append("🛡️ Protegido\n");
            if (slot.getStatus().calamarForcedFaceDown) status.append("🦑 Forzado\n");
            if (slot.getStatus().sumConditionShift != 0) {
                status.append("ΔCondición: ").append(slot.getStatus().sumConditionShift);
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
