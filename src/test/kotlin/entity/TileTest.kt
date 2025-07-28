package entity

import kotlin.test.*

/**
 * Test class for the [Tile] entity in the Collapsi game.
 */
class TileTest {

    /**
     * Tests the initialization and properties of a [Tile] instance.
     * This test checks that the [Tile] is correctly initialized with the specified position, moves to make, and start
     * tile color as well as the default values for collapsed and visited status.
     * It also verifies that the [Tile]s properties for collapsed and visited status can be modified after initialization.
     */
    @Test
    fun testTileInitialization() {
        val tile = Tile(
            position = Vector(1, 2),
            movesToMake = 1,
            startTileColor = PlayerColor.GREEN_SQUARE
        )

        assertEquals(Vector(1, 2), tile.position)
        assertEquals(1, tile.movesToMake)
        assertEquals(PlayerColor.GREEN_SQUARE, tile.startTileColor)
        assertFalse(tile.collapsed)
        assertFalse(tile.visited)

        tile.collapsed = true
        tile.visited = true

        assertTrue(tile.collapsed)
        assertTrue(tile.visited)
    }
}