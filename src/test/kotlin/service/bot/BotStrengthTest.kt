package service.bot

import service.*
import entity.*
import kotlin.test.*

/**
 * This class tests the improvements in bot strength over different levels by repeatedly running matches
 * between bots of varying strength and calculating/logging the win-percentages.
 */
class BotStrengthTest {
    /**
     * The current [RootService].
     */
    private var root = RootService()

    /**
     * The current [TestRefreshable].
     */
    private var testRefreshable = TestRefreshable(root)

    /**
     * Setup function that creates a [RootService] with a [TestRefreshable].
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        root.addRefreshable(testRefreshable)
    }

    /**
     * Fight between two lvl-1 bots as a sanity check. The result should be around 50% for both.
     */
    @Test
    fun lvl1VsLvl1BoardSize4() {
        runMatches(
            difficulties = listOf(1, 1),
            boardSize = 4,
            iterations = 100
        )
    }

    /**
     * Fight between two lvl-2 bots as a sanity check. The result should be around 50% for both.
     */
    @Test
    fun lvl2VsLvl2BoardSize4() {
        runMatches(
            difficulties = listOf(2, 2),
            boardSize = 4,
            iterations = 100
        )
    }

    /**
     * Tests a lvl-1 vs. a lvl-2 bot on a 4x4 board.
     */
    @Test
    fun lvl1VsLvl2BoardSize4() {
        runMatches(
            difficulties = listOf(1, 2),
            boardSize = 4,
            iterations = 100
        )
    }

    /**
     * Tests a lvl-1 vs. a lvl-2 bot on a 5x5 board.
     */
    @Test
    fun lvl1VsLvl2BoardSize5() {
        runMatches(
            difficulties = listOf(1, 2),
            boardSize = 5,
            iterations = 100
        )
    }

    /**
     * Tests a lvl-1 vs. a lvl-2 bot on a 6x6 board.
     */
    @Test
    fun lvl1VsLvl2BoardSize6() {
        runMatches(
            difficulties = listOf(1, 2),
            boardSize = 6,
            iterations = 100
        )
    }

    /**
     * Tests a lvl-1 vs. a lvl-3 bot on a 4x4 board.
     */
    @Test
    fun lvl1VsLvl3BoardSize4() {
        runMatches(
            difficulties = listOf(1, 3),
            boardSize = 4,
            iterations = 20
        )
    }

    /**
     * Tests a lvl-2 vs. a lvl-3 bot on a 4x4 board.
     */
    @Test
    fun lvl2VsLvl3BoardSize4() {
        runMatches(
            difficulties = listOf(2, 3),
            boardSize = 4,
            iterations = 20
        )
    }

    /**
     * Tests a lvl-1 vs. a lvl-4 bot on a 4x4 board.
     */
    @Test
    fun lvl1VsLvl4BoardSize4() {
        runMatches(
            difficulties = listOf(1, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    /**
     * Tests a lvl-2 vs. a lvl-4 bot on a 4x4 board.
     */
    @Test
    fun lvl2VsLvl4BoardSize4() {
        runMatches(
            difficulties = listOf(2, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    /**
     * Tests a lvl-3 vs. a lvl-4 bot on a 4x4 board.
     */
    @Test
    fun lvl3VsLvl4BoardSize4() {
        runMatches(
            difficulties = listOf(3, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    /**
     * Simulates many games with the same bot-player setup and calculates the win percentage for each.
     *
     * @param difficulties The difficulties of all participating players. This parameter also determines
     * how many players there are.
     * @param boardSize The size of the board that the matches take place on.
     * @param iterations The number of games to simulate.
     */
    fun runMatches(difficulties: List<Int>, boardSize: Int, iterations: Int) {
        // Wins sorted by color.
        val wins = MutableList(difficulties.size) { 0 }

        repeat(iterations) {
            root.gameService.startNewGame(
                playerTypes = List(difficulties.size) { PlayerType.BOT },
                botDifficulties = difficulties,
                boardSize = boardSize
            )

            runGame()

            // Award a point to the winner.
            assertTrue(testRefreshable.refreshAfterGameEndCalled)
            val winner = assertNotNull(testRefreshable.winner)
            wins[winner.color.ordinal]++
        }

        // Log the results in the console.
        println()
        println("Matches concluded!")
        println("- $iterations matches fought.")
        println("- ${boardSize}x${boardSize} board.")
        for ((i, difficulty) in difficulties.withIndex()) {
            val percentage = wins[i].toDouble() / iterations
            val percentageString = String.format("%.1f", percentage * 100)
            println("- Lvl. $difficulty bot won ${wins[i]} (${percentageString}%)")
        }
        println()
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