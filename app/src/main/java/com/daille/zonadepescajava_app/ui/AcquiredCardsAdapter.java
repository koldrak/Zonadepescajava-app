package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;

import java.util.ArrayList;
import java.util.List;

public class AcquiredCardsAdapter extends RecyclerView.Adapter<AcquiredCardsAdapter.AcquiredCardViewHolder> {
    private final LayoutInflater inflater;
    private final CardImageResolver imageResolver;
    private final List<Card> cards = new ArrayList<>();

    public AcquiredCardsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        imageResolver = new CardImageResolver(context);
    }

    public void submitList(List<Card> newCards) {
        cards.clear();
        if (newCards != null) {
            cards.addAll(newCards);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AcquiredCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_acquired_card, parent, false);
        return new AcquiredCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AcquiredCardViewHolder holder, int position) {
        Card card = cards.get(position);
        Bitmap image = imageResolver.getImageFor(card, true);
        if (image == null) {
            image = imageResolver.getCardBack();
        }
        holder.cardImage.setImageBitmap(image);
        holder.cardImage.setContentDescription(card != null
                ? card.getName()
                : holder.cardImage.getContext().getString(R.string.card_image_content_description));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class AcquiredCardViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cardImage;

        AcquiredCardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.acquiredCardImage);
        }
    }
}
