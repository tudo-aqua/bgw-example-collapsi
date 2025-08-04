package service.bot

import entity.Coordinate
import entity.PlayerType
import service.*
import kotlin.random.Random

/**
 * Service class for the bot functionality in the Collapsi game.
 * This class is responsible for managing bot players and their actions.
 *
 * @param root The root service that provides access to the overall game state.
 */
class BotService(val root: RootService) {
    fun makeTurn() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        check(player.type == PlayerType.BOT) { "Tried to make a bot move for a non-bot player." }
        check(root.playerActionService.hasValidMove()) { "Bot did not have any valid moves." }

        // Todo: The final version of the bot will only look at where it can end it's turn,
        // Todo: since the route to get there doesn't matter.
        while (root.currentGame != null && gameState.currentPlayer == player) {
            makeMove()
        }
    }

    fun makeMove() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        check(root.playerActionService.hasValidMove()) { "Bot did not have any valid moves." }
        val possibleMoves = getPossibleMoves()

        check(possibleMoves.isNotEmpty()) { "Bot did not have any valid moves." }

        val move = possibleMoves[Random.nextInt(possibleMoves.size)]

        // Todo: Replace this with a ConsoleRefreshable
        println("Moved ${game.currentGame.currentPlayer.color} to $move.")

        root.playerActionService.moveTo(move)
    }

    fun getPossibleMoves(): List<Coordinate> {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        return player.position.neighbours.filter { root.playerActionService.canMoveTo(it) }
    }

    // This returns the end position only.
    fun getPossibleTurns(): List<Coordinate> {
        TODO()
    }
}