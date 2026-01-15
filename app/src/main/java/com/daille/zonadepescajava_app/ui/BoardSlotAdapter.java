package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;

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
    private final List<Integer> remoraBorderSlots = new ArrayList<>();
    private final List<Integer> botaViejaPenaltySlots = new ArrayList<>();
    private final List<Integer> autoHundidoBonusSlots = new ArrayList<>();


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
        holder.itemView.setRotationY(0f);
        holder.itemView.setHasTransientState(false);
        holder.bind(
                slots.get(position),
                highlighted.contains(position),
                remoraBorderSlots.contains(position),
                botaViejaPenaltySlots.contains(position),
                autoHundidoBonusSlots.contains(position)
        );

    }


    @Override
    public int getItemCount() {
        return slots.size();
    }

    public void update(List<BoardSlot> updated, List<Integer> highlights) {
        update(updated, highlights, null, null, null);
    }

    public void update(List<BoardSlot> updated, List<Integer> highlights, List<Integer> remoraSlots) {
        update(updated, highlights, remoraSlots, null, null);
    }

    public void update(List<BoardSlot> updated,
                       List<Integer> highlights,
                       List<Integer> remoraSlots,
                       List<Integer> botaViejaSlots,
                       List<Integer> autoHundidoSlots) {

        slots.clear();
        slots.addAll(updated);

        highlighted.clear();
        if (highlights != null) {
            highlighted.addAll(highlights);
        }

        remoraBorderSlots.clear();
        if (remoraSlots != null) {
            remoraBorderSlots.addAll(remoraSlots);
        }

        botaViejaPenaltySlots.clear();
        if (botaViejaSlots != null) {
            botaViejaPenaltySlots.addAll(botaViejaSlots);
        }

        autoHundidoBonusSlots.clear();
        if (autoHundidoSlots != null) {
            autoHundidoBonusSlots.addAll(autoHundidoSlots);
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
        private void applyBottleHalo(ImageView view, boolean on) {
            if (view == null) return;

            if (!on || view.getVisibility() != ViewGroup.VISIBLE) {
                view.setBackground(null);
                return;
            }

            // Creamos un "glow" radial: centro negro denso -> transparente hacia afuera.
            // Usamos view.post para asegurar que ya tenga width/height y poder calcular el radio.
            view.post(() -> {
                if (view.getWidth() <= 0 || view.getHeight() <= 0) return;

                float d = context.getResources().getDisplayMetrics().density;
                int padPx = Math.max(1, Math.round(4f * d)); // para que el glow tenga espacio y no se recorte

                int maxDim = Math.max(view.getWidth(), view.getHeight());
                float radius = maxDim * 0.85f; // qué tan lejos se difumina

                android.graphics.drawable.GradientDrawable glow = new android.graphics.drawable.GradientDrawable();
                glow.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                glow.setGradientType(android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT);
                glow.setGradientRadius(radius);

                // Colores: centro negro más denso -> anillo medio más suave -> transparente
                // (puedes subir/bajar opacidad cambiando los AA/66/00)
                glow.setColors(new int[] {
                        0xCC000000, // centro (negro denso)
                        0x55000000, // medio (difuminado)
                        0x00000000  // borde (transparente)
                });

                view.setBackground(glow);
                view.setPadding(padPx, padPx, padPx, padPx);
            });
        }

        private void applyBlackHalo(ImageView view, boolean enabled) {
            if (view == null) return;

            if (!enabled) {
                view.setBackground(null);
                return;
            }

            float density = context.getResources().getDisplayMetrics().density;
            int strokePx = Math.max(1, Math.round(3f * density));
            float radiusPx = 8f * density;

            GradientDrawable halo = new GradientDrawable();
            halo.setColor(Color.TRANSPARENT);
            halo.setCornerRadius(radiusPx);
            halo.setStroke(strokePx, Color.BLACK);

            view.setBackground(halo);
        }

        void bind(BoardSlot slot, boolean highlighted, boolean remoraBorder, boolean botaViejaPenalty,
                  boolean autoHundidoBonus) {
            Card card = slot.getCard();
            Bitmap image = null;
            if (card != null) {
                image = imageResolver.getImageFor(card, slot.isFaceUp());
                if (image == null) {
                    image = imageResolver.getCardBack();
                }
            }

            binding.cardImage.setImageBitmap(image);
            binding.cardImage.setVisibility(card == null ? View.INVISIBLE : View.VISIBLE);

            int strokePx;
            int strokeColor;

            boolean bottleBuffed = slot.getStatus() != null && slot.getStatus().bottleDieBonus > 0;
            boolean glassPenalty = slot.getStatus() != null && slot.getStatus().glassBottlePenalty;
            if (remoraBorder) {
                strokePx = (int) (context.getResources().getDisplayMetrics().density * 6);
                strokeColor = ContextCompat.getColor(context, R.color.remora_highlight);
            } else if (highlighted) {
                strokePx = (int) (context.getResources().getDisplayMetrics().density * 6);
                strokeColor = ContextCompat.getColor(context, R.color.selection_highlight);
            } else if (bottleBuffed || glassPenalty) {
                strokePx = (int) (context.getResources().getDisplayMetrics().density * 4);
                strokeColor = ContextCompat.getColor(context, R.color.bottle_highlight);
            } else {
                strokePx = 0;
                strokeColor = ContextCompat.getColor(context, R.color.selection_highlight);
            }

            binding.getRoot().setStrokeWidth(strokePx);
            binding.getRoot().setStrokeColor(strokeColor);


            binding.cardImage.setContentDescription(slot.isFaceUp() && card != null
                    ? card.getName()
                    : context.getString(R.string.card_image_content_description));


            List<Die> dice = slot.getDice();
            binding.cardDiceContainer.setVisibility(dice.isEmpty() ? ViewGroup.GONE : ViewGroup.VISIBLE);

// Valores base (reales)
            int v1 = (dice.size() > 0) ? dice.get(0).getValue() : 0;
            int v2 = (dice.size() > 1) ? dice.get(1).getValue() : 0;

// ✅ Bota Vieja: “−1 a la suma”
// Para que sea consistente con tu regla actual (sum -= 1),
// bajamos visualmente SOLO 1 dado (el de mayor valor) en 1.
            if (botaViejaPenalty && dice.size() > 0) {
                if (dice.size() == 1) {
                    v1 = Math.max(1, v1 - 1);
                } else {
                    if (v1 >= v2) v1 = Math.max(1, v1 - 1);
                    else          v2 = Math.max(1, v2 - 1);
                }
            }

// ✅ Auto Hundido: “+1 a la suma”
// Para mantener consistencia visual, subimos SOLO 1 dado (el de mayor valor) en 1.
            if (autoHundidoBonus && dice.size() > 0) {
                if (dice.size() == 1) {
                    int sides = dice.get(0).getType().getSides();
                    v1 = Math.min(sides, v1 + 1);
                } else {
                    int sides1 = dice.get(0).getType().getSides();
                    int sides2 = dice.get(1).getType().getSides();
                    if (v1 >= v2) v1 = Math.min(sides1, v1 + 1);
                    else          v2 = Math.min(sides2, v2 + 1);
                }
            }

            if (dice.size() > 0) {
                Bitmap dieOne = diceImageResolver.getFace(dice.get(0).getType(), v1);
                binding.dieSlotOne.setVisibility(ViewGroup.VISIBLE);
                binding.dieSlotOne.setImageBitmap(dieOne);
            } else {
                binding.dieSlotOne.setVisibility(ViewGroup.GONE);
            }

            if (dice.size() > 1) {
                Bitmap dieTwo = diceImageResolver.getFace(dice.get(1).getType(), v2);
                binding.dieSlotTwo.setVisibility(ViewGroup.VISIBLE);
                binding.dieSlotTwo.setImageBitmap(dieTwo);
            } else {
                binding.dieSlotTwo.setVisibility(ViewGroup.GONE);
            }

// ✅ Halo negro para dados afectados
// (si ya tenías applyBottleHalo(ImageView, boolean), úsalo así)
            boolean bottleHalo = botaViejaPenalty || autoHundidoBonus || bottleBuffed || glassPenalty;
            applyBottleHalo(binding.dieSlotOne, bottleHalo && dice.size() > 0);
            applyBottleHalo(binding.dieSlotTwo, bottleHalo && dice.size() > 1);

        }
    }
}
