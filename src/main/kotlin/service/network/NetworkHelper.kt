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

class NetworkHelper {
    fun convertGameToInitMessage(game: CollapsiGame): InitMessage {
        val currentState = game.currentState

        val tileTypes = mutableListOf<TileType>()
        for (y in 0..<currentState.boardSize) {
            for (x in 0..<currentState.boardSize) {
                val entityTile = currentState.getTileAt(Coordinate(x, y, currentState.boardSize))
                val tileType = getTileType(entityTile)

                tileTypes.add(tileType)
            }
        }

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

    private fun getTileType(tile: Tile): TileType {
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

    fun convertInitMessageToGame(
        message: InitMessage,
        clientColor: PlayerColor,
        clientBotDifficulty: Int
    ): CollapsiGame {
        val boardSize = sqrt(message.board.size.toDouble()).roundToInt()

        val positions = message.board.indices.map {
            Coordinate(it % boardSize, it / boardSize, boardSize)
        }.toMutableList()

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
}