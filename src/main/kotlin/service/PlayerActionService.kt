package service

import entity.*

class PlayerActionService(val root: RootService) : AbstractRefreshingService() {
    fun move(position: Vector) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        game.redoStack.clear()
        game.undoStack.add(game.currentGame.clone())

        TODO()
    }

    fun canMoveTo(destination: Vector): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        if (gameState.currentPlayer.remainingMoves <= 0)
            return false

        val destinationTile = checkNotNull(gameState.board[destination])
        { "Tile $destination does not exist in the current game." }

        // Check if the given position is adjacent to the player's position.
        val isAdjacent = Vector.isAdjacent(gameState.currentPlayer.position, destination, gameState.boardSize)

        val endsOnPlayer = gameState.currentPlayer.remainingMoves == 1
                && gameState.players.any { it.position == destinationTile.position }

        // Todo: Technically, there is a situation where the player has >= 2 remaining moves and can move onto an
        // Todo: occupied tile, although all future tiles are blocked. This should be prevented.
        // Todo: With up to 4 players, this would likely require a recursive algorithm.

        return !destinationTile.collapsed && !destinationTile.visited && isAdjacent && !endsOnPlayer
    }

    fun canMoveAnywhere(): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val boardSize = game.currentGame.boardSize
        val position = game.currentGame.currentPlayer.position

        return Vector.adjacent.any { direction -> canMoveTo((position + direction).wrap(boardSize)) }
    }

    fun undo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        require(game.undoStack.isNotEmpty()) { "Can't undo, because there are no past states." }

        game.redoStack.add(game.currentGame)
        game.currentGame = game.undoStack.removeLast()
    }

    fun redo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        require(game.redoStack.isNotEmpty()) { "Can't redo, because there are no future states." }

        game.undoStack.add(game.currentGame)
        game.currentGame = game.redoStack.removeLast()
    }
}