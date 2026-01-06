package com.daille.zonadepescajava_app.model;

public class SlotStatus {
    public boolean protectedOnce = false;
    public boolean calamarForcedFaceDown = false;
    public int sumConditionShift = 0;
    public java.util.List<Card> attachedRemoras = new java.util.ArrayList<>();
    public boolean hookPenaltyUsed = false;
    public boolean langostaRecovered = false;
    public int protectedBySlot = -1;        // para vínculo Pez Payaso -> objetivo protegido
    public int lastTriggeredBySlot = -1;    // para vínculo (gatillador -> Almejas)

}
