package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.ui.CardFullscreenDialog;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DeckSelectionAdapter extends RecyclerView.Adapter<DeckSelectionAdapter.DeckSelectionViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private final LayoutInflater inflater;
    private final CardImageResolver imageResolver;
    private final List<Card> cards = new ArrayList<>();
    private final Map<CardId, Integer> selectionCounts = new EnumMap<>(CardId.class);
    private final Map<CardId, Integer> inventoryCounts = new EnumMap<>(CardId.class);
    private final OnSelectionChangedListener selectionListener;
    private final boolean showSellPrice;
    private final int sellPriceMultiplier;

    public DeckSelectionAdapter(Context context, OnSelectionChangedListener selectionListener) {
        this(context, selectionListener, false, 0);
    }

    public DeckSelectionAdapter(Context context, OnSelectionChangedListener selectionListener,
                                boolean showSellPrice, int sellPriceMultiplier) {
        this.inflater = LayoutInflater.from(context);
        this.imageResolver = new CardImageResolver(context);
        this.selectionListener = selectionListener;
        this.showSellPrice = showSellPrice;
        this.sellPriceMultiplier = sellPriceMultiplier;
    }

    public void submitList(List<Card> items) {
        cards.clear();
        if (items != null) {
            cards.addAll(items);
        }
        selectionCounts.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }

    public void setInventoryCounts(Map<CardId, Integer> counts) {
        inventoryCounts.clear();
        if (counts != null) {
            inventoryCounts.putAll(counts);
        }
        notifyDataSetChanged();
    }

    public Map<CardId, Integer> getSelectionCounts() {
        return selectionCounts;
    }

    public void clearSelections() {
        selectionCounts.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }

    public void setSelectionCounts(Map<CardId, Integer> counts) {
        selectionCounts.clear();
        if (counts != null) {
            for (Map.Entry<CardId, Integer> entry : counts.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                int maxCopies = inventoryCounts.getOrDefault(entry.getKey(), 0);
                int desired = entry.getValue() == null ? 0 : entry.getValue();
                int finalCount = Math.min(Math.max(desired, 0), maxCopies);
                if (finalCount > 0) {
                    selectionCounts.put(entry.getKey(), finalCount);
                }
            }
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }

    @NonNull
    @Override
    public DeckSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_deck_selection_card, parent, false);
        return new DeckSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckSelectionViewHolder holder, int position) {
        Card card = cards.get(position);
        Bitmap image = imageResolver.getImageFor(card, true);
        if (image == null) {
            image = imageResolver.getCardBack();
        }
        holder.cardImage.setImageBitmap(image);
        holder.cardImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.cardImage.setContentDescription(card != null ? card.getName()
                : holder.cardImage.getContext().getString(R.string.card_image_content_description));

        int ownedCount = inventoryCounts.getOrDefault(card.getId(), 0);
        holder.ownedBadge.setText(holder.ownedBadge.getContext()
                .getString(R.string.deck_selection_badge, ownedCount));

        int count = selectionCounts.getOrDefault(card.getId(), 0);
        if (count > 0) {
            holder.selectionBadge.setVisibility(View.VISIBLE);
            holder.selectionBadge.setText(holder.selectionBadge.getContext()
                    .getString(R.string.deck_selection_badge, count));
        } else {
            holder.selectionBadge.setVisibility(View.GONE);
        }

        if (showSellPrice) {
            int sellPrice = card.getPoints() * sellPriceMultiplier;
            holder.sellPrice.setText(holder.sellPrice.getContext()
                    .getString(R.string.card_sell_price_format, sellPrice));
            holder.sellPrice.setVisibility(View.VISIBLE);
        } else {
            holder.sellPrice.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int current = selectionCounts.getOrDefault(card.getId(), 0);
            int maxCopies = inventoryCounts.getOrDefault(card.getId(), 0);
            if (maxCopies <= 0) {
                return;
            }
            int next = current + 1;
            if (next > maxCopies) {
                next = 0;
            }
            if (next == 0) {
                selectionCounts.remove(card.getId());
            } else {
                selectionCounts.put(card.getId(), next);
            }
            notifyItemChanged(holder.getBindingAdapterPosition());
            if (selectionListener != null) {
                selectionListener.onSelectionChanged();
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            Bitmap fullImage = imageResolver.getImageFor(card, true);
            if (fullImage == null) {
                fullImage = imageResolver.getCardBack();
            }
            CardFullscreenDialog.show(holder.itemView.getContext(), fullImage);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public List<Card> getSelectedDeck() {
        List<Card> selected = new ArrayList<>();
        for (Card card : cards) {
            int count = selectionCounts.getOrDefault(card.getId(), 0);
            for (int i = 0; i < count; i++) {
                selected.add(card);
            }
        }
        return selected;
    }

    static class DeckSelectionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cardImage;
        private final TextView ownedBadge;
        private final TextView selectionBadge;
        private final TextView sellPrice;

        DeckSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.deckSelectionCardImage);
            ownedBadge = itemView.findViewById(R.id.deckSelectionOwnedBadge);
            selectionBadge = itemView.findViewById(R.id.deckSelectionBadge);
            sellPrice = itemView.findViewById(R.id.deckSelectionSellPrice);
        }
    }
}
