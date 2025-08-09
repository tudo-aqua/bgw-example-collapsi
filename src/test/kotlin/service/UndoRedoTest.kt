package service

import entity.Coordinate
import entity.PlayerType
import kotlin.test.*

/**
 * Test class for the undo/redo functionality in the [GameService].
 */
class UndoRedoTest {
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
     * Test to check the undo functionality after making a move.
     */
    @Test
    fun testUndoAfterMove() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer
        val oldPosition = currentPlayer.position
        var newPosition = currentPlayer.position.rightNeighbour
        if (gameState.players[1].position == newPosition) {
            newPosition = currentPlayer.position.leftNeighbour
        }

        // Simulate a move.
        currentPlayer.remainingMoves = 2
        rootService.playerActionService.moveTo(newPosition)

        // Check that the move was made.
        assertEquals(1, currentPlayer.remainingMoves)
        assertEquals(gameState.currentPlayer.position, newPosition)

        // Undo the move.
        rootService.playerActionService.undo()

        // Check that the move was undone.
        assertEquals(1, currentPlayer.remainingMoves)
        assertEquals(gameState.currentPlayer.position, oldPosition)
    }

    /**
     * Test to check the redo functionality after an undo.
     */
    @Test
    fun testRedoAfterUndo() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val currentPlayer = gameState.currentPlayer
        val oldPosition = currentPlayer.position
        var newPosition = currentPlayer.position.rightNeighbour
        if (gameState.players[1].position == newPosition) {
            newPosition = currentPlayer.position.leftNeighbour
        }

        // Simulate a move.
        currentPlayer.remainingMoves = 2
        rootService.playerActionService.moveTo(newPosition)

        // Undo the move.
        rootService.playerActionService.undo()

        // Redo the move.
        rootService.playerActionService.redo()

        // Check that the move was redone.
        assertEquals(2, currentPlayer.remainingMoves)
        assertEquals(gameState.currentPlayer.position, newPosition)
    }
}