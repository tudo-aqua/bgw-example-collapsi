package service.network

import service.network.messages.*
import service.network.messages.types.*
import service.network.types.ConnectionState
import tools.aqua.bgw.dialog.DialogType
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.*
import tools.aqua.bgw.net.common.response.*

class NetworkClient(
    clientName: String,
    host: String,
    secret: String,
    var networkService: NetworkService
) : BoardGameClient(clientName, host, secret, NetworkLogging.INFO) {
    /** The identifier of this game session. Can be null if no session was started yet. */
    var sessionId: String? = null
        private set

    var color: PlayerColor? = null
        private set

    /**
     * 0 if the player is not a bot. Otherwise, this represents the [entity.Player.botDifficulty].
     */
    var botDifficulty: Int? = null

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
                    "No lobby with the given was found.",
                    DialogType.ERROR
                )
            }

            else -> disconnectAndError(response.status)
        }
    }

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(
            networkService.connectionState in setOf(
                ConnectionState.WAITING_FOR_GUESTS,
                ConnectionState.WAITING_FOR_INIT
            )
        ) { "Received unexpected PlayerJoinedNotification." }

        networkService.onPlayerJoined()
    }

    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        networkService.disconnect()
        networkService.showDialog(
            "You were disconnected.",
            "You have been disconnected from the server, because a player has left the game.",
            DialogType.WARNING
        )
    }

    override fun onGameActionResponse(response: GameActionResponse) {
        check(
            networkService.connectionState in setOf(
                ConnectionState.PLAYING_MY_TURN,
                ConnectionState.WAITING_FOR_OPPONENTS
            )
        ) { "Received unexpected GameActionResponse." }

        when (response.status) {
            GameActionResponseStatus.SUCCESS -> {}
            else -> disconnectAndError(response.status)
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: InitMessage, sender: String) {
        networkService.startNewJoinedGame(message)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onMoveReceived(message: MoveMessage, sender: String) {
        networkService.receiveMoveMessage(message)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onEndTurnReceived(message: EndTurnMessage, sender: String) {
        networkService.receiveEndTurnMessage(message)
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        networkService.showDialog("Network Error", message.toString(), DialogType.EXCEPTION)
    }
}