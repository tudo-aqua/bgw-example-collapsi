package service

import gui.Refreshable

/**
 * Abstract service class that handles multiples [gui.Refreshable]s (usually UI elements, such as
 * specialized [tools.aqua.bgw.core.BoardGameScene] classes/instances) which are notified
 * of changes to refresh via the [onAllRefreshables] method.
 *
 */
abstract class AbstractRefreshingService {
    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Adds a [Refreshable] to the list that gets called
     * whenever [onAllRefreshables] is used.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Executes the passed method (usually a lambda) on all
     * [Refreshable]s registered with the service class that
     * extends this [AbstractRefreshingService]
     *
     * Example usage (from any method within the service):
     * ```
     * onAllRefreshables {
     *   refreshPlayerStack(p1, p1.playStack)
     *   refreshPlayerStack(p2, p2.playStack)
     *   refreshPlayerStack(p1, p1.collectedCardsStack)
     *   refreshPlayerStack(p2, p2.collectedCardsStack)
     * }
     * ```
     *
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) =
        refreshables.forEach { it.method() }
}