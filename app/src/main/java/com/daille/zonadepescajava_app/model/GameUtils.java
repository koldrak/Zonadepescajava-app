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
                    return s >= (5 + shift) && s <= (9 + shift) &&
                            g.getBoard()[slotIndex].getDice().size() == 2 &&
                            g.getBoard()[slotIndex].getDice().get(0).getValue() % 2 != 0 &&
                            g.getBoard()[slotIndex].getDice().get(1).getValue() % 2 != 0;
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
                    BoardSlot slot = g.getBoard()[slotIndex];
                    if (slot.getDice().size() != 2) return false;
                    return slot.getDice().get(0).getValue() >= 8
                            && slot.getDice().get(1).getValue() >= 8;
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
                    return (a == 4 && b >= 5) || (b == 4 && a >= 5);
                }, "", "", ""));

        cards.add(new Card(CardId.PEZ_PAYASO, "Pez Payaso", CardType.PEZ, 5,
                condSumRange(8, 10), "Protege un pez adyacente.", "", ""));

        cards.add(new Card(CardId.PEZ_GLOBO, "Pez Globo", CardType.PEZ, 8,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() == g.getBoard()[slotIndex].getDice().get(0).getType().getSides() &&
                            g.getBoard()[slotIndex].getDice().get(1).getValue() == g.getBoard()[slotIndex].getDice().get(1).getType().getSides();
                }, "", "", ""));

        cards.add(new Card(CardId.MORENA, "Morena", CardType.PEZ, 7,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
                    return (a == 5 && b >= 6) || (b == 5 && a >= 6);
                }, "Voltea una carta adyacente; si es pez pequeño, coloca 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.PEZ_PIEDRA, "Pez piedra", CardType.PEZ, 6,
                (slotIndex, g) -> condSumRange(10, 14).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                "La columna donde está no es afectada por la marea.", "", ""));

        cards.add(new Card(CardId.PEZ_LEON, "Pez León", CardType.PEZ, 6,
                (slotIndex, g) -> {
                    if (!condSumAtLeast(16).isSatisfied(slotIndex, g)) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    if (slot.getDice().size() != 2) return false;
                    for (Die d : slot.getDice()) {
                        if (d.getValue() == d.getType().getSides()) return true;
                    }
                    return false;
                }, "Multiplica por 2 el resultado de un dado.", "", ""));

        cards.add(new Card(CardId.PEZ_DRAGON_AZUL, "Pez Dragón azul", CardType.PEZ, 5,
                (slotIndex, g) -> {
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    int a = g.getBoard()[slotIndex].getDice().get(0).getValue();
                    int b = g.getBoard()[slotIndex].getDice().get(1).getValue();
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() >= 5 && g.getBoard()[slotIndex].getDice().get(1).getValue() >= 5;
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() != 4 && g.getBoard()[slotIndex].getDice().get(1).getValue() != 4;
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() >= 6
                            && g.getBoard()[slotIndex].getDice().get(1).getValue() >= 6;
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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() != 5
                            && g.getBoard()[slotIndex].getDice().get(1).getValue() != 5;
                }, "Relanza dados adyacentes; si alguno es máximo recupera un dado.", "", ""));

        cards.add(new Card(CardId.CACHALOTE, "Cachalote", CardType.PEZ_GRANDE, 7,
                condSumRange(12, 15), "Reposiciona dados en cartas boca arriba sin dados.", "", ""));

        cards.add(new Card(CardId.ESTURION, "Esturión", CardType.PEZ_GRANDE, 6,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && bothDiceSameValue(slotIndex, g),
                "Lanza todos los dados de tu reserva y colócalos en la zona de pesca.", "", ""));

        cards.add(new Card(CardId.BALLENA_JOROBADA, "Ballena jorobada", CardType.PEZ_GRANDE, 8,
                condSumRange(1, 2), "Puedes elegir la dirección de la marea.", "", ""));

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
                    if (g.getBoard()[slotIndex].getDice().size() != 2) return false;
                    return g.getBoard()[slotIndex].getDice().get(0).getValue() == 7
                            && g.getBoard()[slotIndex].getDice().get(1).getValue() == 7;
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
        List<Card> deck = new ArrayList<>(createAllCards());
        if (captureCounts != null) {
            applyUnlocks(deck, captureCounts);
        } else {
            removeLockedCards(deck);
        }
        Collections.shuffle(deck, rng);
        return deck;
    }

    public static List<Card> buildDeck(Random rng) {
        return buildDeck(rng, null);
    }

    private static void applyUnlocks(List<Card> deck, Map<CardId, Integer> captureCounts) {
        if (captureCounts.getOrDefault(CardId.CANGREJO_ROJO, 0) < 3) {
            removeCard(deck, CardId.CANGREJO_BOXEADOR);
        }
        if (captureCounts.getOrDefault(CardId.JAIBA_AZUL, 0) < 3) {
            removeCard(deck, CardId.LANGOSTINO_MANTIS);
        }
        if (captureCounts.getOrDefault(CardId.CAMARON_FANTASMA, 0) < 3) {
            removeCard(deck, CardId.CAMARON_PISTOLA);
        }
        if (captureCounts.getOrDefault(CardId.LANGOSTA_ESPINOSA, 0) < 3) {
            removeCard(deck, CardId.BOGAVANTE);
        }
        if (captureCounts.getOrDefault(CardId.KRILL, 0) < 3) {
            removeCard(deck, CardId.COPEPODO_BRILLANTE);
        }
        if (captureCounts.getOrDefault(CardId.CANGREJO_ERMITANO, 0) < 3) {
            removeCard(deck, CardId.CANGREJO_DECORADOR);
        }
        if (captureCounts.getOrDefault(CardId.PERCEBES, 0) < 3) {
            removeCard(deck, CardId.LOCO);
        }
        if (captureCounts.getOrDefault(CardId.CENTOLLA, 0) < 3) {
            removeCard(deck, CardId.JAIBA_GIGANTE_DE_COCO);
        }
        if (captureCounts.getOrDefault(CardId.NAUTILUS, 0) < 3) {
            removeCard(deck, CardId.CANGREJO_HERRADURA);
        }
        if (captureCounts.getOrDefault(CardId.ALMEJAS, 0) < 3) {
            removeCard(deck, CardId.OSTRAS);
        }
        if (captureCounts.getOrDefault(CardId.CANGREJO_ARANA, 0) < 3) {
            removeCard(deck, CardId.CANGREJO_VIOLINISTA);
        }
        if (captureCounts.getOrDefault(CardId.SARDINA, 0) < 3) {
            removeCard(deck, CardId.CONGRIO);
        }
        if (captureCounts.getOrDefault(CardId.ATUN, 0) < 3) {
            removeCard(deck, CardId.PEZ_BETTA);
        }
        if (captureCounts.getOrDefault(CardId.SALMON, 0) < 3) {
            removeCard(deck, CardId.TRUCHA_ARCOIRIS);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_PAYASO, 0) < 3) {
            removeCard(deck, CardId.PEZ_PIEDRA);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_GLOBO, 0) < 3) {
            removeCard(deck, CardId.PEZ_LEON);
        }
        if (captureCounts.getOrDefault(CardId.MORENA, 0) < 3) {
            removeCard(deck, CardId.PEZ_DRAGON_AZUL);
        }
        if (captureCounts.getOrDefault(CardId.CABALLITO_DE_MAR, 0) < 3) {
            removeCard(deck, CardId.PEZ_PIPA);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_LINTERNA, 0) < 3) {
            removeCard(deck, CardId.PEZ_HACHA_ABISAL);
        }
        if (captureCounts.getOrDefault(CardId.KOI, 0) < 3) {
            removeCard(deck, CardId.CARPA_DORADA);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_VOLADOR, 0) < 3) {
            removeCard(deck, CardId.FLETAN);
        }
        if (captureCounts.getOrDefault(CardId.PIRANA, 0) < 3) {
            removeCard(deck, CardId.PEZ_LOBO);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_FANTASMA, 0) < 3) {
            removeCard(deck, CardId.PEZ_BORRON);
        }
        if (captureCounts.getOrDefault(CardId.PULPO, 0) < 3) {
            removeCard(deck, CardId.SEPIA);
        }
        if (captureCounts.getOrDefault(CardId.ARENQUE, 0) < 3) {
            removeCard(deck, CardId.DAMISELAS);
        }
        if (captureCounts.getOrDefault(CardId.REMORA, 0) < 3) {
            removeCard(deck, CardId.LAMPREA);
        }
        if (captureCounts.getOrDefault(CardId.TIBURON_BLANCO, 0) < 3) {
            removeCard(deck, CardId.TIBURON_TIGRE);
        }
        if (captureCounts.getOrDefault(CardId.TIBURON_MARTILLO, 0) < 3) {
            removeCard(deck, CardId.DELFIN);
        }
        if (captureCounts.getOrDefault(CardId.TIBURON_BALLENA, 0) < 3) {
            removeCard(deck, CardId.TIBURON_PEREGRINO);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_VELA, 0) < 3) {
            removeCard(deck, CardId.NARVAL);
        }
        if (captureCounts.getOrDefault(CardId.CALAMAR_GIGANTE, 0) < 3) {
            removeCard(deck, CardId.ORCA);
        }
        if (captureCounts.getOrDefault(CardId.MANTA_GIGANTE, 0) < 3) {
            removeCard(deck, CardId.ANGUILA_ELECTRICA);
        }
        if (captureCounts.getOrDefault(CardId.BALLENA_AZUL, 0) < 3) {
            removeCard(deck, CardId.CACHALOTE);
        }
        if (captureCounts.getOrDefault(CardId.MERO_GIGANTE, 0) < 3) {
            removeCard(deck, CardId.ESTURION);
        }
        if (captureCounts.getOrDefault(CardId.PEZ_LUNA, 0) < 3) {
            removeCard(deck, CardId.BALLENA_JOROBADA);
        }
        if (captureCounts.getOrDefault(CardId.BOTA_VIEJA, 0) < 3) {
            removeCard(deck, CardId.AUTO_HUNDIDO);
        }
        if (captureCounts.getOrDefault(CardId.BOTELLA_PLASTICO, 0) < 3) {
            removeCard(deck, CardId.BOTELLA_DE_VIDRIO);
        }
        if (captureCounts.getOrDefault(CardId.RED_ENREDADA, 0) < 3) {
            removeCard(deck, CardId.RED_DE_ARRASTRE);
        }
        if (captureCounts.getOrDefault(CardId.LATA_OXIDADA, 0) < 3) {
            removeCard(deck, CardId.MICRO_PLASTICOS);
        }
        if (captureCounts.getOrDefault(CardId.LIMPIADOR_MARINO, 0) < 3) {
            removeCard(deck, CardId.FOSA_ABISAL);
        }
        if (captureCounts.getOrDefault(CardId.ANZUELO_ROTO, 0) < 3) {
            removeCard(deck, CardId.DERRAME_PETROLEO);
        }
        if (captureCounts.getOrDefault(CardId.CORRIENTES_PROFUNDAS, 0) < 3) {
            removeCard(deck, CardId.BARCO_PESQUERO);
        }
    }

    private static void removeLockedCards(List<Card> deck) {
        removeCard(deck, CardId.CANGREJO_BOXEADOR);
        removeCard(deck, CardId.LANGOSTINO_MANTIS);
        removeCard(deck, CardId.CAMARON_PISTOLA);
        removeCard(deck, CardId.BOGAVANTE);
        removeCard(deck, CardId.COPEPODO_BRILLANTE);
        removeCard(deck, CardId.CANGREJO_DECORADOR);
        removeCard(deck, CardId.LOCO);
        removeCard(deck, CardId.JAIBA_GIGANTE_DE_COCO);
        removeCard(deck, CardId.CANGREJO_HERRADURA);
        removeCard(deck, CardId.OSTRAS);
        removeCard(deck, CardId.CANGREJO_VIOLINISTA);
        removeCard(deck, CardId.CONGRIO);
        removeCard(deck, CardId.PEZ_BETTA);
        removeCard(deck, CardId.TRUCHA_ARCOIRIS);
        removeCard(deck, CardId.PEZ_PIEDRA);
        removeCard(deck, CardId.PEZ_LEON);
        removeCard(deck, CardId.PEZ_DRAGON_AZUL);
        removeCard(deck, CardId.PEZ_PIPA);
        removeCard(deck, CardId.PEZ_HACHA_ABISAL);
        removeCard(deck, CardId.CARPA_DORADA);
        removeCard(deck, CardId.FLETAN);
        removeCard(deck, CardId.PEZ_LOBO);
        removeCard(deck, CardId.PEZ_BORRON);
        removeCard(deck, CardId.SEPIA);
        removeCard(deck, CardId.DAMISELAS);
        removeCard(deck, CardId.LAMPREA);
        removeCard(deck, CardId.TIBURON_TIGRE);
        removeCard(deck, CardId.DELFIN);
        removeCard(deck, CardId.TIBURON_PEREGRINO);
        removeCard(deck, CardId.NARVAL);
        removeCard(deck, CardId.ORCA);
        removeCard(deck, CardId.ANGUILA_ELECTRICA);
        removeCard(deck, CardId.CACHALOTE);
        removeCard(deck, CardId.ESTURION);
        removeCard(deck, CardId.BALLENA_JOROBADA);
        removeCard(deck, CardId.AUTO_HUNDIDO);
        removeCard(deck, CardId.BOTELLA_DE_VIDRIO);
        removeCard(deck, CardId.RED_DE_ARRASTRE);
        removeCard(deck, CardId.MICRO_PLASTICOS);
        removeCard(deck, CardId.FOSA_ABISAL);
        removeCard(deck, CardId.DERRAME_PETROLEO);
        removeCard(deck, CardId.BARCO_PESQUERO);
    }

    private static void removeCard(List<Card> deck, CardId id) {
        deck.removeIf(card -> card.getId() == id);
    }

    public static int sumWithModifiers(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        int sum = 0;
        for (Die d : s.getDice()) sum += d.getValue();

        int penalty = 0;
        int bonus = 0;
        int r = slotIndex / 3, c = slotIndex % 3;
        int[][] dirs = ADYACENCIA_INCLUYE_DIAGONALES
                ? new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}
                : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            if (rr < 0 || rr > 2 || cc < 0 || cc > 2) continue;
            int idx = rr * 3 + cc;
            BoardSlot adj = g.getBoard()[idx];
            if (adj.getCard() != null && adj.isFaceUp()) {
                if (adj.getCard().getId() == CardId.BOTA_VIEJA) {
                    penalty += 1;
                } else if (adj.getCard().getId() == CardId.AUTO_HUNDIDO) {
                    bonus += 1;
                }
            }
        }
        sum -= penalty;
        sum += bonus;

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

    public static boolean oneDieIsDouble(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        int a = s.getDice().get(0).getValue();
        int b = s.getDice().get(1).getValue();
        return a == b * 2 || b == a * 2;
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

    public static boolean diceConsecutive(int slotIndex, GameState g) {
        BoardSlot s = g.getBoard()[slotIndex];
        if (s.getDice().size() != 2) return false;
        return Math.abs(s.getDice().get(0).getValue() - s.getDice().get(1).getValue()) == 1;
    }
}
