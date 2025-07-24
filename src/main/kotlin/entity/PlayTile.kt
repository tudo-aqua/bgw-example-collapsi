package entity

class PlayTile(val movesToMake : Int, val startTileColor : PlayerColor?) {
    var collapsed = false

    var visited = false
}