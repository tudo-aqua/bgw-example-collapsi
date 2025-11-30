package entity

import kotlinx.serialization.Serializable

/**
 * Entity class that represents a game state of "Collapsi".
 *
 * @property players The [Player]s currently in the game.
 * @property board A map containing all [Tile]s in the game, including those that are collapsed.
 * @property boardSize Size of one axis in [board], assuming the area is quadratic.
 */
@Serializable
data class GameState(
    val players: List<Player>,
    val board: Map<Coordinate, Tile>,
    val boardSize: Int
) {
    /** The index of the [Player] whose turn it currently is. */
    var currentPlayerIndex = 0

    /** The [Player] whose turn it currently is. */
    val currentPlayer get() = players[currentPlayerIndex]

    /** Whether the game has ended by only having one player alive. */
    val gameEnded get() = players.count { it.alive } <= 1

    /*
    Coding Tip:
    The field above is known as a property without a backing field, so it doesn't save any data.
    An alternative to this would have been a getCurrentPlayer() method, which is functionally identical.
    Properties are used like variables. You can even define custom setters.
     */

    /**
     * Returns the [Tile] at the given position.
     *
     * @param position The position of the [Tile] to retrieve.
     *
     * @return The [Tile] at the specified position.
     * @throws IllegalArgumentException If no [Tile] exists at the specified position.
     */
    fun getTileAt(position: Coordinate): Tile =
        checkNotNull(board[position]) { "Tile at $position does not exist in this GameState." }

    /*
    Note:
    Functions like these are allowed in the entity layer because they are very simple and independent of the
    game's rules. They are - in a sense - shortcuts for readability and faster coding.
     */

    /**
     * Checks if any player is standing on the given tile.
     *
     * @param position The position for which to check occupancy.
     *
     * @return True if there is a player who is standing on the given position.
     */
    fun isTileOccupied(position: Coordinate): Boolean = players.any { it.position == position }

    /**
     * Sets the [currentPlayerIndex] to the next living player.
     */
    fun nextPlayer() {
        check(!gameEnded) { "nextPlayer() requires at least 1 player to be alive." }

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!currentPlayer.alive)
    }

    /**
     * Creates a deep copy of this [GameState] and returns it.
     *
     * Changes to the returned [GameState] will never impact this instance.
     *
     * @return A deep-cloned copy of this object.
     */
    fun clone(): GameState {
        val playersCopy = players.map { it.clone() }
        val boardCopy = board.map { Pair(it.key, it.value.clone()) }.toMap()

        val clone = GameState(playersCopy, boardCopy, boardSize)

        clone.currentPlayerIndex = currentPlayerIndex

        return clone
    }

    /*
    Note:
    For save, load, undo, redo, and for bots, a custom deep-clone method like the one above is recommended.
    Kotlin's .copy() method for data classes is only a shallow-copy, meaning that if the class is referencing
    an object, then both instances (copy and original) will reference the same object, which will break any
    of the above use-cases.
    Also, .copy() only copies the parameters in the constructor. "currentPlayerIndex" would be ignored in this class.
    
    An alternative to a handwritten deep-clone method is to serialize and deserialize the object.
    This is easier to implement and doesn't require reimplementation if the entity layer changes, but there
    are two problems:
    1. It's slower than the custom-written function, which can matter for high-performance cases like the bot.
    2. It breaks if there are any circular or recursive references, e.g., if the Player saves the Tile it's standing
    on and the Tile saves the Player standing on it.
    Circular/recursive references can be tricky even for the handwritten clone() method, so I advise keeping 
    this in mind when planning the entity layer.
     */
}