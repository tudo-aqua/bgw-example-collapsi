package entity

import kotlin.math.*

data class Coordinate(val x: Int, val y: Int) {
    /**
     * Returns the sum of this coordinate and another.
     *
     * @param other The coordinate to add.
     * @return A new coordinate representing the result.
     */
    operator fun plus(other: Coordinate): Coordinate = Coordinate(x + other.x, y + other.y)

    /**
     * Returns the difference between this coordinate and another.
     *
     * @param other The coordinate to subtract.
     * @return A new coordinate representing the result.
     */
    operator fun minus(other: Coordinate): Coordinate = Coordinate(x - other.x, y - other.y)

    /**
     * Scales this coordinate by a given integer.
     *
     * @param scalar The value to multiply by.
     * @return A new coordinate with both components multiplied.
     */
    operator fun times(scalar: Int): Coordinate = Coordinate(x * scalar, y * scalar)

    /**
     * Divides this coordinate by a given integer.
     *
     * @param scalar The value to divide by.
     * @return A new coordinate with both components divided and rounded down.
     */
    operator fun div(scalar: Int): Coordinate = Coordinate(x / scalar, y / scalar)

    /**
     * Returns the negation of this coordinate.
     *
     * @return A new coordinate with both x and y negated.
     */
    operator fun unaryMinus(): Coordinate = Coordinate(-x, -y)

    /**
     * Returns the Manhattan distance of this coordinate.
     *
     * @return The sum of the absolute values of x and y.
     */
    fun magnitude(): Int = abs(x) + abs(y)

    /**
     * Determines if another object is equal to this coordinate.
     *
     * @param other The object to compare with.
     * @return `true` if [other] is a [Coordinate] with matching values.
     */
    override fun equals(other: Any?): Boolean {
        return other is Coordinate && x == other.x && y == other.y
    }

    /**
     * Computes a hash code for this coordinate.
     *
     * @return The hash code derived from [x] and [y].
     */
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    /**
     * Companion object for useful shortcuts.
     */
    companion object {
        /** The coordinate at the origin (0, 0). */
        val zero = Coordinate(0, 0)

        /** Represents one unit to the right (1, 0). */
        val right = Coordinate(1, 0)

        /** Represents one unit to the left (-1, 0). */
        val left = Coordinate(-1, 0)

        /** Represents one unit upward (0, 1). */
        val up = Coordinate(0, 1)

        /** Represents one unit downward (0, -1). */
        val down = Coordinate(0, -1)
    }
}