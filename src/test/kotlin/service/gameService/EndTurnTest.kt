package service.gameService

import entity.PlayerType
import service.RootService
import service.TestRefreshable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test class for the endTurn() functionality in the [service.GameService].
 */
class EndTurnTest {
    private var rootService = RootService()
    private var testRefreshable = TestRefreshable(rootService)

    /**
     * Setup function to start a new game with two players and a board size of 4x4 for each test.
     * Also attaches a [TestRefreshable] to a new [RootService] before each test.
     */
    @BeforeTest
    fun setup() {
        rootService = RootService()
        testRefreshable = TestRefreshable(rootService)
        rootService.addRefreshable(testRefreshable)

        rootService.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(0, 0),
            boardSize = 4
        )
    }

    /**
     * Test to end the turn of the current player´s first move in a game and check if everything is reset correctly.
     */
    @Test
    fun testEndTurn() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Check initial state.
        assertTrue(currentPlayer.alive)
        assertEquals(1, currentPlayer.remainingMoves)

        // Simulate the player making a move.
        currentPlayer.remainingMoves = 0

        // End the turn.
        rootService.gameService.endTurn()

        // Check that the next player is now the current player.
        assertNotEquals(currentPlayer, gameState.players[1])

        // Check that the previous player's visited tiles are reset.
        assertTrue(currentPlayer.visitedTiles.isEmpty())

        // Check that the previous player's remaining moves are reset to the tile's movesToMake.
        assertEquals(gameState.board[gameState.currentPlayer.position]?.movesToMake, currentPlayer.remainingMoves)
    }

    /**
     * Test to end the turn of a player who has remaining moves left.
     */
    @Test
    fun testEndTurnWithRemainingMoves() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Check initial state.
        assertTrue(currentPlayer.alive)
        assertEquals(1, currentPlayer.remainingMoves)

        // End the turn while asserting it to fail.
        assertFailsWith<IllegalStateException> { rootService.gameService.endTurn() }
    }

    /**
     * Test to end the turn of a player who has still valid moves to make.
     */
    @Test
    fun testEndTurnWithNoValidMoves() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Check initial state.
        assertTrue(currentPlayer.alive)
        assertEquals(1, currentPlayer.remainingMoves)

        // End the turn while asserting it to fail.
        assertFailsWith<IllegalStateException> { rootService.gameService.endTurn() }
    }

    /**
     * Test to end the turn ending the game.
     */
    @Test
    fun testEndTurnEndingGame() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Set the player to be no longer part of the game.
        currentPlayer.alive = false
        currentPlayer.remainingMoves = 0

        // End the turn.
        rootService.gameService.endTurn()

        // Check that the game is now over.
        assertNull(rootService.currentGame)
    }
}