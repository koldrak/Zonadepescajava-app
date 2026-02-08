package com.daille.zonadepescajava_app.model;

public class Card {
    private final CardId id;
    private final int nameResId;
    private final CardType type;
    private final int points;
    private final Condition condition;
    private final String onCatch;
    private final String onFail;
    private final String bonus;

    public Card(CardId id, int nameResId, CardType type, int points,
                Condition condition, String onCatch, String onFail, String bonus) {
        this.id = id;
        this.nameResId = nameResId;
        this.type = type;
        this.points = points;
        this.condition = condition;
        this.onCatch = onCatch;
        this.onFail = onFail;
        this.bonus = bonus;
    }

    public CardId getId() {
        return id;
    }

    public String getName(GameTextProvider provider) {
        if (provider == null || nameResId == 0) {
            return "";
        }
        return provider.getString(nameResId);
    }

    public CardType getType() {
        return type;
    }

    public int getPoints() {
        return points;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getOnCatch() {
        return onCatch;
    }

    public String getOnFail() {
        return onFail;
    }

    public String getBonus() {
        return bonus;
    }
}
