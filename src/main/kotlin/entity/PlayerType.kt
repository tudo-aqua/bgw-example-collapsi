package entity

/**
 * Enum class that represents the type of [Player] in the Collapsi game.
 *
 * The main use for this is to determine where the move is coming from and if the GUI should be interactable.
 */
enum class PlayerType {
    /**
     * The player is playing locally from the current device.
     *
     * Instructions to move pieces will come from the [gui] layer.
     */
    LOCAL,

    /**
     * The player is controlled by a bot.
     *
     * The [service.bot.BotService] will be making the moves for this player. GUI interaction is disabled.
     */
    BOT,

    /**
     * The player is controlled by an online player.
     *
     * The moves will come from the [service.network.NetworkService]. GUI interaction is disabled.
     */
    REMOTE
}