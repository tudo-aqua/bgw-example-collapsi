package entity

import kotlinx.serialization.Serializable

/**
 * Entity class that represents a two-dimensional coordinate with integer coordinates.
 *
 * @property x The x-coordinate of the coordinate.
 * @property y The y-coordinate of the coordinate.
 * @property boardSize The size of the board used for wrapping the x/y coordinates.
 */
@Serializable
data class Coordinate(
    val x: Int,
    val y: Int,
    val boardSize: Int
) {
    init {
        require(x in 0 until boardSize) { "X-coordinate must be in range [0, boardSize)." }
        require(y in 0 until boardSize) { "Y-coordinate must be in range [0, boardSize)." }
    }

    /*
    Note:
    Modulo (.mod) and Remainder (%) are different for negative values. When in doubt, use modulo.
    Example:
    -1 mod 3 = 2
    -1 % 3 = 1
     */

    /** The coordinate to the left of this one, wrapped to stay within bounds. */
    val leftNeighbour get() = Coordinate((x - 1).mod(boardSize), y, boardSize)

    /** The coordinate to the right of this one, wrapped to stay within bounds. */
    val rightNeighbour get() = Coordinate((x + 1).mod(boardSize), y, boardSize)

    /** The coordinate upwards of this one, wrapped to stay within bounds. */
    val upNeighbour get() = Coordinate(x, (y - 1).mod(boardSize), boardSize)

    /** The coordinate downwards of this one, wrapped to stay within bounds. */
    val downNeighbour get() = Coordinate(x, (y + 1).mod(boardSize), boardSize)

    /** A list of coordinates on each side of this one, wrapped to stay within bounds. */
    val neighbours get() = listOf(leftNeighbour, rightNeighbour, upNeighbour, downNeighbour)

    /**
     * Checks if the given [Coordinate] is orthogonally (not diagonally) adjacent to this one.
     *
     * @return True if [other] is adjacent to this coordinate.
     *
     * @throws IllegalArgumentException if the [boardSize] of the two coordinates doesn't match.
     */
    fun isAdjacentTo(other: Coordinate): Boolean {
        require(boardSize == other.boardSize) { "Can't compare two coordinates with different board sizes." }

        return neighbours.contains(other)
    }

    override fun toString(): String = "($x, $y)"
}