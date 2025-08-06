package service

import entity.Coordinate

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 *
 * @param rootService The root service to which this service belongs
 */
class TestRefreshable(val rootService: RootService) : Refreshable {

    var refreshAfterStartNewGameCalled: Boolean = false
        private set

    var refreshAfterMoveToCalled: Boolean = false
        private set

    var refreshAfterEndTurnCalled: Boolean = false
        private set

    var refreshAfterGameEndCalled: Boolean = false
        private set

    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartNewGameCalled = false
        refreshAfterMoveToCalled = false
        refreshAfterEndTurnCalled = false
        refreshAfterGameEndCalled = false
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterStartNewGameCalled = true
    }

    override fun refreshAfterMoveTo(from : Coordinate, to : Coordinate) {
        refreshAfterMoveToCalled = true
    }

    override fun refreshAfterEndTurn() {
        refreshAfterEndTurnCalled = true
    }

    override fun refreshAfterGameEnd() {
        refreshAfterGameEndCalled = true
    }
}