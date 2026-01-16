package com.daille.zonadepescajava_app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GameUtils {
    private static final boolean ADYACENCIA_INCLUYE_DIAGONALES = true;

    private GameUtils() {}

    public static List<Card> createAllCards() {
        List<Card> cards = new ArrayList<>();

        // ==== Crustáceos ====
        cards.add(new Card(CardId.CANGREJO_ROJO, "Cangrejo Rojo", CardType.CRUSTACEO, 9,
                condSumRange(6, 8), "Mueve 1 dado entre peces adyacentes.", "", ""));

        cards.add(new Card(CardId.CANGREJO_BOXEADOR, "Cangrejo Boxeador", CardType.CRUSTACEO, 8,
                condSumExact(5), "Mueve 2 dados entre cartas adyacentes.", "", ""));

        cards.add(new Card(CardId.JAIBA_AZUL, "Jaiba Azul", CardType.CRUSTACEO, 8,
                (slotIndex, g) -> bothDiceEven(slotIndex, g), "Ajusta el último dado ±1.", "", ""));

        cards.add(new Card(CardId.LANGOSTINO_MANTIS, "Langostino Mantis", CardType.CRUSTACEO, 9,
                GameUtils::oneDieIsDouble, "Relanza un dado perdido y reemplaza uno en la zona de pesca.", "", ""));

        cards.add(new Card(CardId.CAMARON_FANTASMA, "Camarón Fantasma", CardType.CRUSTACEO, 3,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 1, 2), "Mira 2 cartas boca abajo adyacentes.", "", ""));

        cards.add(new Card(CardId.CAMARON_PISTOLA, "Camarón Pistola", CardType.CRUSTACEO, 4,
                condSumRange(7, 9), "El dado colocado puede moverse aleatoriamente.", "", ""));

        cards.add(new Card(CardId.LANGOSTA_ESPINOSA, "Langosta Espinosa", CardType.CRUSTACEO, 9,
                condSumExact(9), "", "", ""));

        cards.add(new Card(CardId.BOGAVANTE, "Bogavante", CardType.CRUSTACEO, 9,
                condSumExact(10), "Recupera 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.KRILL, "Krill", CardType.CRUSTACEO, 3,
                condSumExact(7), "", "", "Otorga +1 por cada crustáceo capturado."));

        cards.add(new Card(CardId.COPEPODO_BRILLANTE, "Copépodo Brillante", CardType.CRUSTACEO, 4,
                condSumRange(2, 3), "", "", "Otorga +1 por cada crustáceo descartado por fallo."));

        cards.add(new Card(CardId.CANGREJO_ERMITANO, "Cangrejo Ermitaño", CardType.CRUSTACEO, 7,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    return s >= (5 + shift) && s <= (9 + shift) &&
                            values.length == 2 &&
                            values[0] % 2 != 0 &&
                            values[1] % 2 != 0;
                }, "", "", ""));

        cards.add(new Card(CardId.CANGREJO_DECORADOR, "Cangrejo Decorador", CardType.CRUSTACEO, 5,
                (slotIndex, g) -> condSumRange(5, 9).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                "Busca un objeto en el mazo y reemplaza una carta boca abajo sin dados.", "", ""));

        cards.add(new Card(CardId.PERCEBES, "Percebes", CardType.CRUSTACEO, 4,
                GameUtils::bothDiceSameValue, "", "", ""));

        cards.add(new Card(CardId.LOCO, "Loco", CardType.CRUSTACEO, 6,
                GameUtils::bothDiceSameValue,
                "Mueve los dados de esta carta a cartas adyacentes ajustando ±1.", "", ""));

        cards.add(new Card(CardId.CENTOLLA, "Centolla", CardType.CRUSTACEO, 3,
                condSumGreaterThan(10), "", "", ""));

        cards.add(new Card(CardId.JAIBA_GIGANTE_DE_COCO, "Jaiba Gigante de Coco", CardType.CRUSTACEO, 2,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 8 && values[1] >= 8;
                }, "Si el dado es < 7 se pierde automáticamente.", "", ""));

        cards.add(new Card(CardId.NAUTILUS, "Nautilus", CardType.CRUSTACEO, 8,
                condSumAtLeast(8), "", "", ""));

        cards.add(new Card(CardId.CANGREJO_HERRADURA, "Cangrejo herradura", CardType.CRUSTACEO, 8,
                condSumAtLeast(14), "Puedes cambiar el valor de 1 dado en la zona de pesca.", "", ""));

        cards.add(new Card(CardId.ALMEJAS, "Almejas", CardType.CRUSTACEO, 5,
                condSumAtLeast(8), "Lanza un dado descartado y colócalo aquí si se activó una habilidad adyacente.", "", ""));

        cards.add(new Card(CardId.OSTRAS, "Ostras", CardType.CRUSTACEO, 9,
                condSumExact(4),
                "Relanza y reposiciona aleatoriamente un dado perdido al activarse una habilidad adyacente.", "", ""));

        cards.add(new Card(CardId.CANGREJO_ARANA, "Cangrejo araña", CardType.CRUSTACEO, 8,
                condSumAtLeast(5), "Devuelve una carta descartada por fallo.", "", ""));

        cards.add(new Card(CardId.CANGREJO_VIOLINISTA, "Cangrejo violinista", CardType.CRUSTACEO, 5,
                condSumRange(13, 15),
                "Elige una carta descartada por fallo y captúrala directamente.", "", ""));

        // ==== Peces pequeños ====
        cards.add(new Card(CardId.SARDINA, "Sardina", CardType.PEZ, 3,
                condSumExact(6), "", "", "Otorga +1 punto por cada pez pequeño."));

        cards.add(new Card(CardId.ATUN, "Atún", CardType.PEZ, 5,
                condSumRange(3, 7), "Relanza el dado colocado.", "", ""));

        cards.add(new Card(CardId.SALMON, "Salmón", CardType.PEZ, 7,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return (a == 4 && b >= 5) || (b == 4 && a >= 5);
                }, "", "", ""));

        cards.add(new Card(CardId.PEZ_PAYASO, "Pez Payaso", CardType.PEZ, 5,
                condSumRange(8, 10), "Protege un pez adyacente.", "", ""));

        cards.add(new Card(CardId.PEZ_GLOBO, "Pez Globo", CardType.PEZ, 8,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    return values[0] == slot.getDice().get(0).getType().getSides() &&
                            values[1] == slot.getDice().get(1).getType().getSides();
                }, "", "", ""));

        cards.add(new Card(CardId.MORENA, "Morena", CardType.PEZ, 7,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return Math.abs(a - b) >= 4;
                }, "", "", ""));

        cards.add(new Card(CardId.CABALLITO_DE_MAR, "Caballito de Mar", CardType.PEZ, 7,
                condSumRange(2, 4), "", "", ""));

        cards.add(new Card(CardId.PEZ_LINTERNA, "Pez Linterna", CardType.PEZ, 5,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 3), "Revela 1 carta boca abajo.", "", ""));

        cards.add(new Card(CardId.KOI, "Koi", CardType.PEZ, 6,
                condSumExact(9), "", "", ""));

        cards.add(new Card(CardId.PEZ_VOLADOR, "Pez Volador", CardType.PEZ, 6,
                GameUtils::oneEvenOneOdd, "", "", ""));

        cards.add(new Card(CardId.PIRANA, "Piraña", CardType.PEZ, 4,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 6), "", "", ""));

        cards.add(new Card(CardId.PEZ_FANTASMA, "Pez Fantasma", CardType.PEZ, 7,
                (slotIndex, g) -> condSumGreaterThan(6).isSatisfied(slotIndex, g) && !hasAdjacentFaceUp(slotIndex, g),
                "Vuelve boca abajo una carta adyacente y recupera su dado.", "", ""));

        cards.add(new Card(CardId.PULPO, "Pulpo", CardType.PEZ, 7,
                condSumLessThan(8), "Si el dado es par, reemplaza por otra carta boca arriba.", "", ""));

        cards.add(new Card(CardId.ARENQUE, "Arenque", CardType.PEZ, 5,
                condSumRange(5, 7), "Busca 2 peces pequeños y colócalos boca abajo.", "", ""));

        cards.add(new Card(CardId.REMORA, "Rémora", CardType.PEZ, 2,
                (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D4) && containsDieType(slotIndex, g, DieType.D6),
                "Si está adyacente a un pez grande se adhiere.", "", ""));

        cards.add(new Card(CardId.CONGRIO, "Congrio", CardType.PEZ, 7,
                condSumExact(5), "", "", "Otorga +1 por cada pez pequeño descartado por fallo."));

        cards.add(new Card(CardId.PEZ_BETTA, "Pez betta", CardType.PEZ, 2,
                condSumRange(10, 11),
                "Solo puedes colocar dados en la línea horizontal donde esté.", "", ""));

        cards.add(new Card(CardId.TRUCHA_ARCOIRIS, "Trucha Arcoíris", CardType.PEZ, 7,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return (a == 5 && b >= 6) || (b == 5 && a >= 6);
                }, "Voltea una carta adyacente; si es pez pequeño, coloca 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.PEZ_PIEDRA, "Pez piedra", CardType.PEZ, 6,
                (slotIndex, g) -> condSumRange(10, 14).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                "La columna donde está no es afectada por la marea.", "", ""));

        cards.add(new Card(CardId.PEZ_LEON, "Pez León", CardType.PEZ, 6,
                (slotIndex, g) -> {
                    if (!condSumAtLeast(16).isSatisfied(slotIndex, g)) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] == slot.getDice().get(i).getType().getSides()) return true;
                    }
                    return false;
                }, "Multiplica por 2 el resultado de un dado.", "", ""));

        cards.add(new Card(CardId.PEZ_DRAGON_AZUL, "Pez Dragón azul", CardType.PEZ, 5,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return Math.abs(a - b) >= 5;
                }, "Regresa los dados con valor ≥ 6 al voltearse.", "", ""));

        cards.add(new Card(CardId.PEZ_PIPA, "Pez pipa", CardType.PEZ, 9,
                condSumExact(14), "Si usas un D12 recuperas 1 dado.", "", ""));

        cards.add(new Card(CardId.PEZ_HACHA_ABISAL, "Pez Hacha Abisal", CardType.PEZ, 2,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 2), "Debes liberar 2 cartas.", "", ""));

        cards.add(new Card(CardId.CARPA_DORADA, "Carpa Dorada", CardType.PEZ, 5,
                differenceAtLeast(3), "La marea solo afecta dados; los que salen se pierden.", "", ""));

        cards.add(new Card(CardId.FLETAN, "Fletan", CardType.PEZ, 3,
                GameUtils::diceConsecutive, "Al activarse la marea puedes ocultar un pez.", "", ""));

        cards.add(new Card(CardId.PEZ_LOBO, "Pez Lobo", CardType.PEZ, 6,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 7),
                "Descarta una carta adyacente boca arriba y reemplázala.", "", ""));

        cards.add(new Card(CardId.PEZ_BORRON, "Pez borrón", CardType.PEZ, 2,
                condSumGreaterThan(10), "Mueve el dado sobre esta carta a otra boca abajo.", "", ""));

        cards.add(new Card(CardId.SEPIA, "Sepia", CardType.PEZ, 9,
                condSumRange(12, 14), "Si el dado es impar, captura una carta del mazo.", "", ""));

        cards.add(new Card(CardId.DAMISELAS, "Damiselas", CardType.PEZ, 7,
                condSumRange(6, 10), "Mira y ordena las 6 primeras cartas del mazo.", "", ""));

        cards.add(new Card(CardId.LAMPREA, "Lamprea", CardType.PEZ, 6,
                (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D12) && containsDieType(slotIndex, g, DieType.D8),
                "Se adhiere a un pez grande boca arriba.", "", ""));

        // ==== Peces grandes ====
        cards.add(new Card(CardId.TIBURON_BLANCO, "Tiburón Blanco", CardType.PEZ_GRANDE, 8,
                condSumGreaterThan(10), "", "", ""));

        cards.add(new Card(CardId.TIBURON_MARTILLO, "Tiburón Martillo", CardType.PEZ_GRANDE, 2,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 5 && values[1] >= 5;
                }, "", "", "Otorga +2 por cada pez grande capturado."));

        cards.add(new Card(CardId.TIBURON_BALLENA, "Tiburón Ballena", CardType.PEZ_GRANDE, 4,
                condSumGreaterThan(11), "", "", "Si tienes 3 crustáceos, otorga +6."));

        cards.add(new Card(CardId.PEZ_VELA, "Pez Vela", CardType.PEZ_GRANDE, 4,
                condSumExact(12), "Relanza el dado y elige resultado.", "", ""));

        cards.add(new Card(CardId.CALAMAR_GIGANTE, "Calamar Gigante", CardType.PEZ_GRANDE, 2,
                (slotIndex, g) -> condSumGreaterThan(10).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D8), "", "", ""));

        cards.add(new Card(CardId.MANTA_GIGANTE, "Manta Gigante", CardType.PEZ_GRANDE, 9,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (9 + shift) || s > (11 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 4 && values[1] != 4;
                }, "", "", ""));

        cards.add(new Card(CardId.BALLENA_AZUL, "Ballena azul", CardType.PEZ_GRANDE, 9,
                condSumRange(11, 13), "", "", ""));

        cards.add(new Card(CardId.MERO_GIGANTE, "Mero gigante", CardType.PEZ_GRANDE, 7,
                (slotIndex, g) -> condSumAtLeast(10).isSatisfied(slotIndex, g) && diceDistinct(slotIndex, g),
                "Voltea todas las cartas adyacentes boca abajo.", "", ""));

        cards.add(new Card(CardId.PEZ_LUNA, "Pez luna", CardType.PEZ_GRANDE, 3,
                condSumAtLeast(13), "Si sale del tablero por marea, libera tu captura de mayor valor.", "", ""));

        cards.add(new Card(CardId.TIBURON_TIGRE, "Tiburón tigre", CardType.PEZ_GRANDE, 8,
                condSumGreaterThan(13), "Elimina una carta adyacente boca arriba.", "", ""));

        cards.add(new Card(CardId.DELFIN, "Delfín", CardType.PEZ_GRANDE, 5,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 6 && values[1] >= 6;
                }, "El próximo fallo adyacente no descarta la carta.", "", ""));

        cards.add(new Card(CardId.TIBURON_PEREGRINO, "Tiburón Peregrino", CardType.PEZ_GRANDE, 4,
                condSumGreaterThan(9), "Revela 5 cartas, elige 1 arriba y 1 al fondo.", "", ""));

        cards.add(new Card(CardId.NARVAL, "Narval", CardType.PEZ_GRANDE, 4,
                condSumExact(15),
                "Regresa las cartas adyacentes boca arriba al mazo y recupera sus dados.", "", ""));

        cards.add(new Card(CardId.ORCA, "Orca", CardType.PEZ_GRANDE, 10,
                (slotIndex, g) -> condSumGreaterThan(19).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D12),
                "Voltea boca abajo las cartas adyacentes y recupera sus dados.", "", ""));

        cards.add(new Card(CardId.ANGUILA_ELECTRICA, "Anguila Eléctrica", CardType.PEZ_GRANDE, 7,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (10 + shift) || s > (12 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 5 && values[1] != 5;
                }, "Relanza dados adyacentes; si alguno es máximo recupera un dado.", "", ""));

        cards.add(new Card(CardId.CACHALOTE, "Cachalote", CardType.PEZ_GRANDE, 7,
                condSumRange(12, 15), "Reposiciona dados en cartas boca arriba sin dados.", "", ""));

        cards.add(new Card(CardId.ESTURION, "Esturión", CardType.PEZ_GRANDE, 6,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && bothDiceSameValue(slotIndex, g),
                "Lanza todos los dados de tu reserva menos 1 y colócalos en la zona de pesca.", "", ""));

        cards.add(new Card(CardId.BALLENA_JOROBADA, "Ballena jorobada", CardType.PEZ_GRANDE, 8,
                condSumRange(1, 2), "Puedes elegir la dirección de la marea.", "", ""));

        // ==== Objetos ====
        cards.add(new Card(CardId.BOTA_VIEJA, "Bota Vieja", CardType.OBJETO, 1,
                condSumLessOrEqual(6), "Aplica −1 a adyacentes.", "", ""));

        cards.add(new Card(CardId.BOTELLA_PLASTICO, "Botella de Plástico", CardType.OBJETO, 3,
                condSumExact(8), "Elige 1 pez pequeño adyacente boca arriba; sus dados obtienen +3.", "", ""));

        cards.add(new Card(CardId.RED_ENREDADA, "Red Enredada", CardType.OBJETO, 9,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 1 && values[1] == 1;
                }, "Captura además una carta adyacente boca abajo.", "", ""));

        cards.add(new Card(CardId.LATA_OXIDADA, "Lata Oxidada", CardType.OBJETO, 8,
                condSumExact(8), "Recupera 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.LIMPIADOR_MARINO, "Limpiador Marino", CardType.OBJETO, 2,
                condSumGreaterThan(8), "", "", "Otorga +2 por cada objeto capturado."));

        cards.add(new Card(CardId.ANZUELO_ROTO, "Anzuelo Roto", CardType.OBJETO, 3,
                GameUtils::bothDiceSameValue, "Si fallas con 2 dados pierdes 2.", "", ""));

        cards.add(new Card(CardId.CORRIENTES_PROFUNDAS, "Corrientes profundas", CardType.OBJETO, 2,
                differenceAtLeast(3),
                "Activa la marea hacia la derecha si es par, a la izquierda si es impar.", "", ""));

        cards.add(new Card(CardId.AUTO_HUNDIDO, "Auto hundido", CardType.OBJETO, 4,
                condSumExact(13), "Aplica +1 a adyacentes.", "", ""));

        cards.add(new Card(CardId.BOTELLA_DE_VIDRIO, "Botella de vidrio", CardType.OBJETO, 3,
                condSumExact(8), "Elige 1 carta adyacente boca arriba; sus dados obtienen −3.", "", ""));

        cards.add(new Card(CardId.RED_DE_ARRASTRE, "Red de arrastre", CardType.OBJETO, 6,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 7 && values[1] == 7;
                }, "Captura 2 cartas boca abajo y libera 1 carta.", "", ""));

        cards.add(new Card(CardId.MICRO_PLASTICOS, "Micro plásticos", CardType.OBJETO, 4,
                condSumExact(10), "Invierte todas las cartas de la zona de pesca.", "", ""));

        cards.add(new Card(CardId.FOSA_ABISAL, "Fosa abisal", CardType.OBJETO, 4,
                condSumGreaterThan(10), "", "", "Otorga +1 por cada objeto fallado."));

        cards.add(new Card(CardId.DERRAME_PETROLEO, "Derrame de petróleo", CardType.OBJETO, 3,
                condSumGreaterThan(11),
                "Voltea todas las cartas boca arriba; no se pueden voltear hasta que salga.", "", ""));

        cards.add(new Card(CardId.BARCO_PESQUERO, "Barco pesquero", CardType.OBJETO, 2,
                GameUtils::diceConsecutive,
                "Si sale un dado igual, elimina una carta boca arriba adyacente.", "", ""));

        return cards;
    }

    public static List<Card> buildDeck(Random rng, Map<CardId, Integer> captureCounts) {
        List<Card> deck = new ArrayList<>();
        if (captureCounts == null) {
            deck.addAll(createAllCards());
        } else {
            for (Card card : createAllCards()) {
                int copies = captureCounts.getOrDefault(card.getId(), 0);
                for (int i = 0; i < copies; i++) {
                    deck.add(card);
                }
            }
        }
        Collections.shuffle(deck, rng);
        return deck;
    }

    public static List<Card> buildDeckFromSelection(Random rng, List<Card> selection) {
        List<Card> deck = new ArrayList<>();
        if (selection != null) {
            deck.addAll(selection);
        }
        Collections.shuffle(deck, rng);
        return deck;
    }

    public static List<Card> getSelectableCards(Map<CardId, Integer> captureCounts) {
        List<Card> cards = new ArrayList<>(createAllCards());
        if (captureCounts != null) {
            cards.removeIf(card -> captureCounts.getOrDefault(card.getId(), 0) <= 0);
        }
        return cards;
    }

    public static List<Card> buildRandomDeckSelection(Random rng, List<Card> availableCards,
                                                     Map<CardId, Integer> ownedCounts,
                                                     int minPoints, int maxPoints, int minCards) {
        if (availableCards == null || availableCards.isEmpty()) {
            return new ArrayList<>();
        }
        List<Card> cards = new ArrayList<>(availableCards);
        for (int attempt = 0; attempt < 2000; attempt++) {
            Collections.shuffle(cards, rng);
            List<Card> selection = new ArrayList<>();
            int total = 0;
            for (Card card : cards) {
                int maxCopies = ownedCounts != null ? ownedCounts.getOrDefault(card.getId(), 0) : 2;
                if (maxCopies <= 0) {
                    continue;
                }
                int copies = rng.nextInt(maxCopies + 1);
                for (int i = 0; i < copies; i++) {
                    int nextTotal = total + card.getPoints();
                    if (nextTotal > maxPoints) {
                        break;
                    }
                    selection.add(card);
                    total = nextTotal;
                }
            }
            if (selection.size() >= minCards && total >= minPoints && total <= maxPoints) {
                return selection;
            }
        }
        List<Card> fallback = new ArrayList<>();
        int total = 0;
        Collections.shuffle(cards, rng);
        for (Card card : cards) {
            int maxCopies = ownedCounts != null ? ownedCounts.getOrDefault(card.getId(), 0) : 2;
            for (int i = 0; i < maxCopies; i++) {
                int nextTotal = total + card.getPoints();
                if (nextTotal > maxPoints) {
                    break;
                }
                fallback.add(card);
                total = nextTotal;
                if (fallback.size() >= minCards && total >= minPoints) {
                    return fallback;
                }
            }
        }
        return fallback;
    }

    public static List<Card> buildDeck(Random rng) {
        return buildDeck(rng, null);
    }

    public static List<Card> getStarterCards() {
        List<Card> starters = new ArrayList<>(createAllCards());
        List<CardId> locked = getLockedCardIds();
        starters.removeIf(card -> locked.contains(card.getId()));
        return starters;
    }

    public static List<Card> getCardsByType(CardType type) {
        List<Card> cards = new ArrayList<>();
        for (Card card : createAllCards()) {
            if (card.getType() == type) {
                cards.add(card);
            }
        }
        return cards;
    }

    private static List<CardId> getLockedCardIds() {
        List<CardId> locked = new ArrayList<>();
        locked.add(CardId.CANGREJO_BOXEADOR);
        locked.add(CardId.LANGOSTINO_MANTIS);
        locked.add(CardId.CAMARON_PISTOLA);
        locked.add(CardId.BOGAVANTE);
        locked.add(CardId.COPEPODO_BRILLANTE);
        locked.add(CardId.CANGREJO_DECORADOR);
        locked.add(CardId.LOCO);
        locked.add(CardId.JAIBA_GIGANTE_DE_COCO);
        locked.add(CardId.CANGREJO_HERRADURA);
        locked.add(CardId.OSTRAS);
        locked.add(CardId.CANGREJO_VIOLINISTA);
        locked.add(CardId.CONGRIO);
        locked.add(CardId.PEZ_BETTA);
        locked.add(CardId.TRUCHA_ARCOIRIS);
        locked.add(CardId.PEZ_PIEDRA);
        locked.add(CardId.PEZ_LEON);
        locked.add(CardId.PEZ_DRAGON_AZUL);
        locked.add(CardId.PEZ_PIPA);
        locked.add(CardId.PEZ_HACHA_ABISAL);
        locked.add(CardId.CARPA_DORADA);
        locked.add(CardId.FLETAN);
        locked.add(CardId.PEZ_LOBO);
        locked.add(CardId.PEZ_BORRON);
        locked.add(CardId.SEPIA);
        locked.add(CardId.DAMISELAS);
        locked.add(CardId.LAMPREA);
        locked.add(CardId.TIBURON_TIGRE);
        locked.add(CardId.DELFIN);
        locked.add(CardId.TIBURON_PEREGRINO);
        locked.add(CardId.NARVAL);
        locked.add(CardId.ORCA);
        locked.add(CardId.ANGUILA_ELECTRICA);
        locked.add(CardId.CACHALOTE);
        locked.add(CardId.ESTURION);
        locked.add(CardId.BALLENA_JOROBADA);
        locked.add(CardId.AUTO_HUNDIDO);
        locked.add(CardId.BOTELLA_DE_VIDRIO);
        locked.add(CardId.RED_DE_ARRASTRE);
        locked.add(CardId.MICRO_PLASTICOS);
        locked.add(CardId.FOSA_ABISAL);
        locked.add(CardId.DERRAME_PETROLEO);
        locked.add(CardId.BARCO_PESQUERO);
        return locked;
    }

    public static int sumWithModifiers(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        int sum = 0;
        for (Die d : s.getDice()) sum += d.getValue();

        int penalty = countAdjacentCards(slotIndex, g, CardId.BOTA_VIEJA);
        int bonus = countAdjacentCards(slotIndex, g, CardId.AUTO_HUNDIDO);
        sum -= penalty;
        sum += bonus;

        return sum;
    }

    private static int[] adjustedDiceValues(int slotIndex, GameState g) {
        BoardSlot slot = g.getBoard()[slotIndex];
        int size = slot.getDice().size();
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = slot.getDice().get(i).getValue();
        }
        if (size == 0) {
            return values;
        }
        int penalty = countAdjacentCards(slotIndex, g, CardId.BOTA_VIEJA);
        int bonus = countAdjacentCards(slotIndex, g, CardId.AUTO_HUNDIDO);
        applyAdjustedShift(values, slot.getDice(), -penalty);
        applyAdjustedShift(values, slot.getDice(), bonus);
        return values;
    }

    private static void applyAdjustedShift(int[] values, List<Die> dice, int shift) {
        if (shift == 0 || values.length == 0) {
            return;
        }
        int index = 0;
        if (values.length > 1 && values[1] > values[0]) {
            index = 1;
        }
        if (shift > 0) {
            int sides = dice.get(index).getType().getSides();
            values[index] = Math.min(sides, values[index] + shift);
        } else {
            values[index] = Math.max(1, values[index] + shift);
        }
    }

    private static int countAdjacentCards(int slotIndex, GameState g, CardId cardId) {
        int count = 0;
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = ADYACENCIA_INCLUYE_DIAGONALES
                ? new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
                : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            BoardSlot adj = g.getBoard()[rr * 3 + cc];
            if (adj.getCard() != null && adj.isFaceUp() && adj.getCard().getId() == cardId) {
                count++;
            }
        }
        return count;
    }

    public static boolean bothDiceSameValue(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return values[0] == values[1];
    }

    public static boolean bothDiceEven(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return values[0] % 2 == 0 && values[1] % 2 == 0;
    }

    public static boolean oneEvenOneOdd(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return (values[0] % 2) != (values[1] % 2);
    }

    public static boolean oneDieIsDouble(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        int a = values[0];
        int b = values[1];
        return a == b * 2 || b == a * 2;
    }

    public static boolean atLeastOneIs(int slotIndex, GameState g, int... vals) {
        int[] values = adjustedDiceValues(slotIndex, g);
        for (int value : values) {
            for (int v : vals) if (value == v) return true;
        }
        return false;
    }

    public static boolean containsDieType(int slotIndex, GameState g, DieType t) {
        for (Die d : g.getBoard()[slotIndex].getDice()) if (d.getType() == t) return true;
        return false;
    }

    public static boolean diceDistinct(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return values[0] != values[1];
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
            int[] values = adjustedDiceValues(slotIndex, state);
            if (values.length != 2) return false;
            return Math.abs(values[0] - values[1]) >= diff;
        };
    }

    public static boolean diceConsecutive(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return Math.abs(values[0] - values[1]) == 1;
    }
}
