package entity

import kotlin.test.*

/**
 * Test class for the [Player] entity in the Collapsi game.
 */
class PlayerTest {

    /**
     * Tests the initialization and properties of a [Player] instance.
     * This test checks that the [Player] is correctly initialized with the specified color, type, and position, as well
     * as the default values for remaining moves, visited tiles, and alive status.
     * It also verifies that the [Player]s properties for position, remaining moves, visited Tiles and the alive status
     * can be modified after initialization.
     */
    @Test
    fun testPlayerInitialization() {
        val player = Player(
            color = PlayerColor.GREEN_SQUARE,
            type = PlayerType.LOCAL,
            position = Coordinate(3, 2, 4)
        )

        assertEquals(PlayerColor.GREEN_SQUARE, player.color)
        assertEquals(PlayerType.LOCAL, player.type)
        assertEquals(Coordinate(3, 2, 4), player.position)
        assertEquals(0, player.remainingMoves)
        assertTrue(player.visitedTiles.isEmpty())
        assertTrue(player.alive)

        player.position = Coordinate(4, 2, 4)
        player.remainingMoves++
        player.visitedTiles.add(Tile(Coordinate(3, 2, 4), 1, null))
        player.alive = false

        assertEquals(Coordinate(4, 2, 4), player.position)
        assertEquals(1, player.remainingMoves)
        assertEquals(1, player.visitedTiles.size)
        assertFalse(player.alive)
    }
}