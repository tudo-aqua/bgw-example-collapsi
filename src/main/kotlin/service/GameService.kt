package service

import entity.*

class GameService(val root: RootService) : AbstractRefreshingService() {
    fun startNewGame(playerTypes: List<PlayerType>, boardSize: Int) {
        require(playerTypes.size >= 2 && playerTypes.size <= 4) { "The number of players must be between 2 and 4." }
        require(boardSize >= 4 && boardSize <= 6) { "Invalid Board Size" }
        /// Todo: Add requires for board size by player count. (I forgot the rules, so it's on you, Alex)

        val board = mutableMapOf<Coordinate, Tile>()

        // Generate a shuffled list of all positions in the board where a tile will be.
        val unassignedPositions = MutableList(boardSize * boardSize) { Coordinate(it % boardSize, it / boardSize) }
        unassignedPositions.shuffle()

        // Initialize player starting tiles.
        val playerTiles = mutableListOf<Tile>()
        for (i in playerTypes.indices) {
            val position = unassignedPositions.removeFirst()
            val tile = Tile(position, 4, PlayerColor.values()[i])
            playerTiles.add(tile)
            board.put(position, tile)
        }

        // Initialize all other tiles.

        // Maps of non-starting cards for board sizes 2, 3, and 4, mapping steps to respective card count.
        // Note: If there isn't enough space (because of too many players),
        // then the items at the back of the list are ignored.
        val unassignedStepValuesByBoardSize = listOf(
            mapOf(1 to 4, 2 to 4, 3 to 4, 4 to 2),
            mapOf(1 to 6, 2 to 6, 3 to 6, 4 to 4),
            mapOf(1 to 8, 2 to 8, 3 to 8, 4 to 8)
        )
        val unassignedStepValues = unassignedStepValuesByBoardSize[boardSize - 2]
            .flatMap { (value, count) -> List(count) { value } }

        for (stepValue in unassignedStepValues) {
            val position = unassignedPositions.removeFirst()
            val tile = Tile(position, stepValue, null)
            board.put(position, tile)

            if (unassignedPositions.isEmpty())
                break
        }

        assert(unassignedPositions.isEmpty()) { "Not all positions on the board were filled." }

        // Create the Player list.
        val players = mutableListOf<Player>()
        for ((i, type) in playerTypes.withIndex()) {
            players.add(Player(PlayerColor.values()[i], type, playerTiles[i].position))
        }

        // Create and assign root entity objects.
        val gameState = GameState(players, board, boardSize)
        val game = CollapsiGame(gameState)
        root.currentGame = game
    }

    fun endTurn() {
        TODO()
    }

    fun endGame() {
        TODO()
    }

    fun save() {
        TODO()
    }

    fun load() {
        TODO()
    }
}