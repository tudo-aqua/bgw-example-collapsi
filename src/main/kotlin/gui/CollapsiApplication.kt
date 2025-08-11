package gui

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

class CollapsiApplication : BoardGameApplication("Collapsi"), Refreshable {

    private val root = RootService()

    private val lobbyScene = LobbyScene(root)

    private val gameScene = GameScene(root)

    private val endGameMenuScene = EndGameMenuScene(root)

    private val consoleRefreshable = ConsoleRefreshable(root)

    init {
        root.addRefreshables(
            this,
            lobbyScene,
            gameScene,
            consoleRefreshable
        )

        this.showGameScene(gameScene)
        this.showMenuScene(lobbyScene)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(500)
    }

    override fun refreshAfterGameEnd(winner: Player) {
        showMenuScene(endGameMenuScene)
    }
}

