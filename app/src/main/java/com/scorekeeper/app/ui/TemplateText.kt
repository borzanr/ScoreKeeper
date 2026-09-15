package com.scorekeeper.app.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.scorekeeper.app.R
import com.scorekeeper.app.data.MatchSnapshot
import com.scorekeeper.app.rules.DiceCategory
import com.scorekeeper.app.rules.EndType
import com.scorekeeper.app.rules.GameTemplate
import com.scorekeeper.app.rules.QuickPoint
import com.scorekeeper.app.rules.Templates
import com.scorekeeper.app.rules.WinCondition

@StringRes
fun GameTemplate.nameRes(): Int = when (id) {
    Templates.CARD_SUM_100.id -> R.string.template_card_sum_100_name
    Templates.PUSH_LUCK_200.id -> R.string.template_push_luck_200_name
    Templates.DICE.id -> R.string.template_dice_categories_name
    Templates.GOLF.id -> R.string.template_golf_name
    Templates.RUMMY.id -> R.string.template_rummy_name
    Templates.DOMINOES.id -> R.string.template_dominoes_name
    Templates.DOMINOES_HITS.id -> R.string.template_dominoes_hits_name
    Templates.TRUCO.id -> R.string.template_truco_name
    else -> R.string.template_free_name
}

@StringRes
fun GameTemplate.descriptionRes(): Int = when (id) {
    Templates.CARD_SUM_100.id -> R.string.template_card_sum_100_desc
    Templates.PUSH_LUCK_200.id -> R.string.template_push_luck_200_desc
    Templates.DICE.id -> R.string.template_dice_categories_desc
    Templates.GOLF.id -> R.string.template_golf_desc
    Templates.RUMMY.id -> R.string.template_rummy_desc
    Templates.DOMINOES.id -> R.string.template_dominoes_desc
    Templates.DOMINOES_HITS.id -> R.string.template_dominoes_hits_desc
    Templates.TRUCO.id -> R.string.template_truco_desc
    else -> R.string.template_free_desc
}

/** Passos do guia rápido de cada modelo, em ordem. */
fun GameTemplate.guideStepsRes(): List<Int> = when (id) {
    Templates.CARD_SUM_100.id -> listOf(R.string.guide_card_sum_1, R.string.guide_card_sum_2, R.string.guide_card_sum_3)
    Templates.PUSH_LUCK_200.id -> listOf(
        R.string.guide_push_luck_1, R.string.guide_push_luck_2, R.string.guide_push_luck_3, R.string.guide_push_luck_4,
    )
    Templates.DICE.id -> listOf(R.string.guide_dice_1, R.string.guide_dice_2, R.string.guide_dice_3, R.string.guide_dice_4)
    Templates.GOLF.id -> listOf(R.string.guide_golf_1, R.string.guide_golf_2, R.string.guide_golf_3)
    Templates.RUMMY.id -> listOf(R.string.guide_rummy_1, R.string.guide_rummy_2, R.string.guide_rummy_3)
    Templates.DOMINOES.id -> listOf(R.string.guide_dominoes_1, R.string.guide_dominoes_2, R.string.guide_dominoes_3)
    Templates.DOMINOES_HITS.id -> listOf(
        R.string.guide_hits_1, R.string.guide_hits_2, R.string.guide_hits_3, R.string.guide_hits_4,
    )
    Templates.TRUCO.id -> listOf(R.string.guide_truco_1, R.string.guide_truco_2, R.string.guide_truco_3, R.string.guide_truco_4)
    else -> listOf(R.string.guide_free_1, R.string.guide_free_2, R.string.guide_free_3)
}

@StringRes
fun QuickPoint.labelRes(): Int = when (id) {
    "hit_double" -> R.string.quick_hit_double
    "hit_both_ends" -> R.string.quick_hit_both_ends
    "hit_cross" -> R.string.quick_hit_cross
    "blocked" -> R.string.quick_blocked
    "truco_regular" -> R.string.quick_truco_regular
    "truco" -> R.string.quick_truco
    "truco_six" -> R.string.quick_truco_six
    "truco_nine" -> R.string.quick_truco_nine
    "truco_twelve" -> R.string.quick_truco_twelve
    else -> R.string.quick_hit_simple
}

@StringRes
fun DiceCategory.labelRes(): Int = when (this) {
    DiceCategory.ONES -> R.string.dice_ones
    DiceCategory.TWOS -> R.string.dice_twos
    DiceCategory.THREES -> R.string.dice_threes
    DiceCategory.FOURS -> R.string.dice_fours
    DiceCategory.FIVES -> R.string.dice_fives
    DiceCategory.SIXES -> R.string.dice_sixes
    DiceCategory.THREE_OF_A_KIND -> R.string.dice_three_of_a_kind
    DiceCategory.FOUR_OF_A_KIND -> R.string.dice_four_of_a_kind
    DiceCategory.FULL_HOUSE -> R.string.dice_full_house
    DiceCategory.SMALL_STRAIGHT -> R.string.dice_small_straight
    DiceCategory.LARGE_STRAIGHT -> R.string.dice_large_straight
    DiceCategory.FIVE_OF_A_KIND -> R.string.dice_five_of_a_kind
    DiceCategory.CHANCE -> R.string.dice_chance
}

/** Resumo das regras da partida, ex.: "Menor vence · acaba em 100". */
@Composable
fun rulesSummary(match: MatchSnapshot): String {
    val win = stringResource(
        if (match.win == WinCondition.LOWEST) R.string.match_lowest_wins else R.string.match_highest_wins
    )
    val end = when (match.template.endType) {
        EndType.TARGET_SCORE -> match.target
            ?.let { stringResource(R.string.match_ends_at_points, it) }
            ?: stringResource(R.string.match_no_end)

        EndType.ROUNDS -> match.target
            ?.let { pluralStringResource(R.plurals.match_rounds, it, it) }
            ?: stringResource(R.string.match_no_end)

        EndType.ALL_CATEGORIES -> stringResource(R.string.match_all_categories)
    }
    return "$win · $end"
}
