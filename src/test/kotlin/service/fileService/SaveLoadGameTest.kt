package service.fileService

import entity.*
import service.*
import kotlin.test.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow

class SaveLoadGameTest {
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

        // Delete saved game if it exists.
        try {
            root.fileService.deleteSavedGame()
        } catch (_: IllegalStateException) {
        }
    }

    @Test
    fun testLoadSavedGame() {
        root.gameService.startNewGame(
            playerTypes = listOf(PlayerType.LOCAL, PlayerType.LOCAL),
            botDifficulties = listOf(1, 1),
            boardSize = 4
        )

        assertDoesNotThrow { root.fileService.saveGame() }

        // Test overriding an existing file.
        assertDoesNotThrow { root.fileService.saveGame() }

        val savedGame = checkNotNull(root.currentGame)
        root.currentGame = null

        assertDoesNotThrow { root.fileService.loadGame() }

        val loadedGame = assertNotNull(root.currentGame)

        assertEquals(savedGame, loadedGame)
        assertNotSame(savedGame, loadedGame)

        assertEquals(1, loadedGame.currentGame.players[0].botDifficulty)
        assertEquals(4, loadedGame.currentGame.boardSize)

        // Check if all tiles have been loaded.
        for (position in savedGame.currentGame.board.map { it.key }) {
            assertEquals(
                savedGame.currentGame.getTileAt(position).movesToMake,
                loadedGame.currentGame.getTileAt(position).movesToMake
            )
        }

        assertDoesNotThrow { root.fileService.deleteSavedGame() }
    }

    @Test
    fun testExceptions() {
        // No game running.
        assertThrows(IllegalStateException::class.java) { root.fileService.saveGame() }

        // No file found.
        assertThrows(IllegalStateException::class.java) { root.fileService.loadGame() }

        // No file found.
        assertThrows(IllegalStateException::class.java) { root.fileService.deleteSavedGame() }
    }
}