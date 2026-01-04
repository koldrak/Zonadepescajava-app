package com.daille.zonadepescajava_app.model;

public class Die {
    private final String type;
    private final int value;

    public Die(String type, int value) {
        this.type = type;
        this.value = value;
    }

    public String getLabel() {
        return type + "=" + value;
    }
}
