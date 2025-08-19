package service.playerActionService

import entity.Coordinate
import entity.PlayerType
import service.RootService
import service.TestRefreshable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Test class for the function moveTo(...) in the [service.PlayerActionService].
 */
class MoveToTest {
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
     * Test to move the current player to a new position and check if everything is updated correctly. This move is
     * the only step of the player's turn.
     */
    @Test
    fun testMoveToAsLastStep() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer

        // Move the player to a new position.
        var newPosition = currentPlayer.position.rightNeighbour
        if (newPosition == gameState.players[1].position) newPosition = currentPlayer.position.leftNeighbour
        val oldPosition = currentPlayer.position
        rootService.playerActionService.moveTo(newPosition)

        // Check that the player's position has been updated.
        assertEquals(newPosition, currentPlayer.position)

        // Check that the previous tile is marked as collapsed.
        assertTrue(gameState.getTileAt(oldPosition).collapsed)
    }

    /**
     * Test to move the current player to a new position and check if everything is updated correctly. This time the
     * player has remaining moves left after the move.
     */
    @Test
    fun testMoveToWithRemainingMoves() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer

        // Set the player to have remaining moves.
        currentPlayer.remainingMoves = 3

        // Move the player to a new position.
        var newPosition = currentPlayer.position.rightNeighbour
        if (newPosition == gameState.players[1].position) newPosition = currentPlayer.position.leftNeighbour
        val oldPosition = currentPlayer.position
        rootService.playerActionService.moveTo(newPosition)

        // Check that the player's position has been updated.
        assertEquals(newPosition, currentPlayer.position)

        // Check that the previous tile is marked as visited.
        assertTrue(gameState.getTileAt(oldPosition).visited)

        // Check that the previous tile is now part of the player's visited tiles.
        assertTrue(currentPlayer.visitedTiles.contains(oldPosition))
        assertEquals(1, currentPlayer.visitedTiles.size)

        // Check that the player has remaining moves left.
        assertEquals(2, currentPlayer.remainingMoves)
    }

    /**
     * Test to move the current player to a new position while the destination parameter has the wrong wrapping.
     */
    @Test
    fun testMoveToWithWrongWrapping() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer

        // Try to move the player to a new position with wrong wrapping.
        var newPosition = currentPlayer.position.rightNeighbour.copy(boardSize = 5)
        if (currentPlayer.position.rightNeighbour == gameState.players[1].position) {
            newPosition = currentPlayer.position.leftNeighbour.copy(boardSize = 5)
        }

        // Check that an exception is thrown.
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.moveTo(newPosition)
        }
    }

    /**
     * Test to move the current player to a position that is not adjacent to the player's current position.
     */
    @Test
    fun testMoveToNotAdjacent() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer

        // Try to move the player to a position that is not adjacent.
        val newPosition = Coordinate(currentPlayer.position.x + 2, currentPlayer.position.y, boardSize = 4)

        // Check that an exception is thrown.
        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.moveTo(newPosition)
        }
    }
}