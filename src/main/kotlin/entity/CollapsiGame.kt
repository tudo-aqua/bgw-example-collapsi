package entity

class CollapsiGame(val currentGame: GameState) {
    val pastStates: List<GameState> = listOf()

    val futureStates: List<GameState> = listOf()

    var gameSpeed = 0
}