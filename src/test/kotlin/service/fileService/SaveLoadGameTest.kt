package service.fileService

import entity.*
import service.*
import kotlin.test.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File

/**
 * Tests the save/load/delete functionality for saved games of the [FileService].
 */
class SaveLoadGameTest {
    private var root = RootService()

    private var testRefreshable = TestRefreshable(root)

    /**
     * Setup function to attach a [TestRefreshable] to a new [RootService] before each test and clear the saved game.
     */
    @BeforeTest
    fun setup() {
        root = RootService()
        testRefreshable = TestRefreshable(root)
        root.addRefreshable(testRefreshable)

        // Delete saved game if it exists.
        if (File(root.fileService.saveFilePath).exists())
            root.fileService.deleteSavedGame()
    }

    /**
     * Cleans up by deleting the saved game.
     */
    @AfterTest
    fun cleanup() {
        // Delete saved game if it exists.
        if (File(root.fileService.saveFilePath).exists())
            root.fileService.deleteSavedGame()
    }

    /**
     * Tests that a game can be saved and loaded afterwards.
     */
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

        // Both need to have equal values but reference two separate objects.
        assertEquals(savedGame, loadedGame)
        assertNotSame(savedGame, loadedGame)

        assertEquals(1, loadedGame.currentState.players[0].botDifficulty)
        assertEquals(4, loadedGame.currentState.boardSize)

        // Check if all tiles have been loaded.
        for (position in savedGame.currentState.board.map { it.key }) {
            assertEquals(
                savedGame.currentState.getTileAt(position).movesToMake,
                loadedGame.currentState.getTileAt(position).movesToMake
            )
        }

        assertDoesNotThrow { root.fileService.deleteSavedGame() }
    }

    /**
     * Tests various states where calling a method will return an exception.
     */
    @Test
    fun testExceptions() {
        // No game running.
        assertThrows<IllegalStateException> { root.fileService.saveGame() }

        // No file found.
        assertThrows<IllegalStateException> { root.fileService.loadGame() }

        // No file found.
        assertThrows<IllegalStateException> { root.fileService.deleteSavedGame() }
    }
}