package gui

import gui.Refreshable
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

class CollapsiApplication : BoardGameApplication("Collapsi"), Refreshable {

    private val rootService = RootService()

    private val lobbyScene = LobbyScene(rootService)

    private val gameScene = GameScene(rootService)

    private val endGameMenuScene = EndGameMenuScene(rootService)


    init {
        rootService.addRefreshables(
            this,
            lobbyScene,
            gameScene
        )

        this.showGameScene(gameScene)
        this.showMenuScene(lobbyScene)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(500)
    }

    override fun refreshAfterGameEnd() {
        showMenuScene(endGameMenuScene)
    }
}

