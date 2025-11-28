package service.fileService

import entity.*
import service.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the save/load/delete functionality for server/secret (credentials) of the [FileService].
 */
class SaveLoadCredentialsTest {
    private var root = RootService()

    private var testRefreshable = TestRefreshable(root)
    
    private val path = "app_test.properties"

    /**
     * Setup function to attach a [TestRefreshable] to a new [RootService] before each test and clear
     * the saved credentials.
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        testRefreshable = TestRefreshable(root)
        root.addRefreshable(testRefreshable)

        // Delete property file if it exists.
        val file = File(path)
        if (file.exists())
            file.delete()
    }

    /**
     * Cleans up by deleting the saved credentials.
     */
    @AfterTest
    fun cleanup() {
        // Delete property file if it exists.
        val file = File(path)
        if (file.exists())
            file.delete()
    }

    /**
     * Tests saving and loading a secret and server.
     *
     * Note that this test will override and then delete the current secret.
     */
    @Test
    fun testSavingAndLoadingCredentials() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(1, 1),
            boardSize = 4
        )

        val savedServer = "hello234"
        val savedSecret = "test123"

        assertDoesNotThrow { root.fileService.saveCredentials(savedServer, savedSecret, path) }

        // Test overriding an existing file.
        assertDoesNotThrow { root.fileService.saveCredentials(savedServer, savedSecret, path) }

        var loadedSecret: String? = null
        assertDoesNotThrow { loadedSecret = root.fileService.loadSecret(path) }
        assertNotNull(loadedSecret)
        assertEquals(savedSecret, loadedSecret)

        var loadedServer: String? = null
        assertDoesNotThrow { loadedServer = root.fileService.loadServer(path) }
        assertNotNull(loadedServer)
        assertEquals(savedServer, loadedServer)

        assertDoesNotThrow { root.fileService.deleteCredentials(path) }
        assertFalse { File(path).exists() }
    }

    /**
     * Tests that deleting a non-existent file throws and exception.
     */
    @Test
    fun testExceptions() {
        // No file found.
        assertThrows(IllegalStateException::class.java) { root.fileService.deleteCredentials(path) }
    }

    /**
     * Tests that loading credentials without saving first will return empty strings.
     */
    @Test
    fun testLoadNoCredentials() {
        assertEquals("", root.fileService.loadSecret(path))
        assertEquals("", root.fileService.loadServer(path))
    }
}