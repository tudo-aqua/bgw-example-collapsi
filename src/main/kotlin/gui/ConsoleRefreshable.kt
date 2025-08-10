package gui

import entity.Coordinate
import entity.Player
import service.RootService

class ConsoleRefreshable(val root: RootService) : Refreshable {
    override fun refreshAfterStartNewGame() {
        println("[Refresh] Start New Game:")

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        println("- ${gameState.boardSize}x${gameState.boardSize} board")
        println("- ${gameState.players.size} players")
        println("- ${gameState.currentPlayer.color} starts")
        println()
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        println("[Refresh] Move To:")

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        println("- ${gameState.currentPlayer.color} moved from $from to $to")
        println("- ${gameState.currentPlayer.remainingMoves} remaining moves")
        println()
    }

    override fun refreshAfterEndTurn() {
        println("[Refresh] End Turn:")

        val game = checkNotNull(root.currentGame) { "No game is currently running" }
        val gameState = game.currentGame

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