package entity

import kotlin.collections.get

/**
 * Entity class that represents a game state of "Collapsi".
 *
 * @property players The [Player]s currently in the game.
 * @property board A map containing all [Tile]s in the game, including those that are collapsed.
 * @property boardSize Size of one axis in [board], assuming the area is quadratic.
 * @property currentPlayerIndex The index of the [Player] whose turn it is currently.
 * @property currentPlayer The [Player] whose turn it is currently.
 */
data class GameState(
    val players: List<Player>,
    val board: Map<Vector, Tile>,
    val boardSize: Int
) {
    var currentPlayerIndex = 0
        private set

    val currentPlayer get() = players[currentPlayerIndex]

    /**
     * Returns the [Tile] at the given position.
     *
     * @param position The position of the [Tile] to retrieve.
     * @return The [Tile] at the specified position.
     * @throws IllegalArgumentException If no [Tile] exists at the specified position.
     */
    fun getTileAt(position: Vector): Tile =
        checkNotNull(board[position]) { "Tile at $position does not exist in this GameState." }

    /**
     * Sets the currentPlayerIndex to the [Player] of the next turn.
     */
    fun nextPlayer() {
        // Todo: Account for dead players.
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    /**
     * TODO: KDoc
     */
    fun clone(): GameState {
        TODO()
    }
}