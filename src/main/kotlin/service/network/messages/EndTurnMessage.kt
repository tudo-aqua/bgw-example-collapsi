package service.network.messages

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Binding for the message that ends the current player's turn.
 *
 * Contains no information other than that the turn has ended.
 *
 * Schema is defined in 'schemas/endTurnMessage.json'.
 */
@GameActionClass
class EndTurnMessage : GameAction() {
    override fun formatMessage(): String = "End the current player's turn."

    // This class can't be a data class, because it has no parameters, so we need to implement toString() manually.
    override fun toString(): String = "EndTurnMessage"
}