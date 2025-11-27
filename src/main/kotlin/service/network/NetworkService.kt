package service.network

import entity.CollapsiGame
import entity.Coordinate
import entity.GameState
import entity.Player
import entity.Tile
import service.*
import service.network.messages.*
import service.network.types.*

class NetworkService(private val root: RootService) : AbstractRefreshingService() {
    var currentClient: NetworkClient? = null
        private set

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

        // Todo: Name (is this even important?)
        val newClient = NetworkClient("TODO", server, secret, this)

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

    fun startNewHostedGame(playerTypes: List<entity.PlayerType>, botDifficulties: List<Int>, boardSize: Int) {
        val client = checkNotNull(currentClient) { "Client was null." }

        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "Tried to start a game while not in lobby." }

        root.gameService.startNewGame(playerTypes, botDifficulties, boardSize)
        val game = checkNotNull(root.currentGame)
        val currentState = game.currentState

        // Todo: Fill
        val tiles = listOf<TileType>()
        val players = listOf<PlayerType>()

        val message = InitMessage(tiles, players)
        client.sendGameActionMessage(message)

        // Todo: Depends.
        setConnectionState(ConnectionState.PLAYING_MY_TURN)
    }

    // Todo: Params
    fun startNewJoinedGame(message: InitMessage) {
        val client = checkNotNull(currentClient) { "Client was null." }

        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "Tried to start a game while not in lobby." }

        // Todo: Fill
        val players = listOf<Player>()
        val board = mapOf<Coordinate, Tile>()
        val boardSize = 0

        val gameState = GameState(players, board, boardSize)
        val game = CollapsiGame(gameState)

        root.currentGame = game

        // Todo: Depends.
        setConnectionState(ConnectionState.PLAYING_MY_TURN)

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
}