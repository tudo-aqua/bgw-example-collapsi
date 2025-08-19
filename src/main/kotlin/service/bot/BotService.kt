package service.bot

import entity.*
import service.*
import java.util.Date
import kotlin.random.Random

/**
 * A [Path] is a sequence of legal moves that a player can perform.
 *
 * It is characterized by a list of [Coordinate]s the player will move to during their turn.
 * This does not include the current position of the player.
 */
typealias Path = List<Coordinate>

/**
 * An evaluation of a [GameState].
 * Value at index i is the evaluation from the pov of player i.
 *
 * In classic minimax, there are only 2 players, so a player's success is directly correlated to
 * the failure of the opponent, which can both be represented using a single [Double] value.
 * One player therefore tries to maximize that value while the other one tries to minimize it
 * (hence the term "minimax").
 *
 * However, in a game with more than 2 players, it wouldn't make sense for everyone to make all
 * of their decisions based on the standing of one single player, so a list of evaluations is required.
 * This is also known as the max^n algorithm, but is still often referred to as minimax.
 *
 * @see BotService.minimax
 * @see BotService.evaluate
 */
typealias Evaluation = List<Double>

/**
 * Service class for the bot functionality in the Collapsi game.
 * This class is responsible for managing bot players and their actions.
 *
 * @param root The root service that provides access to the overall game state.
 */
class BotService(private val root: RootService) {
    /**
     * A list of moves the bot intends to do during their turn.
     * This is calculated at the start of their turn in [calculateTurn] and acted upon as the gui calls [makeMove].
     *
     * Note: The idea behind the entity-service-gui pattern is for the entire game state to be stored in
     * the entity-layer and for every service to remain stateless.
     *
     * The [BotService] as well as the [NetworkService] are some of the only classes that are occasionally
     * allowed to break this rule. This is because the variables stored there are not related to the state
     * of the game, but rather the plan of the bot or the state of the network respectively.
     *
     * @see calculateTurn
     * @see makeMove
     */
    val intendedMoves = mutableListOf<Coordinate>()

    /**
     * The helper class for the [BotService]. Contains deterministic methods only relevant for the bot.
     */
    val helper = BotHelper(root)

    val winEval = 500.0

    val lossEval = -499.0

    /**
     * Precalculate the exact order of moves to perform in the upcoming turn.
     *
     * This needs to be called before [makeMove] for all types of bots.
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if the current player is not a bot.
     *
     * @see makeMove
     */
    fun calculateTurn() {
        val oldGame = checkNotNull(root.currentGame) { "No game is currently running." }

        // Clone the GameState, so we can work on a separate instance without disturbing the original.
        // Tip: If the bot should not have access to certain information (such as the draw stack in other games),
        // then you could remove that data here.
        val game = CollapsiGame(oldGame.currentGame.clone())
        val currentPlayer = oldGame.currentGame.currentPlayer

        check(currentPlayer.type == PlayerType.BOT) { "Tried to make a bot move for a non-bot player." }
        check(root.playerActionService.hasValidMove()) { "Bot did not have any valid moves." }
        check(currentPlayer.visitedTiles.isEmpty()) { "Tried to calculate a turn for a player that already moved." }
        check(currentPlayer.botDifficulty in 1..4) { "Bot difficulty needs to be between 1 and 4 (inclusive)." }
        check(intendedMoves.isEmpty()) { "Tried to calculate move when upcoming moves were already set." }

        val path = when (currentPlayer.botDifficulty) {
            1 -> getLevel1Path(game)
            2 -> getLevel2Path(game)
            3 -> getLevel3Path(game)
            4 -> getLevel4Path(game)
            else -> throw IllegalStateException("Unsupported bot difficulty: ${currentPlayer.botDifficulty}")
        }

        check(path.size == currentPlayer.remainingMoves) { "The calculated path wasn't of the correct size." }

        intendedMoves.addAll(path)
    }

    /**
     * Move to the next tile.
     *
     * This method assumes [calculateTurn] was called beforehand and has found and saved an intended path.
     *
     * @throws IllegalStateException if there were no moves stored. This can happen if [calculateTurn] wasn't called.
     *
     * @see calculateTurn
     * @see PlayerActionService.moveTo
     */
    fun makeMove() {
        check(intendedMoves.isNotEmpty()) { "Tried to make a move without having calculated first." }

        root.playerActionService.moveTo(intendedMoves.removeFirst())
    }

