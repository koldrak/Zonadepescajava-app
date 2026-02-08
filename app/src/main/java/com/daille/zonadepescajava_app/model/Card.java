package com.daille.zonadepescajava_app.model;

public class Card {
    private final CardId id;
    private final int nameResId;
    private final CardType type;
    private final int points;
    private final Condition condition;
    private final int onCatchResId;
    private final int onFailResId;
    private final int bonusResId;

    public Card(CardId id, int nameResId, CardType type, int points,
                Condition condition, int onCatchResId, int onFailResId, int bonusResId) {
        this.id = id;
        this.nameResId = nameResId;
        this.type = type;
        this.points = points;
        this.condition = condition;
        this.onCatchResId = onCatchResId;
        this.onFailResId = onFailResId;
        this.bonusResId = bonusResId;
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

    public String getOnCatch(GameTextProvider provider) {
        return resolveText(provider, onCatchResId);
    }

    public String getOnFail(GameTextProvider provider) {
        return resolveText(provider, onFailResId);
    }

    public String getBonus(GameTextProvider provider) {
        return resolveText(provider, bonusResId);
    }

    private String resolveText(GameTextProvider provider, int resId) {
        if (provider == null || resId == 0) {
            return "";
        }
        return provider.getString(resId);
    }
}
