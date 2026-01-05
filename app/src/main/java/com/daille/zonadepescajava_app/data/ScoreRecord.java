package com.daille.zonadepescajava_app.data;

public class ScoreRecord {
    private final int score;
    private final long createdAt;

    public ScoreRecord(int score, long createdAt) {
        this.score = score;
        this.createdAt = createdAt;
    }

    public int getScore() {
        return score;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
