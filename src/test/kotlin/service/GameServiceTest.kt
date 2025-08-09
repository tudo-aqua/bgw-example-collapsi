package service

import entity.PlayerType
import kotlin.test.*

/**
 * Test class for the [GameService] functionality.
 * This class tests the remaining (smaller) functions of the [GameService] class.
 */
class GameServiceTest {
    private var rootService = RootService()
    private var testRefreshable = TestRefreshable(rootService)

    /**
     * Setup function to attach a [TestRefreshable] to a new [RootService] before each test.
     */
    @BeforeTest
    fun setup() {
        rootService = RootService()
        testRefreshable = TestRefreshable(rootService)
        rootService.addRefreshable(testRefreshable)
    }

    /**
     * Test to end the game.
     */
    @Test
    fun testEndGame() {
        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0),
            boardSize = 4
        )

        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        assertNotNull(game.currentGame)

        rootService.gameService.endGame()

        assertNull(rootService.currentGame)
    }

    // Tests for load and save are still TODO.
}