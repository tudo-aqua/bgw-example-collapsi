package service.bot

import entity.*
import gui.ConsoleRefreshable
import service.*
import kotlin.test.BeforeTest
import kotlin.test.Test

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
    private var root = RootService()

    /**
     * The value of [Player.botDifficulty] for each bot.
     */
    val botDifficulty = 3

    /**
     * Setup function that creates a [RootService] with a [ConsoleRefreshable].
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        root.addRefreshable(ConsoleRefreshable(root))
    }

    /**
     * Test a 2-player game on a board of size 4.
     */
    @Test
    fun test2Player4x4Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 4
        )

        runGame()
    }

    /**
     * Test a 3-player game on a board of size 5.
     */
    @Test
    fun test3Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 5
        )

        runGame()
    }

    /**
     * Test a 4-player game on a board of size 6.
     */
    @Test
    fun test4Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        runGame()
    }

    /**
     * Calls [BotService.calculateTurn] and [BotService.makeMove] until the current game is over.
     * A game must have been started beforehand.
     *
     * @throws IllegalStateException if no game has been started.
     * @throws IllegalStateException if one of the players wasn't a bot.
     */
    fun runGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        check(gameState.players.all { it.type == PlayerType.BOT }) { "Tried to run a game with a non-bot player." }

        // Call the bots until the game is over.
        while (root.currentGame != null) {
            val currentPlayer = gameState.currentPlayer

            root.botService.calculateTurn()

            // Move until the player switches.
            while (gameState.currentPlayer == currentPlayer) {
                root.botService.makeMove()
            }
        }
    }
}