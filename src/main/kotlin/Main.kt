import gui.*
import service.bot.BotStrengthTest
import service.bot.ConsoleBotTest

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
 */
@Suppress("unused")
private fun testBotWithConsole() {
    ConsoleBotTest().test2Player4x4Board()
}

/**
 * Run multiple games to assess the strength of certain bot levels.
 */
@Suppress("unused")
private fun runBotStrengthTest() {
    BotStrengthTest().lvl1VsLvl1BoardSize4()
}