package service.bot

/**
 * Object class that contains constants used for bot fine-tuning and lvl strength management.
 *
 * @see BotService
 */
object BotConstants {
    /** Assigned eval when a player wins. */
    const val EVAL_WIN = 500.0

    /** Assigned eval when a player loses. */
    const val EVAL_LOSS = -499.0

    /** Chance for the lvl 2 bot to make a smart move vs. a random move. */
    const val LVL2_SMART_MOVE_CHANCE = 0.33

    /** The maximum minimax depth for a lvl 2 bot. */
    const val LVL2_MAX_DEPTH = 6

    /** The maximum minimax search time for a lvl 2 bot. */
    const val LVL2_MAX_DURATION = 400

    /** Chance for the lvl 3 bot to make a smart move vs. a random move.*/
    const val LVL3_SMART_MOVE_CHANCE = 0.67

    /** The maximum minimax depth for a lvl 3 bot. */
    const val LVL3_MAX_DEPTH = 7

    /** The maximum minimax search time for a lvl 3 bot. */
    const val LVL3_MAX_DURATION = 600

    /** The maximum minimax depth for a lvl 4 bot. */
    const val LVL4_MAX_DEPTH = Int.MAX_VALUE

    /** The maximum minimax search time for a lvl 4 bot. */
    const val LVL4_MAX_DURATION = 3000
}