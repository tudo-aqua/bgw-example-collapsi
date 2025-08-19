package service

import entity.*
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Service class that manages al io-related operations for the Collapsi game.
 *
 * @param root The root service that provides access to the overall game state.
 */
class FileService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * The path that the game will be saved in / loaded from.
     */
    val saveFilePath = "src/main/resources/saveFile.json"

    /**
     * The path that the secret for the network will be saved in / loaded from.
     */
    val secretFilePath = "src/main/resources/secret.json"

    /**
     * Saves the [CollapsiGame] from [RootService.currentGame] into the file at [saveFilePath].
     *
     * @throws IllegalStateException if no game was running.
     */
    fun saveGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        val json = Json.encodeToString(game)
        File(saveFilePath).writeText(json)
    }

    /**
     * Loads the [CollapsiGame] from the file at [saveFilePath] into [RootService.currentGame].
     *
     * @throws IllegalStateException if a game was already running.
     * @throws IllegalStateException if no file at [saveFilePath] existed.
     */
    fun loadGame() {
        check(root.currentGame == null) { "Tried to load save file while a game was running." }
        check(File(saveFilePath).exists()) { "Save File doesn't exist." }

        val jsonString = File(saveFilePath).readText()
        root.currentGame = Json.decodeFromString(jsonString)
    }

    /**
     * Stores the secret for the network server connection, so it only needs to be typed once.
     *
     * @param secret The network secret for the SoPra server.
     */
    fun saveSecret(secret: String) {
        File(secretFilePath).writeText(secret)
    }

    /**
     * Loads the secret for the network server connection if it was previously typed.
     *
     * @return The network secret for the SoPra server or null if it is unknown.
     */
    fun loadSecret(): String? {
        if (!File(secretFilePath).exists())
            return null

        return File(secretFilePath).readText()
    }
}