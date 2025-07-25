package entity

/**
 * Entity class that represents a game state of "Collapsi".
 *
 * @property players The players currently in the game.
 * @property playArea A map containing all tiles in the game, including those that are collapsed.
 * @property playAreaSize Size of one axis in [playArea], assuming the area is quadratic.
 */
class GameState(
    val players: List<Player>,
    val playArea: Map<Coordinate, Tile>,
    val playAreaSize: Int
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