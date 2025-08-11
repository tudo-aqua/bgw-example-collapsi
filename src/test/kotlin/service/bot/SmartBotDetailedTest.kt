package service.bot

import entity.*
import gui.ConsoleRefreshable
import service.*
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * This class runs one full game on each board size using the smart bot and logs the games.
 *
 * The idea is to be able to debug constantly occurring problems in the bot or to supply a means
 * to enter debug mode without needing to start a game using the UI.
 */
class SmartBotDetailedTest {
    private var root = RootService()

    val botDifficulty = 4

    @BeforeTest
    fun setup() {
        root = RootService()
        root.addRefreshable(ConsoleRefreshable(root))
    }

    @Test
    fun test2Player4x4Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 4
        )

        runGame()
    }

    @Test
    fun test3Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 5
        )

        runGame()
    }

    @Test
    fun test4Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        runGame()
    }

    fun runGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        // Call the bots until the game is over.
        while (root.currentGame != null) {
            val currentPlayer = gameState.currentPlayer

            root.botService.calculateTurn()

            // Move until the game ends or the player switches.
            while (root.currentGame != null && gameState.currentPlayer == currentPlayer) {
                root.botService.makeMove()
            }
        }
    }
}