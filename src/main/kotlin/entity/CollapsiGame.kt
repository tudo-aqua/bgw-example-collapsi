package entity

class CollapsiGame(
    val currentGame: GameState
) {
    val redoStack: List<GameState> = listOf()

    val undoStack: List<GameState> = listOf()

    var simulationSpeed = 0
}