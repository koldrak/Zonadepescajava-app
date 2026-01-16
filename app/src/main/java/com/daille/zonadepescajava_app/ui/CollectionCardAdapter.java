package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CollectionCardAdapter extends RecyclerView.Adapter<CollectionCardAdapter.CollectionViewHolder> {
    private static final int UNLOCK_TARGET = 3;

    public static class CollectionEntry {
        private final Card card;
        private final int captureCount;

        public CollectionEntry(Card card, int captureCount) {
            this.card = card;
            this.captureCount = captureCount;
        }

        public Card getCard() {
            return card;
        }

        public int getCaptureCount() {
            return captureCount;
        }
    }

    private final LayoutInflater inflater;
    private final CardImageResolver imageResolver;
    private final List<CollectionEntry> entries = new ArrayList<>();
    private final Map<CardId, Integer> ownedCounts = new EnumMap<>(CardId.class);

    public CollectionCardAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.imageResolver = new CardImageResolver(context);
    }

    public void submitList(List<CollectionEntry> newEntries,
                           Map<CardId, Integer> owned) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
        }
        ownedCounts.clear();
        if (owned != null) {
            ownedCounts.putAll(owned);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_collection_card, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        CollectionEntry entry = entries.get(position);
        Card card = entry.getCard();
        Bitmap image = imageResolver.getImageFor(card, true);
        if (image == null) {
            image = imageResolver.getCardBack();
        }
        holder.cardImage.setImageBitmap(image);
        holder.cardImage.setContentDescription(card != null ? card.getName()
                : holder.cardImage.getContext().getString(R.string.card_image_content_description));

        int captureCount = entry.getCaptureCount();
        CardId cardId = card != null ? card.getId() : null;
        int ownedCount = getOwnedCount(cardId);
        int progress = captureCount % UNLOCK_TARGET;

        String label = holder.captureCount.getContext()
                .getString(R.string.collection_copy_progress, progress, UNLOCK_TARGET);
        holder.captureCount.setText(label);
        holder.captureCount.setVisibility(View.VISIBLE);

        if (ownedCount > 0) {
            holder.copyCount.setText(holder.copyCount.getContext()
                    .getString(R.string.collection_copy_badge, ownedCount));
            holder.copyCount.setVisibility(View.VISIBLE);
        } else {
            holder.copyCount.setVisibility(View.GONE);
        }

        boolean shouldLockStyle = ownedCount == 0;
        if (shouldLockStyle) {
            holder.cardImage.setColorFilter(createLockedFilter());
            holder.cardImage.setAlpha(0.9f);
        } else {
            holder.cardImage.setColorFilter(null);
            holder.cardImage.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private static ColorMatrixColorFilter createLockedFilter() {
        ColorMatrix saturation = new ColorMatrix();
        saturation.setSaturation(0f);
        ColorMatrix darken = new ColorMatrix(new float[]{
                0.15f, 0f, 0f, 0f, 0f,
                0f, 0.15f, 0f, 0f, 0f,
                0f, 0f, 0.15f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        });
        saturation.postConcat(darken);
        return new ColorMatrixColorFilter(saturation);
    }

    private int getOwnedCount(CardId id) {
        if (id == null) return 0;
        Integer count = ownedCounts.get(id);
        return count != null ? count : 0;
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cardImage;
        private final TextView captureCount;
        private final TextView copyCount;

        CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.collectionCardImage);
            captureCount = itemView.findViewById(R.id.collectionCaptureCount);
            copyCount = itemView.findViewById(R.id.collectionCardCopies);
        }
    }
}
