package entity

/**
 * Entity class that represents the Collapsi game handling stacks of [GameState]s and the current simulation speed.
 *
 * @property currentGame The current [GameState] of the running game.
 */
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

    /** The speed of the game simulation, where 0 is paused. */
    var simulationSpeed = 0
}