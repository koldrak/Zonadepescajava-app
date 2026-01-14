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
    private final Map<CardId, Integer> captureCounts = new EnumMap<>(CardId.class);

    public CollectionCardAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.imageResolver = new CardImageResolver(context);
    }

    public void submitList(List<CollectionEntry> newEntries, Map<CardId, Integer> counts) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
        }
        captureCounts.clear();
        if (counts != null) {
            captureCounts.putAll(counts);
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
        CardId cardId = card != null ? card.getId() : null;
        CardId unlockSource = getUnlockSource(cardId);
        boolean isUnlockSource = isUnlockSource(cardId);
        boolean isUnlockable = unlockSource != null;
        boolean isLocked = false;
        String label = null;

        if (isUnlockable) {
            int sourceCount = getCaptureCount(unlockSource);
            if (sourceCount < UNLOCK_TARGET) {
                isLocked = true;
                label = holder.captureCount.getContext().getString(R.string.collection_locked);
            } else {
                label = holder.captureCount.getContext()
                        .getString(R.string.collection_capture_count, count);
            }
        } else if (isUnlockSource) {
            label = holder.captureCount.getContext()
                    .getString(R.string.collection_capture_progress, count, UNLOCK_TARGET);
        } else if (count > 0) {
            label = holder.captureCount.getContext()
                    .getString(R.string.collection_capture_count, count);
        }

        if (label != null) {
            holder.captureCount.setText(label);
            holder.captureCount.setVisibility(View.VISIBLE);
        } else {
            holder.captureCount.setVisibility(View.GONE);
        }

        boolean shouldLockStyle = isLocked || (!isUnlockSource && !isUnlockable && count == 0);
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

    private int getCaptureCount(CardId id) {
        if (id == null) return 0;
        Integer count = captureCounts.get(id);
        return count != null ? count : 0;
    }

    private static CardId getUnlockSource(CardId cardId) {
        if (cardId == null) return null;
        switch (cardId) {
            case CANGREJO_BOXEADOR:
                return CardId.CANGREJO_ROJO;
            case LANGOSTINO_MANTIS:
                return CardId.JAIBA_AZUL;
            case CAMARON_PISTOLA:
                return CardId.CAMARON_FANTASMA;
            case BOGAVANTE:
                return CardId.LANGOSTA_ESPINOSA;
            case COPEPODO_BRILLANTE:
                return CardId.KRILL;
            case CANGREJO_DECORADOR:
                return CardId.CANGREJO_ERMITANO;
            case LOCO:
                return CardId.PERCEBES;
            case JAIBA_GIGANTE_DE_COCO:
                return CardId.CENTOLLA;
            case CANGREJO_HERRADURA:
                return CardId.NAUTILUS;
            case OSTRAS:
                return CardId.ALMEJAS;
            case CANGREJO_VIOLINISTA:
                return CardId.CANGREJO_ARANA;
            case CONGRIO:
                return CardId.SARDINA;
            case PEZ_BETTA:
                return CardId.ATUN;
            case TRUCHA_ARCOIRIS:
                return CardId.SALMON;
            case PEZ_PIEDRA:
                return CardId.PEZ_PAYASO;
            case PEZ_LEON:
                return CardId.PEZ_GLOBO;
            case PEZ_DRAGON_AZUL:
                return CardId.MORENA;
            case PEZ_PIPA:
                return CardId.CABALLITO_DE_MAR;
            case PEZ_HACHA_ABISAL:
                return CardId.PEZ_LINTERNA;
            case CARPA_DORADA:
                return CardId.KOI;
            case FLETAN:
                return CardId.PEZ_VOLADOR;
            case PEZ_LOBO:
                return CardId.PIRANA;
            case PEZ_BORRON:
                return CardId.PEZ_FANTASMA;
            case SEPIA:
                return CardId.PULPO;
            case DAMISELAS:
                return CardId.ARENQUE;
            case LAMPREA:
                return CardId.REMORA;
            case TIBURON_TIGRE:
                return CardId.TIBURON_BLANCO;
            case DELFIN:
                return CardId.TIBURON_MARTILLO;
            case TIBURON_PEREGRINO:
                return CardId.TIBURON_BALLENA;
            case NARVAL:
                return CardId.PEZ_VELA;
            case ORCA:
                return CardId.CALAMAR_GIGANTE;
            case ANGUILA_ELECTRICA:
                return CardId.MANTA_GIGANTE;
            case CACHALOTE:
                return CardId.BALLENA_AZUL;
            case ESTURION:
                return CardId.MERO_GIGANTE;
            case BALLENA_JOROBADA:
                return CardId.PEZ_LUNA;
            case AUTO_HUNDIDO:
                return CardId.BOTA_VIEJA;
            case BOTELLA_DE_VIDRIO:
                return CardId.BOTELLA_PLASTICO;
            case RED_DE_ARRASTRE:
                return CardId.RED_ENREDADA;
            case MICRO_PLASTICOS:
                return CardId.LATA_OXIDADA;
            case FOSA_ABISAL:
                return CardId.LIMPIADOR_MARINO;
            case DERRAME_PETROLEO:
                return CardId.ANZUELO_ROTO;
            case BARCO_PESQUERO:
                return CardId.CORRIENTES_PROFUNDAS;
            default:
                return null;
        }
    }

    private static boolean isUnlockSource(CardId cardId) {
        return cardId == CardId.CANGREJO_ROJO
                || cardId == CardId.JAIBA_AZUL
                || cardId == CardId.CAMARON_FANTASMA
                || cardId == CardId.LANGOSTA_ESPINOSA
                || cardId == CardId.KRILL
                || cardId == CardId.CANGREJO_ERMITANO
                || cardId == CardId.PERCEBES
                || cardId == CardId.CENTOLLA
                || cardId == CardId.NAUTILUS
                || cardId == CardId.ALMEJAS
                || cardId == CardId.CANGREJO_ARANA
                || cardId == CardId.SARDINA
                || cardId == CardId.ATUN
                || cardId == CardId.SALMON
                || cardId == CardId.PEZ_PAYASO
                || cardId == CardId.PEZ_GLOBO
                || cardId == CardId.MORENA
                || cardId == CardId.CABALLITO_DE_MAR
                || cardId == CardId.PEZ_LINTERNA
                || cardId == CardId.KOI
                || cardId == CardId.PEZ_VOLADOR
                || cardId == CardId.PIRANA
                || cardId == CardId.PEZ_FANTASMA
                || cardId == CardId.PULPO
                || cardId == CardId.ARENQUE
                || cardId == CardId.REMORA
                || cardId == CardId.TIBURON_BLANCO
                || cardId == CardId.TIBURON_MARTILLO
                || cardId == CardId.TIBURON_BALLENA
                || cardId == CardId.PEZ_VELA
                || cardId == CardId.CALAMAR_GIGANTE
                || cardId == CardId.MANTA_GIGANTE
                || cardId == CardId.BALLENA_AZUL
                || cardId == CardId.MERO_GIGANTE
                || cardId == CardId.PEZ_LUNA
                || cardId == CardId.BOTA_VIEJA
                || cardId == CardId.BOTELLA_PLASTICO
                || cardId == CardId.RED_ENREDADA
                || cardId == CardId.LATA_OXIDADA
                || cardId == CardId.LIMPIADOR_MARINO
                || cardId == CardId.ANZUELO_ROTO
                || cardId == CardId.CORRIENTES_PROFUNDAS;
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
