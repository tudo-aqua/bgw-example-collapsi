package service.bot

import entity.*
import service.*
import kotlin.random.Random

/**
 * A [Path] is a sequence of legal moves that a player can perform.
 */
typealias Path = List<Coordinate>

/**
 * An evaluation of a [GameState].
 * Value at index i is the evaluation from the pov of player i.
 *
 * In classic minimax, there are only 2 players, so a player's success is directly correlated to
 * the failure of the opponent, which can both be represented as a single Double value.
 *
 * However, in a game with more than 2 players, it wouldn't make sense for everyone to make all
 * of their decisions based on the standing of one single player, so a list of evaluations is required.
 */
typealias Evaluation = List<Double>

/**
 * Service class for the bot functionality in the Collapsi game.
 * This class is responsible for managing bot players and their actions.
 *
 * @param mainRootService The root service that provides access to the overall game state.
 */
class BotService(private val mainRootService: RootService) {
    val intendedMoves = mutableListOf<Coordinate>()

    lateinit var root: RootService

    lateinit var helper: BotHelper

    // Todo: For debugging only. Remove in final version.
    val random = Random(1)

    fun calculateTurn() {
        val oldGame = checkNotNull(mainRootService.currentGame) { "No game is currently running." }
        root = RootService()
        helper = BotHelper(root)

        val game = CollapsiGame(oldGame.currentGame.clone())
        root.currentGame = game

        val gameState = oldGame.currentGame
        val player = gameState.currentPlayer

        check(player.type == PlayerType.BOT) { "Tried to make a bot move for a non-bot player." }
        check(mainRootService.playerActionService.hasValidMove()) { "Bot did not have any valid moves." }
        check(player.visitedTiles.isEmpty()) { "Tried to calculate a turn for a player that already moved." }
        check(player.botDifficulty in 1..4) { "Bot difficulty needs to be between 1 and 4 (inclusive)." }
        check(intendedMoves.isEmpty()) { "Tried to calculate move when upcoming moves were already set." }

        val path = when (player.botDifficulty) {
            1 -> getLevel1Path()
            2 -> getLevel2Path()
            3 -> getLevel3Path()
            4 -> getLevel4Path()
            else -> throw IllegalStateException("Unsupported bot difficulty: ${player.botDifficulty}")
        }

        check(path.size == player.remainingMoves) { "The calculated path wasn't of the correct size." }

        intendedMoves.addAll(path)
    }

    fun makeMove() {
        check(intendedMoves.isNotEmpty()) { "Tried to make a move without having calculated first." }

        mainRootService.playerActionService.moveTo(intendedMoves.removeFirst())
    }

    /**
     * Bot player level 1: Random
     *
     * This bot player simply looks at all the possible end positions and picks a random one to go to.
     */
    fun getLevel1Path(): Path {
        val possiblePaths = helper.getPossibleUniquePaths()
        check(possiblePaths.isNotEmpty()) { "Possible paths was empty." }

        return possiblePaths[Random.nextInt(possiblePaths.size)]
    }

    fun getLevel2Path(): Path {
        return minimax(1).first
    }

    fun getLevel3Path(): Path {
        return minimax(4).first
    }

    fun getLevel4Path(): Path {
        return minimax(8).first
    }

    /**
     * Searches recursively to find the best next turn.
     *
     * @param depth The remaining depth to search. Must be at least 1.
     *
     * @return The path to take this turn and the evaluation this will lead to.
     */
    fun minimax(depth: Int): Pair<Path, Evaluation> {
        require(depth >= 1) { "Depth must be at least 1." }

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentPlayerIndex = game.currentGame.currentPlayerIndex

        val possiblePaths = helper.getPossibleUniquePaths()

        var bestPath: Path? = null
        var bestEval: Evaluation? = null

        for (currentPath in possiblePaths) {
            // Try out the path.
            currentPath.forEach { root.playerActionService.moveTo(it) }

            // Undo the game being set to null in endGame().
            val gameEnded = root.currentGame == null
            root.currentGame = game

            val currentEval: Evaluation

            // Evaluate the current position depending on depth.
            if (depth > 1 && !gameEnded) {
                currentEval = minimax(depth - 1).second
            } else {
                currentEval = evaluate()
            }

            // Update bestResult if the current result is better.
            if (bestEval == null || currentEval[currentPlayerIndex] > bestEval[currentPlayerIndex]
            ) {
                bestPath = currentPath
                bestEval = currentEval
            }

            // Undo all the moves.
            repeat(currentPath.size) { root.playerActionService.undo() }
        }

        checkNotNull(bestPath) { "Minimax couldn't find any paths." }
        checkNotNull(bestEval) { "Minimax couldn't find any evaluation." }

        return Pair(bestPath, bestEval)
    }

    /**
     * Evaluates the current state by checking how many valid paths each player can take from this position.
     *
     * @return The evaluation of the current state.
     */
    fun evaluate(): Evaluation {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        val gameEnded = gameState.players.count { it.alive } == 1

        val eval: Evaluation = List(gameState.players.size) { playerIndex ->
            val player = gameState.players[playerIndex]

            if (!player.alive) // Player died.
                -50.0
            else if (gameEnded) // Player won.
                50.0
            else
                helper.getPossibleUniquePathsForPlayer(player.color).size.toDouble()
        }

        return eval
    }
}