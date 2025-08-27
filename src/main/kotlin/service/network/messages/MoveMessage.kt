package service.network.messages

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class MoveMessage(
    val direction : DirectionMessage
) : GameAction() {
    override fun formatMessage(): String? {
        TODO("Not yet implemented")
    }
}