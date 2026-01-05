package com.daille.zonadepescajava_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ScoreDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_CREATED_AT = "created_at";

    public ScoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SCORES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCORE + " INTEGER NOT NULL, " +
                COLUMN_CREATED_AT + " INTEGER NOT NULL"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
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
}
