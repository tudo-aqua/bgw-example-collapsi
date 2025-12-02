package service.network.messages

import service.network.messages.types.Direction
import service.network.messages.types.PlayerColor
import service.network.messages.types.TileType
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Binding for the message for moving the current player's pawn one tile.
 *
 * Schema is defined in 'schemas/moveMessage.json'.
 *
 * @param direction The direction to move the current player's pawn in.
 */
@GameActionClass
data class MoveMessage(
    val direction: Direction
) : GameAction() {
    override fun formatMessage(): String = "Move pawn in direction $direction."
}