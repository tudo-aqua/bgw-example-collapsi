package service.network.messages

import service.network.messages.types.PlayerColor
import service.network.messages.types.TileType
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Binding for the message for initializing the game.
 *
 * Schema is defined in 'schemas/initMessage.json'.
 *
 * @param board A list of [TileType]s that represent the board, starting from the top left, going row by row.
 * @param players A list of [PlayerColor]s that are in the game. The order in the list is the turn order.
 */
@GameActionClass
data class InitMessage(
    val board: List<TileType>,
    val players: List<PlayerColor>
) : GameAction() {
    override fun formatMessage(): String {
        return "Initialize game with board ${board.joinToString()}, and players ${players.joinToString()}.)"
    }
}