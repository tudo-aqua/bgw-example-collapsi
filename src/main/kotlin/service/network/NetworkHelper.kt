package service.network

import entity.CollapsiGame
import entity.Coordinate
import entity.GameState
import entity.Player
import entity.PlayerType
import entity.Tile
import service.network.messages.*
import service.network.messages.types.*
import kotlin.math.*

/**
 * Helper class for the [NetworkService].
 *
 * This class contains non-network methods that are only relevant for the network implementation, such as
 * converting between messages and entity-layer representations.
 */
class NetworkHelper {
    /**
     * Creates an [InitMessage] using a given [CollapsiGame]. Assumes that the game was just started.
     *
     * @param game The input [CollapsiGame].
     *
     * @return An [InitMessage] based on [game].
     */
    internal fun convertGameToInitMessage(game: CollapsiGame): InitMessage {
        val currentState = game.currentState

        // Create [InitMessage.board].
        val tileTypes = mutableListOf<TileType>()
        for (y in 0..<currentState.boardSize) {
            for (x in 0..<currentState.boardSize) {
                val entityTile = currentState.getTileAt(Coordinate(x, y, currentState.boardSize))
                val tileType = convertTileToTileType(entityTile)

                tileTypes.add(tileType)
            }
        }

        // Create [InitMessage.players].
        val playerColors = mutableListOf<PlayerColor>()
        for (player in currentState.players) {
            val playerColor = when (player.color) {
                entity.PlayerColor.GREEN_SQUARE -> PlayerColor.GREEN_SQUARE
                entity.PlayerColor.ORANGE_HEXAGON -> PlayerColor.ORANGE_HEXAGON
                entity.PlayerColor.YELLOW_CIRCLE -> PlayerColor.YELLOW_CIRCLE
                entity.PlayerColor.RED_TRIANGLE -> PlayerColor.RED_TRIANGLE
            }

            playerColors.add(playerColor)
        }

        return InitMessage(tileTypes, playerColors)
    }

    /**
     * Converts a [Tile] into a [TileType].
     *
     * @param tile The input [Tile].
     *
     * @return A [TileType] based on [tile].
     */
    private fun convertTileToTileType(tile: Tile): TileType {
        val startTileColor = tile.startTileColor
        return if (startTileColor != null) {
            when (startTileColor) {
                entity.PlayerColor.GREEN_SQUARE -> TileType.START_GREEN
                entity.PlayerColor.ORANGE_HEXAGON -> TileType.START_ORANGE
                entity.PlayerColor.YELLOW_CIRCLE -> TileType.START_YELLOW
                entity.PlayerColor.RED_TRIANGLE -> TileType.START_RED
            }
        } else {
            when (tile.movesToMake) {
                1 -> TileType.ONE
                2 -> TileType.TWO
                3 -> TileType.THREE
                4 -> TileType.FOUR
                else -> throw IllegalStateException("Found tile with illegal step count.")
            }
        }
    }

    /**
     * Creates a [CollapsiGame] using a given [InitMessage]. The game will be in a state as if it
     * has just been started.
     *
     * @param message The input [InitMessage].
     * @param clientColor The color of the local client. Used for setting [Player.type].
     * @param clientBotDifficulty The bot difficulty of the local client.
     * Used for setting [Player.type] and [Player.botDifficulty].
     *
     * @return A [CollapsiGame] based on the given [InitMessage].
     */
    internal fun convertInitMessageToGame(
        message: InitMessage,
        clientColor: PlayerColor,
        clientBotDifficulty: Int
    ): CollapsiGame {
        val boardSize = sqrt(message.board.size.toDouble()).roundToInt()

        val positions = message.board.indices.map {
            Coordinate(it % boardSize, it / boardSize, boardSize)
        }.toMutableList()

        // Create board.
        val board = mutableMapOf<Coordinate, Tile>()
        val playerPositions = mutableMapOf<entity.PlayerColor, Coordinate>()
        for (tileType in message.board) {
            val position = positions.removeFirst()
            val startTileColor = tileType.getPlayerColor()
            val tile = Tile(position, tileType.getStepCount(), startTileColor)
            board[position] = tile

            if (startTileColor != null) {
                playerPositions[startTileColor] = position
            }
        }

        // Create player list.
        val players = mutableListOf<Player>()
        for (playerColor in message.players) {
            val isClient = playerColor == clientColor

            val entityPlayerColor = playerColor.toEntityPlayerColor()
            val position = playerPositions.getValue(entityPlayerColor)
            val playerType = if (!isClient) {
                PlayerType.REMOTE
            } else if (clientBotDifficulty == 0) {
                PlayerType.LOCAL
            } else {
                PlayerType.BOT
            }

            val player = Player(entityPlayerColor, position, playerType, clientBotDifficulty)
            players.add(player)
        }

        val gameState = GameState(players, board, boardSize)

        return CollapsiGame(gameState)
    }

    /**
     * Applies a [Direction] to a given [Coordinate] and returns the result.
     *
     * @param originalPosition The [Coordinate] to apply the direction to.
     * @param direction The [Direction] to move in.
     *
     * @return The new position. Always adjacent to [originalPosition] in the direction of [direction].
     */
    internal fun convertDirectionToPosition(originalPosition: Coordinate, direction: Direction) =
        when (direction) {
            Direction.LEFT -> originalPosition.leftNeighbour
            Direction.RIGHT -> originalPosition.rightNeighbour
            Direction.UP -> originalPosition.upNeighbour
            Direction.DOWN -> originalPosition.downNeighbour
        }

    /**
     * Finds and returns the relative [Direction] between two adjacent [Coordinate]s.
     *
     * @param originalPosition Position 1.
     * @param newPosition Position 2. Must be adjacent ot [originalPosition].
     *
     * @return The relative [Direction] between [originalPosition] and [newPosition].
     *
     * @throws IllegalArgumentException if the given [Coordinate]s were not adjacent.
     */
    internal fun convertPositionsToDirection(originalPosition: Coordinate, newPosition: Coordinate) =
        when (newPosition) {
            originalPosition.leftNeighbour -> Direction.LEFT
            originalPosition.rightNeighbour -> Direction.RIGHT
            originalPosition.upNeighbour -> Direction.UP
            originalPosition.downNeighbour -> Direction.DOWN
            else -> throw IllegalArgumentException("Given coordinates were not adjacent.")
        }
}