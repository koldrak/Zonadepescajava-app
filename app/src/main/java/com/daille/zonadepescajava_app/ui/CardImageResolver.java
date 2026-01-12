package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import android.content.res.Resources;
/**
 * Helper that mirrors the asset resolution strategy from the desktop example, but adapted to Android assets.
 */
public class CardImageResolver {

    private static final String ASSET_DIR = "img/";
    private static final String CARD_BACK_FILE = "cartabocaabajo.png";

    private final AssetManager assets;
    private final Map<String, Bitmap> cache = new HashMap<>();
    private final Map<CardId, String> explicitMap = new HashMap<>();
    private final Set<String> assetIndex = new HashSet<>();

    public CardImageResolver(Context context) {
        this.assets = context.getAssets();
        seedExplicitMap();
        indexAssets();
    }

    public Bitmap getImageFor(Card card, boolean faceUp) {
        if (!faceUp || card == null) {
            return getCardBack();
        }

        String mapped = resolveFrontFile(card);
        Bitmap bmp = loadBitmap(mapped);
        if (bmp != null) return bmp;

        return getCardBack();
    }

    public Bitmap getCardBack() {
        return loadBitmap(CARD_BACK_FILE);
    }

    private String resolveFrontFile(Card card) {
        if (card == null) return null;

        String mapped = explicitMap.get(card.getId());
        if (assetExists(mapped)) return mapped;

        for (String candidate : buildCandidates(card)) {
            if (assetExists(candidate)) return candidate;
        }

        return mapped;
    }

    private List<String> buildCandidates(Card card) {
        List<String> candidates = new ArrayList<>();

        String normalizedId = normalize(card.getId().name());
        candidates.add(normalizedId + ".png");
        candidates.add(normalizedId.replace("_", "") + ".png");

        String normalizedName = normalize(card.getName());
        candidates.add(normalizedName + ".png");
        candidates.add(normalizedName.replace(" ", "") + ".png");

        return candidates;
    }

    private Bitmap loadBitmap(String fileName) {
        if (fileName == null) return null;
        if (cache.containsKey(fileName)) {
            return cache.get(fileName);
        }

        try (InputStream stream = assets.open(ASSET_DIR + fileName)) {
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            cache.put(fileName, bmp);
            return bmp;
        } catch (IOException e) {
            cache.put(fileName, null);
            return null;
        }
    }

    private boolean assetExists(String fileName) {
        return fileName != null && assetIndex.contains(fileName);
    }

    private void seedExplicitMap() {
        explicitMap.put(CardId.ANZUELO_ROTO, "anzueloroto.png");
        explicitMap.put(CardId.ATUN, "atun.png");
        explicitMap.put(CardId.BALLENA_AZUL, "ballenaazul.png");
        explicitMap.put(CardId.BOTA_VIEJA, "botavieja.png");
        explicitMap.put(CardId.BOTELLA_PLASTICO, "botellaplastica.png");
        explicitMap.put(CardId.CABALLITO_DE_MAR, "caballitodemar.png");
        explicitMap.put(CardId.CALAMAR_GIGANTE, "calamargigante.png");
        explicitMap.put(CardId.CAMARON_FANTASMA, "camaronfantasma.png");
        explicitMap.put(CardId.CAMARON_PISTOLA, "camaronpistola.png");
        explicitMap.put(CardId.CANGREJO_ARANA, "cangrejoaraña.png");
        explicitMap.put(CardId.CANGREJO_BOXEADOR, "cangrejoboxeador.png");
        explicitMap.put(CardId.CANGREJO_ERMITANO, "cangrejoermitaño.png");
        explicitMap.put(CardId.CANGREJO_ROJO, "cangrejorojo.png");
        explicitMap.put(CardId.CENTOLLA, "centolla.png");
        explicitMap.put(CardId.KOI, "koi.png");
        explicitMap.put(CardId.KRILL, "krill.png");
        explicitMap.put(CardId.LANGOSTA_ESPINOSA, "langostaespinosa.png");
        explicitMap.put(CardId.LANGOSTINO_MANTIS, "langostinomantis.png");
        explicitMap.put(CardId.LATA_OXIDADA, "lataoxidada.png");
        explicitMap.put(CardId.LIMPIADOR_MARINO, "limpiadormarino.png");
        explicitMap.put(CardId.MANTA_GIGANTE, "mantagigante.png");
        explicitMap.put(CardId.MORENA, "morena.png");
        explicitMap.put(CardId.NAUTILUS, "nautilus.png");
        explicitMap.put(CardId.PEZ_GLOBO, "pezglobo.png");
        explicitMap.put(CardId.PEZ_LINTERNA, "pezlinterna.png");
        explicitMap.put(CardId.PEZ_PAYASO, "pezpayaso.png");
        explicitMap.put(CardId.PEZ_VELA, "pezvela.png");
        explicitMap.put(CardId.PEZ_VOLADOR, "pezvolador.png");
        explicitMap.put(CardId.PIRANA, "piraña.png");
        explicitMap.put(CardId.RED_ENREDADA, "redenredada.png");
        explicitMap.put(CardId.SALMON, "salmon.png");
        explicitMap.put(CardId.SARDINA, "sardina.png");
        explicitMap.put(CardId.TIBURON_BALLENA, "tiburonballena.png");
        explicitMap.put(CardId.TIBURON_BLANCO, "tiburonblanco.png");
        explicitMap.put(CardId.TIBURON_MARTILLO, "tiburonmartillo.png");
        explicitMap.put(CardId.PERCEBES, "percebes.png");
    }

    private void indexAssets() {
        try {
            String[] files = assets.list("img");
            if (files == null) return;
            for (String f : files) {
                assetIndex.add(f);
            }
        } catch (IOException ignored) {
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.ROOT);
        String norm = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
