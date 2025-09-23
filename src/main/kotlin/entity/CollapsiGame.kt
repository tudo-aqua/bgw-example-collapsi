package entity

import kotlinx.serialization.Serializable

/**
 * Entity class that represents the Collapsi game handling stacks of [GameState]s and the current simulation speed.
 *
 * @property currentGame The current [GameState] of the running game.
 */
@Serializable
data class CollapsiGame(
    var currentGame: GameState // Todo: Rename to "currentState" to match asta.
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
     * -1 is paused. >=0 value is the delay per turn in seconds.
     */
    var simulationSpeed = 0.0
}