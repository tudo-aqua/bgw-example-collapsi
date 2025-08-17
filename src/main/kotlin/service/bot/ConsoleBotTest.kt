package service.bot

import entity.*
import service.*
import gui.*

/**
 * This class runs one full game on each board size using bots with identical difficulties and logs the games.
 *
 * The idea is to be able to debug constantly occurring problems in the bot or to supply a means
 * to enter debug mode without needing to start a game using the UI.
 */
class ConsoleBotTest {
    /**
     * The current [RootService].
     */
    private val root = RootService()

    private val helper = BotHelper(root)

    /**
     * The value of [Player.botDifficulty] for each bot.
     */
    val botDifficulty = 3

    /**
     * Test a 2-player game on a board of size 4.
     */
    fun test2Player4x4Board() {
        root.addRefreshable(ConsoleRefreshable(root))
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 4
        )

        helper.runGame()
    }

    /**
     * Test a 3-player game on a board of size 5.
     */
    fun test3Player5x5Board() {
        root.addRefreshable(ConsoleRefreshable(root))
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 5
        )

        helper.runGame()
    }

    /**
     * Test a 4-player game on a board of size 6.
     */
    fun test4Player6x6Board() {
        root.addRefreshable(ConsoleRefreshable(root))
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        helper.runGame()
    }
}