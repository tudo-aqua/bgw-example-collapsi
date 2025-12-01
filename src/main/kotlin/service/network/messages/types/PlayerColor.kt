package service.network.messages.types

import entity.PlayerColor

enum class PlayerColor {
    GREEN_SQUARE,
    ORANGE_HEXAGON,
    YELLOW_CIRCLE,
    RED_TRIANGLE;

    fun toEntityPlayerColor(): PlayerColor = when (this) {
        GREEN_SQUARE -> PlayerColor.GREEN_SQUARE
        ORANGE_HEXAGON -> PlayerColor.ORANGE_HEXAGON
        YELLOW_CIRCLE -> PlayerColor.YELLOW_CIRCLE
        RED_TRIANGLE -> PlayerColor.RED_TRIANGLE
    }
}