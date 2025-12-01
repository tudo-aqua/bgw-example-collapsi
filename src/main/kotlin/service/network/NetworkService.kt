package service.network

import entity.Coordinate
import entity.PlayerType
import service.*
import service.network.messages.*
import service.network.messages.types.*
import service.network.types.ConnectionState
import tools.aqua.bgw.dialog.DialogType
import kotlin.random.Random

@Suppress("TooManyFunctions")
class NetworkService(private val root: RootService) : AbstractRefreshingService() {
    private val networkHelper = NetworkHelper()

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

        val clientName = "Collapsi Client ${Random.nextInt(100000).toString().padStart(5, '0')}"
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

        val message = networkHelper.convertGameToInitMessage(game)
        client.sendGameActionMessage(message)

        if (game.currentState.currentPlayer.color == entity.PlayerColor.GREEN_SQUARE)
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)
    }

    fun startNewJoinedGame(message: InitMessage) {
        val client = checkNotNull(currentClient) { "Client was null." }
        val clientColor = checkNotNull(client.color) { "Client didn't have a color assigned." }
        val clientBotDifficulty = checkNotNull(client.botDifficulty) { "Client didn't have a difficulty assigned." }

        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "Tried to start a game while not in lobby." }

        val game = networkHelper.convertInitMessageToGame(message, clientColor, clientBotDifficulty)

        root.currentGame = game

        if (game.currentState.currentPlayer.color == clientColor.toEntityPlayerColor())
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

        setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    fun receiveEndTurnMessage(message: EndTurnMessage) {
        require(connectionState == ConnectionState.WAITING_FOR_OPPONENTS)
        { "Unexpected EndTurnMessage. Not opponents turn." }

        root.gameService.endTurn()

        val client = checkNotNull(currentClient) { "Client was null." }
        val clientColor = checkNotNull(client.color) { "Client didn't have an assigned player color." }
        val game = root.currentGame ?: return // Don't throw, because currentGame may be null after game end.
        val currentPlayer = game.currentState.currentPlayer

        if (clientColor.toEntityPlayerColor() == currentPlayer.color) {
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
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

    fun showDialog(header: String, message: String, dialogType: DialogType) {
        onAllRefreshables { this.showDialog(header, message, dialogType) }
    }
}