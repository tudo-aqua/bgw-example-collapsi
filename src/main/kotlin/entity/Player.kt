package entity

class Player(
    val color: PlayerColor,
    val type: PlayerType,
    var position: Coordinate
) {
    var remainingMoves = 0

    val visitedTiles: MutableList<Tile> = mutableListOf()

    fun clone(): Player {
        TODO()
    }
}