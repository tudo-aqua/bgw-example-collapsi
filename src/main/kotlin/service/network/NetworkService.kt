package service.network

import entity.CollapsiGame
import entity.Coordinate
import entity.GameState
import entity.Player
import entity.PlayerType
import entity.Tile
import service.*
import service.network.messages.*
import service.network.types.*
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class NetworkService(private val root: RootService) : AbstractRefreshingService() {
    private var currentClient: NetworkClient? = null

    var connectionState = ConnectionState.DISCONNECTED
        private set

    fun hostGame(server: String, secret: String, sessionId: String) {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't host while already connected to a server." }

        val success = connect(server, secret)
        check(success) { "Couldn't connect to $server." }

        setConnectionState(ConnectionState.CONNECTED)

        val client = checkNotNull(currentClient)
        client.createGame("Collapsi", sessionId, "Hello :3")

        setConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    fun joinGame(server: String, secret: String, sessionId: String) {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't join while already connected to a server." }

        val success = connect(server, secret)
        check(success) { "Couldn't connect to $server." }

        setConnectionState(ConnectionState.CONNECTED)

        val client = checkNotNull(currentClient)
        client.joinGame(sessionId, "Heyo :)")

        setConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    fun connect(server: String, secret: String): Boolean {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't connect while already connected to a server." }
        check(currentClient == null) { "client must be null." }

        val clientName = "Client ${Random.nextInt(100000).toString().padStart(5, '0')}"
        val newClient = NetworkClient(clientName, server, secret, this)

        val success = newClient.connect()

        if (success) {
            currentClient = newClient
        }

        return success
    }

    fun disconnect() {
        val client = checkNotNull(currentClient) { "Tried to disconnect without a connected client." }

        client.apply {
            if (sessionId != null) leaveGame("Farewell!")
            if (isOpen) disconnect()
        }
        currentClient = null

        setConnectionState(ConnectionState.DISCONNECTED)
    }

    fun startNewHostedGame(playerTypes: List<PlayerType>, botDifficulties: List<Int>, boardSize: Int) {
        val client = checkNotNull(currentClient) { "Client was null." }

        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "Tried to start a game while not in lobby." }

        root.gameService.startNewGame(playerTypes, botDifficulties, boardSize)
        val game = checkNotNull(root.currentGame)
        val currentState = game.currentState

        val tileTypes = mutableListOf<TileType>()
        for (y in 0..<boardSize) {
            for (x in 0..<boardSize) {
                val entityTile = currentState.getTileAt(Coordinate(x, y, boardSize))

                val startTileColor = entityTile.startTileColor
                val tileType = if (startTileColor != null) {
                    when (startTileColor) {
                        entity.PlayerColor.GREEN_SQUARE -> TileType.START_GREEN
                        entity.PlayerColor.ORANGE_HEXAGON -> TileType.START_ORANGE
                        entity.PlayerColor.YELLOW_CIRCLE -> TileType.START_YELLOW
                        entity.PlayerColor.RED_TRIANGLE -> TileType.START_RED
                    }
                } else {
                    when (entityTile.movesToMake) {
                        1 -> TileType.ONE
                        2 -> TileType.TWO
                        3 -> TileType.THREE
                        4 -> TileType.FOUR
                        else -> throw IllegalStateException("Found tile with illegal step count.")
                    }
                }

                tileTypes.add(tileType)
            }
        }

        val playerColors = mutableListOf<PlayerColor>()
        for (player in currentState.players) {
            val playerColor = when (player.color) {
                entity.PlayerColor.GREEN_SQUARE -> PlayerColor.GREEN_SQUARE
                entity.PlayerColor.ORANGE_HEXAGON -> PlayerColor.ORANGE_HEXAGON
                entity.PlayerColor.YELLOW_CIRCLE -> PlayerColor.YELLOW_CIRCLE
                entity.PlayerColor.RED_TRIANGLE -> PlayerColor.RED_TRIANGLE
            }

            playerColors.add(playerColor)
        }

        val message = InitMessage(tileTypes, playerColors)
        client.sendGameActionMessage(message)

        if (currentState.currentPlayer.color == entity.PlayerColor.GREEN_SQUARE)
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)
    }

    fun startNewJoinedGame(message: InitMessage) {
        val client = checkNotNull(currentClient) { "Client was null." }
        val clientColor = checkNotNull(client.color) { "Client didn't have a color assigned." }

        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "Tried to start a game while not in lobby." }

        val boardSize = sqrt(message.board.size.toDouble()).roundToInt()

        val positions = message.board.indices.map {
            Coordinate(it % boardSize, it / boardSize, boardSize)
        }.toMutableList()

        val board = mutableMapOf<Coordinate, Tile>()
        val playerPositions = mutableMapOf<entity.PlayerColor, Coordinate>()
        for (tileType in message.board) {
            val position = positions.removeFirst()
            val startTileColor = tileType.getPlayerColor()
            val tile = Tile(position, tileType.getStepCount(), startTileColor)
            board[position] = tile

            if (startTileColor != null) {
                playerPositions[startTileColor] = position
            }
        }

        val players = mutableListOf<Player>()
        for (playerColor in message.players) {
            val isClient = playerColor == client.color

            val entityPlayerColor = playerColor.toEntityPlayerColor()
            val position = playerPositions.getValue(entityPlayerColor)
            val botDifficulty = checkNotNull(client.botDifficulty) { "Client didn't have a botDifficulty assigned." }
            val playerType = if (!isClient) {
                PlayerType.REMOTE
            } else if (botDifficulty == 0) {
                PlayerType.LOCAL
            } else {
                PlayerType.BOT
            }

            val player = Player(entityPlayerColor, position, playerType, botDifficulty)
            players.add(player)
        }

        val gameState = GameState(players, board, boardSize)
        val game = CollapsiGame(gameState)

        root.currentGame = game

        if (gameState.currentPlayer.color == clientColor.toEntityPlayerColor())
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    fun sendMoveMessage(previousPosition: Coordinate, newPosition: Coordinate) {
        val client = checkNotNull(currentClient) { "Client was null." }

        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "Can't send message. Not my turn." }

        val direction = when (newPosition) {
            previousPosition.leftNeighbour -> Direction.LEFT
            previousPosition.rightNeighbour -> Direction.RIGHT
            previousPosition.upNeighbour -> Direction.UP
            previousPosition.downNeighbour -> Direction.DOWN
            else -> throw IllegalArgumentException("Given coordinates were not adjacent.")
        }

        val message = MoveMessage(direction)

        client.sendGameActionMessage(message)
    }

    fun receiveMoveMessage(message: MoveMessage) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentPlayer = game.currentState.currentPlayer

        require(connectionState == ConnectionState.WAITING_FOR_OPPONENTS)
        { "Unexpected MoveMessage. Not opponents turn." }

        val newPosition = when (message.direction) {
            Direction.LEFT -> currentPlayer.position.leftNeighbour
            Direction.RIGHT -> currentPlayer.position.rightNeighbour
            Direction.UP -> currentPlayer.position.upNeighbour
            Direction.DOWN -> currentPlayer.position.downNeighbour
        }

        root.playerActionService.moveTo(newPosition)
    }

    fun sendEndTurnMessage() {
        val client = checkNotNull(currentClient) { "Client was null." }

        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "Can't send message. Not my turn." }

        val message = EndTurnMessage()

        client.sendGameActionMessage(message)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun receiveEndTurnMessage(message: EndTurnMessage) {
        require(connectionState == ConnectionState.WAITING_FOR_OPPONENTS)
        { "Unexpected EndTurnMessage. Not opponents turn." }

        root.gameService.endTurn()
    }

    fun setConnectionState(newState: ConnectionState) {
        connectionState = newState
        onAllRefreshables { refreshAfterConnectionStateChange(newState) }
    }

    fun onPlayerJoined() {
        onAllRefreshables { refreshAfterPlayerJoined() }
    }

    fun setBotDifficultyOfClient(difficulty: Int) {
        val client = checkNotNull(currentClient) { "Client was null." }
        checkNotNull(client.sessionId) { "Client was not connected." }
        check(root.currentGame == null) { "The game has already started." }

        client.botDifficulty = difficulty
    }
}