package gui

import entity.*

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see service.AbstractRefreshingService
 */
interface Refreshable {
    /**
     * Perform refreshes that are necessary after a new game started.
     */
    fun refreshAfterStartNewGame() {}

    /**
     * Perform refreshes that are necessary after a move is executed.
     */
    fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {}

    /**
     * Perform refreshes that are necessary after a turn ended.
     */
    fun refreshAfterEndTurn() {}

    /**
     * Perform refreshes that are necessary after a player died because of a collapsing tile.
     */
    fun refreshAfterPlayerDied(player: Player) {}

    /**
     * Perform refreshes that are necessary after the game has ended.
     */
    fun refreshAfterGameEnd(winner: Player) {}
}