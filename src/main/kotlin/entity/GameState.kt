package entity

/**
 * Entity class that represents a game state of "Collapsi".
 *
 * @property players The [Player]s currently in the game.
 * @property board A map containing all [Tile]s in the game, including those that are collapsed.
 * @property boardSize Size of one axis in [board], assuming the area is quadratic.
 */
data class GameState(
    val players: List<Player>,
    val board: Map<Coordinate, Tile>,
    val boardSize: Int
) {
    /** The index of the [Player] whose turn it currently is. */
    var currentPlayerIndex = 0

    /** The [Player] whose turn it currently is. */
    val currentPlayer get() = players[currentPlayerIndex]

    /**
     * Returns the [Tile] at the given position.
     *
     * @param position The position of the [Tile] to retrieve.
     * @return The [Tile] at the specified position.
     * @throws IllegalArgumentException If no [Tile] exists at the specified position.
     */
    fun getTileAt(position: Coordinate): Tile =
        checkNotNull(board[position]) { "Tile at $position does not exist in this GameState." }

    fun isTileOccupied(position: Coordinate): Boolean = players.any { it.position == position }

    /**
     * Increases the [currentPlayerIndex] by one with wrapping. Does not check for whether a player is alive.
     */
    fun nextPlayer() {
        check(players.count { it.alive } >= 1) { "nextPlayer() requires at least 1 player to be alive." }

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!currentPlayer.alive)
    }

    /**
     * Creates a deep copy of this [GameState] and returns it.
     *
     * Changes to the returned [GameState] will never impact this instance.
     *
     * @return A deep-cloned copy of this object.
     */
    fun clone(): GameState {
        val playersCopy = players.map { it.clone() }
        val boardCopy = board.map { Pair(it.key, it.value.clone()) }.toMap()

        val clone = GameState(playersCopy, boardCopy, boardSize)

        clone.currentPlayerIndex = currentPlayerIndex

        return clone
    }
}