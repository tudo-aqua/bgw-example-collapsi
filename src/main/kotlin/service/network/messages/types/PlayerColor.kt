package service.network.messages.types

import entity.PlayerColor

/**
 * The color of a player.
 */
enum class PlayerColor {
    GREEN_SQUARE,
    ORANGE_HEXAGON,
    YELLOW_CIRCLE,
    RED_TRIANGLE;

    /**
     * Convert this network PlayerColor enum into the PlayerColor enum of our entity layer.
     */
    fun toEntityPlayerColor(): PlayerColor = when (this) {
        GREEN_SQUARE -> PlayerColor.GREEN_SQUARE
        ORANGE_HEXAGON -> PlayerColor.ORANGE_HEXAGON
        YELLOW_CIRCLE -> PlayerColor.YELLOW_CIRCLE
        RED_TRIANGLE -> PlayerColor.RED_TRIANGLE
    }
}

/*
Note:
You may ask why it is necessary to create another PlayerColor enum, when it is seemingly identical
to [entity.PlayerColor].

When transforming a game to work online, we want to decouple the network messages from the specific implementation
of the program. This is because, for SoPra, we want to be able to play a match with different implementations of
the same game.
With this, two implementations can have completely different entity layers (or different patterns entirely) and as
long as they both work with the same messages and the same ruleset, they can interact with each other.
 */