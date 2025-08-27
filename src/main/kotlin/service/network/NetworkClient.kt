package service.network

import tools.aqua.bgw.net.client.BoardGameClient

class NetworkClient(
    playerName : String,
    host: String,
    secret: String,
    var networkService: NetworkService
) : BoardGameClient(playerName, host, secret) {

}