package service.bot

import service.*
import entity.*
import kotlin.test.*

class BotStrengthTest {
    private var root = RootService()

    private var testRefreshable = TestRefreshable(root)

    @BeforeTest
    fun setup() {
        root = RootService()
        root.addRefreshable(testRefreshable)
    }

    @Test
    fun lvl1VsLvl1BoardSize4() {
        runMatch(
            difficulties = listOf(1, 1),
            boardSize = 4,
            iterations = 100
        )
    }

    @Test
    fun lvl2VsLvl2BoardSize4() {
        runMatch(
            difficulties = listOf(2, 2),
            boardSize = 4,
            iterations = 100
        )
    }

    @Test
    fun lvl1VsLvl2BoardSize4() {
        runMatch(
            difficulties = listOf(1, 2),
            boardSize = 4,
            iterations = 100
        )
    }

    @Test
    fun lvl1VsLvl2BoardSize5() {
        runMatch(
            difficulties = listOf(1, 2),
            boardSize = 5,
            iterations = 100
        )
    }

    @Test
    fun lvl1VsLvl2BoardSize6() {
        runMatch(
            difficulties = listOf(1, 2),
            boardSize = 6,
            iterations = 100
        )
    }

    @Test
    fun lvl1VsLvl3BoardSize4() {
        runMatch(
            difficulties = listOf(1, 3),
            boardSize = 4,
            iterations = 20
        )
    }

    @Test
    fun lvl2VsLvl3BoardSize4() {
        runMatch(
            difficulties = listOf(2, 3),
            boardSize = 4,
            iterations = 20
        )
    }

    @Test
    fun lvl1VsLvl4BoardSize4() {
        runMatch(
            difficulties = listOf(1, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    @Test
    fun lvl2VsLvl4BoardSize4() {
        runMatch(
            difficulties = listOf(2, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    @Test
    fun lvl3VsLvl4BoardSize4() {
        runMatch(
            difficulties = listOf(3, 4),
            boardSize = 4,
            iterations = 3
        )
    }

    fun runMatch(difficulties: List<Int>, boardSize: Int, iterations: Int) {
        // Wins sorted by color.
        val wins = MutableList(difficulties.size) { 0 }

        repeat(iterations) {
            root.gameService.startNewGame(
                playerTypes = List(difficulties.size) { PlayerType.BOT },
                botDifficulties = difficulties,
                boardSize = boardSize
            )

            runGame()

            assertTrue(testRefreshable.refreshAfterGameEndCalled)
            val winner = assertNotNull(testRefreshable.winner)
            wins[winner.color.ordinal]++
        }

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