package com.daille.zonadepescajava_app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GameUtils {
    private static final boolean ADYACENCIA_INCLUYE_DIAGONALES = true;

    private GameUtils() {}

    public static List<Card> createAllCards() {
        List<Card> cards = new ArrayList<>();

        // ==== Crustáceos ====
        cards.add(new Card(CardId.CANGREJO_ROJO, "Cangrejo Rojo", CardType.CRUSTACEO, 8,
                condSumRange(6, 8), "Mueve 1 dado entre peces adyacentes.", "", ""));

        cards.add(new Card(CardId.JAIBA_AZUL, "Jaiba Azul", CardType.CRUSTACEO, 6,
                (slotIndex, g) -> bothDiceEven(slotIndex, g), "Ajusta el último dado ±1.", "", ""));

        cards.add(new Card(CardId.CAMARON_FANTASMA, "Camarón Fantasma", CardType.CRUSTACEO, 2,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 1, 2), "Mira 2 cartas boca abajo adyacentes.", "", ""));

        cards.add(new Card(CardId.LANGOSTA_ESPINOSA, "Langosta Espinosa", CardType.CRUSTACEO, 9,
                condSumExact(9), "", "", ""));

        cards.add(new Card(CardId.KRILL, "Krill", CardType.CRUSTACEO, 3,
                condSumExact(7), "", "", "Otorga +1 por cada crustáceo capturado."));

        cards.add(new Card(CardId.CANGREJO_ERMITANO, "Cangrejo Ermitaño", CardType.CRUSTACEO, 6,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    return s >= (5 + shift) && s <= (9 + shift) &&
                            g.getBoard()[slotIndex].getDice().size() == 2 &&
                            g.getBoard()[slotIndex].getDice().get(0).getValue() % 2 != 0 &&
                            g.getBoard()[slotIndex].getDice().get(1).getValue() % 2 != 0;
                }, "", "", ""));

        cards.add(new Card(CardId.PERCEBES, "Percebes", CardType.CRUSTACEO, 5,
                GameUtils::bothDiceSameValue, "", "", ""));

        cards.add(new Card(CardId.CENTOLLA, "Centolla", CardType.CRUSTACEO, 4,
                condSumGreaterThan(10), "", "", ""));

        cards.add(new Card(CardId.NAUTILUS, "Nautilus", CardType.CRUSTACEO, 7,
                condSumAtLeast(8), "", "", ""));

        cards.add(new Card(CardId.ALMEJAS, "Almejas", CardType.CRUSTACEO, 4,
                condSumAtLeast(8), "Lanza un dado descartado y colócalo aquí si se activó una habilidad adyacente.", "", ""));

        cards.add(new Card(CardId.CANGREJO_ARANA, "Cangrejo araña", CardType.CRUSTACEO, 5,
                condSumAtLeast(5), "Devuelve una carta descartada por fallo.", "", ""));

        // ==== Peces pequeños ====
        cards.add(new Card(CardId.SARDINA, "Sardina", CardType.PEZ, 3,
                condSumExact(6), "", "", "Otorga +1 punto por cada pez pequeño."));

        cards.add(new Card(CardId.ATUN, "Atún", CardType.PEZ, 4,
                condSumRange(3, 7), "Relanza el dado colocado.", "", ""));

        cards.add(new Card(CardId.SALMON, "Salmón", CardType.PEZ, 5,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
                    return (a == 4 && b >= 5) || (b == 4 && a >= 5);
                }, "", "", ""));

        cards.add(new Card(CardId.PEZ_PAYASO, "Pez Payaso", CardType.PEZ, 5,
                condSumRange(8, 10), "Protege un pez adyacente.", "", ""));

        cards.add(new Card(CardId.PEZ_GLOBO, "Pez Globo", CardType.PEZ, 7,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() == g.getBoard()[slotIndex].getDice().get(0).getType().getSides() &&
                            g.getBoard()[slotIndex].getDice().get(1).getValue() == g.getBoard()[slotIndex].getDice().get(1).getType().getSides();
                }, "", "", ""));

        cards.add(new Card(CardId.MORENA, "Morena", CardType.PEZ, 6,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
                    return Math.abs(a - b) >= 4;
                }, "", "", ""));

        cards.add(new Card(CardId.CABALLITO_DE_MAR, "Caballito de Mar", CardType.PEZ, 7,
                condSumRange(2, 4), "", "", ""));

        cards.add(new Card(CardId.PEZ_LINTERNA, "Pez Linterna", CardType.PEZ, 2,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 3), "Revela 1 carta boca abajo.", "", ""));

        cards.add(new Card(CardId.KOI, "Koi", CardType.PEZ, 9,
                condSumExact(9), "", "", ""));

        cards.add(new Card(CardId.PEZ_VOLADOR, "Pez Volador", CardType.PEZ, 4,
                GameUtils::oneEvenOneOdd, "", "", ""));

        cards.add(new Card(CardId.PIRANA, "Piraña", CardType.PEZ, 4,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 6), "", "", ""));

        cards.add(new Card(CardId.PEZ_FANTASMA, "Pez Fantasma", CardType.PEZ, 6,
                (slotIndex, g) -> condSumGreaterThan(6).isSatisfied(slotIndex, g) && !hasAdjacentFaceUp(slotIndex, g),
                "Vuelve boca abajo una carta adyacente y recupera su dado.", "", ""));

        cards.add(new Card(CardId.PULPO, "Pulpo", CardType.PEZ, 6,
                condSumLessThan(8), "Si el dado es par, reemplaza por otra carta boca arriba.", "", ""));

        cards.add(new Card(CardId.ARENQUE, "Arenque", CardType.PEZ, 5,
                condSumRange(5, 7), "Busca 2 peces pequeños y colócalos boca abajo.", "", ""));


        cards.add(new Card(CardId.REMORA, "Rémora", CardType.PEZ, 1,
                (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D4) && containsDieType(slotIndex, g, DieType.D6),
                "Si está adyacente a un pez grande se adhiere.", "", ""));

        // ==== Peces grandes ====
        cards.add(new Card(CardId.TIBURON_BLANCO, "Tiburón Blanco", CardType.PEZ_GRANDE, 8,
                condSumGreaterThan(10), "", "", ""));

        cards.add(new Card(CardId.TIBURON_MARTILLO, "Tiburón Martillo", CardType.PEZ_GRANDE, 1,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() >= 5 && g.getBoard()[slotIndex].getDice().get(1).getValue() >= 5;
                }, "", "", "Otorga +2 por cada pez grande capturado."));

        cards.add(new Card(CardId.TIBURON_BALLENA, "Tiburón Ballena", CardType.PEZ_GRANDE, 5,
                condSumGreaterThan(11), "", "", "Si tienes 3 crustáceos, otorga +6."));

        cards.add(new Card(CardId.PEZ_VELA, "Pez Vela", CardType.PEZ_GRANDE, 5,
                condSumExact(12), "Relanza el dado y elige resultado.", "", ""));

        cards.add(new Card(CardId.CALAMAR_GIGANTE, "Calamar Gigante", CardType.PEZ_GRANDE, 2,
                (slotIndex, g) -> condSumGreaterThan(10).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D8), "", "", ""));

        cards.add(new Card(CardId.MANTA_GIGANTE, "Manta Gigante", CardType.PEZ_GRANDE, 9,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (9 + shift) || s > (11 + shift)) return false;
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() != 4 && g.getBoard()[slotIndex].getDice().get(1).getValue() != 4;
                }, "", "", ""));

        cards.add(new Card(CardId.BALLENA_AZUL, "Ballena azul", CardType.PEZ_GRANDE, 8,
                condSumRange(11, 13), "", "", ""));

        cards.add(new Card(CardId.MERO_GIGANTE, "Mero gigante", CardType.PEZ_GRANDE, 7,
                (slotIndex, g) -> condSumAtLeast(10).isSatisfied(slotIndex, g) && diceDistinct(slotIndex, g),
                "Voltea todas las cartas adyacentes boca abajo.", "", ""));

        cards.add(new Card(CardId.PEZ_LUNA, "Pez luna", CardType.PEZ_GRANDE, 4,
                condSumAtLeast(13), "Si sale del tablero por marea, libera tu captura de mayor valor.", "", ""));

        // ==== Objetos ====
        cards.add(new Card(CardId.BOTA_VIEJA, "Bota Vieja", CardType.OBJETO, 1,
                condSumLessOrEqual(6), "Aplica −1 a adyacentes.", "", ""));

        cards.add(new Card(CardId.BOTELLA_PLASTICO, "Botella de Plástico", CardType.OBJETO, 3,
                condSumExact(8), "Elige 1 pez pequeño adyacente boca arriba; sus dados obtienen +3.", "", ""));

        cards.add(new Card(CardId.RED_ENREDADA, "Red Enredada", CardType.OBJETO, 9,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() == 1 && g.getBoard()[slotIndex].getDice().get(1).getValue() == 1;
                }, "Captura además una carta adyacente boca abajo.", "", ""));

        cards.add(new Card(CardId.LATA_OXIDADA, "Lata Oxidada", CardType.OBJETO, 8,
                condSumExact(8), "Recupera 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.LIMPIADOR_MARINO, "Limpiador Marino", CardType.OBJETO, 1,
                condSumGreaterThan(8), "", "", "Otorga +2 por cada objeto capturado."));

        cards.add(new Card(CardId.ANZUELO_ROTO, "Anzuelo Roto", CardType.OBJETO, 3,
                GameUtils::bothDiceSameValue, "Si fallas con 2 dados pierdes 2.", "", ""));

        cards.add(new Card(CardId.CORRIENTES_PROFUNDAS, "Corrientes profundas", CardType.OBJETO, 2,
                differenceAtLeast(3),
                "Activa la marea hacia la derecha si es par, a la izquierda si es impar.", "", ""));

        return cards;
    }

    public static List<Card> buildDeck(Random rng) {
        List<Card> deck = new ArrayList<>(createAllCards());
        Collections.shuffle(deck, rng);
        return deck;
    }

    public static int sumWithModifiers(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        int sum = 0;
        for (Die d : s.getDice()) sum += d.getValue();
        sum += s.getStatus().bottleDieBonus * s.getDice().size();

        int penalty = 0;
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = ADYACENCIA_INCLUYE_DIAGONALES
                ? new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
                : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            int idx = rr * 3 + cc;
            BoardSlot adj = g.getBoard()[idx];
            if (adj.getCard() != null && adj.getCard().getId() == CardId.BOTA_VIEJA && adj.isFaceUp()) {
                penalty += 1;
            }
        }
        sum -= penalty;

        return sum;
    }

    public static boolean bothDiceSameValue(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        return s.getDice().get(0).getValue() == s.getDice().get(1).getValue();
    }

    public static boolean bothDiceEven(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        return s.getDice().get(0).getValue() % 2 == 0 && s.getDice().get(1).getValue() % 2 == 0;
    }

    public static boolean oneEvenOneOdd(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        return (s.getDice().get(0).getValue() % 2) != (s.getDice().get(1).getValue() % 2);
    }

    public static boolean atLeastOneIs(int slotIndex, GameState g, int... vals) {
        BoardSlot s = g.getBoard()[slotIndex];
        for (Die d : s.getDice()) {
            for (int v : vals) if (d.getValue() == v) return true;
        }
        return false;
    }

    public static boolean containsDieType(int slotIndex, GameState g, DieType t) {
        for (Die d : g.getBoard()[slotIndex].getDice()) if (d.getType() == t) return true;
        return false;
    }

    public static boolean diceDistinct(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        return s.getDice().get(0).getValue() != s.getDice().get(1).getValue();
    }

    public static boolean hasAdjacentFaceUp(int slotIndex, GameState g) {
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = ADYACENCIA_INCLUYE_DIAGONALES
                ? new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
                : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            BoardSlot adj = g.getBoard()[rr * 3 + cc];
            if (adj.getCard() != null && adj.isFaceUp()) return true;
        }
        return false;
    }

    public static Condition condSumRange(int min, int max) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s >= (min + shift) && s <= (max + shift);
        };
    }

    public static Condition condSumExact(int value) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s == (value + shift);
        };
    }

    public static Condition condSumAtLeast(int min) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s >= (min + shift);
        };
    }

    public static Condition condSumGreaterThan(int v) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s > (v + shift);
        };
    }

    public static Condition condSumLessOrEqual(int v) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s <= (v + shift);
        };
    }

    public static Condition condSumLessThan(int value) {
        return (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s < (value + shift);
        };
    }

    public static Condition differenceAtLeast(int diff) {
        return (slotIndex, state) -> {
            BoardSlot s = state.getBoard()[slotIndex];
            if (s.getDice().size() != 2) return false;
            return Math.abs(s.getDice().get(0).getValue() - s.getDice().get(1).getValue()) >= diff;
        };
    }
}
