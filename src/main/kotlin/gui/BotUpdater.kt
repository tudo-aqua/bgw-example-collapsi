package gui

import entity.PlayerType
import service.RootService

class BotUpdater(private val root: RootService) : Refreshable {
    private var callBot = false

    private var updateThread: Thread? = null

    override fun refreshAfterStartNewGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer

        val thread = Thread {
            while (root.currentGame != null) {
                Thread.sleep(10)

                if (callBot) {
                    callBot = false
                    root.botService.makeTurn()
                }
            }
        }
        updateThread = thread
        thread.start()

        if (player.type == PlayerType.BOT) {
            callBot = true
        }
    }

    override fun refreshAfterEndTurn() {
        check(!callBot) { "Turn ended without calling the bot." }
        val currentThread = checkNotNull(updateThread) { "Update thread was null." }
        check(currentThread.isAlive) { "The current update thread wasn't running." }

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer

        if (player.type == PlayerType.BOT) {
            callBot = true
        }
    }
}