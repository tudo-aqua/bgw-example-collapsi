package gui

import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*

class MainMenuScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080) {
    val paneWidth = 800

    val paneHeight = 760

    private val contentPane = Pane<StaticComponentView<*>>(
        posX = 1920 / 2 - paneWidth / 2,
        posY = 1080 / 2 - paneHeight / 2,
        width = paneWidth,
        height = paneHeight,
        visual = ColorVisual(Constants.color_background)
    )

    private val logo = Label(
        posX = paneWidth / 2 - 750 / 2,
        posY = 60,
        width = 750,
        height = 210,
        visual = ImageVisual("MenuScenes/Exports/Logo.png")
    )

    private val localLabel = Label(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 300,
        width = 200,
        height = 100,
        font = Constants.font_heading,
        text = "Local"
    )

    private val onlineLabel = Label(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 300,
        width = 200,
        height = 100,
        font = Constants.font_heading,
        text = "Online"
    )

    val newGameButton = Button(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 430,
        width = 200,
        height = 100,
        visual = ImageVisual("MenuScenes/Exports/Button_MainMenu_New.png")
    ).apply {
        onMouseClicked = {
            app.lobbyScene.previousScene = app.mainMenuScene
            app.showMenuScene(app.lobbyScene)
        }
    }

    val loadGameButton = Button(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 550,
        width = 200,
        height = 100,
        visual = ImageVisual("MenuScenes/Exports/Button_MainMenu_Load.png")
    ).apply {
        onMouseClicked = {
            root.fileService.loadGame()
        }
    }

    val hostGameButton = Button(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 430,
        width = 200,
        height = 100,
        visual = ImageVisual("MenuScenes/Exports/Button_MainMenu_Host.png")
    ).apply {
        onMouseClicked = {
            app.hostOnlineLobbyScene.generateNewCode()
            app.hostOnlineLobbyScene.loadCredentials()
            app.showMenuScene(app.hostOnlineLobbyScene)
        }
    }

    val joinGameButton = Button(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 550,
        width = 200,
        height = 100,
        visual = ImageVisual("MenuScenes/Exports/Button_MainMenu_Join.png")
    ).apply {
        onMouseClicked = {
            app.joinOnlineLobbyScene.loadCredentials()
            app.showMenuScene(app.joinOnlineLobbyScene)
        }
    }

    init {
        background = Visual.EMPTY

        addComponents(
            contentPane
        )

        contentPane.addAll(
            logo,
            localLabel,
            onlineLabel,
            newGameButton,
            loadGameButton,
            hostGameButton,
            joinGameButton
        )
    }
}