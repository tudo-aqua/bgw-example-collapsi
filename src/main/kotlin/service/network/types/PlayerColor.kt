package service.network.types

enum class PlayerColor {
    GREEN_SQUARE,
    ORANGE_HEXAGON,
    YELLOW_CIRCLE,
    RED_TRIANGLE;

    fun toEntityPlayerColor(): entity.PlayerColor = when (this) {
        GREEN_SQUARE -> entity.PlayerColor.GREEN_SQUARE
        ORANGE_HEXAGON -> entity.PlayerColor.ORANGE_HEXAGON
        YELLOW_CIRCLE -> entity.PlayerColor.YELLOW_CIRCLE
        RED_TRIANGLE -> entity.PlayerColor.RED_TRIANGLE
    }
}