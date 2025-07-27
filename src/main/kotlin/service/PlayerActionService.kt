package service

import entity.*

class PlayerActionService(val root: RootService) : AbstractRefreshingService() {
    fun moveTo(destination: Vector) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        check(canMoveTo(destination)) { "Tried to perform an illegal move to $destination." }

        val nextTile = gameState.getTileAt(destination)
        val previousTile = gameState.getTileAt(player.position)

        game.redoStack.clear()
        game.undoStack.add(game.currentGame.clone())

        // Collapse the previous tile if this was the player's first step.
        if (player.visitedTiles.isEmpty()) {
            previousTile.collapsed = true
        }

        previousTile.visited = true
        player.visitedTiles.add(previousTile)

        player.position = destination
        player.remainingMoves--

        // Collapse the tile if the player has nowhere to move.
        if (!canMoveAnywhere()) {
            player.alive = false
            nextTile.collapsed = true

            root.gameService.endTurn()

            return
        }

        if (player.remainingMoves <= 0) {
            root.gameService.endTurn()
        }
    }

    fun canMoveTo(destination: Vector): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        if (player.remainingMoves <= 0 || !player.alive)
            return false

        val tile = gameState.getTileAt(destination)

        // Check if the given position is adjacent to the player's position.
        val isAdjacent = Vector.isAdjacent(player.position, destination, gameState.boardSize)

        val endsOnPlayer = player.remainingMoves == 1
                && gameState.players.any { it.position == tile.position }

        // Todo: Technically, there is a situation where the player has >= 2 remaining moves and can move onto an
        // Todo: occupied tile, although all future tiles are blocked. This should be prevented.
        // Todo: With up to 4 players, this would likely require a recursive algorithm.

        return !tile.collapsed && !tile.visited && isAdjacent && !endsOnPlayer
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