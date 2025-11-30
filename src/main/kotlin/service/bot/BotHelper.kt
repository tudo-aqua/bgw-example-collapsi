package service.bot

import entity.*
import service.RootService

/**
 * Helper class for the [BotService].
 *
 * This class contains deterministic methods that are only relevant for the bot.
 *
 * @param root The [RootService] that provides access to the overall game state and the other services.
 */
class BotHelper(private val root: RootService) {
    /**
     * Returns a list of all [Path]s the current player could take.
     *
     * In Collapsi, the exact moves you take to get to a destination don't matter, so this
     * method only returns one path per unique end position (last element in the path).
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @return All paths with unique end positions for the current player.
     *
     * @see Path
     */
    fun getPossibleUniquePaths(game: CollapsiGame): List<Path> {
        val paths = mutableListOf<Path>()

        completePath(paths, game)

        return paths
    }

    /**
     * Returns a list of all [Path]s the given player could take if it was their turn.
     *
     * In Collapsi, the exact moves you take to get to a destination don't matter, so this
     * method only returns one path per unique end position (last element in the path).
     *
     * @param color The color of the player to get the paths for.
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @return All paths with unique end positions for the player with the color in [color].
     *
     * @see Path
     */
    fun getPossibleUniquePathsForPlayer(color: PlayerColor, game: CollapsiGame): List<Path> {
        val gameState = game.currentState

        val paths = mutableListOf<Path>()

        val oldPlayerIndex = gameState.currentPlayerIndex
        gameState.currentPlayerIndex = gameState.players.indexOfFirst { it.color == color }

        completePath(paths, game)

        gameState.currentPlayerIndex = oldPlayerIndex

        return paths
    }

    /**
     * Recursive method to collect all [Path]s that the current player could take.
     *
     * Will call itself if the player has more than 1 move remaining, otherwise it will add the
     * newly found paths to [allPaths] using [Player.visitedTiles].
     *
     * @param allPaths The list of already found paths. Will be modified if a new path is found.
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     */
    private fun completePath(allPaths: MutableList<Path>, game: CollapsiGame) {
        val gameState = game.currentState
        val player = gameState.currentPlayer

        if (player.remainingMoves <= 1) {
            for (newPosition in getPossibleMoves(game)) {
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
            for (newPosition in getPossibleMoves(game)) {
                root.playerActionService.moveTo(newPosition, game)
                completePath(allPaths, game)
                root.playerActionService.undo(game)
            }
        }
    }

    /**
     * Finds all the valid [Coordinate]s the current player could move to in 1 step.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     *
     * @return A list of valid [Coordinate]s for [service.PlayerActionService.moveTo].
     */
    private fun getPossibleMoves(game: CollapsiGame): List<Coordinate> {
        val player = game.currentState.currentPlayer

        return player.position.neighbours.filter { root.playerActionService.canMoveTo(it, game) }
    }

    /**
     * Move along the given path and end the turn until the next alive player.
     *
     * @param game The cloned [CollapsiGame] that the bot simulation runs on.
     * @param path The [Path] to move along.
     */
    fun applyPath(game: CollapsiGame, path: Path) {
        // Move along the path.
        path.forEach { root.playerActionService.moveTo(it, game) }
        root.gameService.endTurn(game)

        // Automatically end the turn for all dead players.
        while (!game.currentState.currentPlayer.alive && !game.currentState.gameEnded) {
            root.gameService.endTurn(game)
        }
    }

    /**
     * Calls [BotService.calculateTurn] and [BotService.makeMove] until the current game is over.
     * A game must have been started beforehand.
     *
     * @throws IllegalStateException if no game has been started.
     * @throws IllegalStateException if one of the players wasn't a bot.
     */
    fun runGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState

        check(gameState.players.all { it.type == PlayerType.BOT }) { "Tried to run a game with a non-bot player." }

        // Call the bots until the game is over.
        while (root.currentGame != null) {
            root.botService.calculateTurn()

            repeat(gameState.currentPlayer.remainingMoves) {
                root.botService.makeMove()
            }

            root.gameService.endTurn()

            // Automatically end the turn for all dead players.
            while (!gameState.currentPlayer.alive && root.currentGame != null) {
                root.gameService.endTurn(game)
            }
        }
    }
}