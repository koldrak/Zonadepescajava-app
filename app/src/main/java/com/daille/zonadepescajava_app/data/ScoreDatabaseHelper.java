package com.daille.zonadepescajava_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.daille.zonadepescajava_app.model.CardId;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ScoreDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 3;
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
    private static final String TABLE_WALLET = "wallet";
    private static final String COLUMN_SPENT_POINTS = "spent_points";

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
        db.execSQL("CREATE TABLE " + TABLE_WALLET + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY CHECK (" + COLUMN_ID + " = 1), " +
                COLUMN_SPENT_POINTS + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedCaptureCounts(db);
        seedDiceInventory(db);
        seedWallet(db);
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

    public void incrementCaptureCount(CardId cardId) {
        if (cardId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_ID, cardId.name());
        values.put(COLUMN_CAPTURE_COUNT, 0);
        db.insertWithOnConflict(TABLE_CARD_CAPTURES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE " + TABLE_CARD_CAPTURES + " SET " + COLUMN_CAPTURE_COUNT + " = "
                + COLUMN_CAPTURE_COUNT + " + 1 WHERE " + COLUMN_CARD_ID + " = ?",
                new Object[]{cardId.name()});
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

    private void ensureCaptureRows() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_CAPTURES + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CAPTURE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedCaptureCounts(db);
        ensureDiceInventoryRows(db);
        ensureWalletRow(db);
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
            String[] dice = new String[]{"D4", "D6", "D8", "D12"};
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

    private void seedWallet(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 1);
        values.put(COLUMN_SPENT_POINTS, 0);
        db.insertWithOnConflict(TABLE_WALLET, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
