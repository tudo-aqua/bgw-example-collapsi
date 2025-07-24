package entity

class GameState(val players: List<Player>, val tiles: Map<Coordinate, Tile>) {
    var currentPlayerIndex = 0
        private set

    val currentPlayer get() = players[currentPlayerIndex]

    fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }
}