package entity

/**
 * Entity class that represents a two-dimensional coordinate with integer coordinates.
 *
 * @property x The x-coordinate of the coordinate.
 * @property y The y-coordinate of the coordinate.
 * @property boardSize The size of the board used for wrapping the x/y coordinates.
 */
data class Coordinate(
    val x: Int,
    val y: Int,
    val boardSize: Int
) {
    // Note: Modulo (.mod) and Remainder (%) are different for negative values.

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

    fun isAdjacentTo(other: Coordinate): Boolean {
        require(boardSize == other.boardSize) { "Can't compare two coordinates with different board sizes." }

        return neighbours.contains(other)
    }

    override fun equals(other: Any?): Boolean {
        return other is Coordinate && x == other.x && y == other.y && boardSize == other.boardSize
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String = "($x, $y)"
}