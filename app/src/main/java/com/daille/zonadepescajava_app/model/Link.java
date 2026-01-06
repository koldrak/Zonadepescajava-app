package com.daille.zonadepescajava_app.model;

public final class Link {
    public final int from;     // 0..8
    public final int to;       // 0..8
    public final LinkType type;

    public Link(int from, int to, LinkType type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }
}
