package service.bot.types

import service.bot.*

/**
 * The result of one recursive call of the [BotService.minimax] function.
 *
 * @param bestPath The best [Path] that the current player could take right now.
 * @param evaluation The [Evaluation] of the [bestPath].
 * @param estimatedEvaluation Whether the minimax had to use the [BotService.evaluate] method to guess the evaluation
 * of a state instead of doing a full search.
 *
 * If this is false, the [bestPath] is guaranteed
 * to be the best move, assuming both players play optimally and [evaluation] will show the player
 * who can win if both play optimally.
 *
 * @param aborted Whether the minimax was aborted due to having too little time. False if the minimax
 * reached maxDepth or the end of the game on every path.
 */
data class MinimaxResult(
    val bestPath: Path,
    val evaluation: Evaluation,
    val estimatedEvaluation: Boolean,
    val aborted: Boolean
)