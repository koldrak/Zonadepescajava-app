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

import java.util.ArrayList;
import java.util.List;

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

    public CollectionCardAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.imageResolver = new CardImageResolver(context);
    }

    public void submitList(List<CollectionEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
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

        int count = entry.getCaptureCount();
        if (count > 0) {
            holder.captureCount.setText(holder.captureCount.getContext()
                    .getString(R.string.collection_capture_count, count));
            holder.captureCount.setVisibility(View.VISIBLE);
            holder.cardImage.setColorFilter(null);
            holder.cardImage.setAlpha(1f);
        } else {
            holder.captureCount.setVisibility(View.GONE);
            holder.cardImage.setColorFilter(createLockedFilter());
            holder.cardImage.setAlpha(0.9f);
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

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cardImage;
        private final TextView captureCount;

        CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.collectionCardImage);
            captureCount = itemView.findViewById(R.id.collectionCaptureCount);
        }
    }
}
