package service

import entity.CollapsiGame
import service.bot.*
import service.network.*

/**
 * Main class of the service layer for the Collapsi game. Provides access
 * to all other service classes and holds the [currentGame] state for these
 * services to access.
 *
 * @see GameService
 * @see PlayerActionService
 * @see FileService
 * @see BotService
 */
class RootService {
    /**
     * A reference to the [GameService] attached to this [RootService].
     *
     * @see GameService
     */
    val gameService = GameService(this)

    /**
     * A reference to the [PlayerActionService] attached to this [RootService].
     *
     * @see PlayerActionService
     */
    val playerActionService = PlayerActionService(this)

    /**
     * A reference to the [FileService] attached to this [RootService].
     *
     * @see FileService
     */
    val fileService = FileService(this)

    /**
     * A reference to the [BotService] attached to this [RootService].
     *
     * @see BotService
     */
    val botService = BotService(this)

    /**
     * A reference to the [NetworkService] attached to this [RootService].
     *
     * @see NetworkService
     */
    val networkService = NetworkService(this)

    /**
     * The currently active game. Can be `null`, if no game has started yet or after a game has finished.
     */
    var currentGame: CollapsiGame? = null

    /**
     * Adds the provided [newRefreshable] to all [AbstractRefreshingService]s connected to this [RootService].
     *
     * @param newRefreshable The [Refreshable] that should be added.
     *
     * @see addRefreshables
     * @see Refreshable
     * @see AbstractRefreshingService
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        fileService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all [AbstractRefreshingService]s
     * connected to this [RootService].
     *
     * @param newRefreshables The [Refreshable]s that should be added.
     *
     * @see addRefreshable
     * @see Refreshable
     * @see AbstractRefreshingService
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}
