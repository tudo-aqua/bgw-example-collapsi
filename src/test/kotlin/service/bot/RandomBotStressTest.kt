package service.bot

import entity.*
import org.junit.jupiter.api.RepeatedTest
import service.*
import kotlin.test.BeforeTest

/**
 * This class repeatedly runs full games on each possible setup using the random bot.
 *
 * The idea is to check if the game throws any errors in a random configuration.
 */
class RandomBotStressTest {
    private var root = RootService()

    val botDifficulty = 0

    @BeforeTest
    fun setup() {
        root = RootService()
    }

    @RepeatedTest(10)
    fun test2Player4x4Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 4
        )

        runGame()
    }

    @RepeatedTest(10)
    fun test2Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 5
        )

        runGame()
    }

    @RepeatedTest(10)
    fun test2Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 6
        )

        runGame()
    }

    @RepeatedTest(10)
    fun test3Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 5
        )

        runGame()
    }

    @RepeatedTest(10)
    fun test3Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        runGame()
    }

    @RepeatedTest(10)
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

            root.botService.calculateBestTurn()

            // Move until the game ends or the player switches.
            while (root.currentGame != null && gameState.currentPlayer == currentPlayer) {
                root.botService.makeMove()
            }
        }
    }
}