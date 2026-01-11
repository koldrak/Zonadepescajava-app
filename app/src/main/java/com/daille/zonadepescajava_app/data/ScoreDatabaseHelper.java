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
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String TABLE_CARD_CAPTURES = "card_captures";
    private static final String COLUMN_CARD_ID = "card_id";
    private static final String COLUMN_CAPTURE_COUNT = "capture_count";

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
        seedCaptureCounts(db);
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

    private void ensureCaptureRows() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CARD_CAPTURES + " (" +
                COLUMN_CARD_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CAPTURE_COUNT + " INTEGER NOT NULL DEFAULT 0"
                + ")");
        seedCaptureCounts(db);
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
}
