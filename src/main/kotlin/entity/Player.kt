package entity

/**
 * Entity class that represents a player in the Collapsi game.
 *
 * @property color The color (or playing pieces) of the player, represented by a [PlayerColor].
 * @property type The type of the player, represented by a [PlayerType].
 * @property position The current position of the player on the board, represented by a [Coordinate].
 * @property remainingMoves The number of moves the player has left to make in his current turn.
 * @property visitedTiles A list of [Tile]s that the player has visited in the current turn through moving over.
 * @property alive Indicates whether the player is still part of the game.
 */
data class Player(
    val color: PlayerColor,
    val type: PlayerType,
    var position: Coordinate
) {
    var remainingMoves = 1

    val visitedTiles: MutableList<Tile> = mutableListOf()

    var alive = true

    val isBot get() = type == PlayerType.BOT_HARD || type == PlayerType.BOT_EASY

    /**
     * TODO: KDoc
     */
    fun clone(): Player {
        TODO()
    }
}