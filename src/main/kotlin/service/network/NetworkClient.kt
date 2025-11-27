package service.network

import service.network.messages.EndTurnMessage
import service.network.messages.InitMessage
import service.network.messages.MoveMessage
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
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService
) : BoardGameClient(playerName, host, secret) {
    /** The identifier of this game session. Can be null if no session was started yet. */
    var sessionId: String? = null

    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        { "Received unexpected CreateGameResponse." }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                networkService.setConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                sessionId = response.sessionID
            }

            else -> disconnectAndError(response.status)
        }
    }

    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        { "Received unexpected JoinGameResponse." }

        when (response.status) {
            JoinGameResponseStatus.SUCCESS -> {
                // Todo: Save opponent names? response.opponents[]
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
        )
        { "Received unexpected GameActionResponse." }

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