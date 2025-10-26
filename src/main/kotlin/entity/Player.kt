package entity

import kotlinx.serialization.Serializable

/**
 * Entity class that represents a player in the Collapsi game.
 *
 * @property color The color/shape of the pawn.
 * @property position The current position of the player on the board.
 * @property type The type of the player (local / bot / remote).
 * @property botDifficulty The difficulty of the player if [type] is set to [PlayerType.BOT]. (Range 1-4).
 * 1 means random moves. 4 means always make the best move the bot can find.
 */
@Serializable
data class Player(
    val color: PlayerColor,
    var position: Coordinate,
    val type: PlayerType,
    val botDifficulty: Int = 0
) {
    /** The number of moves the player has left to make in his current turn. */
    var remainingMoves = 1

    /** A list of [Coordinate]s that the player has visited in the current turn. */
    val visitedTiles: MutableList<Coordinate> = mutableListOf()

    /** Indicates whether the player is still part of the game. */
    var alive = true

    /** The winner-rank of the player after death. Winner is 0, 4th place is 3. */
    var rank: Int? = null

    /**
     * Creates a deep copy of this [Player] and returns it.
     *
     * Changes to the returned [Player] will never impact this instance.
     *
     * @return A deep-cloned copy of this object.
     */
    fun clone(): Player {
        val clone = Player(color, position, type, botDifficulty)

        clone.remainingMoves = remainingMoves
        clone.visitedTiles.addAll(visitedTiles)
        clone.alive = alive
        clone.rank = rank

        return clone
    }
}