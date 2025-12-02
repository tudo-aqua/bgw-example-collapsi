package entity

import kotlin.test.*

/**
 * Test class for the [GameState] entity in the Collapsi game.
 */
class GameStateTest {

    /**
     * Tests the initialization and properties of a [GameState] instance.
     * This test checks that the [GameState] is correctly initialized with the specified players, board and board size,
     * and that the current PlayerIndex and the current player are set correctly.
     */
    @Test
    fun testGameStateInitialization() {
        val player1 = Player(PlayerColor.GREEN_SQUARE, Coordinate(0, 0, 4), PlayerType.LOCAL)
        val player2 = Player(PlayerColor.ORANGE_HEXAGON, Coordinate(1, 1, 4), PlayerType.LOCAL)
        val players = listOf(player1, player2)

        val tile1 = Tile(Coordinate(0, 0, 4), 1, PlayerColor.GREEN_SQUARE)
        val tile2 = Tile(Coordinate(1, 1, 4), 2, PlayerColor.ORANGE_HEXAGON)
        val board = mapOf(tile1.position to tile1, tile2.position to tile2)

        val gameState = GameState(players, board, 4)

        assertEquals(2, gameState.players.size)
        assertEquals(2, gameState.board.size)
        assertEquals(4, gameState.boardSize)
        assertEquals(0, gameState.currentPlayerIndex)
        assertEquals(player1, gameState.currentPlayer)

        gameState.currentPlayerIndex++

        assertEquals(1, gameState.currentPlayerIndex)
    }

    /**
     * Tests the `getTileAt` method of the [GameState] class.
     * This test checks that the method correctly retrieves a tile at a given position and throws an exception
     * if no tile exists at that position.
     */
    @Test
    fun testGetTileAt() {
        val player = Player(PlayerColor.GREEN_SQUARE, Coordinate(0, 0, 4), PlayerType.LOCAL)
        val tile = Tile(Coordinate(0, 0, 4), 1, PlayerColor.GREEN_SQUARE)
        val board = mapOf(tile.position to tile)
        val gameState = GameState(listOf(player), board, 4)

        assertEquals(tile, gameState.getTileAt(Coordinate(0, 0, 4)))

        assertFailsWith<IllegalStateException> {
            gameState.getTileAt(Coordinate(1, 1, 4))
        }
    }

    /**
     * Tests the `isTileOccupied` method of the [GameState] class.
     * This test checks that the method correctly identifies whether a tile is occupied by a player.
     */
    @Test
    fun testIsTileOccupied() {
        val player1 = Player(PlayerColor.GREEN_SQUARE, Coordinate(0, 0, 4), PlayerType.LOCAL)
        val player2 = Player(PlayerColor.ORANGE_HEXAGON, Coordinate(1, 1, 4), PlayerType.LOCAL)
        val board = mapOf(
            player1.position to Tile(player1.position, 1, player1.color),
            player2.position to Tile(player2.position, 2, player2.color)
        )
        val gameState = GameState(listOf(player1, player2), board, 4)

        assertTrue(gameState.isTileOccupied(Coordinate(0, 0, 4)))
        assertTrue(gameState.isTileOccupied(Coordinate(1, 1, 4)))
        assertFalse(gameState.isTileOccupied(Coordinate(2, 2, 4)))
    }

    /**
     * Tests the `nextPlayer` method of the [GameState] class.
     * This test checks that the method correctly sets the current player to the next living player.
     */
    @Test
    fun testNextPlayer() {
        val player1 = Player(PlayerColor.GREEN_SQUARE, Coordinate(0, 0, 4), PlayerType.LOCAL)
        val player2 = Player(PlayerColor.ORANGE_HEXAGON, Coordinate(1, 1, 4), PlayerType.LOCAL)
        val player3 = Player(PlayerColor.YELLOW_CIRCLE, Coordinate(2, 1, 4), PlayerType.LOCAL)
        val players = listOf(player1, player2, player3)
        val board = mapOf(
            player1.position to Tile(player1.position, 1, player1.color),
            player2.position to Tile(player2.position, 1, player2.color),
            player3.position to Tile(player3.position, 1, player3.color)
        )
        val gameState = GameState(players, board, 4)

        assertEquals(player1, gameState.currentPlayer)

        gameState.nextPlayer()
        assertEquals(player2, gameState.currentPlayer)

        gameState.nextPlayer()
        assertEquals(player3, gameState.currentPlayer)

        gameState.nextPlayer()
        assertEquals(player1, gameState.currentPlayer)

        player2.alive = false
        gameState.nextPlayer()
        assertEquals(player3, gameState.currentPlayer)
    }
}