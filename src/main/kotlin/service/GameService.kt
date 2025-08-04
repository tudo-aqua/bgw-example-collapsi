package service

import entity.*

/**
 * Service class that manages al game-related operations in the Collapsi game.
 *
 * @param root The root service that provides access to the overall game state.
 */
class GameService(val root: RootService) : AbstractRefreshingService() {
    fun startNewGame(playerTypes: List<PlayerType>, botDifficulties: List<Double>, boardSize: Int) {
        check(root.currentGame == null) { "Tried to start a game, while one was already in progress." }
        require(playerTypes.size >= 2 && playerTypes.size <= 4) { "The number of players must be between 2 and 4." }
        require(botDifficulties.size == playerTypes.size)
        { "Difficulty needs to be defined for all players (even non-bot players)." }
        require(boardSize >= 4 && boardSize <= 6) { "The board size must be between 4 and 6." }
        require(boardSize >= playerTypes.size + 2)
        { "Playeramount of ${playerTypes.size} requires a minimal boardsize of ${playerTypes.size + 2}." }

        val board = mutableMapOf<Coordinate, Tile>()

        // Generate a shuffled list of all positions in the board where a tile will be.
        val unassignedPositions = MutableList(boardSize * boardSize) {
            Coordinate(it % boardSize, it / boardSize, boardSize)
        }
        unassignedPositions.shuffle()

        // Initialize player starting tiles.
        val playerTiles = mutableListOf<Tile>()
        for (i in playerTypes.indices) {
            val position = unassignedPositions.removeFirst()
            val tile = Tile(position, 1, PlayerColor.values()[i])
            playerTiles.add(tile)
            board.put(position, tile)
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
            board.put(position, tile)

            if (unassignedPositions.isEmpty())
                break
        }

        check(unassignedPositions.isEmpty()) { "Not all positions on the board were filled." }

        // Create the Player list.
        val players = mutableListOf<Player>()
        for ((i, type) in playerTypes.withIndex()) {
            players.add(Player(PlayerColor.values()[i], playerTiles[i].position, type, botDifficulties[i]))
        }
        players.shuffle()

        // Create and assign root entity objects.
        val gameState = GameState(players, board, boardSize)
        val game = CollapsiGame(gameState)
        root.currentGame = game

        if (gameState.currentPlayer.type == PlayerType.BOT) {
            root.botService.makeTurn()
        }

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Ends the current player's turn and starts the next player's turn.
     * Called when the current player has no remaining moves to make or no legal moves as options.
     */
    fun endTurn() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        var player = gameState.currentPlayer
        val currentTile = gameState.getTileAt(player.position)

        check(player.remainingMoves <= 0 || !root.playerActionService.hasValidMove())
        { "A player ended their turn with ${player.remainingMoves} steps left and some possible valid move." }

        player.visitedTiles.forEach { gameState.getTileAt(it).visited = false }
        player.visitedTiles.clear()
        player.remainingMoves = currentTile.movesToMake

        // Declare a winner.
        if (gameState.players.count { it.alive } <= 1) {
            root.gameService.endGame()

            return
        }

        gameState.nextPlayer()
        player = gameState.currentPlayer

        // Replace the most recent state in the undo stack to be at the start of the next player's turn instead.
        if (game.undoStack.isNotEmpty())
            game.undoStack[game.undoStack.lastIndex] = game.currentGame.clone()

        // Collapse the tile if the player has nowhere to move at the start of their turn.
        if (!root.playerActionService.hasValidMove()) {
            player.alive = false
            gameState.getTileAt(player.position).collapsed = true

            endTurn()
        } else {
            if (player.type == PlayerType.BOT) {
                root.botService.makeTurn()
            }
        }
    }

    fun endGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        root.currentGame = null

        onAllRefreshables { refreshAfterGameEnd() }

        println("Game Finished")
    }

    fun save() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        TODO()
    }

    fun load() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        TODO()
    }
}