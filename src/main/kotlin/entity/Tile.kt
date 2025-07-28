package entity

/**
 * Entity class that represents a tile in the Collapsi game.
 *
 * @property position The position of the tile on the board represented by a [Coordinate].
 * @property movesToMake The number of moves required to make when starting the turn on this tile.
 * @property startTileColor Connects the tile to the [Player], if it´s a starting tile. Else null.
 * @property collapsed Indicates whether the tile has been collapsed.
 * @property visited Indicates whether the tile has been visited by a [Player] in the current turn.
 */
data class Tile(
    val position: Coordinate,
    val movesToMake: Int,
    val startTileColor: PlayerColor?
) {
    var collapsed = false

    var visited = false

    /**
     * TODO: KDoc
     */
    fun clone(): Tile {
        TODO()
    }
}