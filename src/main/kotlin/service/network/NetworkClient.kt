package service.network

import service.network.messages.EndTurnMessage
import service.network.messages.InitMessage
import service.network.messages.MoveMessage
import service.network.types.PlayerColor
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

class NetworkClient(
    clientName: String,
    host: String,
    secret: String,
    var networkService: NetworkService
) : BoardGameClient(clientName, host, secret) {
    /** The identifier of this game session. Can be null if no session was started yet. */
    var sessionId: String? = null

    var color: PlayerColor? = null

    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        { "Received unexpected CreateGameResponse." }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                color = PlayerColor.GREEN_SQUARE
                sessionId = response.sessionID
                networkService.setConnectionState(ConnectionState.WAITING_FOR_GUESTS)
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
                sessionId = response.sessionID
                networkService.setConnectionState(ConnectionState.WAITING_FOR_INIT)
            }

            else -> disconnectAndError(response.status)
        }
    }

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "Received unexpected PlayerJoinedNotification." }

        networkService.onPlayerJoined()
    }

    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        disconnectAndError(notification.message)
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
        error(message)
    }
}