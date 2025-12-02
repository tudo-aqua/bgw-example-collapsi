package gui.types

import gui.*

/**
 * Enum to represent the mode of the [LobbyScene].
 */
enum class LobbyMode {
    /**
     * The lobby only contains non-online players.
     */
    LOCAL,

    /**
     * The lobby is an online game. The current application is the host of the lobby.
     */
    HOST,

    /**
     * The lobby is an online game. The current application has joined another application's lobby.
     */
    GUEST
}