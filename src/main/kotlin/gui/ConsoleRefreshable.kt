package gui

import entity.Coordinate
import entity.Player
import entity.PlayerType
import service.RootService

/**
 * Refreshable that listens to all refreshes and logs them to the console.
 *
 * This can be useful for debugging or for understanding the game state without the need for a GUI.
 */
class ConsoleRefreshable(private val root: RootService) : Refreshable {
    override fun refreshAfterStartNewGame() {
        println("[Refresh] Start New Game:")

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState

        println("- ${gameState.boardSize}x${gameState.boardSize} board")
        println("- ${gameState.players.size} players")
        for (player in gameState.players) {
            val playerTypeString = when (player.type) {
                PlayerType.LOCAL -> "Local"
                PlayerType.BOT -> "Lvl. ${player.botDifficulty} Bot"
                PlayerType.REMOTE -> "Remote"
            }
            println("- ${player.color} ($playerTypeString) starts on ${player.position}")
        }
        println("- ${gameState.currentPlayer.color} is first to move.")
        println()
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        println("[Refresh] Move To:")

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState

        println("- ${gameState.currentPlayer.color} moved from $from to $to")
        println("- ${gameState.currentPlayer.remainingMoves} remaining moves")
        println()
    }

    override fun refreshAfterEndTurn() {
        println("[Refresh] End Turn:")

        val game = checkNotNull(root.currentGame) { "No game is currently running" }
        val gameState = game.currentState

        println("- ${gameState.currentPlayer.color} is now the current player")
        println("- ${gameState.currentPlayer.remainingMoves} remaining moves")
        println()
    }

    override fun refreshAfterPlayerDied(player: Player) {
        println("[Refresh] Player Died:")

        println("- ${player.color} couldn't move and the tile below collapsed")
        println()
    }

    override fun refreshAfterGameEnd(winner: Player) {
        println("[Refresh] Game Ended:")

        println("- ${winner.color} has won")
        println()
    }
}