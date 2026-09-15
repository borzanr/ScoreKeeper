package com.scorekeeper.app.rules

enum class WinCondition { HIGHEST, LOWEST }

/** Quando a partida acaba sozinha. O valor-alvo fica na partida, não no modelo. */
enum class EndType { TARGET_SCORE, ROUNDS, ALL_CATEGORIES }

/** Como a pontuação de uma célula é lançada. */
enum class EntryMode {
    /** Teclado numérico. */
    NUMBER,

    /** Tocar no valor de cada carta que sobrou (−2 a 12), com opção de digitar. */
    CARD_SUM,

    /** Cartas numéricas únicas, bônus, ×2 e estouro. */
    PUSH_LUCK,

    /** Cinco dados numa categoria da cartela. */
    DICE_CATEGORIES,

    /** Botões de valor fixo (tipo de batida, pedido de truco), com opção de digitar. */
    QUICK_POINTS,
}

/** Botão de pontuação rápida. [id] identifica o rótulo e fica salvo na célula. */
data class QuickPoint(val id: String, val points: Int)

/**
 * Regras de um tipo de jogo.
 *
 * Os nomes exibidos são genéricos de propósito: citar jogos de marca registrada
 * no app ou no ícone é motivo de remoção da Play. O nome comercial do jogo só
 * aparece na descrição da loja, como "compatível com".
 */
data class GameTemplate(
    val id: String,
    val entryMode: EntryMode,
    val win: WinCondition,
    val endType: EndType,
    val defaultTarget: Int? = null,
    val winEditable: Boolean = false,
    val targetEditable: Boolean = true,
    /** Quem encerra a rodada sem ter o menor total marca em dobro. */
    val closerDoubles: Boolean = false,
    /**
     * Só um lado pontua por rodada (quem ganhou a mão). Nesses jogos a rodada
     * nunca fica "completa", então o alvo encerra a partida na hora.
     */
    val oneScorerPerRound: Boolean = false,
    val quickPoints: List<QuickPoint> = emptyList(),
    val minPlayers: Int = 2,
    val maxPlayers: Int = 12,
)

object Templates {

    val FREE = GameTemplate(
        id = "free",
        entryMode = EntryMode.NUMBER,
        win = WinCondition.HIGHEST,
        endType = EndType.TARGET_SCORE,
        winEditable = true,
        minPlayers = 1,
    )

    val CARD_SUM_100 = GameTemplate(
        id = "card_sum_100",
        entryMode = EntryMode.CARD_SUM,
        win = WinCondition.LOWEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 100,
        closerDoubles = true,
        maxPlayers = 8,
    )

    val PUSH_LUCK_200 = GameTemplate(
        id = "push_luck_200",
        entryMode = EntryMode.PUSH_LUCK,
        win = WinCondition.HIGHEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 200,
        maxPlayers = 18,
    )

    val DICE = GameTemplate(
        id = "dice_categories",
        entryMode = EntryMode.DICE_CATEGORIES,
        win = WinCondition.HIGHEST,
        endType = EndType.ALL_CATEGORIES,
        targetEditable = false,
        minPlayers = 1,
        maxPlayers = 10,
    )

    val GOLF = GameTemplate(
        id = "golf",
        entryMode = EntryMode.NUMBER,
        win = WinCondition.LOWEST,
        endType = EndType.ROUNDS,
        defaultTarget = 9,
    )

    val RUMMY = GameTemplate(
        id = "rummy",
        entryMode = EntryMode.NUMBER,
        win = WinCondition.LOWEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 100,
    )

    /** Dominó por soma das pedras: quem bate marca o que sobrou na mão dos outros. */
    val DOMINOES = GameTemplate(
        id = "dominoes",
        entryMode = EntryMode.NUMBER,
        win = WinCondition.HIGHEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 100,
        oneScorerPerRound = true,
    )

    /** Dominó brasileiro por batidas, em geral jogado em duplas. */
    val DOMINOES_HITS = GameTemplate(
        id = "dominoes_hits",
        entryMode = EntryMode.QUICK_POINTS,
        win = WinCondition.HIGHEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 6,
        oneScorerPerRound = true,
        maxPlayers = 4,
        quickPoints = listOf(
            QuickPoint("hit_simple", 1),
            QuickPoint("hit_double", 2),
            QuickPoint("hit_both_ends", 3),
            QuickPoint("hit_cross", 4),
            QuickPoint("blocked", 1),
        ),
    )

    /** Truco paulista: dois lados, cada mão vale conforme o pedido. */
    val TRUCO = GameTemplate(
        id = "truco",
        entryMode = EntryMode.QUICK_POINTS,
        win = WinCondition.HIGHEST,
        endType = EndType.TARGET_SCORE,
        defaultTarget = 12,
        oneScorerPerRound = true,
        maxPlayers = 2,
        quickPoints = listOf(
            QuickPoint("truco_regular", 1),
            QuickPoint("truco", 3),
            QuickPoint("truco_six", 6),
            QuickPoint("truco_nine", 9),
            QuickPoint("truco_twelve", 12),
        ),
    )

    val ALL: List<GameTemplate> = listOf(
        FREE, CARD_SUM_100, PUSH_LUCK_200, DICE, GOLF, RUMMY, DOMINOES, DOMINOES_HITS, TRUCO,
    )

    /** Um id desconhecido (modelo removido numa versão futura) cai no placar livre. */
    fun byId(id: String): GameTemplate = ALL.firstOrNull { it.id == id } ?: FREE
}
