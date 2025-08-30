package service.bot

import entity.*
import service.*
import java.util.Locale

/**
 * This class tests the improvements in bot strength over different levels by repeatedly running matches
 * between bots of varying strength and calculating/logging the win-percentages.
 */
class BotStrengthSimulation {
    /**
     * The current [RootService].
     */
    private val root = RootService()

    private val helper = BotHelper(root)

    /**
     * Fight between two lvl-1 bots as a sanity check. The result should be around 50% for both.
     */
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
    private fun runMatches(difficulties: List<Int>, boardSize: Int, iterations: Int) {
        // Wins sorted by color.
        val wins = MutableList(difficulties.size) { 0 }

        repeat(iterations) {
            root.gameService.startNewGame(
                playerTypes = List(difficulties.size) { PlayerType.BOT },
                botDifficulties = difficulties,
                boardSize = boardSize
            )

            // Cache game, because it will be set to null on the game end.
            val game = checkNotNull(root.currentGame)

            helper.runGame()

            // Award a point to the winner.
            val winner = game.currentGame.players.first { it.alive }
            wins[winner.color.ordinal]++
        }

        // Log the results in the console.
        println()
        println("Matches concluded!")
        println("- $iterations matches fought.")
        println("- ${boardSize}x${boardSize} board.")
        for ((i, difficulty) in difficulties.withIndex()) {
            val percentage = wins[i].toDouble() / iterations
            val percentageString = String.format(Locale.US, "%.1f", percentage * 100)
            println("- Lvl. $difficulty bot won ${wins[i]} (${percentageString}%)")
        }
        println()
    }
}