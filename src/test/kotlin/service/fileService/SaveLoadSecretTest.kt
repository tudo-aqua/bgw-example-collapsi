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

class SaveLoadSecretTest {
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

        // Delete secret if it exists.
        try {
            root.fileService.deleteSecret()
        } catch (e: IllegalStateException) {
        }
    }

    @Test
    fun testLoadSavedSecret() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(1, 1),
            boardSize = 4
        )

        val savedSecret = "test123"

        assertDoesNotThrow { root.fileService.saveSecret(savedSecret) }

        // Test overriding an existing file.
        assertDoesNotThrow { root.fileService.saveSecret(savedSecret) }

        var loadedSecret: String? = null
        assertDoesNotThrow { loadedSecret = root.fileService.loadSecret() }

        assertNotNull(loadedSecret)

        assertEquals(savedSecret, loadedSecret)

        assertDoesNotThrow { root.fileService.deleteSecret() }
    }

    @Test
    fun testExceptions() {
        // No file found.
        assertThrows(IllegalStateException::class.java) { root.fileService.deleteSecret() }
    }

    @Test
    fun testLoadNoSecret() {
        assertNull(root.fileService.loadSecret())
    }
}