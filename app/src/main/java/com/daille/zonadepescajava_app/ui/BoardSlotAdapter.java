package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
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

public class BoardSlotAdapter extends RecyclerView.Adapter<BoardSlotAdapter.SlotViewHolder> {

    public interface OnSlotInteractionListener {
        void onSlotTapped(int position);

        void onSlotLongPressed(int position);
    }

    private final List<BoardSlot> slots = new ArrayList<>();
    private final OnSlotInteractionListener listener;
    private final CardImageResolver imageResolver;
    private final DiceImageResolver diceImageResolver;
    private final Context context;
    private final List<Integer> highlighted = new ArrayList<>();

    public BoardSlotAdapter(Context context, List<BoardSlot> data, OnSlotInteractionListener listener) {
        this.context = context;
        slots.addAll(data);
        this.listener = listener;
        this.imageResolver = new CardImageResolver(context);
        this.diceImageResolver = new DiceImageResolver(context);
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBoardSlotBinding binding = ItemBoardSlotBinding.inflate(inflater, parent, false);
        return new SlotViewHolder(binding, listener, imageResolver, diceImageResolver, context);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(slots.get(position), highlighted.contains(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public void update(List<BoardSlot> updated, List<Integer> highlights) {
        slots.clear();
        slots.addAll(updated);
        highlighted.clear();
        if (highlights != null) {
            highlighted.addAll(highlights);
        }
        notifyDataSetChanged();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemBoardSlotBinding binding;
        private final CardImageResolver imageResolver;
        private final DiceImageResolver diceImageResolver;
        private final Context context;
        private final OnSlotInteractionListener listener;

        SlotViewHolder(ItemBoardSlotBinding binding, OnSlotInteractionListener listener,
                       CardImageResolver imageResolver, DiceImageResolver diceImageResolver,
                       Context context) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageResolver = imageResolver;
            this.diceImageResolver = diceImageResolver;
            this.context = context;
            this.listener = listener;
            binding.getRoot().setOnClickListener(v -> listener.onSlotTapped(getBindingAdapterPosition()));
            binding.getRoot().setOnLongClickListener(v -> {
                this.listener.onSlotLongPressed(getBindingAdapterPosition());
                return true;
            });
        }

        void bind(BoardSlot slot, boolean highlighted) {
            Card card = slot.getCard();
            Bitmap image = imageResolver.getImageFor(card, slot.isFaceUp());
            if (image == null) {
                image = imageResolver.getCardBack();
            }

            binding.cardImage.setImageBitmap(image);

            int strokePx = highlighted
                    ? (int) (context.getResources().getDisplayMetrics().density * 6)
                    : 0;
            binding.getRoot().setStrokeWidth(strokePx);
            binding.getRoot().setStrokeColor(
                    ContextCompat.getColor(context, R.color.selection_highlight));

            binding.cardImage.setContentDescription(slot.isFaceUp() && card != null
                    ? card.getName()
                    : context.getString(R.string.card_image_content_description));

            binding.getRoot().setOnLongClickListener(v -> {
                listener.onSlotLongPressed(getBindingAdapterPosition());
                return true;
            });

            List<Die> dice = slot.getDice();
            binding.cardDiceContainer.setVisibility(dice.isEmpty() ? ViewGroup.GONE : ViewGroup.VISIBLE);

            if (dice.size() > 0) {
                Bitmap dieOne = diceImageResolver.getFace(dice.get(0));
                binding.dieSlotOne.setVisibility(ViewGroup.VISIBLE);
                binding.dieSlotOne.setImageBitmap(dieOne);
            } else {
                binding.dieSlotOne.setVisibility(ViewGroup.GONE);
            }

            if (dice.size() > 1) {
                Bitmap dieTwo = diceImageResolver.getFace(dice.get(1));
                binding.dieSlotTwo.setVisibility(ViewGroup.VISIBLE);
                binding.dieSlotTwo.setImageBitmap(dieTwo);
            } else {
                binding.dieSlotTwo.setVisibility(ViewGroup.GONE);
            }
        }
    }
}
