package service.bot

import entity.*
import service.RootService

class BotHelper(private val root: RootService) {
    // This returns the list of paths the player can take. Only one path per end position.
    fun getPossibleUniquePaths(): List<Path> {
        val paths = mutableListOf<Path>()

        completePath(paths)

        return paths
    }

    fun getPossibleUniquePathsForPlayer(color: PlayerColor): List<Path> {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame

        val paths = mutableListOf<Path>()

        val oldPlayerIndex = gameState.currentPlayerIndex
        gameState.currentPlayerIndex = gameState.players.indexOfFirst { it.color == color }

        completePath(paths)

        gameState.currentPlayerIndex = oldPlayerIndex

        return paths
    }

    fun completePath(allPaths: MutableList<Path>) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        if (player.remainingMoves <= 1) {
            for (newPosition in getPossibleMoves()) {
                // Ignore this path if there is already a path with this end position.
                if (allPaths.any { it.last() == newPosition })
                    continue

                val extendedPath = player.visitedTiles.toMutableList()

                extendedPath.add(player.position)
                extendedPath.add(newPosition)
                extendedPath.removeFirst()

                allPaths.add(extendedPath)
            }
        } else {
            for (newPosition in getPossibleMoves()) {
                root.playerActionService.moveTo(newPosition)
                completePath(allPaths)
                root.playerActionService.undo()
            }
        }
    }

    fun getPossibleMoves(): List<Coordinate> {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
        val player = gameState.currentPlayer

        return player.position.neighbours.filter { root.playerActionService.canMoveTo(it) }
    }
}