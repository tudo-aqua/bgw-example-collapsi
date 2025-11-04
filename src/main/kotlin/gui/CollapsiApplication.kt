package gui

import service.*
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.util.Font

/**
 * Implementation of the BGW [BoardGameApplication] for the game "Collapsi".
 */
class CollapsiApplication : BoardGameApplication("Collapsi"), Refreshable {

    private val root = RootService()

    val mainMenuScene: MainMenuScene = MainMenuScene(this, root)

    val lobbyScene = LobbyScene(this, root)

    val hostOnlineLobbyScene = HostOnlineLobbyScene(this, root)

    val joinOnlineLobbyScene = JoinOnlineLobbyScene(this, root)

    val waitingForHostScene = WaitingForHostScene(this)

    val gameScene = GameScene(this, root)

    private val consoleRefreshable = ConsoleRefreshable(root)

    init {
        loadFont("fonts/RussoOne-Regular.ttf", "RussoOne", Font.FontWeight.NORMAL)

        root.addRefreshables(
            this,
            lobbyScene,
            gameScene,
            consoleRefreshable
        )

        showGameScene(gameScene)
        showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(500)
    }

    override fun refreshAfterLoad() {
        hideMenuScene(500)
    }
}

