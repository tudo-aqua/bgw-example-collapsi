package service

import entity.PlayerType
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.assertDoesNotThrow
import service.bot.BotHelper
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

    private var botHelper = BotHelper(root)

    /**
     * The value of [entity.Player.botDifficulty] for each bot.
     */
    val botDifficulty = 1

    /**
     * Setup function that creates a [RootService].
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        botHelper = BotHelper(root)
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

        assertDoesNotThrow { botHelper.runGame() }
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

        assertDoesNotThrow { botHelper.runGame() }
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

        assertDoesNotThrow { botHelper.runGame() }
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

        assertDoesNotThrow { botHelper.runGame() }
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

        assertDoesNotThrow { botHelper.runGame() }
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

        assertDoesNotThrow { botHelper.runGame() }
    }
}