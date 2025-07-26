package entity

/**
 * Entity class that represents a game state of "Collapsi".
 *
 * @property players The players currently in the game.
 * @property board A map containing all tiles in the game, including those that are collapsed.
 * @property boardSize Size of one axis in [board], assuming the area is quadratic.
 */
data class GameState(
    val players: List<Player>,
    val board: Map<Vector, Tile>,
    val boardSize: Int
) {
    var currentPlayerIndex = 0
        private set

    val currentPlayer get() = players[currentPlayerIndex]

    fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    fun clone(): GameState {
        TODO()
    }
}