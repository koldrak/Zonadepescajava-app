package com.daille.zonadepescajava_app.model;

import com.daille.zonadepescajava_app.R;

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

        // ==== Cartas Naranjas ====
        cards.add(new Card(CardId.CANGREJO_ROJO, R.string.card_name_cangrejo_rojo, CardType.CRUSTACEO, 2,
                condSumRange(6, 8),
                R.string.card_ability_cangrejo_rojo_on_catch,
                0, 0));

        cards.add(new Card(CardId.CANGREJO_BOXEADOR, R.string.card_name_cangrejo_boxeador, CardType.CRUSTACEO, 5,
                condSumExact(5), R.string.card_ability_cangrejo_boxeador_on_catch, 0, 0));

        cards.add(new Card(CardId.JAIBA_AZUL, R.string.card_name_jaiba_azul, CardType.CRUSTACEO, 2,
                conditionWithDescription(GameUtils::bothDiceEven, R.string.card_condition_both_even),
                R.string.card_ability_jaiba_azul_on_catch,
                0, 0));

        cards.add(new Card(CardId.LANGOSTINO_MANTIS, R.string.card_name_langostino_mantis, CardType.CRUSTACEO, 3,
                conditionWithDescription(GameUtils::oneDieIsDouble, R.string.card_condition_one_die_double),
                R.string.card_ability_langostino_mantis_on_catch,
                0, 0));

        cards.add(new Card(CardId.CAMARON_FANTASMA, R.string.card_name_camaron_fantasma, CardType.CRUSTACEO, 5,
                conditionWithDescription((slotIndex, g) -> atLeastOneIs(slotIndex, g, 1, 2),
                        R.string.card_condition_at_least_one_two_values_format, 1, 2),
                R.string.card_ability_camaron_fantasma_on_catch,
                0, 0));

        cards.add(new Card(CardId.CAMARON_PISTOLA, R.string.card_name_camaron_pistola, CardType.CRUSTACEO, 7,
                condSumRange(7, 9),
                R.string.card_ability_camaron_pistola_on_catch,
                0, 0));

        cards.add(new Card(CardId.LANGOSTA_ESPINOSA, R.string.card_name_langosta_espinosa, CardType.CRUSTACEO, 4,
                condSumExact(9),
                R.string.card_ability_langosta_espinosa_on_catch,
                0, 0));

        cards.add(new Card(CardId.BOGAVANTE, R.string.card_name_bogavante, CardType.CRUSTACEO, 3,
                condSumExact(10), R.string.card_ability_bogavante_on_catch, 0, 0));

        cards.add(new Card(CardId.KRILL, R.string.card_name_krill, CardType.CRUSTACEO, 9,
                condSumExact(7), 0, 0, R.string.card_ability_krill_bonus));

        cards.add(new Card(CardId.COPEPODO_BRILLANTE, R.string.card_name_copepodo_brillante, CardType.CRUSTACEO, 8,
                condSumRange(2, 3), 0, 0, R.string.card_ability_copepodo_brillante_bonus));

        cards.add(new Card(CardId.CANGREJO_ERMITANO, R.string.card_name_cangrejo_ermitano, CardType.CRUSTACEO, 6,
                conditionWithDescription((slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    return s >= (5 + shift) && s <= (9 + shift) &&
                            values.length == 2 &&
                            values[0] % 2 != 0 &&
                            values[1] % 2 != 0;
                }, R.string.card_condition_sum_range_and_both_odd_format, 5, 9),
                R.string.card_ability_cangrejo_ermitano_on_catch, 0, 0));

        cards.add(new Card(CardId.CANGREJO_DECORADOR, R.string.card_name_cangrejo_decorador, CardType.CRUSTACEO, 7,
                conditionWithDescription(
                        (slotIndex, g) -> condSumRange(5, 9).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                        R.string.card_condition_sum_range_and_both_even_format, 5, 9),
                R.string.card_ability_cangrejo_decorador_on_catch, 0, 0));

        cards.add(new Card(CardId.PERCEBES, R.string.card_name_percebes, CardType.CRUSTACEO, 8,
                conditionWithDescription(GameUtils::bothDiceSameValue, R.string.card_condition_both_same),
                R.string.card_ability_percebes_on_catch,
                0, 0));

        cards.add(new Card(CardId.LOCO, R.string.card_name_loco, CardType.CRUSTACEO, 7,
                conditionWithDescription(GameUtils::bothDiceSameValue, R.string.card_condition_both_same),
                R.string.card_ability_loco_on_catch, 0, 0));

        cards.add(new Card(CardId.CENTOLLA, R.string.card_name_centolla, CardType.CRUSTACEO, 7,
                condSumGreaterThan(10),
                R.string.card_ability_centolla_on_catch,
                0, 0));

        cards.add(new Card(CardId.JAIBA_GIGANTE_DE_COCO, R.string.card_name_jaiba_gigante_de_coco, CardType.CRUSTACEO, 9,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 8 && values[1] >= 8;
                }, R.string.card_condition_both_at_least_format, 8),
                R.string.card_ability_jaiba_gigante_de_coco_on_catch, 0, 0));

        cards.add(new Card(CardId.NAUTILUS, R.string.card_name_nautilus, CardType.CRUSTACEO, 2,
                condSumAtLeast(8),
                R.string.card_ability_nautilus_on_catch,
                0, 0));

        cards.add(new Card(CardId.CANGREJO_HERRADURA, R.string.card_name_cangrejo_herradura, CardType.CRUSTACEO, 3,
                condSumAtLeast(14), R.string.card_ability_cangrejo_herradura_on_catch, 0, 0));

        cards.add(new Card(CardId.ALMEJAS, R.string.card_name_almejas, CardType.CRUSTACEO, 2,
                condSumAtLeast(8), R.string.card_ability_almejas_on_catch, 0, 0));

        cards.add(new Card(CardId.OSTRAS, R.string.card_name_ostras, CardType.CRUSTACEO, 3,
                condSumExact(4),
                R.string.card_ability_ostras_on_catch, 0, 0));

        cards.add(new Card(CardId.CANGREJO_ARANA, R.string.card_name_cangrejo_arana, CardType.CRUSTACEO, 2,
                condSumAtLeast(5),
                R.string.card_ability_cangrejo_arana_on_catch,
                0, 0));

        cards.add(new Card(CardId.CANGREJO_VIOLINISTA, R.string.card_name_cangrejo_violinista, CardType.CRUSTACEO, 6,
                condSumRange(13, 15),
                R.string.card_ability_cangrejo_violinista_on_catch, 0, 0));

        cards.add(new Card(CardId.FOCA_MOTEADA, R.string.card_name_foca_moteada, CardType.PEZ_GRANDE, 2,
                condSumAtLeast(10),
                R.string.card_ability_foca_moteada_on_catch,
                0, 0));

        // ==== Cartas Celestes ====
        cards.add(new Card(CardId.SARDINA, R.string.card_name_sardina, CardType.PEZ, 9,
                condSumExact(6), 0, 0, R.string.card_ability_sardina_bonus));

        cards.add(new Card(CardId.ATUN, R.string.card_name_atun, CardType.PEZ, 4,
                condSumRange(3, 7),
                R.string.card_ability_atun_on_catch,
                0, 0));

        cards.add(new Card(CardId.SALMON, R.string.card_name_salmon, CardType.PEZ, 6,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return (a == 4 && b >= 5) || (b == 4 && a >= 5);
                }, R.string.card_condition_one_value_other_at_least_format, 4, 5),
                R.string.card_ability_salmon_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_PAYASO, R.string.card_name_pez_payaso, CardType.PEZ, 5,
                condSumRange(8, 10),
                R.string.card_ability_pez_payaso_on_catch,
                0, 0));

        cards.add(new Card(CardId.PEZ_GLOBO, R.string.card_name_pez_globo, CardType.PEZ, 6,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    return values[0] == slot.getDice().get(0).getType().getSides() &&
                            values[1] == slot.getDice().get(1).getType().getSides();
                }, R.string.card_condition_both_maximum),
                R.string.card_ability_pez_globo_on_catch, 0, 0));

        cards.add(new Card(CardId.MORENA, R.string.card_name_morena, CardType.PEZ, 3,
                differenceAtLeast(4),
                R.string.card_ability_morena_on_catch, 0, 0));

        cards.add(new Card(CardId.CABALLITO_DE_MAR, R.string.card_name_caballito_de_mar, CardType.PEZ, 5,
                condSumRange(2, 4), R.string.card_ability_caballito_de_mar_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_LINTERNA, R.string.card_name_pez_linterna, CardType.PEZ, 6,
                conditionWithDescription((slotIndex, g) -> atLeastOneIs(slotIndex, g, 3),
                        R.string.card_condition_at_least_one_value_format, 3),
                R.string.card_ability_pez_linterna_on_catch,
                0, 0));

        cards.add(new Card(CardId.KOI, R.string.card_name_koi, CardType.PEZ, 7,
                condSumExact(9),
                R.string.card_ability_koi_on_catch,
                0, 0));

        cards.add(new Card(CardId.PEZ_VOLADOR, R.string.card_name_pez_volador, CardType.PEZ, 2,
                conditionWithDescription(GameUtils::oneEvenOneOdd, R.string.card_condition_one_even_one_odd),
                R.string.card_ability_pez_volador_on_catch,
                0, 0));

        cards.add(new Card(CardId.PIRANA, R.string.card_name_pirana, CardType.PEZ, 8,
                conditionWithDescription(
                        (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 6),
                        R.string.card_condition_sum_at_least_and_at_least_one_value_format, 8, 6),
                R.string.card_ability_pirana_on_catch,
                0, 0));

        cards.add(new Card(CardId.PEZ_FANTASMA, R.string.card_name_pez_fantasma, CardType.PEZ, 2,
                conditionWithDescription(
                        (slotIndex, g) -> condSumGreaterThan(6).isSatisfied(slotIndex, g) && !hasAdjacentFaceUp(slotIndex, g),
                        R.string.card_condition_sum_greater_than_and_no_adjacent_face_up_format, 6),
                R.string.card_ability_pez_fantasma_on_catch, 0, 0));

        cards.add(new Card(CardId.PULPO, R.string.card_name_pulpo, CardType.CRUSTACEO, 2,
                condSumLessThan(8),
                R.string.card_ability_pulpo_on_catch,
                0, 0));

        cards.add(new Card(CardId.ARENQUE, R.string.card_name_arenque, CardType.PEZ, 5,
                condSumRange(5, 7),
                R.string.card_ability_arenque_on_catch,
                0, 0));

        cards.add(new Card(CardId.REMORA, R.string.card_name_remora, CardType.PEZ, 7,
                conditionWithDescription(
                        (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D4) && containsDieType(slotIndex, g, DieType.D6),
                        R.string.card_condition_contains_die_types_two_format, DieType.D4.getLabel(), DieType.D6.getLabel()),
                R.string.card_ability_remora_on_catch,
                0, 0));

        cards.add(new Card(CardId.CONGRIO, R.string.card_name_congrio, CardType.PEZ, 7,
                condSumExact(5), 0, 0, R.string.card_ability_congrio_bonus));

        cards.add(new Card(CardId.PEZ_BETTA, R.string.card_name_pez_betta, CardType.PEZ, 9,
                condSumRange(10, 11),
                R.string.card_ability_pez_betta_on_catch, 0, 0));

        cards.add(new Card(CardId.TRUCHA_ARCOIRIS, R.string.card_name_trucha_arcoiris, CardType.PEZ, 5,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    int a = values[0];
                    int b = values[1];
                    return (a == 5 && b >= 6) || (b == 5 && a >= 6);
                }, R.string.card_condition_one_value_other_at_least_format, 5, 6),
                R.string.card_ability_trucha_arcoiris_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_PIEDRA, R.string.card_name_pez_piedra, CardType.PEZ, 6,
                conditionWithDescription(
                        (slotIndex, g) -> condSumRange(10, 14).isSatisfied(slotIndex, g) && bothDiceEven(slotIndex, g),
                        R.string.card_condition_sum_range_and_both_even_format, 10, 14),
                R.string.card_ability_pez_piedra_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_LEON, R.string.card_name_pez_leon, CardType.PEZ, 6,
                conditionWithDescription((slotIndex, g) -> {
                    if (!condSumAtLeast(16).isSatisfied(slotIndex, g)) return false;
                    BoardSlot slot = g.getBoard()[slotIndex];
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] == slot.getDice().get(i).getType().getSides()) return true;
                    }
                    return false;
                }, R.string.card_condition_sum_at_least_and_one_is_max_format, 16),
                R.string.card_ability_pez_leon_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_DRAGON_AZUL, R.string.card_name_pez_dragon_azul, CardType.PEZ, 5,
                differenceAtLeast(5),
                R.string.card_ability_pez_dragon_azul_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_PIPA, R.string.card_name_pez_pipa, CardType.PEZ, 4,
                condSumExact(14), R.string.card_ability_pez_pipa_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_HACHA_ABISAL, R.string.card_name_pez_hacha_abisal, CardType.PEZ, 9,
                conditionWithDescription((slotIndex, g) -> atLeastOneIs(slotIndex, g, 2),
                        R.string.card_condition_at_least_one_value_format, 2),
                R.string.card_ability_pez_hacha_abisal_on_catch, 0, 0));

        cards.add(new Card(CardId.CARPA_DORADA, R.string.card_name_carpa_dorada, CardType.PEZ, 7,
                differenceAtLeast(3), R.string.card_ability_carpa_dorada_on_catch, 0, 0));

        cards.add(new Card(CardId.FLETAN, R.string.card_name_fletan, CardType.PEZ, 7,
                conditionWithDescription(GameUtils::diceConsecutive, R.string.card_condition_dice_consecutive),
                R.string.card_ability_fletan_on_catch,
                0, 0));

        cards.add(new Card(CardId.PEZ_LOBO, R.string.card_name_pez_lobo, CardType.PEZ, 6,
                conditionWithDescription(
                        (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && atLeastOneIs(slotIndex, g, 7),
                        R.string.card_condition_sum_at_least_and_at_least_one_value_format, 8, 7),
                R.string.card_ability_pez_lobo_on_catch,
                0, 0));

        cards.add(new Card(CardId.PEZ_BORRON, R.string.card_name_pez_borron, CardType.PEZ, 7,
                condSumGreaterThan(10),
                R.string.card_ability_pez_borron_on_catch,
                0, 0));

        cards.add(new Card(CardId.SEPIA, R.string.card_name_sepia, CardType.CRUSTACEO, 3,
                condSumRange(12, 14),
                R.string.card_ability_sepia_on_catch,
                0, 0));

        cards.add(new Card(CardId.DAMISELAS, R.string.card_name_damiselas, CardType.PEZ, 3,
                condSumRange(6, 10), R.string.card_ability_damiselas_on_catch, 0, 0));

        cards.add(new Card(CardId.LAMPREA, R.string.card_name_lamprea, CardType.PEZ, 2,
                conditionWithDescription(
                        (slotIndex, g) -> containsDieType(slotIndex, g, DieType.D12) && containsDieType(slotIndex, g, DieType.D8),
                        R.string.card_condition_contains_die_types_two_format, DieType.D12.getLabel(), DieType.D8.getLabel()),
                R.string.card_ability_lamprea_on_catch,
                0, 0));

        // ==== Cartas Verdes ====
        cards.add(new Card(CardId.MORSA, R.string.card_name_morsa, CardType.PEZ_GRANDE, 4,
                condSumRange(7, 8),
                R.string.card_ability_morsa_on_catch,
                0, 0));

        cards.add(new Card(CardId.LEON_MARINO, R.string.card_name_leon_marino, CardType.PEZ_GRANDE, 3,
                condSumRange(13, 14),
                R.string.card_ability_leon_marino_on_catch,
                0, 0));

        cards.add(new Card(CardId.MANATI, R.string.card_name_manati, CardType.PEZ_GRANDE, 5,
                condSumRange(8, 9),
                R.string.card_ability_manati_on_catch,
                0, 0));

        cards.add(new Card(CardId.TIBURON_BLANCO, R.string.card_name_tiburon_blanco, CardType.PEZ_GRANDE, 3,
                condSumGreaterThan(10),
                R.string.card_ability_tiburon_blanco_on_catch,
                0, 0));

        cards.add(new Card(CardId.TIBURON_MARTILLO, R.string.card_name_tiburon_martillo, CardType.PEZ_GRANDE, 8,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 5 && values[1] >= 5;
                }, R.string.card_condition_both_at_least_format, 5),
                0, 0, R.string.card_ability_tiburon_martillo_bonus));

        cards.add(new Card(CardId.TIBURON_BALLENA, R.string.card_name_tiburon_ballena, CardType.PEZ_GRANDE, 5,
                condSumGreaterThan(11), 0, 0, R.string.card_ability_tiburon_ballena_bonus));

        cards.add(new Card(CardId.PEZ_VELA, R.string.card_name_pez_vela, CardType.PEZ_GRANDE, 9,
                condSumExact(12), R.string.card_ability_pez_vela_on_catch, 0, 0));

        cards.add(new Card(CardId.CALAMAR_GIGANTE, R.string.card_name_calamar_gigante, CardType.CRUSTACEO, 9,
                conditionWithDescription(
                        (slotIndex, g) -> condSumGreaterThan(10).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D8),
                        R.string.card_condition_sum_greater_than_and_contains_die_type_format, 10, DieType.D8.getLabel()),
                R.string.card_ability_calamar_gigante_on_catch,
                0, 0));

        cards.add(new Card(CardId.MANTA_GIGANTE, R.string.card_name_manta_gigante, CardType.PEZ_GRANDE, 3,
                conditionWithDescription((slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (9 + shift) || s > (11 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 4 && values[1] != 4;
                }, R.string.card_condition_sum_range_and_no_value_format, 9, 11, 4),
                R.string.card_ability_manta_gigante_on_catch, 0, 0));

        cards.add(new Card(CardId.BALLENA_AZUL, R.string.card_name_ballena_azul, CardType.PEZ_GRANDE, 2,
                condSumRange(11, 13),
                R.string.card_ability_ballena_azul_on_catch,
                0, 0));

        cards.add(new Card(CardId.MERO_GIGANTE, R.string.card_name_mero_gigante, CardType.PEZ_GRANDE, 2,
                conditionWithDescription(
                        (slotIndex, g) -> condSumAtLeast(10).isSatisfied(slotIndex, g) && diceDistinct(slotIndex, g),
                        R.string.card_condition_sum_at_least_and_dice_distinct_format, 10),
                R.string.card_ability_mero_gigante_on_catch, 0, 0));

        cards.add(new Card(CardId.PEZ_LUNA, R.string.card_name_pez_luna, CardType.PEZ_GRANDE, 8,
                condSumAtLeast(13), R.string.card_ability_pez_luna_on_catch, 0, 0));

        cards.add(new Card(CardId.TIBURON_TIGRE, R.string.card_name_tiburon_tigre, CardType.PEZ_GRANDE, 3,
                condSumGreaterThan(13),
                R.string.card_ability_tiburon_tigre_on_catch,
                0, 0));

        cards.add(new Card(CardId.DELFIN, R.string.card_name_delfin, CardType.PEZ_GRANDE, 4,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] >= 6 && values[1] >= 6;
                }, R.string.card_condition_both_at_least_format, 6),
                R.string.card_ability_delfin_on_catch, 0, 0));

        cards.add(new Card(CardId.TIBURON_PEREGRINO, R.string.card_name_tiburon_peregrino, CardType.PEZ_GRANDE, 4,
                condSumGreaterThan(9), R.string.card_ability_tiburon_peregrino_on_catch, 0, 0));

        cards.add(new Card(CardId.NARVAL, R.string.card_name_narval, CardType.PEZ_GRANDE, 8,
                condSumExact(15),
                R.string.card_ability_narval_on_catch, 0, 0));

        cards.add(new Card(CardId.ORCA, R.string.card_name_orca, CardType.PEZ_GRANDE, 4,
                conditionWithDescription(
                        (slotIndex, g) -> condSumGreaterThan(19).isSatisfied(slotIndex, g) && containsDieType(slotIndex, g, DieType.D12),
                        R.string.card_condition_sum_greater_than_and_contains_die_type_format, 19, DieType.D12.getLabel()),
                R.string.card_ability_orca_on_catch, 0, 0));

        cards.add(new Card(CardId.ANGUILA_ELECTRICA, R.string.card_name_anguila_electrica, CardType.PEZ_GRANDE, 4,
                conditionWithDescription((slotIndex, g) -> {
                    int s = sumWithModifiers(slotIndex, g);
                    int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
                    if (s < (10 + shift) || s > (12 + shift)) return false;
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] != 5 && values[1] != 5;
                }, R.string.card_condition_sum_range_and_no_value_format, 10, 12, 5),
                R.string.card_ability_anguila_electrica_on_catch, 0, 0));

        cards.add(new Card(CardId.CACHALOTE, R.string.card_name_cachalote, CardType.PEZ_GRANDE, 4,
                condSumRange(12, 15),
                R.string.card_ability_cachalote_on_catch,
                0, 0));

        cards.add(new Card(CardId.ESTURION, R.string.card_name_esturion, CardType.PEZ_GRANDE, 8,
                conditionWithDescription(
                        (slotIndex, g) -> condSumAtLeast(8).isSatisfied(slotIndex, g) && bothDiceSameValue(slotIndex, g),
                        R.string.card_condition_sum_at_least_and_both_same_format, 8),
                R.string.card_ability_esturion_on_catch,
                0, 0));

        cards.add(new Card(CardId.BALLENA_JOROBADA, R.string.card_name_ballena_jorobada, CardType.PEZ_GRANDE, 4,
                condSumRange(1, 2), R.string.card_ability_ballena_jorobada_on_catch, 0, 0));

        // ==== Cartas Negras ====
        cards.add(new Card(CardId.BOTA_VIEJA, R.string.card_name_bota_vieja, CardType.OBJETO, 7,
                condSumLessOrEqual(6), R.string.card_ability_bota_vieja_on_catch, 0, 0));

        cards.add(new Card(CardId.BOTELLA_PLASTICO, R.string.card_name_botella_plastico, CardType.OBJETO, 9,
                condSumExact(8),
                R.string.card_ability_botella_plastico_on_catch,
                0, 0));

        cards.add(new Card(CardId.RED_ENREDADA, R.string.card_name_red_enredada, CardType.OBJETO, 4,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 1 && values[1] == 1;
                }, R.string.card_condition_both_exact_format, 1),
                R.string.card_ability_red_enredada_on_catch, 0, 0));

        cards.add(new Card(CardId.LATA_OXIDADA, R.string.card_name_lata_oxidada, CardType.OBJETO, 4,
                condSumExact(8), R.string.card_ability_lata_oxidada_on_catch, 0, 0));

        cards.add(new Card(CardId.LIMPIADOR_MARINO, R.string.card_name_limpiador_marino, CardType.OBJETO, 5,
                condSumGreaterThan(8), 0, 0, R.string.card_ability_limpiador_marino_bonus));

        cards.add(new Card(CardId.ANZUELO_ROTO, R.string.card_name_anzuelo_roto, CardType.OBJETO, 9,
                conditionWithDescription(GameUtils::bothDiceSameValue, R.string.card_condition_both_same),
                R.string.card_ability_anzuelo_roto_on_catch,
                0, 0));

        cards.add(new Card(CardId.CORRIENTES_PROFUNDAS, R.string.card_name_corrientes_profundas, CardType.OBJETO, 6,
                differenceAtLeast(3),
                R.string.card_ability_corrientes_profundas_on_catch,
                0, 0));

        cards.add(new Card(CardId.AUTO_HUNDIDO, R.string.card_name_auto_hundido, CardType.OBJETO, 8,
                condSumExact(13), R.string.card_ability_auto_hundido_on_catch, 0, 0));

        cards.add(new Card(CardId.BOTELLA_DE_VIDRIO, R.string.card_name_botella_de_vidrio, CardType.OBJETO, 9,
                condSumExact(8), R.string.card_ability_botella_de_vidrio_on_catch, 0, 0));

        cards.add(new Card(CardId.RED_DE_ARRASTRE, R.string.card_name_red_de_arrastre, CardType.OBJETO, 8,
                conditionWithDescription((slotIndex, g) -> {
                    int[] values = adjustedDiceValues(slotIndex, g);
                    if (values.length != 2) return false;
                    return values[0] == 7 && values[1] == 7;
                }, R.string.card_condition_both_exact_format, 7),
                R.string.card_ability_red_de_arrastre_on_catch, 0, 0));

        cards.add(new Card(CardId.MICRO_PLASTICOS, R.string.card_name_micro_plasticos, CardType.OBJETO, 8,
                condSumExact(10), R.string.card_ability_micro_plasticos_on_catch, 0, 0));

        cards.add(new Card(CardId.FOSA_ABISAL, R.string.card_name_fosa_abisal, CardType.OBJETO, 5,
                condSumGreaterThan(10), 0, 0, R.string.card_ability_fosa_abisal_bonus));

        cards.add(new Card(CardId.DERRAME_PETROLEO, R.string.card_name_derrame_petroleo, CardType.OBJETO, 10,
                condSumGreaterThan(11),
                R.string.card_ability_derrame_petroleo_on_catch, 0, 0));

        cards.add(new Card(CardId.BARCO_PESQUERO, R.string.card_name_barco_pesquero, CardType.OBJETO, 6,
                conditionWithDescription(GameUtils::diceConsecutive, R.string.card_condition_dice_consecutive),
                R.string.card_ability_barco_pesquero_on_catch,
                0, 0));

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
            Map<CardId, Integer> counts = new java.util.EnumMap<>(CardId.class);
            for (Card card : selection) {
                if (card == null) {
                    continue;
                }
                int current = counts.getOrDefault(card.getId(), 0);
                if (current >= 3) {
                    continue;
                }
                counts.put(card.getId(), current + 1);
                deck.add(card);
            }
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
            int copies = Math.min(3, Math.max(0, ownedCounts.getOrDefault(card.getId(), 0)));
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

    private static ConditionInfo conditionWithDescription(Condition condition, int resId, Object... args) {
        return new ConditionInfo(ConditionInfo.Type.CUSTOM, 0, 0, condition, resId, args);
    }

    public static Condition condSumRange(int min, int max) {
        return new ConditionInfo(ConditionInfo.Type.SUM_RANGE, min, max, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s >= (min + shift) && s <= (max + shift);
        });
    }

    public static Condition condSumExact(int value) {
        return new ConditionInfo(ConditionInfo.Type.SUM_EXACT, value, value, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s == (value + shift);
        });
    }

    public static Condition condSumAtLeast(int min) {
        return new ConditionInfo(ConditionInfo.Type.SUM_AT_LEAST, min, 0, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s >= (min + shift);
        });
    }

    public static Condition condSumGreaterThan(int v) {
        return new ConditionInfo(ConditionInfo.Type.SUM_GREATER_THAN, v, 0, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s > (v + shift);
        });
    }

    public static Condition condSumLessOrEqual(int v) {
        return new ConditionInfo(ConditionInfo.Type.SUM_LESS_OR_EQUAL, v, 0, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s <= (v + shift);
        });
    }

    public static Condition condSumLessThan(int value) {
        return new ConditionInfo(ConditionInfo.Type.SUM_LESS_THAN, value, 0, (slotIndex, g) -> {
            int shift = g.getBoard()[slotIndex].getStatus().sumConditionShift;
            int s = sumWithModifiers(slotIndex, g);
            return s < (value + shift);
        });
    }

    public static Condition differenceAtLeast(int diff) {
        return new ConditionInfo(ConditionInfo.Type.DIFFERENCE_AT_LEAST, diff, 0, (slotIndex, state) -> {
            int[] values = adjustedDiceValues(slotIndex, state);
            if (values.length != 2) return false;
            return Math.abs(values[0] - values[1]) >= diff;
        });
    }

    public static boolean diceConsecutive(int slotIndex, GameState g) {
        int[] values = adjustedDiceValues(slotIndex, g);
        if (values.length != 2) return false;
        return Math.abs(values[0] - values[1]) == 1;
    }
}
