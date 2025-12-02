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
     * Whether this game is played online.
     */
    val isOnlineGame get() = currentState.players.any { it.type == PlayerType.REMOTE }
}