    /**
     * Calculate the [Path] for bot level 1: Random.
     *
     * This bot player simply looks at all the possible end positions and picks a random one to go to.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     */
    private fun getLevel1Path(game: CollapsiGame): Path {
        return getRandomPath(game)
    }

    /**
     * Calculate the [Path] for bot level 2: Minimax at depth 6 for 0.4s at 33% accuracy.
     *
     * The bot will pick the best path it can find up to depth 6 with 0.4s with a 33% likelihood or
     * a random path at 67%.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @see getRandomPath
     * @see startTimedMinimax
     * @see minimax
     * @see Path
     */
    private fun getLevel2Path(game: CollapsiGame): Path {
        return if (Random.nextDouble() < 0.33)
            startTimedMinimax(game, 6, 400)
        else
            getRandomPath(game)
    }

    /**
     * Calculate the [Path] for bot level 3: Minimax at depth 6 for 0.4s at 67% accuracy.
     *
     * The bot will pick the best path it can find up to depth 6 with 0.4s with a 67% likelihood or
     * a random path at 33%.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @see getRandomPath
     * @see startTimedMinimax
     * @see minimax
     * @see Path
     */
    private fun getLevel3Path(game: CollapsiGame): Path {
        return if (Random.nextDouble() < 0.67)
            startTimedMinimax(game, 6, 400)
        else
            getRandomPath(game)
    }

    /**
     * Calculate the [Path] for bot level 4: Minimax at variable depth.
     *
     * This is the strongest available bot in this version of Collapsi.
     * It calculates for up to 3 seconds.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @see startTimedMinimax
     * @see minimax
     * @see Path
     */
    private fun getLevel4Path(game: CollapsiGame): Path {
        return startTimedMinimax(game, Int.MAX_VALUE, 3000)
    }

    /**
     * Return a random valid path.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     */
    private fun getRandomPath(game: CollapsiGame): Path {
        val possiblePaths = helper.getPossibleUniquePaths(game)
        check(possiblePaths.isNotEmpty()) { "Possible paths was empty." }

        return possiblePaths[Random.nextInt(possiblePaths.size)]
    }

    /**
     * Finds the best path according to the recursive [minimax].
     *
     * This function increases the depth until [maxDepth] or [maxDuration] is reached.
     * In essence, it turns [minimax] into a breadth-first search where only the last complete layer is used.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     * @param maxDepth The maximum depth the minimax can search at. Depth increases from 1 to this value.
     * @param maxDuration The maximum amount of time in milliseconds that the minimax can calculate for.
     *
     * @return The best path.
     *
     * @see minimax
     * @see Path
     */
    private fun startTimedMinimax(game: CollapsiGame, maxDepth: Int, maxDuration: Int): Path {
        val log = false

        var currentDepth = 1
        val maxTime = Date().time + maxDuration

        var bestResult: MinimaxResult? = null;

        while (currentDepth <= maxDepth) {
            if (log) println("Searching at depth $currentDepth.")
            val minimaxResult = minimax(currentDepth, maxTime, game)

            if (minimaxResult.aborted) {
                if (log) println("Ran out of time at depth $currentDepth.")
                break
            }

            bestResult = minimaxResult

            if (!minimaxResult.estimatedEvaluation) {
                if (log) println("Fully evaluated all paths at depth $currentDepth.")
                break
            }

            currentDepth++
        }

        checkNotNull(bestResult) { "Minimax couldn't find any path in time." }

        if (log) println("Evaluation: ${bestResult.evaluation}.")

        return bestResult.bestPath
    }

