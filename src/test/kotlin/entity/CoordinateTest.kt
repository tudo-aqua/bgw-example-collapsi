package entity

import kotlin.test.*

/**
 * Test class for the [Coordinate] entity in the Collapsi game.
 */
class CoordinateTest {

    /**
     * Tests the initialization and properties of a [Coordinate] instance.
     * This test checks that the [Coordinate] is correctly initialized with the specified x, y, and board size,
     * and verifies the properties of the left, right, up, and down neighbours.
     */
    @Test
    fun testCoordinateInitialization() {
        val testCoordinate = Coordinate(2, 2, 4)

        assertEquals(2, testCoordinate.x)
        assertEquals(2, testCoordinate.y)
        assertEquals(4, testCoordinate.boardSize)

        assertEquals(Coordinate(1, 2, 4), testCoordinate.leftNeighbour)
        assertEquals(Coordinate(3, 2, 4), testCoordinate.rightNeighbour)
        assertEquals(Coordinate(2, 1, 4), testCoordinate.upNeighbour)
        assertEquals(Coordinate(2, 3, 4), testCoordinate.downNeighbour)
    }

    /**
     * Tests the `isAdjacentTo` method of the [Coordinate] class.
     * This test checks that the method correctly identifies adjacent coordinates,
     * including wrapping around the board edges.
     */
    @Test
    fun testIsAdjacentTo() {
        val coordinate = Coordinate(0, 0, 4)

        assertTrue(coordinate.isAdjacentTo(Coordinate(1, 0, 4)))
        assertTrue(coordinate.isAdjacentTo(Coordinate(3, 0, 4)))
        assertTrue(coordinate.isAdjacentTo(Coordinate(0, 1, 4)))
        assertTrue(coordinate.isAdjacentTo(Coordinate(0, 3, 4)))

        assertFalse(coordinate.isAdjacentTo(Coordinate(2, 2, 4)))
    }
}