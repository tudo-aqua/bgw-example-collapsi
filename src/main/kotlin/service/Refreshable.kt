package service

import entity.*
import service.network.ConnectionState
import tools.aqua.bgw.dialog.DialogType

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {
    /**
     * Perform refreshes that are necessary after a new game started.
     */
    fun refreshAfterStartNewGame() {}

    /**
     * Perform refreshes that are necessary after a move is executed.
     * This is referring to a single step, instead of a full turn.
     *
     * @param from The previous position of the player.
     * @param to The position the player was moved to.
     */
    fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {}

    /**
     * Perform refreshes that are necessary after a turn ended.
     */
    fun refreshAfterEndTurn() {}

    /**
     * Perform refreshes that are necessary after a player died because of a collapsing tile.
     *
     * @param player The [entity.Player] that died.
     */
    fun refreshAfterPlayerDied(player: Player) {}

    /**
     * Perform refreshes that are necessary after the game has ended.
     * [RootService.currentGame] will still be set to a non-null value when this gets called,
     * but gets reset to null immediately after.
     *
     * @param winner The [Player] that won the game.
     */
    fun refreshAfterGameEnd(winner: Player) {}

    /**
     * Performs refreshes that are necessary after a move was undone.
     */
    fun refreshAfterUndo() {}

    /**
     * Performs refreshes that are necessary after a move was redone.
     */
    fun refreshAfterRedo() {}

    /**
     * Performs refreshes that are necessary after a past game was loaded from a file.
     */
    fun refreshAfterLoad() {}

    /**
     * Performs refreshes that are necessary after the [ConnectionState] has changed.
     */
    fun refreshAfterConnectionStateChange(newState: ConnectionState) {}

    /**
     * Performs refreshes that are necessary after a new remote player joins the lobby in the pre-game.
     */
    fun refreshAfterPlayerJoined() {}

    /**
     * Shows a dialog window in the gui.
     */
    fun showDialogue(header: String, message: String, dialogType: DialogType) {}
}