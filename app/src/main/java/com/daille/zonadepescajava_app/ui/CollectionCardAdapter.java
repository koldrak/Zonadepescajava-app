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
import com.daille.zonadepescajava_app.model.GameUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CollectionCardAdapter extends RecyclerView.Adapter<CollectionCardAdapter.CollectionViewHolder> {
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

        CardId cardId = card != null ? card.getId() : null;
        int ownedCount = getOwnedCount(cardId);
        int captureCount = entry.getCaptureCount();
        int baseTarget = card != null ? card.getPoints() : 0;
        int progress = GameUtils.getCaptureRewardProgress(captureCount, baseTarget);
        int target = GameUtils.getCaptureRewardTarget(captureCount, baseTarget);
        holder.captureProgress.setText(holder.captureProgress.getContext()
                .getString(R.string.collection_capture_progress_format, progress, target));

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
        private final TextView copyCount;
        private final TextView captureProgress;

        CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.collectionCardImage);
            copyCount = itemView.findViewById(R.id.collectionCardCopies);
            captureProgress = itemView.findViewById(R.id.collectionCardProgress);
        }
    }
}
