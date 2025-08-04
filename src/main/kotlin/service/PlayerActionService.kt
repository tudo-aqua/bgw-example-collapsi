package service

import entity.*

/**
 * Service class that manages all player actions in the Collapsi game.
 *
 * @param root The root service that provides access to the overall game state.
 */
class PlayerActionService(val root: RootService) : AbstractRefreshingService() {
    fun moveTo(destination: Coordinate) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        require(destination.boardSize == gameState.boardSize) { "Input coordinate did not have correct wrapping." }
        check(canMoveTo(destination)) { "Tried to perform an illegal move to $destination." }

        val previousTile = gameState.getTileAt(player.position)

        game.redoStack.clear()
        game.undoStack.add(game.currentGame.clone())

        // Collapse the previous tile if this was the player's first step.
        if (player.visitedTiles.isEmpty()) {
            previousTile.collapsed = true
        }

        previousTile.visited = true
        player.visitedTiles.add(player.position)

        player.position = destination
        player.remainingMoves--

        if (player.remainingMoves <= 0) {
            root.gameService.endTurn()
        }
    }

    fun canMoveTo(destination: Coordinate): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        require(destination.boardSize == gameState.boardSize) { "Input coordinate did not have correct wrapping." }
        check(player.alive) { "Current player is not alive." }

        if (player.remainingMoves <= 0)
            return false

        val tile = gameState.getTileAt(destination)

        // Check if the given position is adjacent to the player's position.
        val isAdjacent = player.position.isAdjacentTo(destination)

        val extendedVisitedTiles = player.visitedTiles.toMutableList()
        extendedVisitedTiles.add(player.position)
        val hasValidMovesOnDestination = hasValidMove(
            destination,
            player.remainingMoves - 1,
            extendedVisitedTiles
        )

        return !tile.collapsed && !tile.visited && isAdjacent && hasValidMovesOnDestination
    }

    fun hasValidMove(): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer
        return hasValidMove(player.position, player.remainingMoves, player.visitedTiles)
    }

    /**
     * Checks whether there is any sequence of legal moves that a player can perform.
     *
     * @param position The current position of the player.
     * @param remainingMoves The moves that this player still needs to make from this position.
     * @param visitedTiles The path the player took to this position. Empty if the player starts at the specified position.
     *
     * @return True if there exists a path starting from [position] that makes [remainingMoves] steps and doesn't
     * end on a player or go through a collapsed or visited tile.
     */
    fun hasValidMove(
        position: Coordinate,
        remainingMoves: Int,
        visitedTiles: MutableList<Coordinate> = mutableListOf()
    ): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val tile = gameState.getTileAt(position)

        // Not a valid position if the tile is collapsed, or it ends on an occupied tile.
        if (tile.collapsed || (remainingMoves <= 0 && gameState.isTileOccupied(position) && visitedTiles.isNotEmpty()))
            return false

        // Valid position if the path ends on a non-collapsed, non-occupied tile.
        if (remainingMoves <= 0)
            return true

        visitedTiles.add(position)

        for (destination in position.neighbours) {
            if (visitedTiles.contains(destination))
                continue

            if (hasValidMove(destination, remainingMoves - 1, visitedTiles)) {
                visitedTiles.removeLast()
                return true
            }
        }

        visitedTiles.removeLast()

        return false
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