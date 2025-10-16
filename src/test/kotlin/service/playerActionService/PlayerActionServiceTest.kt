package service.playerActionService

import entity.Coordinate
import entity.PlayerType
import service.RootService
import service.TestRefreshable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test class for the remaining helping functions of the [service.PlayerActionService].
 * This class tests the helping functions canMoveTo(...) and hasValidMove(...).
 */
class PlayerActionServiceTest {
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
     * Test to check the canMoveTo(...) function.
     */
    @Test
    fun testCanMoveTo() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Check that the player can move to an adjacent tile.
        var destination = currentPlayer.position.rightNeighbour
        if (destination == gameState.players[1].position) {
            destination = currentPlayer.position.leftNeighbour
        }
        assertTrue(rootService.playerActionService.canMoveTo(destination))

        // Check that the player cannot move to a non-adjacent tile.
        val nonAdjacentDestination = destination.upNeighbour
        assertFalse(rootService.playerActionService.canMoveTo(nonAdjacentDestination))
    }

    /**
     * Test to check the canMoveTo(...) function with non matching wrapping of the destination parameter.
     */
    @Test
    fun testCanMoveToWithWrongWrapping() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer
        var newPosition = currentPlayer.position.rightNeighbour
        if (newPosition == gameState.players[1].position) {
            newPosition = currentPlayer.position.leftNeighbour
        }

        // Check that the player cannot move to a tile with a different wrapping.
        val destination = Coordinate(newPosition.x + 1, newPosition.y, gameState.boardSize + 1)
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.canMoveTo(destination)
        }
    }

    /**
     * Test to check the canMoveTo(...) function with the current player not being alive.
     */
    @Test
    fun testCanMoveToWithDeadPlayer() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = gameState.currentPlayer

        // Set the player to not be alive.
        currentPlayer.alive = false

        // Check that the player cannot move to any tile.
        var destination = currentPlayer.position.rightNeighbour
        if (destination == gameState.players[1].position) {
            destination = currentPlayer.position.leftNeighbour
        }
        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.canMoveTo(destination)
        }
    }

    /**
     * Test to check the hasValidMove(...) function. After testing the method with at least one valid tile,
     * the player is set to not have any remaining moves and not having any valid tiles to move to which should
     * result in the method returning false.
     */
    @Test
    fun testHasValidMove() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val currentPlayer = game.currentState.currentPlayer

        // Check that there are valid tiles around the player.
        assertTrue(currentPlayer.position.neighbours.any {
            val neighbourTile = gameState.getTileAt(it)
            !gameState.isTileOccupied(it) || !neighbourTile.collapsed || !neighbourTile.visited
        })

        // Check that the player has valid moves.
        assertEquals(1, currentPlayer.remainingMoves)
        assertTrue(rootService.playerActionService.hasValidMove())

        // Set the player to have no valid tiles around.
        currentPlayer.position.neighbours.forEach {
            val neighbourTile = gameState.getTileAt(it)
            neighbourTile.collapsed = true
        }
        assertFalse(rootService.playerActionService.hasValidMove())
    }
}