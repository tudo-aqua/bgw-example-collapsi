package entity

import kotlin.math.*

/**
 * Entity class that represents a two-dimensional coordinate with integer coordinates.
 *
 * @property x The x-coordinate of the coordinate.
 * @property y The y-coordinate of the coordinate.
 * @property wrap The size of the board used for wrapping the x/y coordinates.
 */
data class Coordinate(
    val x: Int,
    val y: Int,
    val wrap: Int
) {
    val leftNeighbour get() = Coordinate((x + 1).mod(wrap), y, wrap)

    val rightNeighbour get() = Coordinate((x - 1).mod(wrap), y, wrap)

    val upNeighbour get() = Coordinate(x, (y + 1).mod(wrap), wrap)

    val downNeighbour get() = Coordinate(x, (y - 1).mod(wrap), wrap)

    val neighbours get() = listOf(leftNeighbour, rightNeighbour, upNeighbour, downNeighbour)

    fun isAdjacentTo(other: Coordinate): Boolean {
        require(wrap == other.wrap) { "Can't compare two coordinates with different wrapping." }
        
        return neighbours.contains(other)
    }

    override fun equals(other: Any?): Boolean {
        return other is Coordinate && x == other.x && y == other.y && wrap == other.wrap
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String = "($x, $y)"
}