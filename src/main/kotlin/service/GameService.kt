package service

import entity.*

/**
 * Service class that manages all game-related operations in the Collapsi game.
 *
 * @param root The [RootService] that provides access to the overall game state and the other services.
 *
 * @see RootService
 * @see AbstractRefreshingService
 */
class GameService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * Initializes a new instance of [CollapsiGame] in [RootService].
     *
     * Creates the board and distributes tiles to fill it.
     *
     * @param playerTypes A list of players that are in this game.
     * @param botDifficulties A list of values for [Player.botDifficulty] for each player.
     * Must be the same size as [playerTypes].
     * @param boardSize The size of the board (4-6). The board will be quadratic, so 4x4, 5x5 or 6x6.
     *
     * @throws IllegalStateException if a game was already running in [root].
     * @throws IllegalArgumentException if [playerTypes] or [botDifficulties] weren't between 2-4 and the same size.
     * @throws IllegalArgumentException if [boardSize] wasn't between 4-6.
     * @throws IllegalArgumentException if [boardSize] isn't big enough for the given player count.
     */
    fun startNewGame(playerTypes: List<PlayerType>, botDifficulties: List<Int>, boardSize: Int) {
        check(root.currentGame == null) { "Tried to start a game, while one was already in progress." }
        require(playerTypes.size in 2..4) { "The number of players must be between 2 and 4." }
        require(botDifficulties.size == playerTypes.size)
        { "Difficulty needs to be defined for all players (even non-bot players)." }
        require(boardSize in 4..6) { "The board size must be between 4 and 6." }
        require(boardSize >= playerTypes.size + 2)
        { "Player amount of ${playerTypes.size} requires a minimal board size of ${playerTypes.size + 2}." }

        val board = mutableMapOf<Coordinate, Tile>()

        // Generate a shuffled list of all positions in the board where a tile will be.
        val unassignedPositions = MutableList(boardSize * boardSize) {
            Coordinate(it % boardSize, it / boardSize, boardSize)
        }
        unassignedPositions.shuffle()

        // Initialize player starting tiles.
        val playerTiles = mutableListOf<Tile>()
        val expectedPlayerTiles = boardSize - 2
        for (i in 0..<expectedPlayerTiles) {
            val position = unassignedPositions.removeFirst()
            val tile = Tile(position, 1, PlayerColor.entries[i])
            board[position] = tile

            if (i < playerTypes.size) {
                playerTiles.add(tile)
            } else {
                tile.collapsed = true
            }
        }

        // Initialize all other tiles.

        // Maps of non-starting cards for board sizes 2, 3, and 4, mapping steps to respective card count.
        // Note: If there isn't enough space (because of too many players),
        // then the items at the back of the list are ignored.
        val unassignedStepValuesByBoardSize = mapOf(
            4 to mapOf(1 to 4, 2 to 4, 3 to 4, 4 to 2),
            5 to mapOf(1 to 6, 2 to 6, 3 to 6, 4 to 4),
            6 to mapOf(1 to 8, 2 to 8, 3 to 8, 4 to 8)
        )
        val unassignedStepValues = unassignedStepValuesByBoardSize.getValue(boardSize)
            .flatMap { (value, count) -> List(count) { value } }

        for (stepValue in unassignedStepValues) {
            val position = unassignedPositions.removeFirst()
            val tile = Tile(position, stepValue, null)
            board[position] = tile

            if (unassignedPositions.isEmpty())
                break
        }

        check(unassignedPositions.isEmpty()) { "Not all positions on the board were filled." }

        // Create the Player list.
        val players = mutableListOf<Player>()
        for ((i, type) in playerTypes.withIndex()) {
            players.add(Player(PlayerColor.entries[i], playerTiles[i].position, type, botDifficulties[i]))
        }
        players.shuffle()

        // Create and assign root entity objects.
        val gameState = GameState(players, board, boardSize)
        val game = CollapsiGame(gameState)
        root.currentGame = game

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Ends the current player's turn and starts the next player's turn.
     * Called when the current player has no remaining moves to make or no legal moves as options.
     *
     * Will also call [endTurn] again if the next player has no legal moves.
     *
     * Will end the game and declare a winner if only one player is left alive.
     *
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if the current player still had valid moves and steps to take.
     */
    fun endTurn(game: CollapsiGame? = root.currentGame) {
        checkNotNull(game) { "No game is currently running." }
        val gameState = game.currentState
        var player = gameState.currentPlayer
        val currentTile = gameState.getTileAt(player.position)

        check(player.remainingMoves <= 0 || !root.playerActionService.hasValidMove(game))
        { "A player ended their turn with ${player.remainingMoves} steps left and some possible valid move." }

        // Reset visited tiles.
        player.visitedTiles.forEach { gameState.getTileAt(it).visited = false }
        player.visitedTiles.clear()
        player.remainingMoves = currentTile.movesToMake

        if (player.type == PlayerType.REMOTE) {
            root.networkService.sendEndTurnMessage()
        }

        // Declare a winner.
        if (gameState.players.count { it.alive } <= 1) {
            root.gameService.endGame(game)

            return
        }

        gameState.nextPlayer()
        player = gameState.currentPlayer

        // Update the refreshables if this method was performed on the current game (instead of in a bot simulation).
        if (game == root.currentGame)
            onAllRefreshables { refreshAfterEndTurn() }

        // Collapse the tile if the player has nowhere to move at the start of their turn.
        if (!root.playerActionService.hasValidMove(game)) {
            player.alive = false
            player.rank = gameState.players.count { it.alive }
            gameState.getTileAt(player.position).collapsed = true

            // Update the refreshables if this method was performed on the
            // current game (instead of in a bot simulation).
            if (game == root.currentGame)
                onAllRefreshables { refreshAfterPlayerDied(player) }

            endTurn(game)
        }
    }

    /**
     * Ends the game, finds the winner and sets [RootService.currentGame] to null.
     *
     * @param game The [CollapsiGame] this function will be applied on. Defaults to the game in [RootService].
     *
     * @throws IllegalStateException if no game was running.
     * @throws IllegalStateException if not exactly 1 player was alive.
     */
    private fun endGame(game: CollapsiGame? = root.currentGame) {
        checkNotNull(game) { "No game is currently running." }
        check(game.currentState.players.count { it.alive } == 1) { "Game should end with exactly 1 alive player." }

        val winner = game.currentState.players.first { it.alive }

        // Update the refreshables and end the game if this method was performed on the current game
        // (instead of in a bot simulation).
        if (game == root.currentGame) {
            onAllRefreshables { refreshAfterGameEnd(winner) }

            root.currentGame = null
        }
    }
}