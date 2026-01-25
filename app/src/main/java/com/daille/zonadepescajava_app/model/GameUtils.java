package com.daille.zonadepescajava_app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GameUtils {
    private static final boolean ADYACENCIA_INCLUYE_DIAGONALES = true;

    private GameUtils() {}

    public static boolean isCaptureRewardThreshold(int totalCaptures, int baseTarget) {
        if (totalCaptures <= 0) {
            return false;
        }
        int target = Math.max(1, baseTarget);
        int accumulated = 0;
        while (accumulated < totalCaptures) {
            accumulated += target;
            if (accumulated == totalCaptures) {
                return true;
            }
            target++;
        }
        return false;
    }

    public static int getCaptureRewardProgress(int totalCaptures, int baseTarget) {
        int target = Math.max(1, baseTarget);
        int remaining = Math.max(0, totalCaptures);
        while (remaining >= target) {
            remaining -= target;
            target++;
        }
        return remaining;
    }

    public static int getCaptureRewardTarget(int totalCaptures, int baseTarget) {
        int target = Math.max(1, baseTarget);
        int remaining = Math.max(0, totalCaptures);
        while (remaining >= target) {
            remaining -= target;
            target++;
        }
        return target;
    }

    public static List<Card> createAllCards() {
        List<Card> cards = new ArrayList<>();

        // ==== Crustáceos ====
        cards.add(new Card(CardId.CANGREJO_ROJO, "Cangrejo Rojo", CardType.CRUSTACEO, 2,
                condSumRange(6, 8),
                "Mueve 1 dado desde una carta adyacente hacia otra carta adyacente (máx. 2 dados por carta).",
                "", ""));

        cards.add(new Card(CardId.CANGREJO_BOXEADOR, "Cangrejo Boxeador", CardType.CRUSTACEO, 5,
                condSumExact(5), "Mueve 1 dado entre cartas adyacentes (hasta 2 veces).", "", ""));

        cards.add(new Card(CardId.JAIBA_AZUL, "Jaiba Azul", CardType.CRUSTACEO, 2,
                (slotIndex, g) -> bothDiceEven(slotIndex, g),
                "Puedes ajustar en ±1 uno de los dados en la zona de pesca (mín. 1, máx. su cara).",
                "", ""));

        cards.add(new Card(CardId.LANGOSTINO_MANTIS, "Langostino Mantis", CardType.CRUSTACEO, 3,
                GameUtils::oneDieIsDouble,
                "Selecciona y relanza un dado perdido para reemplazar un dado de la zona de pesca.",
                "", ""));

        cards.add(new Card(CardId.CAMARON_FANTASMA, "Camarón Fantasma", CardType.CRUSTACEO, 5,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 1, 2),
                "Mira 2 cartas boca abajo adyacentes y decide si invertir su posición.",
                "", ""));

        cards.add(new Card(CardId.CAMARON_PISTOLA, "Camarón Pistola", CardType.CRUSTACEO, 7,
                condSumRange(7, 9),
                "Al colocar un dado aquí, tiene 50% de probabilidad de reposicionarse al azar.",
                "", ""));

        cards.add(new Card(CardId.LANGOSTA_ESPINOSA, "Langosta Espinosa", CardType.CRUSTACEO, 4,
                condSumExact(9),
                "Si uno de los dados usados fue D8, recupera 1 dado perdido.",
                "", ""));

        cards.add(new Card(CardId.BOGAVANTE, "Bogavante", CardType.CRUSTACEO, 3,
                condSumExact(10), "Recupera 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.KRILL, "Krill", CardType.CRUSTACEO, 9,
                condSumExact(7), "", "", "Otorga +1 por cada crustáceo capturado."));

        cards.add(new Card(CardId.COPEPODO_BRILLANTE, "Copépodo Brillante", CardType.CRUSTACEO, 8,
                condSumRange(2, 3), "", "", "Otorga +1 por cada crustáceo descartado por fallo."));

        cards.add(new Card(CardId.CANGREJO_ERMITANO, "Cangrejo Ermitaño", CardType.CRUSTACEO, 6,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    return s >= (5 + shift) && s <= (9 + shift) &&
                            values.length == 2 &&
                            values[0] % 2 != 0 &&
                            values[1] % 2 != 0;
                }, "Si hay un objeto adyacente boca arriba, puedes descartarlo y reemplazarlo.", "", ""));

        cards.add(new Card(CardId.CANGREJO_DECORADOR, "Cangrejo Decorador", CardType.CRUSTACEO, 7,
                (slotIndex, g) -> condSumRange(5, 9).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                "Busca un objeto en el mazo y reemplaza una carta boca abajo sin dados.", "", ""));

        cards.add(new Card(CardId.PERCEBES, "Percebes", CardType.CRUSTACEO, 8,
                GameUtils::bothDiceSameValue,
                "Los dados de esta carta deben moverse a cartas adyacentes conservando su valor.",
                "", ""));

        cards.add(new Card(CardId.LOCO, "Loco", CardType.CRUSTACEO, 7,
                GameUtils::bothDiceSameValue,
                "Mueve los dados de esta carta a cartas adyacentes ajustando ±1.", "", ""));

        cards.add(new Card(CardId.CENTOLLA, "Centolla", CardType.CRUSTACEO, 7,
                condSumGreaterThan(10),
                "El próximo dado lanzado debe ir obligatoriamente a esta carta.",
                "", ""));

        cards.add(new Card(CardId.JAIBA_GIGANTE_DE_COCO, "Jaiba Gigante de Coco", CardType.CRUSTACEO, 9,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 8 && values[1] >= 8;
                }, "Si el dado colocado es < 7 se pierde automáticamente.", "", ""));

        cards.add(new Card(CardId.NAUTILUS, "Nautilus", CardType.CRUSTACEO, 2,
                condSumAtLeast(8),
                "Puede cambiar el valor de 2 dados sumando o restando 2 puntos.",
                "", ""));

        cards.add(new Card(CardId.CANGREJO_HERRADURA, "Cangrejo herradura", CardType.CRUSTACEO, 3,
                condSumAtLeast(14), "Puedes cambiar el valor de 1 dado al número que elijas.", "", ""));

        cards.add(new Card(CardId.ALMEJAS, "Almejas", CardType.CRUSTACEO, 2,
                condSumAtLeast(8), "Lanza un dado descartado y colócalo aquí si se activó una habilidad adyacente.", "", ""));

        cards.add(new Card(CardId.OSTRAS, "Ostras", CardType.CRUSTACEO, 3,
                condSumExact(4),
                "Relanza y reposiciona aleatoriamente un dado perdido al activarse una habilidad adyacente.", "", ""));

        cards.add(new Card(CardId.CANGREJO_ARANA, "Cangrejo araña", CardType.CRUSTACEO, 2,
                condSumAtLeast(5),
                "Elige una carta descartada por fallo y reemplaza una carta boca abajo.",
                "", ""));

        cards.add(new Card(CardId.CANGREJO_VIOLINISTA, "Cangrejo violinista", CardType.CRUSTACEO, 6,
                condSumRange(13, 15),
                "Elige una carta descartada por fallo y captúrala directamente.", "", ""));

        // ==== Peces pequeños ====
        cards.add(new Card(CardId.SARDINA, "Sardina", CardType.PEZ, 9,
                condSumExact(6), "", "", "Otorga +1 punto por cada pez pequeño capturado."));

        cards.add(new Card(CardId.ATUN, "Atún", CardType.PEZ, 4,
                condSumRange(3, 7),
                "Puedes relanzar el dado recién lanzado y reposicionarlo en cualquier carta.",
                "", ""));

        cards.add(new Card(CardId.SALMON, "Salmón", CardType.PEZ, 6,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return (a == 4 && b >= 5) || (b == 4 && a >= 5);
                }, "Selecciona 1 carta boca abajo y voltéala sin ponerle dado.", "", ""));

        cards.add(new Card(CardId.PEZ_PAYASO, "Pez Payaso", CardType.PEZ, 5,
                condSumRange(8, 10),
                "Elige una carta adyacente boca arriba para protegerla del próximo fallo.",
                "", ""));

        cards.add(new Card(CardId.PEZ_GLOBO, "Pez Globo", CardType.PEZ, 6,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    return values[0] == slot.getDice().get(0).getType().getSides() &&
                            values[1] == slot.getDice().get(1).getType().getSides();
                }, "Puedes inflar el resultado de cualquier dado a su valor máximo.", "", ""));

        cards.add(new Card(CardId.MORENA, "Morena", CardType.PEZ, 3,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return Math.abs(a - b) >= 4;
                }, "Mueve 1 dado de una carta adyacente a otra carta adyacente.", "", ""));

        cards.add(new Card(CardId.CABALLITO_DE_MAR, "Caballito de Mar", CardType.PEZ, 5,
                condSumRange(2, 4), "Puedes recuperar un D4 perdido.", "", ""));

        cards.add(new Card(CardId.PEZ_LINTERNA, "Pez Linterna", CardType.PEZ, 6,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 3),
                "Selecciona 1 carta boca abajo: si es pez grande, mueve el dado; si es objeto, pierdes el dado.",
                "", ""));

        cards.add(new Card(CardId.KOI, "Koi", CardType.PEZ, 7,
                condSumExact(9),
                "Intercambia un dado de esta carta con uno de una carta adyacente boca arriba con 1 dado.",
                "", ""));

        cards.add(new Card(CardId.PEZ_VOLADOR, "Pez Volador", CardType.PEZ, 2,
                GameUtils::oneEvenOneOdd,
                "Revela una línea: si el par es mayor, es vertical; si es menor, es horizontal.",
                "", ""));

        cards.add(new Card(CardId.PIRANA, "Piraña", CardType.PEZ, 8,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 6),
                "Descarta 1 pez pequeño adyacente boca arriba y reemplázalo sin perder sus dados.",
                "", ""));

        cards.add(new Card(CardId.PEZ_FANTASMA, "Pez Fantasma", CardType.PEZ, 2,
                (slotIndex, g) -> condSumGreaterThan(6).isSatisfied(slotIndex, g) && !hasAdjacentFaceUp(slotIndex, g),
                "Vuelve boca abajo una carta adyacente y recupera su dado.", "", ""));

        cards.add(new Card(CardId.PULPO, "Pulpo", CardType.PEZ, 2,
                condSumLessThan(8),
                "Si el dado es par, reemplaza esta carta por otra del mazo boca arriba manteniendo el dado.",
                "", ""));

        cards.add(new Card(CardId.ARENQUE, "Arenque", CardType.PEZ, 5,
                condSumRange(5, 7),
                "Busca 2 peces pequeños y colócalos boca abajo adyacentes, reemplazando cartas boca abajo.",
                "", ""));

        cards.add(new Card(CardId.REMORA, "Rémora", CardType.PEZ, 7,
                (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D4) && containsDieType(slotIndex, g, DieType.D6),
                "Si está adyacente a un pez grande, se adhiere y se captura junto a él.",
                "", ""));

        cards.add(new Card(CardId.CONGRIO, "Congrio", CardType.PEZ, 7,
                condSumExact(5), "", "", "Otorga +1 por cada pez pequeño descartado por fallo."));

        cards.add(new Card(CardId.PEZ_BETTA, "Pez betta", CardType.PEZ, 9,
                condSumRange(10, 11),
                "Mientras esté boca arriba, solo puedes colocar dados en su fila.", "", ""));

        cards.add(new Card(CardId.TRUCHA_ARCOIRIS, "Trucha Arcoíris", CardType.PEZ, 5,
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

        cards.add(new Card(CardId.PEZ_PIPA, "Pez pipa", CardType.PEZ, 4,
                condSumExact(14), "Si usas un D12 en su captura, recuperas 1 dado.", "", ""));

        cards.add(new Card(CardId.PEZ_HACHA_ABISAL, "Pez Hacha Abisal", CardType.PEZ, 9,
                (slotIndex, g) -> atLeastOneIs(slotIndex, g, 2), "Debes liberar 2 cartas.", "", ""));

        cards.add(new Card(CardId.CARPA_DORADA, "Carpa Dorada", CardType.PEZ, 7,
                differenceAtLeast(3), "La marea solo afecta a los dados; los que salen se pierden.", "", ""));

        cards.add(new Card(CardId.FLETAN, "Fletan", CardType.PEZ, 7,
                GameUtils::diceConsecutive,
                "Al activarse la marea, elige un pez boca arriba y ponlo boca abajo; recuperas su dado.",
                "", ""));

        cards.add(new Card(CardId.PEZ_LOBO, "Pez Lobo", CardType.PEZ, 6,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 7),
                "Descarta una carta adyacente boca arriba y reemplázala sin perder sus dados; luego vuelve al mazo.",
                "", ""));

        cards.add(new Card(CardId.PEZ_BORRON, "Pez borrón", CardType.PEZ, 7,
                condSumGreaterThan(10),
                "Elige 1 carta boca abajo y mueve el dado de esta carta a ella sin voltearla.",
                "", ""));

        cards.add(new Card(CardId.SEPIA, "Sepia", CardType.PEZ, 3,
                condSumRange(12, 14),
                "Si el dado es impar, mira 3 cartas, captura 1 y baraja esta carta en el mazo.",
                "", ""));

        cards.add(new Card(CardId.DAMISELAS, "Damiselas", CardType.PEZ, 3,
                condSumRange(6, 10), "Mira las 6 primeras cartas del mazo y ordénalas.", "", ""));

        cards.add(new Card(CardId.LAMPREA, "Lamprea", CardType.PEZ, 2,
                (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D12) && containsDieType(slotIndex, g, DieType.D8),
                "Se adhiere a un pez grande y permite ajustar su dado ±1 al colocar.",
                "", ""));

        // ==== Peces grandes ====
        cards.add(new Card(CardId.TIBURON_BLANCO, "Tiburón Blanco", CardType.PEZ_GRANDE, 3,
                condSumGreaterThan(10),
                "Elimina una carta adyacente boca arriba y mueve su dado a esta carta.",
                "", ""));

        cards.add(new Card(CardId.TIBURON_MARTILLO, "Tiburón Martillo", CardType.PEZ_GRANDE, 8,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 5 && values[1] >= 5;
                }, "", "", "Otorga +2 por cada pez grande capturado."));

        cards.add(new Card(CardId.TIBURON_BALLENA, "Tiburón Ballena", CardType.PEZ_GRANDE, 5,
                condSumGreaterThan(11), "", "", "Si tienes 3 crustáceos, otorga +6."));

        cards.add(new Card(CardId.PEZ_VELA, "Pez Vela", CardType.PEZ_GRANDE, 9,
                condSumExact(12), "Relanza el dado y elige resultado.", "", ""));

        cards.add(new Card(CardId.CALAMAR_GIGANTE, "Calamar Gigante", CardType.PEZ_GRANDE, 9,
                (slotIndex, g) -> condSumGreaterThan(10).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D8),
                "Voltea boca abajo las cartas adyacentes manteniendo sus dados; podrán activarse de nuevo.",
                "", ""));

        cards.add(new Card(CardId.MANTA_GIGANTE, "Manta Gigante", CardType.PEZ_GRANDE, 3,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (9 + shift) || s > (11 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 4 && values[1] != 4;
                }, "Recuperas un dado D8 perdido.", "", ""));

        cards.add(new Card(CardId.BALLENA_AZUL, "Ballena azul", CardType.PEZ_GRANDE, 2,
                condSumRange(11, 13),
                "Reposiciona todos los dados del tablero manteniendo sus valores.",
                "", ""));

        cards.add(new Card(CardId.MERO_GIGANTE, "Mero gigante", CardType.PEZ_GRANDE, 2,
                (slotIndex, g) -> condSumAtLeast(10).isSatisfied(slotIndex, g) && diceDistinct(slotIndex, g),
                "Revela todas las cartas adyacentes que estén boca abajo.", "", ""));

        cards.add(new Card(CardId.PEZ_LUNA, "Pez luna", CardType.PEZ_GRANDE, 8,
                condSumAtLeast(13), "Si sale del tablero por marea, libera tu captura de mayor valor.", "", ""));

        cards.add(new Card(CardId.TIBURON_TIGRE, "Tiburón tigre", CardType.PEZ_GRANDE, 3,
                condSumGreaterThan(13),
                "Elimina una carta adyacente boca arriba y devuelve su dado a la reserva.",
                "", ""));

        cards.add(new Card(CardId.DELFIN, "Delfín", CardType.PEZ_GRANDE, 4,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 6 && values[1] >= 6;
                }, "El próximo fallo adyacente no descarta la carta.", "", ""));

        cards.add(new Card(CardId.TIBURON_PEREGRINO, "Tiburón Peregrino", CardType.PEZ_GRANDE, 4,
                condSumGreaterThan(9), "Revela 5 cartas, elige 1 arriba y 1 al fondo.", "", ""));

        cards.add(new Card(CardId.NARVAL, "Narval", CardType.PEZ_GRANDE, 8,
                condSumExact(15),
                "Regresa las cartas adyacentes boca arriba al mazo y recupera sus dados.", "", ""));

        cards.add(new Card(CardId.ORCA, "Orca", CardType.PEZ_GRANDE, 4,
                (slotIndex, g) -> condSumGreaterThan(19).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D12),
                "Voltea boca abajo las cartas adyacentes y recupera sus dados.", "", ""));

        cards.add(new Card(CardId.ANGUILA_ELECTRICA, "Anguila Eléctrica", CardType.PEZ_GRANDE, 4,
                (slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (10 + shift) || s > (12 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 5 && values[1] != 5;
                }, "Relanza dados adyacentes; si alguno es máximo recupera un dado.", "", ""));

        cards.add(new Card(CardId.CACHALOTE, "Cachalote", CardType.PEZ_GRANDE, 4,
                condSumRange(12, 15),
                "Reposiciona dados en cartas boca arriba sin dados, eligiendo el valor del dado.",
                "", ""));

        cards.add(new Card(CardId.ESTURION, "Esturión", CardType.PEZ_GRANDE, 8,
                (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && bothDiceSameValue(slotIndex, g),
                "Lanza todos los dados de tu reserva y colócalos en la zona de pesca.",
                "", ""));

        cards.add(new Card(CardId.BALLENA_JOROBADA, "Ballena jorobada", CardType.PEZ_GRANDE, 4,
                condSumRange(1, 2), "Puedes elegir la dirección de la marea.", "", ""));

        // ==== Objetos ====
        cards.add(new Card(CardId.BOTA_VIEJA, "Bota Vieja", CardType.OBJETO, 7,
                condSumLessOrEqual(6), "Mientras esté boca arriba, aplica −1 a adyacentes.", "", ""));

        cards.add(new Card(CardId.BOTELLA_PLASTICO, "Botella de Plástico", CardType.OBJETO, 9,
                condSumExact(8),
                "Elige 1 pez pequeño adyacente boca arriba; sus dados obtienen +3.",
                "", ""));

        cards.add(new Card(CardId.RED_ENREDADA, "Red Enredada", CardType.OBJETO, 4,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 1 && values[1] == 1;
                }, "Captura además una carta adyacente boca abajo.", "", ""));

        cards.add(new Card(CardId.LATA_OXIDADA, "Lata Oxidada", CardType.OBJETO, 4,
                condSumExact(8), "Recupera 1 dado perdido.", "", ""));

        cards.add(new Card(CardId.LIMPIADOR_MARINO, "Limpiador Marino", CardType.OBJETO, 5,
                condSumGreaterThan(8), "", "", "Otorga +2 por cada objeto capturado."));

        cards.add(new Card(CardId.ANZUELO_ROTO, "Anzuelo Roto", CardType.OBJETO, 9,
                GameUtils::bothDiceSameValue,
                "Mientras esté boca arriba, el primer fallo con 2 dados pierde 2 dados.",
                "", ""));

        cards.add(new Card(CardId.CORRIENTES_PROFUNDAS, "Corrientes profundas", CardType.OBJETO, 6,
                differenceAtLeast(3),
                "Si un dado iguala el suyo, activa marea: par a la derecha, impar a la izquierda.",
                "", ""));

        cards.add(new Card(CardId.AUTO_HUNDIDO, "Auto hundido", CardType.OBJETO, 8,
                condSumExact(13), "Mientras esté boca arriba, aplica +1 a adyacentes.", "", ""));

        cards.add(new Card(CardId.BOTELLA_DE_VIDRIO, "Botella de vidrio", CardType.OBJETO, 9,
                condSumExact(8), "Elige 1 carta adyacente boca arriba; sus dados obtienen −3.", "", ""));

        cards.add(new Card(CardId.RED_DE_ARRASTRE, "Red de arrastre", CardType.OBJETO, 8,
                (slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 7 && values[1] == 7;
                }, "Captura 2 cartas boca abajo y libera 1 carta.", "", ""));

        cards.add(new Card(CardId.MICRO_PLASTICOS, "Micro plásticos", CardType.OBJETO, 8,
                condSumExact(10), "Invierte todas las cartas de la zona de pesca.", "", ""));

        cards.add(new Card(CardId.FOSA_ABISAL, "Fosa abisal", CardType.OBJETO, 5,
                condSumGreaterThan(10), "", "", "Otorga +1 por cada objeto fallado."));

        cards.add(new Card(CardId.DERRAME_PETROLEO, "Derrame de petróleo", CardType.OBJETO, 10,
                condSumGreaterThan(11),
                "Voltea todas las cartas boca arriba; no se pueden voltear hasta que salga.", "", ""));

        cards.add(new Card(CardId.BARCO_PESQUERO, "Barco pesquero", CardType.OBJETO, 6,
                GameUtils::diceConsecutive,
                "Si un dado iguala el suyo, elimina una carta adyacente boca arriba.",
                "", ""));

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

    public static List<Card> buildDeckFromSelection(Random rng, List<Card> selection,
                                                    Map<CardId, Integer> ownedCounts) {
        List<Card> deck = new ArrayList<>();
        if (selection != null) {
            deck.addAll(selection);
        }
        List<Card> extra = drawExtraOwnedCards(rng, selection, ownedCounts, 10);
        deck.addAll(extra);
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
                                                     int minCards, int maxCards) {
        if (availableCards == null || availableCards.isEmpty()) {
            return new ArrayList<>();
        }
        List<Card> pool = buildOwnedPool(availableCards, ownedCounts);
        if (pool.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.shuffle(pool, rng);
        int clampedMax = Math.min(maxCards, pool.size());
        int clampedMin = Math.min(minCards, clampedMax);
        int targetSize = clampedMin;
        if (clampedMax > clampedMin) {
            targetSize = clampedMin + rng.nextInt(clampedMax - clampedMin + 1);
        }
        return new ArrayList<>(pool.subList(0, targetSize));
    }

    public static List<Card> buildDeck(Random rng) {
        return buildDeck(rng, null);
    }

    public static List<Card> getRandomStarterCards(Random rng, int count) {
        List<Card> allCards = createAllCards();
        if (allCards.isEmpty() || count <= 0) {
            return new ArrayList<>();
        }
        List<Card> starters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card card = drawWeightedByPoints(rng, allCards);
            if (card != null) {
                starters.add(card);
            }
        }
        return starters;
    }

    public static Card drawWeightedByPoints(Random rng, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        double totalWeight = 0.0;
        for (Card card : cards) {
            int points = Math.max(1, card.getPoints());
            totalWeight += 1.0 / points;
        }
        if (totalWeight <= 0.0) {
            return cards.get(rng.nextInt(cards.size()));
        }
        double roll = rng.nextDouble() * totalWeight;
        for (Card card : cards) {
            int points = Math.max(1, card.getPoints());
            roll -= 1.0 / points;
            if (roll <= 0.0) {
                return card;
            }
        }
        return cards.get(cards.size() - 1);
    }

    private static List<Card> drawExtraOwnedCards(Random rng, List<Card> selection,
                                                  Map<CardId, Integer> ownedCounts, int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }
        List<Card> allCards = createAllCards();
        if (allCards.isEmpty()) {
            return new ArrayList<>();
        }
        List<Card> extra = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            extra.add(allCards.get(rng.nextInt(allCards.size())));
        }
        return extra;
    }

    private static List<Card> buildOwnedPool(List<Card> availableCards, Map<CardId, Integer> ownedCounts) {
        List<Card> pool = new ArrayList<>();
        if (availableCards == null || availableCards.isEmpty()) {
            return pool;
        }
        if (ownedCounts == null || ownedCounts.isEmpty()) {
            pool.addAll(availableCards);
            return pool;
        }
        for (Card card : availableCards) {
            int copies = Math.max(0, ownedCounts.getOrDefault(card.getId(), 0));
            for (int i = 0; i < copies; i++) {
                pool.add(card);
            }
        }
        return pool;
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
