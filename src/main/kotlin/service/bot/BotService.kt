package service.bot

import entity.CollapsiGame
import entity.Coordinate
import entity.PlayerType
import service.*
import kotlin.random.Random

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
    fun getLevel1Path(): List<Coordinate> {
        val possiblePaths = helper.getPossibleUniquePaths()
        check(possiblePaths.isNotEmpty()) { "Possible paths was empty." }

        return possiblePaths[Random.nextInt(possiblePaths.size)]
    }
}