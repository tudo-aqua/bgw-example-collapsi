import gui.*
import service.bot.BotStrengthSimulation
import service.bot.ConsoleBotSimulation

/**
 * Main method of the program. Called when you press "run" in gradle.
 */
fun main() {
    // Uncomment what you want to do.

    runGui()
    // testBotWithConsole()
    // runBotStrengthTest()

    println("Application ended. Goodbye")
}

/**
 * Opens the main GUI of the game.
 */
@Suppress("unused")
private fun runGui() {
    CollapsiApplication().show()
}

/**
 * Run a game with bots using the same difficulty. Log each action to the console.
 *
 * @see ConsoleBotSimulation
 */
@Suppress("unused")
private fun testBotWithConsole() {
    ConsoleBotSimulation().test2Player4x4Board()
}

/**
 * Run multiple games to assess the strength of certain bot levels.
 *
 * @see BotStrengthSimulation
 */
@Suppress("unused")
private fun runBotStrengthTest() {
    BotStrengthSimulation().lvl2VsLvl4BoardSize4()
}