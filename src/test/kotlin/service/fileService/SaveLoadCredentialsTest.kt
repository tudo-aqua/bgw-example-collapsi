package service.fileService

import entity.PlayerType
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import service.RootService
import service.TestRefreshable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SaveLoadCredentialsTest {
    private var root = RootService()

    private var testRefreshable = TestRefreshable(root)

    /**
     * Setup function to attach a [TestRefreshable] to a new [RootService] before each test.
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        testRefreshable = TestRefreshable(root)
        root.addRefreshable(testRefreshable)

        // Delete property file if it exists.
        try {
            root.fileService.deleteCredentials()
        } catch (_: IllegalStateException) {
        }
    }

    @Test
    fun testLoadSavedSecret() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(1, 1),
            boardSize = 4
        )

        val savedServer = "hello234"
        val savedSecret = "test123"

        assertDoesNotThrow { root.fileService.saveCredentials(savedServer, savedSecret) }

        // Test overriding an existing file.
        assertDoesNotThrow { root.fileService.saveCredentials(savedServer, savedSecret) }

        var loadedSecret: String? = null
        assertDoesNotThrow { loadedSecret = root.fileService.loadSecret() }
        assertNotNull(loadedSecret)
        assertEquals(savedSecret, loadedSecret)

        var loadedServer: String? = null
        assertDoesNotThrow { loadedServer = root.fileService.loadServer() }
        assertNotNull(loadedServer)
        assertEquals(savedServer, loadedServer)


        assertDoesNotThrow { root.fileService.deleteCredentials() }
    }

    @Test
    fun testExceptions() {
        // No file found.
        assertThrows(IllegalStateException::class.java) { root.fileService.deleteCredentials() }
    }

    @Test
    fun testLoadNoSecret() {
        assertEquals("", root.fileService.loadSecret())
        assertEquals("", root.fileService.loadServer())
    }
}