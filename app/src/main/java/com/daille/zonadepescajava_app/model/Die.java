package com.daille.zonadepescajava_app.model;

import java.util.Locale;
import java.util.Random;

public class Die {
    private final DieType type;
    private final int value;

    public Die(DieType type, int value) {
        this.type = type;
        this.value = value;
    }

    public static Die roll(DieType type, Random rng) {
        int rolled = 1 + rng.nextInt(type.getSides());
        return new Die(type, rolled);
    }

    public DieType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return String.format(Locale.getDefault(), "%s=%d", type.getLabel(), value);
    }
}
