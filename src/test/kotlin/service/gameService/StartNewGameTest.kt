package service.gameService

import entity.PlayerType
import service.RootService
import service.TestRefreshable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test class for the function startNewGame(...) in the [service.GameService].
 */
class StartNewGameTest {
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
     * Test to start a new game with two players, a board size of 4x4 and valid parameters.
     */
    @Test
    fun testStartNewGameOfTwo() {
        assertNull(rootService.currentGame)

        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0),
            boardSize = 4
        )

        val game = rootService.currentGame
        assertNotNull(game)
        val currentState = game.currentState
        assertNotNull(currentState)

        assertEquals(2, currentState.players.size)
        assertEquals(2, currentState.players.count { it.type == PlayerType.LOCAL })
        assertEquals(16, currentState.board.size)
        assertEquals(4, currentState.boardSize)
        // Check that there are two starting tiles on the board.
        assertEquals(currentState.board.count { it.value.startTileColor != null && !it.value.collapsed }, 2)
        // Check that there are four tiles with a step value of 1 (+ the two starting Tiles).
        assertEquals(currentState.board.count { it.value.movesToMake == 1 && !it.value.collapsed }, 6)
        // Check that there are four tiles with a step value of 2.
        assertEquals(currentState.board.count { it.value.movesToMake == 2 }, 4)
        // Check that there are four tiles with a step value of 3.
        assertEquals(currentState.board.count { it.value.movesToMake == 3 }, 4)
        // Check that there are two tiles with a step value of 4.
        assertEquals(currentState.board.count { it.value.movesToMake == 4 }, 2)
    }

    /**
     * Test to start a new game with three players, a board size of 5x5 and valid parameters.
     */
    @Test
    fun testStartNewGameOfThree() {
        assertNull(rootService.currentGame)

        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0, 0),
            boardSize = 5
        )

        val game = rootService.currentGame
        assertNotNull(game)
        val currentState = game.currentState
        assertNotNull(currentState)

        assertEquals(3, currentState.players.size)
        assertEquals(3, currentState.players.count { it.type == PlayerType.LOCAL })
        assertEquals(25, currentState.board.size)
        assertEquals(5, currentState.boardSize)
        // Check that there are three starting tiles on the board.
        assertEquals(currentState.board.count { it.value.startTileColor != null && !it.value.collapsed }, 3)
        // Check that there are six tiles with a step value of 1 (+ the three starting Tiles).
        assertEquals(currentState.board.count { it.value.movesToMake == 1 && !it.value.collapsed }, 9)
        // Check that there are six tiles with a step value of 2.
        assertEquals(currentState.board.count { it.value.movesToMake == 2 }, 6)
        // Check that there are six tiles with a step value of 3.
        assertEquals(currentState.board.count { it.value.movesToMake == 3 }, 6)
        // Check that there are four tiles with a step value of 4.
        assertEquals(currentState.board.count { it.value.movesToMake == 4 }, 4)
    }

    /**
     * Test to start a new game with four players, a board size of 6x6 and valid parameters.
     */
    @Test
    fun testStartNewGameOfFour() {
        assertNull(rootService.currentGame)

        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL, PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0, 0, 0),
            boardSize = 6
        )

        val game = rootService.currentGame
        assertNotNull(game)
        val currentState = game.currentState
        assertNotNull(currentState)

        assertEquals(4, currentState.players.size)
        assertEquals(4, currentState.players.count { it.type == PlayerType.LOCAL })
        assertEquals(36, currentState.board.size)
        assertEquals(6, currentState.boardSize)
        // Check that there are four starting tiles on the board.
        assertEquals(currentState.board.count { it.value.startTileColor != null }, 4)
        // Check that there are eight tiles with a step value of 1 (+ the four starting Tiles).
        assertEquals(currentState.board.count { it.value.movesToMake == 1 }, 12)
        // Check that there are eight tiles with a step value of 2.
        assertEquals(currentState.board.count { it.value.movesToMake == 2 }, 8)
        // Check that there are eight tiles with a step value of 3.
        assertEquals(currentState.board.count { it.value.movesToMake == 3 }, 8)
        // Check that there are eight tiles with a step value of 4.
        assertEquals(currentState.board.count { it.value.movesToMake == 4 }, 8)
    }

    /**
     * Test to start a new game with an invalid number of players.
     */
    @Test
    fun testStartNewGameWithInvalidPlayerCount() {
        assertNull(rootService.currentGame)

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(
                playerTypes = listOf(PlayerType.LOCAL),
                botDifficulties = listOf(0, 0),
                boardSize = 4
            )
        }

        assertNull(rootService.currentGame)
    }

    /**
     * Test to start a new game with an invalid number of bot difficulties.
     */
    @Test
    fun testStartNewGameWithInvalidBotDifficulties() {
        assertNull(rootService.currentGame)

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(
                playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
                botDifficulties = listOf(0),
                boardSize = 4
            )
        }

        assertNull(rootService.currentGame)
    }

    /**
     * Test to start a new game with an invalid board size.
     */
    @Test
    fun testStartNewGameWithInvalidBoardSize() {
        assertNull(rootService.currentGame)

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(
                playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
                botDifficulties = listOf(0, 0),
                boardSize = 3
            )
        }

        assertNull(rootService.currentGame)
    }

    /**
     * Test to start a new game with an invalid board size needed for a special amount of players.
     */
    @Test
    fun testStartNewGameWithInvalidBoardSizeForPlayerCount() {
        assertNull(rootService.currentGame)

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(
                playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL, PlayerType.LOCAL),
                botDifficulties = listOf(0, 0, 0),
                boardSize = 4
            )
        }

        assertNull(rootService.currentGame)
    }

    /**
     * Test to start a new game while one is already in progress.
     */
    @Test
    fun testStartNewGameWhileOneIsInProgress() {
        assertNull(rootService.currentGame)

        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0),
            boardSize = 4
        )

        assertNotNull(rootService.currentGame)

        assertFailsWith<IllegalStateException> {
            rootService.gameService.startNewGame(
                playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
                botDifficulties = listOf(0, 0),
                boardSize = 4
            )
        }

        assertNotNull(rootService.currentGame)
    }
}