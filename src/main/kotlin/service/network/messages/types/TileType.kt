package service.network.messages.types

import entity.PlayerColor

/**
 * An enum to represent the type and values of a tile on the board.
 *
 * This contains information about the number of steps to take, whether this is a starting tile, and the color
 * if it is a starting tile.
 */
enum class TileType {
    START_GREEN,
    START_ORANGE,
    START_YELLOW,
    START_RED,
    ONE,
    TWO,
    THREE,
    FOUR;

    /**
     * Returns the [entity.Tile.movesToMake] value, a.k.a. the step count.
     */
    fun getStepCount(): Int = when (this) {
        START_GREEN -> 1
        START_ORANGE -> 1
        START_YELLOW -> 1
        START_RED -> 1
        ONE -> 1
        TWO -> 2
        THREE -> 3
        FOUR -> 4
    }

    /**
     * If this is a starting tile (a tile that a player spawns on), this will return the color of that player.
     *
     * Otherwise, this will return null.
     */
    fun getPlayerColor(): PlayerColor? = when (this) {
        START_GREEN -> PlayerColor.GREEN_SQUARE
        START_ORANGE -> PlayerColor.ORANGE_HEXAGON
        START_YELLOW -> PlayerColor.YELLOW_CIRCLE
        START_RED -> PlayerColor.RED_TRIANGLE
        ONE -> null
        TWO -> null
        THREE -> null
        FOUR -> null
    }
}