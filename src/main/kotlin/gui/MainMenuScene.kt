package gui

import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*

/**
 * The main menu scene. Contains buttons to move to the other scenes.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 * @param root The main [RootService] containing all other services.
 */
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
        visual = ImageVisual("menuScenes/Logo.png")
    )

    private val localLabel = Label(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 290,
        width = 200,
        height = 100,
        font = Constants.font_heading,
        text = "Local"
    )

    private val localLabelHeadingLine = Label(
        posX = paneWidth * 0.3 - 250 / 2,
        posY = 380,
        width = 250,
        height = 5,
        visual = ImageVisual("menuScenes/HeadingLine.png")
    )

    private val onlineLabel = Label(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 290,
        width = 200,
        height = 100,
        font = Constants.font_heading,
        text = "Online"
    )

    private val onlineLabelHeadingLine = Label(
        posX = paneWidth * 0.7 - 250 / 2,
        posY = 380,
        width = 250,
        height = 5,
        visual = ImageVisual("menuScenes/HeadingLine.png")
    )

    val newGameButton = Button(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 430,
        width = 200,
        height = 100,
        visual = ImageVisual("menuScenes/Button_MainMenu_New.png")
    ).apply {
        onMouseClicked = {
            app.lobbyScene.previousScene = app.mainMenuScene
            app.lobbyScene.setNetworkMode(false)
            app.showMenuScene(app.lobbyScene)
            app.playSound(app.clickSfx)
        }
    }

    val loadGameButton = Button(
        posX = paneWidth * 0.3 - 200 / 2,
        posY = 550,
        width = 200,
        height = 100,
        visual = ImageVisual("menuScenes/Button_MainMenu_Load.png")
    ).apply {
        onMouseClicked = {
            root.fileService.loadGame()
            app.playSound(app.clickSfx)
        }
    }

    val hostGameButton = Button(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 430,
        width = 200,
        height = 100,
        visual = ImageVisual("menuScenes/Button_MainMenu_Host.png")
    ).apply {
        onMouseClicked = {
            app.hostOnlineLobbyScene.generateNewCode()
            app.hostOnlineLobbyScene.loadCredentials()
            app.showMenuScene(app.hostOnlineLobbyScene)
            app.playSound(app.clickSfx)
        }
    }

    val joinGameButton = Button(
        posX = paneWidth * 0.7 - 200 / 2,
        posY = 550,
        width = 200,
        height = 100,
        visual = ImageVisual("menuScenes/Button_MainMenu_Join.png")
    ).apply {
        onMouseClicked = {
            app.joinOnlineLobbyScene.loadCredentials()
            app.showMenuScene(app.joinOnlineLobbyScene)
            app.playSound(app.clickSfx)
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
            localLabelHeadingLine,
            onlineLabel,
            onlineLabelHeadingLine,
            newGameButton,
            loadGameButton,
            hostGameButton,
            joinGameButton
        )

        updateButtons()
    }

    /**
     * Enables or disables the load game button depending on whether a saved game is present.
     */
    fun updateButtons() {
        if (root.fileService.saveFileExists()) {
            loadGameButton.isDisabled = false
            loadGameButton.visual = ImageVisual("menuScenes/Button_MainMenu_Load.png")
        } else {
            loadGameButton.isDisabled = true
            loadGameButton.visual = ImageVisual("menuScenes/Button_MainMenu_Load_Disabled.png")
        }
    }
}