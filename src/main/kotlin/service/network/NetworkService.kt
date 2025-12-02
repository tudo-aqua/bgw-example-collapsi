package service.network

import entity.Coordinate
import entity.PlayerType
import service.*
import service.network.messages.*
import service.network.messages.types.*
import service.network.types.ConnectionState
import tools.aqua.bgw.dialog.DialogType
import kotlin.random.Random

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages in multiplayer network
 * games. Bridges between the [NetworkClient] and the other services.
 *
 * @param root The [RootService] that provides access to the overall game state and the other services.
 */
@Suppress("TooManyFunctions")
class NetworkService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * Reference to the helper class containing non-network methods.
     *
     * @see NetworkHelper
     */
    private val networkHelper = NetworkHelper()

    /**
     * Reference to the [NetworkClient] if we are currently connected to a server.
     */
    var currentClient: NetworkClient? = null
        private set

    /**
     * The current state of the connection.
     *
     * @see ConnectionState
     */
    var connectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Connects to the server and creates a new game session.
     *
     * @param server The server address to connect to.
     * @param secret The secret of the server.
     * @param sessionId The identifier for the session/lobby to host. A.k.a. the "lobby code".
     *
     * @throws IllegalStateException if already connected to a server.
     */
    fun hostGame(server: String, secret: String, sessionId: String) {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't host while already connected to a server." }

        val success = connect(server, secret)
        check(success) { "Couldn't connect to $server." }

        setConnectionState(ConnectionState.CONNECTED)

        val client = checkNotNull(currentClient)
        client.createGame("Collapsi", sessionId, "Hello :3")

        setConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to the server and joins the given lobby.
     *
     * @param server The server address to connect to.
     * @param secret The secret of the server.
     * @param sessionId The identifier for the session/lobby to join. A.k.a. the "lobby code".
     *
     * @throws IllegalStateException if already connected to a server.
     */
    fun joinGame(server: String, secret: String, sessionId: String) {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't join while already connected to a server." }

        val success = connect(server, secret)
        check(success) { "Couldn't connect to $server." }

        setConnectionState(ConnectionState.CONNECTED)

        val client = checkNotNull(currentClient)
        client.joinGame(sessionId, "Heyo :)")

        setConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * Connects to the server and creates a [NetworkClient] if successful.
     *
     * @param server The server address to connect to.
     * @param secret The secret of the server.
     *
     * @return true if the connection was successfully established.
     *
     * @throws IllegalStateException if already connected to a server.
     */
    private fun connect(server: String, secret: String): Boolean {
        check(connectionState == ConnectionState.DISCONNECTED) { "Can't connect while already connected to a server." }
        check(currentClient == null) { "client must be null." }

        // We generate a random client name, because players are only identified by they pawn color
        // which is only assigned after joining.
        // For a different game, you might consider adding a text box before joining and using that name.
        val clientName = "Client ${Random.nextInt(100000).toString().padStart(5, '0')} (Collapsi)"
        val newClient = NetworkClient(clientName, server, secret, this)

        // Note that connect() is 'blocking', so we get the response immediately.
        val success = newClient.connect()

        // Only set currentClient if the client was connected successfully.
        if (success) {
            currentClient = newClient
        }

        return success
    }

    /**
     * Disconnects the [currentClient] from the server if it exists.
     *
     * Can safely be called even if no connection is currently active.
     */
    fun disconnect() {
        // If no client exists, we exit immediately.
        val client = currentClient ?: return

        client.apply {
            if (sessionId != null) leaveGame("Farewell!")
            if (isOpen) disconnect()
        }
        currentClient = null

        setConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Sets up a local game using [GameService.startNewGame] and sends the entire starting game state to the
     * other clients connected to the lobby via the [InitMessage].
     *
     * The parameters mirror those of [GameService.startNewGame].
     *
     * @param playerTypes A list of players that are in this game.
     * @param botDifficulties A list of values for [entity.Player.botDifficulty] for each player.
     * Must be the same size as [playerTypes].
     * @param boardSize The size of the board (4-6). The board will be quadratic, so 4x4, 5x5 or 6x6.
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_GUESTS]
     *
     * @see GameService.startNewGame
     */
    fun startNewHostedGame(playerTypes: List<PlayerType>, botDifficulties: List<Int>, boardSize: Int) {
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "Tried to start a game while not in lobby." }

        val client = checkNotNull(currentClient) { "Client was null." }
        val clientColor = checkNotNull(client.color) { "Client didn't have a color assigned." }

        // Start the game locally.
        root.gameService.startNewGame(playerTypes, botDifficulties, boardSize)
        val game = checkNotNull(root.currentGame)

        // We need to convert the game state into a message to send to the other clients.
        val message = networkHelper.convertGameToInitMessage(game)
        client.sendGameActionMessage(message)

        if (game.currentState.currentPlayer.color == clientColor.toEntityPlayerColor())
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)
    }


    /**
     * Sets up a local game using an [InitMessage] that was received from the [NetworkClient].
     *
     * Bypasses the need to call [GameService.startNewGame].
     *
     * @param message The received [InitMessage].
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_INIT]
     */
    internal fun startNewJoinedGame(message: InitMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "Tried to start a game while not in lobby." }

        val client = checkNotNull(currentClient) { "Client was null." }
        val clientColor = checkNotNull(client.color) { "Client didn't have a color assigned." }
        val clientBotDifficulty = checkNotNull(client.botDifficulty) { "Client didn't have a difficulty assigned." }

        // Read the message and transform it into a complete entity layer.
        val game = networkHelper.convertInitMessageToGame(message, clientColor, clientBotDifficulty)

        // Set the currently active game here instead of using GameService.startNewGame().
        root.currentGame = game

        if (game.currentState.currentPlayer.color == clientColor.toEntityPlayerColor())
            setConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Sends a move that happened locally to the connected clients using [MoveMessage].
     *
     * @param previousPosition The player's position before moving.
     * @param newPosition The player's position after moving.
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.PLAYING_MY_TURN]
     * @throws IllegalArgumentException if the given positions were not adjacent.
     */
    fun sendMoveMessage(previousPosition: Coordinate, newPosition: Coordinate) {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "Can't send message. Not my turn." }

        val client = checkNotNull(currentClient) { "Client was null." }

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

    /**
     * Moves a pawn in the local game using a [MoveMessage] that was received from the [NetworkClient].
     *
     * Decodes the [MoveMessage] and uses [PlayerActionService.moveTo] to perform the move.
     *
     * @param message The received [MoveMessage].
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_OPPONENTS]
     *
     * @see PlayerActionService.moveTo
     */
    internal fun receiveMoveMessage(message: MoveMessage) {
        require(connectionState == ConnectionState.WAITING_FOR_OPPONENTS)
        { "Unexpected MoveMessage. Not opponents turn." }

        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentPlayer = game.currentState.currentPlayer

        val newPosition = when (message.direction) {
            Direction.LEFT -> currentPlayer.position.leftNeighbour
            Direction.RIGHT -> currentPlayer.position.rightNeighbour
            Direction.UP -> currentPlayer.position.upNeighbour
            Direction.DOWN -> currentPlayer.position.downNeighbour
        }

        root.playerActionService.moveTo(newPosition)
    }

    /**
     * Sends a message that the current player ended their turn locally to the connected
     * clients using [EndTurnMessage].
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.PLAYING_MY_TURN]
     */
    fun sendEndTurnMessage() {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "Can't send message. Not my turn." }

        val client = checkNotNull(currentClient) { "Client was null." }

        val message = EndTurnMessage()

        client.sendGameActionMessage(message)

        setConnectionState(ConnectionState.WAITING_FOR_OPPONENTS)
    }

    /**
     * Ends the current player's turn locally, because an [EndTurnMessage] that was received from the [NetworkClient].
     *
     * @param message The received [EndTurnMessage].
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_OPPONENTS]
     *
     * @see GameService.endTurn
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    internal fun receiveEndTurnMessage(message: EndTurnMessage) {
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

    /**
     * Sets the [connectionState] to the given value and notifies refreshables.
     *
     * @param newState The new [ConnectionState] for [connectionState].
     */
    internal fun setConnectionState(newState: ConnectionState) {
        connectionState = newState
        onAllRefreshables { refreshAfterConnectionStateChange(newState) }
    }

    /**
     * Notifies all refreshables that a player has joined.
     *
     * Note that [NetworkClient] can't be an [AbstractRefreshingService], because it already extends
     * [tools.aqua.bgw.net.client.BoardGameClient] and is also not persistent.
     */
    internal fun onPlayerJoined() {
        onAllRefreshables { refreshAfterPlayerJoined() }
    }

    /**
     * Sets the [NetworkClient.botDifficulty] value of the [currentClient].
     *
     * @throws IllegalStateException if no client exists.
     * @throws IllegalStateException if the game was already started.
     */
    fun setBotDifficultyOfClient(difficulty: Int) {
        val client = checkNotNull(currentClient) { "Client was null." }
        checkNotNull(client.sessionId) { "Client was not connected." }
        check(root.currentGame == null) { "The game has already started." }

        client.botDifficulty = difficulty
    }

    /**
     * Shows a dialog window in the gui.
     *
     * @param header The header of the dialog window.
     * @param message The main message of the dialog window.
     * @param dialogType The type of the dialog window (info, warning, error, exception).
     */
    internal fun showDialog(header: String, message: String, dialogType: DialogType) {
        onAllRefreshables { showDialog(header, message, dialogType) }
    }
}