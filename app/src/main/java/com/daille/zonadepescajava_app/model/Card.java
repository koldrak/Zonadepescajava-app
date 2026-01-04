package com.daille.zonadepescajava_app.model;

public class Card {
    private final String name;
    private final String type;
    private final int points;

    public Card(String name, String type, int points) {
        this.name = name;
        this.type = type;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPoints() {
        return points;
    }
}
