package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.daille.zonadepescajava_app.model.Die;
import com.daille.zonadepescajava_app.model.DieType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Loads PNG faces for each die type/value from the assets folder, mirroring the desktop example.
 */
public class DiceImageResolver {

    private static final String ASSET_DIR = "img/";
    private final AssetManager assets;
    private final Map<String, Bitmap> cache = new HashMap<>();
    private final Random rng = new Random();

    public DiceImageResolver(Context context) {
        this.assets = context.getAssets();
    }

    public Bitmap getFace(Die die) {
        if (die == null) return null;
        return getFace(die.getType(), die.getValue());
    }

    public Bitmap getFace(DieType type, int value) {
        String name = "D" + type.getSides() + value + ".png";
        return load(name);
    }

    public Bitmap getTypePreview(DieType type) {
        return getFace(type, 1);
    }

    public Bitmap randomFace(DieType type) {
        int value = 1 + rng.nextInt(type.getSides());
        return getFace(type, value);
    }

    private Bitmap load(String fileName) {
        if (fileName == null) return null;
        if (cache.containsKey(fileName)) return cache.get(fileName);

        try (InputStream stream = assets.open(ASSET_DIR + fileName)) {
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            cache.put(fileName, bmp);
            return bmp;
        } catch (IOException e) {
            cache.put(fileName, null);
            return null;
        }
    }
}
