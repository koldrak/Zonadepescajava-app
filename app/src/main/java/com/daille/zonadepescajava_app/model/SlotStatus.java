package com.daille.zonadepescajava_app.model;

public class SlotStatus {
    public boolean protectedOnce = false;
    public boolean calamarForcedFaceDown = false;
    public int sumConditionShift = 0;
    public int bottleDieBonus = 0;
    public java.util.List<Card> attachedRemoras = new java.util.ArrayList<>();
    public boolean hookPenaltyUsed = false;
    public boolean langostaRecovered = false;
    public int protectedBySlot = -1;        // para vínculo Pez Payaso -> objetivo protegido
    public int lastTriggeredBySlot = -1;    // para vínculo (gatillador -> Almejas)
    public boolean oilSpillLock = false;    // derrame de petróleo activo en esta carta
    public boolean autoHundidoBonus = false; // marca visual para bonus del Auto hundido
    public boolean glassBottlePenalty = false; // marca visual para penalización de Botella de vidrio
    public boolean delfinProtectionUsed = false; // protección del delfín usada

}
