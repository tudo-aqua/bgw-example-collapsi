package entity

class GameState(val playerList: List<Player>, val playArea: Map<Coordinate, PlayTile>) {
    var currentPlayerIndex = 0
        private set

    val currentPlayer get() = playerList[currentPlayerIndex]

    fun nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size
    }
}