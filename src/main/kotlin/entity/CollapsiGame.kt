package entity

data class CollapsiGame(
    var currentGame: GameState
) {
    /**
     * "Past" [GameState]s for undo. Filled during redo or when the player performs an action.
     * This is a LIFO stack with the top element at the last index.
     */
    val undoStack = mutableListOf<GameState>()

    /**
     * "Future" [GameState]s for redo. Filled during undo.
     * This is a LIFO stack with the top element at the last index.
     */
    val redoStack = mutableListOf<GameState>()

    var simulationSpeed = 0
}