package service.network.messages

import service.network.messages.types.PlayerColor
import service.network.messages.types.TileType
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class InitMessage(
    val board: List<TileType>,
    val players: List<PlayerColor>
) : GameAction() {
    override fun formatMessage(): String {
        return "Initialize game with board ${board.joinToString()}, and players ${players.joinToString()}.)"
    }
}