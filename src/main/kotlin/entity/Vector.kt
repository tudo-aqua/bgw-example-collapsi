package entity

import kotlin.math.*

/**
 * Entity class that represents a two-dimensional vector with integer coordinates.
 *
 * @property x The x-coordinate of the vector.
 * @property y The y-coordinate of the vector.
 */
data class Vector(
    val x: Int,
    val y: Int
) {
    /**
     * Returns this vector, wrapped to stay within the bounds of the play area. Works for negative values.
     *
     * @param wrap The size of the play area for wrapping.
     * @return A new vector where 0 <= (x, y) < [wrap].
     */
    fun wrap(wrap: Int): Vector = Vector(x.mod(wrap), y.mod(wrap))

    /**
     * Returns the sum of this vector and another.
     *
     * @param other The vector to add.
     * @return A new vector representing the result.
     */
    operator fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)

    /**
     * Returns the difference between this vector and another.
     *
     * @param other The vector to subtract.
     * @return A new vector representing the result.
     */
    operator fun minus(other: Vector): Vector = Vector(x - other.x, y - other.y)

    /**
     * Scales this vector by a given integer.
     *
     * @param scalar The value to multiply by.
     * @return A new vector with both components multiplied.
     */
    operator fun times(scalar: Int): Vector = Vector(x * scalar, y * scalar)

    /**
     * Divides this vector by a given integer.
     *
     * @param scalar The value to divide by.
     * @return A new vector with both components divided and rounded down.
     */
    operator fun div(scalar: Int): Vector = Vector(x / scalar, y / scalar)

    /**
     * Returns the negation of this vector.
     *
     * @return A new vector with both x and y negated.
     */
    operator fun unaryMinus(): Vector = Vector(-x, -y)

    /**
     * Returns the Manhattan distance of this vector.
     *
     * @return The sum of the absolute values of x and y.
     */
    fun magnitude(): Int = abs(x) + abs(y)

    override fun equals(other: Any?): Boolean {
        return other is Vector && x == other.x && y == other.y
    }

    /**
     * TODO: KDoc
     */
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    /**
     * TODO:KDoc
     */
    override fun toString(): String = "($x, $y)"

    /**
     * Companion object for useful shortcuts and helper-functions.
     */
    companion object {
        /**
         * Checks if two vectors are adjacent while considering wrapping.
         *
         * @param a The first vector.
         * @param b The second vector.
         * @param wrap The size of the play area for wrapping.
         * @return True if [a] and [b] are adjacent.
         */
        fun isAdjacent(a: Vector, b: Vector, wrap: Int): Boolean {
            return adjacent.any {
                (a + it).wrap(wrap) == b
            }
        }

        /** The vector at the origin (0, 0). */
        val zero = Vector(0, 0)

        /** Represents one unit to the right (1, 0). */
        val right = Vector(1, 0)

        /** Represents one unit to the left (-1, 0). */
        val left = Vector(-1, 0)

        /** Represents one unit upward (0, 1). */
        val up = Vector(0, 1)

        /** Represents one unit downward (0, -1). */
        val down = Vector(0, -1)

        /** A set of vectors in each direction. */
        val adjacent = setOf(right, left, up, down)
    }
}