package service.network.messages

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass


@GameActionClass
data class InitMessage (
    val board : List<TileTypeMessage>,
    val players : List<PlayerTypeMessage>
) : GameAction() {
    override fun formatMessage(): String? {
        TODO("Not yet implemented")
    }
}