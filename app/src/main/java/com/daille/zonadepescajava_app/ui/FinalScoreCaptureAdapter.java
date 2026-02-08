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

import java.util.ArrayList;
import java.util.List;

public class FinalScoreCaptureAdapter extends RecyclerView.Adapter<FinalScoreCaptureAdapter.CaptureViewHolder> {
    public static final class Entry {
        private final Card card;
        private final int points;
        private final int cumulative;

        public Entry(Card card, int points, int cumulative) {
            this.card = card;
            this.points = points;
            this.cumulative = cumulative;
        }
    }

    private final LayoutInflater inflater;
    private final CardImageResolver imageResolver;
    private final List<Entry> entries = new ArrayList<>();

    public FinalScoreCaptureAdapter(Context context, CardImageResolver imageResolver) {
        this.inflater = LayoutInflater.from(context);
        this.imageResolver = imageResolver;
    }

    public void submitList(List<Entry> items) {
        entries.clear();
        if (items != null) {
            entries.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CaptureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_final_score_capture, parent, false);
        return new CaptureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptureViewHolder holder, int position) {
        Entry entry = entries.get(position);
        Card card = entry.card;
        String name = card != null ? card.getName() : holder.itemView.getContext()
                .getString(R.string.final_score_unknown_card);
        holder.name.setText(name);

        int points = entry.points;
        String signValue = (points >= 0 ? "+" : "") + points;
        holder.points.setText(holder.itemView.getContext().getString(
                R.string.final_score_capture_points_format, signValue, entry.cumulative));

        Bitmap image = card != null ? imageResolver.getImageFor(card, true) : null;
        if (image == null) {
            image = imageResolver.getCardBack();
        }
        holder.image.setImageBitmap(image);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class CaptureViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView name;
        private final TextView points;

        CaptureViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.finalScoreCaptureImage);
            name = itemView.findViewById(R.id.finalScoreCaptureName);
            points = itemView.findViewById(R.id.finalScoreCapturePoints);
        }
    }
}
