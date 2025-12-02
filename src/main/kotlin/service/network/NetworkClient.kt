package service.network

import service.network.types.*
import service.network.messages.*
import service.network.messages.types.*
import tools.aqua.bgw.dialog.DialogType
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.*
import tools.aqua.bgw.net.common.response.*

/**
 * [BoardGameClient] implementation for network communication.
 *
 * A [NetworkClient] object only exists while connected to a server.
 *
 * @param clientName The name of the client, given to the server.
 * @param server The server address of the host server.
 * @param secret The server secret of the host server.
 * @param networkService A reference to the [NetworkService] that created this object.
 */
class NetworkClient(
    clientName: String,
    server: String,
    secret: String,
    var networkService: NetworkService
) : BoardGameClient(clientName, server, secret, NetworkLogging.INFO) {
    /**
     * The identifier of this game session.
     *
     * Can be null if no session was started yet.
     */
    var sessionId: String? = null
        private set

    /**
     * The [PlayerColor] of the player represented by this client.
     *
     * Can be null if no session was started yet.
     */
    var color: PlayerColor? = null
        private set

    /**
     * The difficulty level of the player represented by this client.
     *
     * 0 if the player is not a bot. Otherwise, this represents the [entity.Player.botDifficulty].
     *
     * Can be null if no session was started yet.
     */
    var botDifficulty: Int? = null

    /**
     * Handle a [CreateGameResponse] sent by the server.
     *
     * Will await the guest player when its status is [CreateGameResponseStatus.SUCCESS].
     * Will disconnect and show an error when the status is anything else.
     *
     * @throws IllegalStateException if the client wasn't expecting this response.
     *
     * @see disconnectAndError
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        { "Received unexpected CreateGameResponse." }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                color = PlayerColor.GREEN_SQUARE
                botDifficulty = 3
                sessionId = response.sessionID
                networkService.setConnectionState(ConnectionState.WAITING_FOR_GUESTS)
            }

            CreateGameResponseStatus.SESSION_WITH_ID_ALREADY_EXISTS -> {
                networkService.disconnect()
                networkService.showDialog(
                    "Invalid Lobby Code",
                    "A lobby with the given code already exists.",
                    DialogType.ERROR
                )
            }

            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a [JoinGameResponse] sent by the server.
     *
     * Will await the init message when its status is [CreateGameResponseStatus.SUCCESS].
     * Will disconnect and show an error when the status is anything else.
     *
     * @throws IllegalStateException if the client wasn't expecting this response.
     *
     * @see disconnectAndError
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        { "Received unexpected JoinGameResponse." }

        when (response.status) {
            JoinGameResponseStatus.SUCCESS -> {
                color = PlayerColor.entries[response.opponents.size]
                botDifficulty = 0
                sessionId = response.sessionID
                networkService.setConnectionState(ConnectionState.WAITING_FOR_INIT)
            }

            JoinGameResponseStatus.INVALID_SESSION_ID -> {
                networkService.disconnect()
                networkService.showDialog(
                    "Invalid Lobby Code",
                    "No lobby with the given code was found.",
                    DialogType.ERROR
                )
            }

            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server.
     *
     * Will forward this message to the gui to update the lobby scene.
     *
     * @throws IllegalStateException if the client wasn't expecting this notification.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(
            networkService.connectionState in setOf(
                ConnectionState.WAITING_FOR_GUESTS,
                ConnectionState.WAITING_FOR_INIT
            )
        ) { "Received unexpected PlayerJoinedNotification." }

        networkService.onPlayerJoined()
    }

    /**
     * Handle a [PlayerLeftNotification] sent by the server.
     *
     * Due to the strict color ordering and additional implementation effort handling this message would cause,
     * this will simply disconnect the client and show a warning to the player.
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        networkService.disconnect()
        networkService.showDialog(
            "You were disconnected.",
            "You have been disconnected from the server, because a player has left the game.",
            DialogType.WARNING
        )
    }

    /**
     * Handle a [GameActionResponse] sent by the server.
     *
     * Does nothing unless the status is not [GameActionResponseStatus.SUCCESS], in which case the
     * client will disconnect and show an error.
     *
     * @throws IllegalStateException if the client wasn't expecting this notification.
     *
     * @see disconnectAndError
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        check(
            networkService.connectionState in setOf(
                ConnectionState.PLAYING_MY_TURN,
                ConnectionState.WAITING_FOR_OPPONENTS
            )
        ) { "Received unexpected GameActionResponse." }

        if (response.status != GameActionResponseStatus.SUCCESS) {
            disconnectAndError(response.status)
        }
    }

    /**
     * Handle an [InitMessage] sent by the server.
     *
     * Forwards this message to the [NetworkService].
     *
     * @see NetworkService.startNewJoinedGame
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: InitMessage, sender: String) {
        networkService.startNewJoinedGame(message)
    }

    /**
     * Handle a [MoveMessage] sent by the server.
     *
     * Forwards this message to the [NetworkService].
     *
     * @see NetworkService.receiveMoveMessage
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onMoveReceived(message: MoveMessage, sender: String) {
        networkService.receiveMoveMessage(message)
    }

    /**
     * Handle an [EndTurnMessage] sent by the server.
     *
     * Forwards this message to the [NetworkService].
     *
     * @see NetworkService.receiveEndTurnMessage
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onEndTurnReceived(message: EndTurnMessage, sender: String) {
        networkService.receiveEndTurnMessage(message)
    }

    /**
     * Disconnects the client from the server and shows an error dialog to the user.
     *
     * @see NetworkService.disconnect
     * @see NetworkService.showDialog
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        networkService.showDialog("Network Error", message.toString(), DialogType.EXCEPTION)
    }
}