package entity

import kotlinx.serialization.Serializable

/**
 * Entity class that represents the Collapsi game.
 *
 * It contains stacks of [GameState]s for undo/redo and the simulation speed.
 *
 * @property currentState The current [GameState] of the running game.
 */
@Serializable
data class CollapsiGame(
    var currentState: GameState
) {
    /**
     * "Past" [GameState]s for undo. Filled during redo or when the player performs an action.
     *
     * This is a LIFO stack with the top element at the last index.
     */
    val undoStack = mutableListOf<GameState>()

    /**
     * "Future" [GameState]s for redo. Filled during undo.
     *
     * This is a LIFO stack with the top element at the last index.
     */
    val redoStack = mutableListOf<GameState>()

    /**
     * The speed that the bots play at.
     *
     * 0 means the game is paused, 1 is normal speed, 2 is double speed, etc.
     */
    var simulationSpeed = 1.0
}