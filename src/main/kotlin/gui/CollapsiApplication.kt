package gui

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.util.Font

/**
 * Implementation of the BGW [BoardGameApplication] for the game "Collapsi".
 */
class CollapsiApplication : BoardGameApplication("Collapsi"), Refreshable {

    private val root = RootService()

    private val mainMenuScene: MainMenuScene by lazy {
        MainMenuScene().apply {
            localGameButton.onMouseClicked = {
                showMenuScene(lobbyScene)
            }
            hostGameButton.onMouseClicked = {
                hostOnlineLobbyScene.generateNewCode()
                hostOnlineLobbyScene.loadSecret()
                showMenuScene(hostOnlineLobbyScene)
            }
            joinGameButton.onMouseClicked = {
                joinOnlineLobbyScene.loadSecret()
                showMenuScene(joinOnlineLobbyScene)
            }
        }
    }

    private val lobbyScene = LobbyScene(root).apply {
        backButton.onMouseClicked = {
            // Todo: Show join scene if it was the previous scene, but where to save that information?
            showMenuScene(mainMenuScene)
        }
    }

    private val hostOnlineLobbyScene = HostOnlineLobbyScene(root).apply {
        joinLobbyButton.onMouseClicked = {
            saveSecret()
            showMenuScene(lobbyScene)
        }
        backButton.onMouseClicked = {
            showMenuScene(mainMenuScene)
        }
    }

    private val joinOnlineLobbyScene = JoinOnlineLobbyScene(root).apply {
        joinLobbyButton.onMouseClicked = {
            saveSecret()
            showMenuScene(lobbyScene)
        }
        backButton.onMouseClicked = {
            showMenuScene(mainMenuScene)
        }
    }

    private val gameScene = GameScene(root)

    private val endGameMenuScene = EndGameMenuScene(root)

    private val consoleRefreshable = ConsoleRefreshable(root)

    init {
        loadFont("Fonts/RussoOne-Regular.ttf", "RussoOne", Font.FontWeight.NORMAL)

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

    override fun refreshAfterGameEnd(winner: Player) {
        //showMenuScene(endGameMenuScene)
    }
}

