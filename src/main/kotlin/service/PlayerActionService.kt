package service

import entity.*

/**
 * Service class that manages all player actions in the Collapsi game.
 *
 * @param root The root service that provides access to the overall game state.
 */
class PlayerActionService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * Moves the current player to the given [Coordinate].
     *
     * Ends the turn if the player had no more steps left.
     *
     * @param destination The [Coordinate] that the current player should move to.
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if the move was not valid based on [canMoveTo].
     *
     * @see canMoveTo
     * @see hasValidMove
     */
    fun moveTo(destination: Coordinate) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        require(destination.boardSize == gameState.boardSize) { "Input coordinate did not have correct wrapping." }
        check(canMoveTo(destination)) { "Tried to perform an illegal move to $destination." }

        val previousTile = gameState.getTileAt(player.position)

        // Save the current game state, so undo will return to just before this move was made.
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

        onAllRefreshables { refreshAfterMoveTo(previousTile.position, destination) }

        // End the turn if the player has used all steps.
        if (player.remainingMoves <= 0) {
            root.gameService.endTurn()
        }
    }

    /**
     * Checks if the given [Coordinate] would be a valid destination for the current player.
     *
     * Conditions are as follows:
     * - The player must have at least one [Player.remainingMoves].
     * - The destination must be adjacent to the current position.
     * - The destination tile may not be collapsed.
     * - The player is not allowed to move to the same tile twice in one turn.
     * - The player must have at least one valid path from the destination tile. (It is assumed that the player
     * has at least one valid path from the current position as well.)
     *
     * @param destination The [Coordinate] that the current player wants to move to.
     *
     * @return True if this is a valid move.
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if the current player is not alive.
     * @throws IllegalStateException if [destination] was not a valid [Coordinate] given the [GameState.boardSize].
     *
     * @see hasValidMove
     * @see moveTo
     */
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

    /**
     * Checks whether there is any sequence of legal moves that the current player can perform given the
     * current game state.
     *
     * @throws IllegalStateException if no game was running.
     */
    fun hasValidMove(): Boolean {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val player = game.currentGame.currentPlayer
        return hasValidMove(player.position, player.remainingMoves, player.visitedTiles)
    }

    /**
     * Recursive function that checks whether there is any sequence of legal moves that a player can perform.
     *
     * @param position The current position of the player.
     * @param remainingMoves The number of moves that this player still needs to make from this position.
     * @param visitedTiles The path the player took to this position. Empty if the player starts at the
     * specified position. Will be modified but will return to its original state after this function is done.
     *
     * @return True if there exists a path starting from [position] that makes [remainingMoves] steps and doesn't
     * end on a player or go through a collapsed or visited tile.
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if any of the given [Coordinate] were not valid.
     */
    private fun hasValidMove(
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

        // Recursively search all adjacent tiles.
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

    /**
     * Returns to the previous [GameState] saved in [CollapsiGame.undoStack].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if [CollapsiGame.undoStack] was empty.
     *
     * @see redo
     */
    fun undo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        check(game.undoStack.isNotEmpty()) { "Can't undo, because there are no past states." }

        game.redoStack.add(game.currentGame)
        game.currentGame = game.undoStack.removeLast()
    }

    /**
     * After calling [undo], this will return to the most recent "future" [GameState] saved in [CollapsiGame.redoStack].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if [CollapsiGame.redoStack] was empty.
     *
     * @see undo
     */
    fun redo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        check(game.redoStack.isNotEmpty()) { "Can't redo, because there are no future states." }

        game.undoStack.add(game.currentGame)
        game.currentGame = game.redoStack.removeLast()
    }
}