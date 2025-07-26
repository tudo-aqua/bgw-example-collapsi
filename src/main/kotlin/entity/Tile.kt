package entity

data class Tile(
    val position: Coordinate,
    val movesToMake: Int,
    val startTileColor: PlayerColor?
) {
    var collapsed = false

    var visited = false

    fun clone(): Tile {
        TODO()
    }
}