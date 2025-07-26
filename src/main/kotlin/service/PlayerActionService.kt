package service

import entity.*

class PlayerActionService(val root: RootService) : AbstractRefreshingService() {
    fun move(position: Coordinate) {
        TODO()
    }

    fun canMoveTo(destination: Coordinate): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        if (gameState.currentPlayer.remainingMoves <= 0)
            return false

        val destinationTile = checkNotNull(gameState.playArea[destination])
        { "Tile $destination does not exist in the current game." }

        // Check if the given position is adjacent to the player's position.
        val isAdjacent = Coordinate.isAdjacent(gameState.currentPlayer.position, destination, gameState.playAreaSize)

        val endsOnPlayer = gameState.currentPlayer.remainingMoves == 1
                && gameState.players.any { it.position == destinationTile.position }

        // Todo: Technically, there is a situation where the player has >= 2 remaining moves and can move onto an
        // Todo: occupied tile, although all future tiles are blocked. This should be prevented.
        // Todo: With 4 players, this would likely require a recursive algorithm.

        return !destinationTile.collapsed && !destinationTile.visited && isAdjacent && !endsOnPlayer
    }

    fun undo() {
        TODO()
    }

    fun redo() {
        TODO()
    }
}