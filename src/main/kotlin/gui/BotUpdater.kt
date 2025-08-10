package gui

import entity.*
import service.*
import kotlin.math.*

class BotUpdater(private val root: RootService) : Refreshable {
    private var callBot = false

    private var updateThread: Thread? = null

    /** The delay between each move based on [entity.CollapsiGame.simulationSpeed]. */
    private val moveDelayMultiplier = 0.35

    private fun updateThread() {
        while (root.currentGame != null) {
            Thread.sleep(10)

            if (callBot) {
                callBot = false

                val game = checkNotNull(root.currentGame) { "No game is currently running." }
                val player = game.currentGame.currentPlayer

                root.botService.calculateBestTurn()

                // Move (with delay) until the game ends or the player switches.
                while (root.currentGame != null && game.currentGame.currentPlayer == player) {
                    Thread.sleep((game.simulationSpeed * moveDelayMultiplier * 1000).roundToLong())

                    root.botService.makeMove()
                }

                // Delay after a full turn.
                Thread.sleep((game.simulationSpeed * 1000).roundToLong())
            }
        }
    }

    override fun refreshAfterStartNewGame() {
        val thread = Thread { updateThread() }
        updateThread = thread
        thread.start()

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer

        if (player.type == PlayerType.BOT && game.simulationSpeed >= 0) {
            callBot = true
        }
    }

    override fun refreshAfterEndTurn() {
        check(!callBot) { "Turn ended without calling the bot." }
        val currentThread = checkNotNull(updateThread) { "Update thread was null." }
        check(currentThread.isAlive) { "The current update thread wasn't running." }

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer

        // Make the next move if the next player is a bot and the game isn't paused.
        if (player.type == PlayerType.BOT && game.simulationSpeed >= 0) {
            callBot = true
        }
    }
}