package entity

data class Player(
    val color: PlayerColor,
    val type: PlayerType,
    var position: Vector
) {
    var remainingMoves = 0

    val visitedTiles: MutableList<Tile> = mutableListOf()

    fun clone(): Player {
        TODO()
    }
}