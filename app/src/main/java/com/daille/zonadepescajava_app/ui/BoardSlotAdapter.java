package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.databinding.ItemBoardSlotBinding;
import com.daille.zonadepescajava_app.model.BoardSlot;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.Die;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BoardSlotAdapter extends RecyclerView.Adapter<BoardSlotAdapter.SlotViewHolder> {

    public interface OnSlotInteractionListener {
        void onSlotTapped(int position);

        void onSlotLongPressed(int position);
    }

    private final List<BoardSlot> slots = new ArrayList<>();
    private final OnSlotInteractionListener listener;
    private static final Map<CardId, String> CARD_ASSETS = new EnumMap<>(CardId.class);
    private static final String CARD_BACK = "cartabocaabajo.png";
    private final Map<String, Bitmap> imageCache = new HashMap<>();

    static {
        CARD_ASSETS.put(CardId.CANGREJO_ROJO, "cangrejorojo.png");
        CARD_ASSETS.put(CardId.JAIBA_AZUL, "jaibaazul.png");
        CARD_ASSETS.put(CardId.CAMARON_FANTASMA, "camaronfantasma.png");
        CARD_ASSETS.put(CardId.LANGOSTA_ESPINOSA, "langostaespinosa.png");
        CARD_ASSETS.put(CardId.KRILL, "krill.png");
        CARD_ASSETS.put(CardId.CANGREJO_ERMITANO, "cangrejoermitaño.png");
        CARD_ASSETS.put(CardId.PERCEBES, "percebes.png");
        CARD_ASSETS.put(CardId.CENTOLLA, "centolla.png");
        CARD_ASSETS.put(CardId.NAUTILUS, "nautilus.png");

        CARD_ASSETS.put(CardId.SARDINA, "sardina.png");
        CARD_ASSETS.put(CardId.ATUN, "atun.png");
        CARD_ASSETS.put(CardId.SALMON, "salmon.png");
        CARD_ASSETS.put(CardId.PEZ_PAYASO, "pezpayaso.png");
        CARD_ASSETS.put(CardId.PEZ_GLOBO, "pezglobo.png");
        CARD_ASSETS.put(CardId.MORENA, "morena.png");
        CARD_ASSETS.put(CardId.CABALLITO_DE_MAR, "caballitodemar.png");
        CARD_ASSETS.put(CardId.PEZ_LINTERNA, "pezlinterna.png");
        CARD_ASSETS.put(CardId.KOI, "koi.png");
        CARD_ASSETS.put(CardId.PEZ_VOLADOR, "pezvolador.png");
        CARD_ASSETS.put(CardId.PIRANA, "piraña.png");

        CARD_ASSETS.put(CardId.TIBURON_BLANCO, "tiburonblanco.png");
        CARD_ASSETS.put(CardId.TIBURON_MARTILLO, "tiburonmartillo.png");
        CARD_ASSETS.put(CardId.TIBURON_BALLENA, "tiburonballena.png");
        CARD_ASSETS.put(CardId.PEZ_VELA, "pezvela.png");
        CARD_ASSETS.put(CardId.CALAMAR_GIGANTE, "calamargigante.png");
        CARD_ASSETS.put(CardId.MANTA_GIGANTE, "mantagigante.png");
        CARD_ASSETS.put(CardId.BALLENA_AZUL, "ballenaazul.png");

        CARD_ASSETS.put(CardId.BOTA_VIEJA, "botavieja.png");
        CARD_ASSETS.put(CardId.BOTELLA_PLASTICO, "botellaplastica.png");
        CARD_ASSETS.put(CardId.RED_ENREDADA, "redenredada.png");
        CARD_ASSETS.put(CardId.LATA_OXIDADA, "lataoxidada.png");
        CARD_ASSETS.put(CardId.LIMPIADOR_MARINO, "limpiadormarino.png");
        CARD_ASSETS.put(CardId.ANZUELO_ROTO, "anzueloroto.png");
    }

    public BoardSlotAdapter(List<BoardSlot> data, OnSlotInteractionListener listener) {
        slots.addAll(data);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBoardSlotBinding binding = ItemBoardSlotBinding.inflate(inflater, parent, false);
        return new SlotViewHolder(binding, listener);
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

        SlotViewHolder(ItemBoardSlotBinding binding, OnSlotInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> listener.onSlotTapped(getBindingAdapterPosition()));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onSlotLongPressed(getBindingAdapterPosition());
                return true;
            });
        }

        void bind(BoardSlot slot) {
            Card card = slot.getCard();
            Context context = binding.getRoot().getContext();
            if (card == null) {
                binding.cardTitle.setText("Vacío");
                binding.cardType.setText("Sin carta");
                binding.cardPoints.setText("");
                binding.cardImage.setImageDrawable(null);
            } else {
                binding.cardTitle.setText(slot.isFaceUp() ? card.getName() : "Boca abajo");
                binding.cardType.setText(String.format(Locale.getDefault(), "%s • %s",
                        card.getType().name(), slot.isFaceUp() ? card.getCondition().getClass().getSimpleName() : "?"));
                binding.cardPoints.setText(String.format(Locale.getDefault(), "%d pts", card.getPoints()));
                bindCardImage(context, card, slot.isFaceUp());
            }
            renderDiceRow(context, slot.getDice());

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

        private void bindCardImage(Context context, Card card, boolean faceUp) {
            String fileName = faceUp ? CARD_ASSETS.get(card.getId()) : CARD_BACK;
            Bitmap bmp = loadBitmapFromAssets(context, fileName);
            if (bmp != null) {
                binding.cardImage.setImageBitmap(bmp);
            } else {
                binding.cardImage.setImageDrawable(null);
            }
        }

        private Bitmap loadBitmapFromAssets(Context context, String fileName) {
            if (fileName == null) return null;
            if (imageCache.containsKey(fileName)) return imageCache.get(fileName);
            try (InputStream is = context.getAssets().open("img/" + fileName)) {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                imageCache.put(fileName, bmp);
                return bmp;
            } catch (IOException e) {
                return null;
            }
        }

        private void renderDiceRow(Context context, List<Die> dice) {
            binding.diceRow.removeAllViews();
            if (dice == null || dice.isEmpty()) {
                TextView tv = new TextView(context);
                tv.setText("Dados: -");
                binding.diceRow.addView(tv);
                return;
            }

            for (Die die : dice) {
                ImageView iv = new ImageView(context);
                int size = (int) (48 * context.getResources().getDisplayMetrics().density);
                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(size, size);
                lp.rightMargin = (int) (8 * context.getResources().getDisplayMetrics().density);
                iv.setLayoutParams(lp);
                Bitmap bmp = loadBitmapFromAssets(context, buildDieAssetName(die));
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                } else {
                    iv.setContentDescription(die.getLabel());
                }
                binding.diceRow.addView(iv);
            }
        }

        private String buildDieAssetName(Die die) {
            return "D" + die.getType().getSides() + die.getValue() + ".png";
        }
    }
}
