package entity

/**
 * Entity class that represents a tile in the Collapsi game.
 *
 * @property position The position of the tile on the board represented by a [Coordinate].
 * @property movesToMake The number of moves required to make when starting the turn on this tile.
 * @property startTileColor Connects the tile to the [Player], if it's a starting tile. Else null.
 */
data class Tile(
    val position: Coordinate,
    val movesToMake: Int,
    val startTileColor: PlayerColor?
) {
    /** Indicates whether the tile has been collapsed. */
    var collapsed = false

    /** Indicates whether the tile has been visited by a [Player] in the current turn. */
    var visited = false

    /**
     * Creates a deep copy of this [Tile] and returns it.
     *
     * Changes to the returned [Tile] will never impact this instance.
     *
     * @return A deep-cloned copy of this object.
     */
    fun clone(): Tile {
        val clone = Tile(position, movesToMake, startTileColor)

        clone.collapsed = collapsed
        clone.visited = visited

        return clone
    }
}