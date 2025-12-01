package service.network.messages.types

import entity.PlayerColor

enum class TileType {
    START_GREEN,
    START_ORANGE,
    START_YELLOW,
    START_RED,
    ONE,
    TWO,
    THREE,
    FOUR;

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