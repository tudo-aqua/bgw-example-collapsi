package service

import entity.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Service class that manages al io-related operations for the Collapsi game.
 *
 * @param root The [RootService] that provides access to the overall game state and the other services.
 *
 * @see RootService
 * @see AbstractRefreshingService
 */
class FileService(private val root: RootService) : AbstractRefreshingService() {
    /**
     * The path that the game will be saved in / loaded from.
     */
    val saveFilePath = "saveFile.json"

    /**
     * The path that the secret for the network will be saved in / loaded from.
     */
    val propertiesFilePath = "app.properties"

    /*
    Note:
    The above two variables are both constants and are thus allowed to be in this stateless class.
     */

    /**
     * Saves the [CollapsiGame] from [RootService.currentGame] into the file at [saveFilePath].
     *
     * @throws IllegalStateException if no game was running.
     */
    fun saveGame(path: String = saveFilePath) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        check(!game.isOnlineGame()) { "Can't save an online game." }

        val json = Json {
            allowStructuredMapKeys = true
            prettyPrint = true
        }
        val jsonString = json.encodeToString(game)
        File(path).writeText(jsonString)
    }

    /**
     * Loads the [CollapsiGame] from the file at [saveFilePath] into [RootService.currentGame].
     *
     * @throws IllegalStateException if a game was already running.
     * @throws IllegalStateException if no file at [saveFilePath] exists.
     */
    fun loadGame(path: String = saveFilePath) {
        check(root.currentGame == null) { "Tried to load save file while a game was running." }
        check(saveFileExists(path)) { "Save File doesn't exist." }

        val json = Json {
            allowStructuredMapKeys = true
        }
        val jsonString = File(path).readText()
        root.currentGame = json.decodeFromString(jsonString)

        onAllRefreshables { refreshAfterLoad() }
    }

    /**
     * Checks if there is a saved game that can be loaded.
     *
     * @return True if a saved game exists.
     */
    fun saveFileExists(path: String = saveFilePath): Boolean = File(path).exists()

    /**
     * Deletes the [CollapsiGame] from the file at [saveFilePath].
     *
     * @throws IllegalStateException if no file at [saveFilePath] exists.
     */
    fun deleteSavedGame(path: String = saveFilePath) {
        check(saveFileExists(path)) { "Save File doesn't exist." }

        File(path).delete()
    }

    /**
     * Stores the secret and server for the network connection, so it only needs to be typed once.
     *
     * @param server The used network server.
     * @param secret The network secret for the server.
     */
    fun saveCredentials(server: String, secret: String, path: String = propertiesFilePath) {
        val properties = Properties()
        properties.setProperty("server", server)
        properties.setProperty("secret", secret)

        FileOutputStream(path).use { output ->
            properties.store(output, "Network Credentials")
        }
    }

    /**
     * Loads the server for the network connection if it was previously typed.
     *
     * @return The network server for the SoPra server or empty if it is unknown.
     */
    fun loadServer(path: String = propertiesFilePath): String {
        if (!File(path).exists())
            return ""

        val properties = Properties()
        FileInputStream(path).use { input ->
            properties.load(input)
        }

        return properties.getProperty("server")
    }

    /**
     * Loads the secret for the network connection if it was previously typed.
     *
     * @return The network secret for the SoPra server or empty if it is unknown.
     */
    fun loadSecret(path: String = propertiesFilePath): String {
        if (!File(path).exists())
            return ""

        val properties = Properties()
        FileInputStream(path).use { input ->
            properties.load(input)
        }

        return properties.getProperty("secret")
    }

    /**
     * Deletes the secret and server for network connection.
     *
     * @throws IllegalStateException if no file at [propertiesFilePath] exists.
     */
    fun deleteCredentials(path: String = propertiesFilePath) {
        val file = File(path)
        check(file.exists()) { "Credentials file doesn't exist." }
        file.delete()
    }
}