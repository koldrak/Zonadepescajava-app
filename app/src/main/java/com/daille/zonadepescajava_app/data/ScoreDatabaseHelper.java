package com.daille.zonadepescajava_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardId;
import com.daille.zonadepescajava_app.model.GameUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ScoreDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 8;
    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String TABLE_CARD_CAPTURES = "card_captures";
    private static final String COLUMN_CARD_ID = "card_id";
    private static final String COLUMN_CAPTURE_COUNT = "capture_count";
    private static final String TABLE_DICE_INVENTORY = "dice_inventory";
    private static final String COLUMN_DIE_TYPE = "die_type";
    private static final String COLUMN_DIE_COUNT = "die_count";
    private static final String TABLE_CARD_INVENTORY = "card_inventory";
    private static final String COLUMN_OWNED_COUNT = "owned_count";
    private static final String TABLE_WALLET = "wallet";
    private static final String COLUMN_SPENT_POINTS = "spent_points";
    private static final String TABLE_DICE_CAPACITY = "dice_capacity";
    private static final String COLUMN_MAX_DICE = "max_dice";
    private static final int BASE_DICE_CAPACITY = 6;
    private static final String TABLE_AUDIO_SETTINGS = "audio_settings";
    private static final String COLUMN_MUSIC_VOLUME = "music_volume";
    private static final String COLUMN_SFX_VOLUME = "sfx_volume";
    private static final String COLUMN_BUTTON_VOLUME = "button_volume";
    private static final String COLUMN_MUSIC_ENABLED = "music_enabled";
    private static final String COLUMN_SFX_ENABLED = "sfx_enabled";
    private static final String COLUMN_BUTTON_ENABLED = "button_enabled";
    private static final String TABLE_DECK_PRESETS = "deck_presets";
    private static final String COLUMN_DECK_NAME = "deck_name";
    private static final String COLUMN_DECK_CARDS = "deck_cards";
    private static final int DEFAULT_VOLUME = 100;
    private static final int DEFAULT_BUTTON_VOLUME = 25;
    private static final int STARTING_POINTS = 500;

    public ScoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ensureCaptureRows();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SCORES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCORE + " INTEGER NOT NULL, " +
                COLUMN_CREATED_AT + " INTEGER NOT NULL"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_CARD_CAPTURES + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CAPTURE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_DICE_INVENTORY + " (" +
                COLUMN_DIE_TYPE + " TEXT PRIMARY KEY, " +
                COLUMN_DIE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_CARD_INVENTORY + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_OWNED_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_WALLET + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_SPENT_POINTS + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_DICE_CAPACITY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_MAX_DICE + " INTEGER NOT NULL DEFAULT " + BASE_DICE_CAPACITY
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_AUDIO_SETTINGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_MUSIC_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                COLUMN_SFX_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                COLUMN_BUTTON_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_BUTTON_VOLUME + ", " +
                COLUMN_MUSIC_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_SFX_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_BUTTON_ENABLED + " INTEGER NOT NULL DEFAULT 1"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_DECK_PRESETS + " (" +
                COLUMN_DECK_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_DECK_CARDS + " TEXT NOT NULL"
                + ")");
        seedInitialScore(db);
        seedCaptureCounts(db);
        seedDiceInventory(db);
        seedCardInventory(db);
        seedWallet(db);
        seedDiceCapacity(db);
        seedAudioSettings(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_CAPTURES + " (" +
                    COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_CAPTURE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                    + ")");
            seedCaptureCounts(db);
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DICE_INVENTORY + " (" +
                    COLUMN_DIE_TYPE + " TEXT PRIMARY KEY, " +
                    COLUMN_DIE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                    + ")");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WALLET + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                    COLUMN_SPENT_POINTS + " INTEGER NOT NULL DEFAULT 0"
                    + ")");
            seedDiceInventory(db);
            seedWallet(db);
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_INVENTORY + " (" +
                    COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_OWNED_COUNT + " INTEGER NOT NULL DEFAULT 0"
                    + ")");
            seedCardInventory(db);
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DICE_CAPACITY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                    COLUMN_MAX_DICE + " INTEGER NOT NULL DEFAULT " + BASE_DICE_CAPACITY
                    + ")");
            seedDiceCapacity(db);
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUDIO_SETTINGS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                    COLUMN_MUSIC_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                    COLUMN_SFX_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                    COLUMN_MUSIC_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                    COLUMN_SFX_ENABLED + " INTEGER NOT NULL DEFAULT 1"
                    + ")");
            seedAudioSettings(db);
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DECK_PRESETS + " (" +
                    COLUMN_DECK_NAME + " TEXT PRIMARY KEY, " +
                    COLUMN_DECK_CARDS + " TEXT NOT NULL"
                    + ")");
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE " + TABLE_AUDIO_SETTINGS + " ADD COLUMN " +
                    COLUMN_BUTTON_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_BUTTON_VOLUME);
            db.execSQL("ALTER TABLE " + TABLE_AUDIO_SETTINGS + " ADD COLUMN " +
                    COLUMN_BUTTON_ENABLED + " INTEGER NOT NULL DEFAULT 1");
        }
    }

    public void saveScore(int score) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        db.insert(TABLE_SCORES, null, values);
    }

    public List<ScoreRecord> getTopScores(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        List<ScoreRecord> records = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLE_SCORES,
                new String[]{COLUMN_SCORE, COLUMN_CREATED_AT},
                null,
                null,
                null,
                null,
                COLUMN_SCORE + " DESC",
                String.valueOf(limit))) {

            while (cursor.moveToNext()) {
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                records.add(new ScoreRecord(score, createdAt));
            }
        }

        return records;
    }

    public int incrementCaptureCount(CardId cardId) {
        if (cardId == null) return 0;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_ID, cardId.name());
        values.put(COLUMN_CAPTURE_COUNT, 0);
        db.insertWithOnConflict(TABLE_CARD_CAPTURES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + TABLE_CARD_CAPTURES + " SET " + COLUMN_CAPTURE_COUNT + " = "
                + COLUMN_CAPTURE_COUNT + " + 1 WHERE " + COLUMN_CARD_ID + " = ?",
                new Object[]{cardId.name()});
        return getCaptureCount(cardId);
    }

    public int getCaptureCount(CardId cardId) {
        if (cardId == null) return 0;
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.query(TABLE_CARD_CAPTURES,
                new String[]{COLUMN_CAPTURE_COUNT},
                COLUMN_CARD_ID + " = ?",
                new String[]{cardId.name()}, null, null, null)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPTURE_COUNT));
            }
        }
        return count;
    }

    public Map<CardId, Integer> getCaptureCounts() {
        SQLiteDatabase db = getReadableDatabase();
        Map<CardId, Integer> counts = new EnumMap<>(CardId.class);
        for (CardId id : CardId.values()) {
            counts.put(id, 0);
        }

        try (Cursor cursor = db.query(
                TABLE_CARD_CAPTURES,
                new String[]{COLUMN_CARD_ID, COLUMN_CAPTURE_COUNT},
                null,
                null,
                null,
                null,
                COLUMN_CARD_ID + " ASC")) {

            while (cursor.moveToNext()) {
                String cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_ID));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPTURE_COUNT));
                try {
                    counts.put(CardId.valueOf(cardName), count);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return counts;
    }

    public int getTotalScore() {
        SQLiteDatabase db = getReadableDatabase();
        int total = 0;
        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SCORE + ") FROM " + TABLE_SCORES, null)) {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        }
        return total;
    }

    public int getSpentPoints() {
        SQLiteDatabase db = getReadableDatabase();
        int spent = 0;
        try (Cursor cursor = db.query(TABLE_WALLET, new String[]{COLUMN_SPENT_POINTS},
                COLUMN_ID + " = 1", null, null, null, null)) {
            if (cursor.moveToFirst()) {
                spent = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SPENT_POINTS));
            }
        }
        return spent;
    }

    public int getAvailablePoints() {
        int total = getTotalScore();
        int spent = getSpentPoints();
        return Math.max(0, total - spent);
    }

    public int getPurchasedDiceCount(String dieType) {
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        try (Cursor cursor = db.query(TABLE_DICE_INVENTORY, new String[]{COLUMN_DIE_COUNT},
                COLUMN_DIE_TYPE + " = ?", new String[]{dieType}, null, null, null)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIE_COUNT));
            }
        }
        return count;
    }

    public void addPurchasedDice(String dieType, int amount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIE_TYPE, dieType);
        values.put(COLUMN_DIE_COUNT, 0);
        db.insertWithOnConflict(TABLE_DICE_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + TABLE_DICE_INVENTORY + " SET " + COLUMN_DIE_COUNT + " = "
                + COLUMN_DIE_COUNT + " + ? WHERE " + COLUMN_DIE_TYPE + " = ?",
                new Object[]{amount, dieType});
    }

    public void addSpentPoints(int amount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_SPENT_POINTS, 0);
        db.insertWithOnConflict(TABLE_WALLET, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + TABLE_WALLET + " SET " + COLUMN_SPENT_POINTS + " = "
                + COLUMN_SPENT_POINTS + " + ? WHERE " + COLUMN_ID + " = 1",
                new Object[]{amount});
    }

    public void addBonusPoints(int amount) {
        if (amount == 0) {
            return;
        }
        addSpentPoints(-amount);
    }

    public int getDiceCapacity() {
        SQLiteDatabase db = getReadableDatabase();
        int capacity = BASE_DICE_CAPACITY;
        try (Cursor cursor = db.query(TABLE_DICE_CAPACITY, new String[]{COLUMN_MAX_DICE},
                COLUMN_ID + " = 1", null, null, null, null)) {
            if (cursor.moveToFirst()) {
                capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_DICE));
            }
        }
        return capacity;
    }

    public void setDiceCapacity(int capacity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_MAX_DICE, capacity);
        db.insertWithOnConflict(TABLE_DICE_CAPACITY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Map<CardId, Integer> getCardInventoryCounts() {
        SQLiteDatabase db = getReadableDatabase();
        Map<CardId, Integer> counts = new EnumMap<>(CardId.class);
        for (CardId id : CardId.values()) {
            counts.put(id, 0);
        }

        try (Cursor cursor = db.query(
                TABLE_CARD_INVENTORY,
                new String[]{COLUMN_CARD_ID, COLUMN_OWNED_COUNT},
                null,
                null,
                null,
                null,
                COLUMN_CARD_ID + " ASC")) {

            while (cursor.moveToNext()) {
                String cardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_ID));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED_COUNT));
                try {
                    counts.put(CardId.valueOf(cardName), count);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return counts;
    }

    public void addCardCopies(CardId cardId, int amount) {
        if (cardId == null || amount == 0) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_ID, cardId.name());
        values.put(COLUMN_OWNED_COUNT, 0);
        db.insertWithOnConflict(TABLE_CARD_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + TABLE_CARD_INVENTORY + " SET " + COLUMN_OWNED_COUNT + " = "
                + COLUMN_OWNED_COUNT + " + ? WHERE " + COLUMN_CARD_ID + " = ?",
                new Object[]{amount, cardId.name()});
    }

    public void removeCardCopies(CardId cardId, int amount) {
        if (cardId == null || amount <= 0) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_ID, cardId.name());
        values.put(COLUMN_OWNED_COUNT, 0);
        db.insertWithOnConflict(TABLE_CARD_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        int current = 0;
        try (Cursor cursor = db.query(
                TABLE_CARD_INVENTORY,
                new String[]{COLUMN_OWNED_COUNT},
                COLUMN_CARD_ID + " = ?",
                new String[]{cardId.name()},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                current = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED_COUNT));
            }
        }
        int next = Math.max(0, current - amount);
        ContentValues update = new ContentValues();
        update.put(COLUMN_OWNED_COUNT, next);
        db.update(TABLE_CARD_INVENTORY, update, COLUMN_CARD_ID + " = ?", new String[]{cardId.name()});
    }

    public void resetAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SCORES, null, null);
        db.delete(TABLE_CARD_CAPTURES, null, null);
        db.delete(TABLE_DICE_INVENTORY, null, null);
        db.delete(TABLE_CARD_INVENTORY, null, null);
        db.delete(TABLE_WALLET, null, null);
        db.delete(TABLE_DICE_CAPACITY, null, null);
        db.delete(TABLE_AUDIO_SETTINGS, null, null);
        db.delete(TABLE_DECK_PRESETS, null, null);
        seedCaptureCounts(db);
        seedDiceInventory(db);
        seedCardInventory(db);
        seedWallet(db);
        seedDiceCapacity(db);
        seedAudioSettings(db);
    }

    private void ensureCaptureRows() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_CAPTURES + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CAPTURE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedCaptureCounts(db);
        ensureDiceInventoryRows(db);
        ensureWalletRow(db);
        ensureCardInventoryRows(db);
        ensureDiceCapacityRow(db);
        ensureAudioSettingsRow(db);
        ensureDeckPresetTable(db);
    }

    private void seedCaptureCounts(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (CardId id : CardId.values()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CARD_ID, id.name());
                values.put(COLUMN_CAPTURE_COUNT, 0);
                db.insertWithOnConflict(TABLE_CARD_CAPTURES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void ensureDiceInventoryRows(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DICE_INVENTORY + " (" +
                COLUMN_DIE_TYPE + " TEXT PRIMARY KEY, " +
                COLUMN_DIE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedDiceInventory(db);
    }

    private void seedDiceInventory(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            String[] dice = new String[]{"D4", "D6", "D8", "D10", "D12", "D20"};
            for (String die : dice) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_DIE_TYPE, die);
                values.put(COLUMN_DIE_COUNT, 0);
                db.insertWithOnConflict(TABLE_DICE_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void ensureWalletRow(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WALLET + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_SPENT_POINTS + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedWallet(db);
    }

    private void ensureCardInventoryRows(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_INVENTORY + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_OWNED_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedCardInventory(db);
    }

    private void ensureDiceCapacityRow(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DICE_CAPACITY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_MAX_DICE + " INTEGER NOT NULL DEFAULT " + BASE_DICE_CAPACITY
                + ")");
        seedDiceCapacity(db);
    }

    private void ensureAudioSettingsRow(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUDIO_SETTINGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_MUSIC_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                COLUMN_SFX_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_VOLUME + ", " +
                COLUMN_BUTTON_VOLUME + " INTEGER NOT NULL DEFAULT " + DEFAULT_BUTTON_VOLUME + ", " +
                COLUMN_MUSIC_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_SFX_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_BUTTON_ENABLED + " INTEGER NOT NULL DEFAULT 1"
                + ")");
        seedAudioSettings(db);
    }

    private void ensureDeckPresetTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DECK_PRESETS + " (" +
                COLUMN_DECK_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_DECK_CARDS + " TEXT NOT NULL"
                + ")");
    }

    private void seedCardInventory(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (CardId id : CardId.values()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CARD_ID, id.name());
                values.put(COLUMN_OWNED_COUNT, 0);
                db.insertWithOnConflict(TABLE_CARD_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            if (hasAnyOwnedCards(db)) {
                db.setTransactionSuccessful();
                return;
            }
            for (Card card : GameUtils.getRandomStarterCards(new java.util.Random(), 40)) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CARD_ID, card.getId().name());
                values.put(COLUMN_OWNED_COUNT, 0);
                db.insertWithOnConflict(TABLE_CARD_INVENTORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                db.execSQL("UPDATE " + TABLE_CARD_INVENTORY + " SET " + COLUMN_OWNED_COUNT + " = "
                        + COLUMN_OWNED_COUNT + " + 1 WHERE " + COLUMN_CARD_ID + " = ?",
                        new Object[]{card.getId().name()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private boolean hasAnyOwnedCards(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_CARD_INVENTORY +
                        " WHERE " + COLUMN_OWNED_COUNT + " > 0 LIMIT 1",
                null)) {
            return cursor.moveToFirst();
        }
    }

    private void seedInitialScore(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, STARTING_POINTS);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        db.insert(TABLE_SCORES, null, values);
    }

    private void seedWallet(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_SPENT_POINTS, 0);
        db.insertWithOnConflict(TABLE_WALLET, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedDiceCapacity(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_MAX_DICE, BASE_DICE_CAPACITY);
        db.insertWithOnConflict(TABLE_DICE_CAPACITY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedAudioSettings(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_MUSIC_VOLUME, DEFAULT_VOLUME);
        values.put(COLUMN_SFX_VOLUME, DEFAULT_VOLUME);
        values.put(COLUMN_BUTTON_VOLUME, DEFAULT_BUTTON_VOLUME);
        values.put(COLUMN_MUSIC_ENABLED, 1);
        values.put(COLUMN_SFX_ENABLED, 1);
        values.put(COLUMN_BUTTON_ENABLED, 1);
        db.insertWithOnConflict(TABLE_AUDIO_SETTINGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public AudioSettings getAudioSettings() {
        SQLiteDatabase db = getReadableDatabase();
        int musicVolume = DEFAULT_VOLUME;
        int sfxVolume = DEFAULT_VOLUME;
        int buttonVolume = DEFAULT_BUTTON_VOLUME;
        boolean musicEnabled = true;
        boolean sfxEnabled = true;
        boolean buttonEnabled = true;
        try (Cursor cursor = db.query(TABLE_AUDIO_SETTINGS,
                new String[]{COLUMN_MUSIC_VOLUME, COLUMN_SFX_VOLUME, COLUMN_BUTTON_VOLUME,
                        COLUMN_MUSIC_ENABLED, COLUMN_SFX_ENABLED, COLUMN_BUTTON_ENABLED},
                COLUMN_ID + " = 1", null, null, null, null)) {
            if (cursor.moveToFirst()) {
                musicVolume = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MUSIC_VOLUME));
                sfxVolume = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SFX_VOLUME));
                buttonVolume = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUTTON_VOLUME));
                musicEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MUSIC_ENABLED)) == 1;
                sfxEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SFX_ENABLED)) == 1;
                buttonEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUTTON_ENABLED)) == 1;
            }
        }
        return new AudioSettings(musicVolume / 100f, sfxVolume / 100f, buttonVolume / 100f,
                musicEnabled, sfxEnabled, buttonEnabled);
    }

    public void saveAudioSettings(AudioSettings settings) {
        if (settings == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_MUSIC_VOLUME, Math.round(settings.getMusicVolume() * 100f));
        values.put(COLUMN_SFX_VOLUME, Math.round(settings.getSfxVolume() * 100f));
        values.put(COLUMN_BUTTON_VOLUME, Math.round(settings.getButtonVolume() * 100f));
        values.put(COLUMN_MUSIC_ENABLED, settings.isMusicEnabled() ? 1 : 0);
        values.put(COLUMN_SFX_ENABLED, settings.isSfxEnabled() ? 1 : 0);
        values.put(COLUMN_BUTTON_ENABLED, settings.isButtonEnabled() ? 1 : 0);
        db.insertWithOnConflict(TABLE_AUDIO_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveDeckPreset(String name, Map<CardId, Integer> selectionCounts) {
        if (name == null || name.trim().isEmpty() || selectionCounts == null) {
            return;
        }
        String trimmed = name.trim();
        String serialized = serializeDeckSelection(selectionCounts);
        if (serialized.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DECK_NAME, trimmed);
        values.put(COLUMN_DECK_CARDS, serialized);
        db.insertWithOnConflict(TABLE_DECK_PRESETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<String> getDeckPresetNames() {
        SQLiteDatabase db = getReadableDatabase();
        List<String> names = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_DECK_PRESETS,
                new String[]{COLUMN_DECK_NAME},
                null, null, null, null,
                COLUMN_DECK_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DECK_NAME)));
            }
        }
        return names;
    }

    public Map<CardId, Integer> getDeckPreset(String name) {
        Map<CardId, Integer> selection = new EnumMap<>(CardId.class);
        if (name == null || name.trim().isEmpty()) {
            return selection;
        }
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_DECK_PRESETS,
                new String[]{COLUMN_DECK_CARDS},
                COLUMN_DECK_NAME + " = ?",
                new String[]{name.trim()}, null, null, null)) {
            if (cursor.moveToFirst()) {
                String serialized = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DECK_CARDS));
                selection.putAll(deserializeDeckSelection(serialized));
            }
        }
        return selection;
    }

    public boolean deleteDeckPreset(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_DECK_PRESETS, COLUMN_DECK_NAME + " = ?", new String[]{name.trim()}) > 0;
    }

    private String serializeDeckSelection(Map<CardId, Integer> selectionCounts) {
        StringBuilder builder = new StringBuilder();
        for (CardId id : CardId.values()) {
            int count = selectionCounts.getOrDefault(id, 0);
            if (count <= 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(id.name()).append(':').append(count);
        }
        return builder.toString();
    }

    private Map<CardId, Integer> deserializeDeckSelection(String serialized) {
        Map<CardId, Integer> selection = new EnumMap<>(CardId.class);
        if (serialized == null || serialized.trim().isEmpty()) {
            return selection;
        }
        String[] entries = serialized.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) {
                continue;
            }
            try {
                CardId id = CardId.valueOf(parts[0]);
                int count = Integer.parseInt(parts[1]);
                if (count > 0) {
                    selection.put(id, count);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return selection;
    }
}
