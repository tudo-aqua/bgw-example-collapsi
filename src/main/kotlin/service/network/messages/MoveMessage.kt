package service.network.messages

import service.network.messages.types.Direction
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class MoveMessage(
    val direction: Direction
) : GameAction() {
    override fun formatMessage(): String = "Move pawn in direction $direction."
}