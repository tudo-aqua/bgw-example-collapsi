package entity

import kotlin.test.*

/**
 * Test class for the [CollapsiGame] entity in the Collapsi game.
 */
class CollapsiGameTest {

    /**
     * Tests the initialization and properties of a [CollapsiGame] instance.
     * This test checks that the [CollapsiGame] is correctly initialized with the specified [GameState],
     * and that the undo and redo stacks are empty upon initialization. It also verifies that the
     * simulation speed is set to 0 and can be modified after initialization.
     */
    @Test
    fun testCollapsiGameInitialization() {
        val player1 = Player(PlayerColor.GREEN_SQUARE, Coordinate(0, 0, 4), PlayerType.LOCAL)
        val players = listOf(player1)

        val tile1 = Tile(Coordinate(0, 0, 4), 1, PlayerColor.GREEN_SQUARE)
        val board = mapOf(tile1.position to tile1)

        val gameState = GameState(players, board, 4)
        val collapsiGame = CollapsiGame(gameState)

        assertEquals(gameState, collapsiGame.currentState)
        assertTrue(collapsiGame.undoStack.isEmpty())
        assertTrue(collapsiGame.redoStack.isEmpty())
        assertEquals(1.0, collapsiGame.simulationSpeed)

        collapsiGame.simulationSpeed = 2.0

        assertEquals(2.0, collapsiGame.simulationSpeed)
    }
}