    /**
     * Searches recursively to find the best next turn.
     *
     * Essentially, minimax tries every possible path a player can take.
     * Then it tries every path the next player can take. And so on.
     *
     * You can think of this like a tree, where each node represents a [GameState], the root is
     * [RootService.currentGame] and the edges are [Path]s (actions).
     *
     * The number of states to look at will increase exponentially; hence we include a [maxDepth] parameter
     * to stop the search after a certain number of moves has been made.
     *
     * At the leaves of the tree, the [evaluate] function decides how favourable the [GameState] is
     * for every player.
     *
     * These [Evaluation]s are then propagated upwards. Minimax assumes that every player will make
     * the decision that benefits themselves the most. If that assumption happens to be wrong,
     * that player has put themselves at a disadvantage anyway. (Note: This logic only works for 2-player
     * minimax; see [Evaluation])
     *
     * For Collapsi in particular, it doesn't make sense to look at the individual steps of a turn.
     * Only the final position matters, which is why we simulate a full path of one player
     * per iteration of the for-loop.
     *
     * @param maxDepth The remaining depth to search. Must be at least 1.
     * @param maxTime The timestamp at which this method aborts.
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @return The result of the minimax including the best path and its evaluation.
     * See [MinimaxResult] for more info.
     *
     * @see Evaluation
     * @see evaluate
     * @see MinimaxResult
     */
    private fun minimax(maxDepth: Int, maxTime: Long, game: CollapsiGame): MinimaxResult {
        require(maxDepth >= 1) { "Depth must be at least 1." }

        if (Date().time > maxTime)
            return MinimaxResult(listOf(), listOf(), estimatedEvaluation = false, aborted = true)

        val currentPlayerIndex = game.currentGame.currentPlayerIndex

        // Get all paths the player could take right now.
        val possiblePaths = helper.getPossibleUniquePaths(game)

        check(possiblePaths.isNotEmpty()) { "Bot could not find any possible paths." }

        // We only care about the path with the best result.
        var bestPath: Path? = null
        var bestEval: Evaluation? = null
        var maxDepthReached = false

        for (currentPath in possiblePaths) {
            // Try out the path.
            currentPath.forEach { root.playerActionService.moveTo(it, game) }
            root.gameService.endTurn(game)

            val gameEnded = game.currentGame.players.count { it.alive } == 1

            // Evaluate the current position depending on depth.
            // If the maxDepth or maxTime was reached or the game ended, we stop here.
            val currentEval: Evaluation
            if (maxDepth > 1 && !gameEnded) {
                val minimaxResult = minimax(maxDepth - 1, maxTime, game)
                if (minimaxResult.aborted)
                    return minimaxResult

                currentEval = minimaxResult.evaluation
                maxDepthReached = maxDepthReached || minimaxResult.estimatedEvaluation
            } else {
                currentEval = evaluate(game)
                maxDepthReached = maxDepthReached || maxDepth <= 1
            }

            // Update bestResult if the current result is better.
            if (bestEval == null || currentEval[currentPlayerIndex] > bestEval[currentPlayerIndex]
            ) {
                bestPath = currentPath
                bestEval = currentEval
            }

            // Undo all the moves.
            repeat(currentPath.size) { root.playerActionService.undo(game) }

            // If this path results in a win, take it.
            if (bestEval[currentPlayerIndex] >= winEval)
                break
        }

        // Cast to non-nullable.
        checkNotNull(bestPath) { "Minimax couldn't find any paths." }
        checkNotNull(bestEval) { "Minimax couldn't find any evaluation." }

        return MinimaxResult(bestPath, bestEval, maxDepthReached, aborted = false)
    }

    /**
     * Evaluates the current [GameState] for each player.
     *
     * The evaluation function is a key component for a minimax bot. In games where the players have
     * a lot of options every turn, this function is where most of the bots intelligence comes from.
     *
     * If we could simulate every possible continuation of a state in [minimax],
     * this function would simply return -1 for a loss and +1 for a win.
     * But because of how the size of the tree scales exponentially with depth, this becomes impossible to rely on,
     * which is why we need to estimate how good a [GameState] is using heuristics and testing.
     *
     * In a lot of other games this is usually done by looking at victory points, resources, income, etc.
     *
     * In Collapsi, the best we can do is to correlate the evaluation directly to how many moves the player
     * can still make from this position.
     *
     * Of course, dying or winning is always more important and will therefore override the other evaluation.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @return The evaluation of the current state.
     *
     * @see Evaluation
     * @see minimax
     */
    private fun evaluate(game: CollapsiGame): Evaluation {
        val gameState = game.currentGame

        val gameEnded = gameState.players.count { it.alive } == 1

        val eval: Evaluation = List(gameState.players.size) { playerIndex ->
            val player = gameState.players[playerIndex]

            if (!player.alive) // Player died. Gain a penalty.
                lossEval
            else if (gameEnded) // Player won. Earn a reward.
                winEval
            else // Game still going. Associate more movement options with a better evaluation.
                helper.getPossibleUniquePathsForPlayer(player.color, game).size.toDouble()
        }

        return eval
    }
}