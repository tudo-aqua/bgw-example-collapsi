package entity

class Player(val color: PlayerColor, val type: PlayerType) {
    var remainingMoves = 0

    val visited : MutableList<PlayTile> = mutableListOf()
}