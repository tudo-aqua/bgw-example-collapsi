package service.network.messages.types

import service.network.messages.*

/**
 * The direction of a pawn move. Used in [MoveMessage].
 */
enum class Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN
}