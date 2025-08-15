package service.bot

import entity.*
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.assertDoesNotThrow
import service.*
import kotlin.test.BeforeTest

/**
 * This class repeatedly runs full games on each possible setup using the random bot.
 *
 * The idea is to check if the game throws any errors in a random configuration.
 */
class RandomBotStressTest {
    /**
     * The current [RootService].
     */
    private var root = RootService()

    /**
     * The value of [Player.botDifficulty] for each bot.
     */
    val botDifficulty = 1

    /**
     * Setup function that creates a [RootService].
     */
    @BeforeTest
    fun setup() {
        root = RootService()
    }

    /**
     * Test a 2-player game on a board of size 4.
     */
    @RepeatedTest(10)
    fun test2Player4x4Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 4
        )

        assertDoesNotThrow { runGame() }
    }

    /**
     * Test a 2-player game on a board of size 5.
     */
    @RepeatedTest(10)
    fun test2Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 5
        )

        assertDoesNotThrow { runGame() }
    }

    /**
     * Test a 2-player game on a board of size 6.
     */
    @RepeatedTest(10)
    fun test2Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty),
            boardSize = 6
        )

        assertDoesNotThrow { runGame() }
    }

    /**
     * Test a 3-player game on a board of size 5.
     */
    @RepeatedTest(10)
    fun test3Player5x5Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 5
        )

        assertDoesNotThrow { runGame() }
    }

    /**
     * Test a 3-player game on a board of size 6.
     */
    @RepeatedTest(10)
    fun test3Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        assertDoesNotThrow { runGame() }
    }

    /**
     * Test a 4-player game on a board of size 6.
     */
    @RepeatedTest(10)
    fun test4Player6x6Board() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.BOT, PlayerType.BOT, PlayerType.BOT, PlayerType.BOT),
            botDifficulties = listOf(botDifficulty, botDifficulty, botDifficulty, botDifficulty),
            boardSize = 6
        )

        assertDoesNotThrow { runGame() }
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