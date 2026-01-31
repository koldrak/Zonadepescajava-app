package com.daille.zonadepescajava_app.model;

public final class ConditionInfo implements Condition {
    public enum Type {
        SUM_RANGE,
        SUM_EXACT,
        SUM_AT_LEAST,
        SUM_GREATER_THAN,
        SUM_LESS_OR_EQUAL,
        SUM_LESS_THAN,
        DIFFERENCE_AT_LEAST,
        CUSTOM
    }

    private final Type type;
    private final int first;
    private final int second;
    private final Condition evaluator;
    private final Integer descriptionResId;
    private final Object[] descriptionArgs;

    public ConditionInfo(Type type, int first, int second, Condition evaluator) {
        this(type, first, second, evaluator, null, null);
    }

    public ConditionInfo(Type type, int first, int second, Condition evaluator,
                         Integer descriptionResId, Object[] descriptionArgs) {
        this.type = type;
        this.first = first;
        this.second = second;
        this.evaluator = evaluator;
        this.descriptionResId = descriptionResId;
        this.descriptionArgs = descriptionArgs;
    }

    @Override
    public boolean isSatisfied(int slotIndex, GameState game) {
        return evaluator.isSatisfied(slotIndex, game);
    }

    public Type getType() {
        return type;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public Integer getDescriptionResId() {
        return descriptionResId;
    }

    public Object[] getDescriptionArgs() {
        return descriptionArgs;
    }
}
