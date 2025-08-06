package service

import kotlin.test.*
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Test class for the functionality of the [RootService].
 */
class RootServiceTest {

    /** Test adding a single Refreshable to the RootService. */
    @Test
    fun testAddRefreshable() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable(rootService)

        // Test: The Refreshable is added without an error
        assertDoesNotThrow { rootService.addRefreshable(testRefreshable) }
    }

    /** Test adding multiple Refreshables to the RootService. */
    @Test
    fun testAddMultipleRefreshables() {
        val rootService = RootService()
        val testRefreshable1 = TestRefreshable(rootService)
        val testRefreshable2 = TestRefreshable(rootService)

        // Test: The Refreshables are added without an error
        assertDoesNotThrow { rootService.addRefreshables(testRefreshable1, testRefreshable2) }
    }
}