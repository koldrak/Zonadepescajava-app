package com.daille.zonadepescajava_app.model;

public final class ShopPrices {
    public static final int D4_COST = 50;
    public static final int D6_COST = 100;
    public static final int D8_COST = 150;
    public static final int D10_COST = 200;
    public static final int D12_COST = 250;
    public static final int D20_COST = 500;

    public static final int PACK_RANDOM_COST = 100;
    public static final int PACK_CRUSTACEO_COST = 120;
    public static final int PACK_SMALL_FISH_COST = 120;
    public static final int PACK_BIG_FISH_COST = 120;
    public static final int PACK_OBJECT_COST = 120;

    private ShopPrices() {
    }

    public static int getDiceCapacityUpgradeCost(int targetCapacity) {
        switch (targetCapacity) {
            case 7:
                return 100;
            case 8:
                return 300;
            case 9:
                return 500;
            case 10:
                return 1000;
            default:
                return Integer.MAX_VALUE;
        }
    }
}
