package service

import entity.*

/**
 * Service class that manages all player actions in the Collapsi game.
 *
 * @param root The [RootService] that provides access to the overall game state and the other services.
 *
 * @see RootService
 * @see AbstractRefreshingService
 */
class PlayerActionService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * Moves the current player to the given [Coordinate]. The given coordinate must be exactly one step
     * away from the current position.
     *
     * @param destination The [Coordinate] that the current player should move to.
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if the given [game] was null.
     * @throws IllegalStateException if the move was not valid based on [canMoveTo].
     *
     * @see canMoveTo
     * @see hasValidMove
     */
    fun moveTo(destination: Coordinate, game: CollapsiGame? = root.currentGame) {
        checkNotNull(game) { "No game is currently running." }
        val gameState = game.currentState
        val player = gameState.currentPlayer

        require(destination.boardSize == gameState.boardSize) { "Input coordinate did not have correct wrapping." }
        require(canMoveTo(destination, game)) { "Tried to perform an illegal move to $destination." }

        val previousTile = gameState.getTileAt(player.position)

        // Save the current game state, so undo will return to just before this move was made.
        game.redoStack.clear()
        game.undoStack.add(game.currentState.clone())

        // Collapse the previous tile if this was the player's first step.
        if (player.visitedTiles.isEmpty()) {
            previousTile.collapsed = true
        }

        // Mark the tile as visited.
        previousTile.visited = true
        player.visitedTiles.add(player.position)

        // Move the player.
        player.position = destination
        player.remainingMoves--

        // Update the refreshables if this move was performed on the current game (instead of in a bot simulation).
        if (game == root.currentGame)
            onAllRefreshables { refreshAfterMoveTo(previousTile.position, destination) }
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
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
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
    fun canMoveTo(destination: Coordinate, game: CollapsiGame? = root.currentGame): Boolean {
        checkNotNull(game) { "No game is currently running." }
        val gameState = game.currentState
        val player = gameState.currentPlayer

        require(destination.boardSize == gameState.boardSize) { "Input coordinate did not have correct wrapping." }
        check(player.alive) { "Current player is not alive." }

        if (player.remainingMoves <= 0)
            return false

        val tile = gameState.getTileAt(destination)

        // Check if the given position is adjacent to the player's position.
        val isAdjacent = player.position.isAdjacentTo(destination)

        // Check if the player has any more valid paths starting from the given position.
        val extendedVisitedTiles = player.visitedTiles.toMutableList()
        extendedVisitedTiles.add(player.position)
        val hasValidMovesOnDestination = hasValidMove(
            destination,
            player.remainingMoves - 1,
            extendedVisitedTiles,
            game
        )

        return !tile.collapsed && !tile.visited && isAdjacent && hasValidMovesOnDestination
    }

    /**
     * Checks whether there is any sequence of legal moves that the current player can perform given the
     * current game state.
     *
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if no game was running.
     */
    fun hasValidMove(game: CollapsiGame? = root.currentGame): Boolean {
        checkNotNull(game) { "No game is currently running." }
        val player = game.currentState.currentPlayer
        return hasValidMove(player.position, player.remainingMoves, player.visitedTiles, game)
    }

    /**
     * Recursive function that checks whether there is any sequence of legal moves that a player can perform.
     *
     * @param position The current position of the player.
     * @param remainingMoves The number of moves that this player still needs to make from this position.
     * @param visitedTiles The path the player took to this position. Empty if the player starts at the
     * specified position. Will be modified but will return to its original state after this function is done.
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
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
        visitedTiles: MutableList<Coordinate> = mutableListOf(),
        game: CollapsiGame? = root.currentGame
    ): Boolean {
        checkNotNull(game) { "No game is currently running." }
        val gameState = game.currentState
        val tile = gameState.getTileAt(position)

        val endsOnOccupiedTile = remainingMoves <= 0 && gameState.isTileOccupied(position) && visitedTiles.isNotEmpty()

        // Not a valid position if the tile is collapsed, or it ends on an occupied tile.
        if (tile.collapsed || (endsOnOccupiedTile))
            return false

        // Valid position if the path ends on a non-collapsed, non-occupied tile.
        if (remainingMoves <= 0)
            return true

        visitedTiles.add(position)

        // Recursively search all adjacent tiles.
        for (destination in position.neighbours) {
            if (visitedTiles.contains(destination))
                continue

            if (hasValidMove(destination, remainingMoves - 1, visitedTiles, game)) {
                visitedTiles.removeLast()
                return true
            }
        }

        // Make sure that visitedTiles doesn't change.
        visitedTiles.removeLast()

        return false
    }

    /**
     * Returns to the previous [GameState] saved in [CollapsiGame.undoStack].
     *
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if [CollapsiGame.undoStack] was empty.
     *
     * @see redo
     */
    fun undo(game: CollapsiGame? = root.currentGame) {
        checkNotNull(game) { "No game is currently running." }
        check(game.undoStack.isNotEmpty()) { "Can't undo, because there are no past states." }
        check(!game.isOnlineGame()) { "Can't undo in an online game." }

        val doRefresh = game == root.currentGame

        game.redoStack.add(game.currentState)
        game.currentState = game.undoStack.removeLast()

        // Update the refreshables if this was performed on the current game (instead of in a bot simulation).
        if (doRefresh)
            onAllRefreshables { refreshAfterUndo() }
    }

    /**
     * After calling [undo], this will return to the most recent "future" [GameState] saved in [CollapsiGame.redoStack].
     *
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if [CollapsiGame.redoStack] was empty.
     *
     * @see undo
     */
    fun redo(game: CollapsiGame? = root.currentGame) {
        checkNotNull(game) { "No game is currently running." }
        check(game.redoStack.isNotEmpty()) { "Can't redo, because there are no future states." }
        check(!game.isOnlineGame()) { "Can't redo in an online game." }

        val doRefresh = game == root.currentGame

        game.undoStack.add(game.currentState)
        game.currentState = game.redoStack.removeLast()

        // Update the refreshables if this was performed on the current game (instead of in a bot simulation).
        if (doRefresh)
            onAllRefreshables { refreshAfterRedo() }
    }
}