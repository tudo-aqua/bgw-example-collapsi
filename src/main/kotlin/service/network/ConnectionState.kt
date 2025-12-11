package service.network

/**
 * Enum to distinguish the different states that a client can be in, in particular
 * during connection and game setup. Used in [NetworkService] and [NetworkClient].
 */

enum class ConnectionState {
    /**
     * Initial state. No connection is active and no [NetworkClient] exists.
     */
    DISCONNECTED,

    /**
     * The current [NetworkClient] is connected to a server, but has not started, hosted, or joined any game yet.
     */
    CONNECTED,

    /**
     * The current [NetworkClient] is connected and is trying to create a lobby.
     * Currently waiting for a response from the server.
     */
    WAITING_FOR_HOST_CONFIRMATION,

    /**
     * The current [NetworkClient] is connected and is trying to join a lobby.
     * Currently waiting for a response from the server.
     */
    WAITING_FOR_JOIN_CONFIRMATION,

    /**
     * The current [NetworkClient] is connected and has created a lobby, but has not started a game yet.
     */
    WAITING_FOR_GUESTS,

    /**
     * The current [NetworkClient] is connected and has joined a lobby, but the game hasn't started yet.
     */
    WAITING_FOR_INIT,

    /**
     * The current [NetworkClient] is connected, is in an active game, and is currently waiting to make a move.
     */
    PLAYING_MY_TURN,

    /**
     * The current [NetworkClient] is connected, is in an active game, and is currently waiting for the
     * opponent(s) to make a move.
     */
    WAITING_FOR_OPPONENTS
}