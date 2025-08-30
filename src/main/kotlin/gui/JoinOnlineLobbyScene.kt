package gui

import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.*

class JoinOnlineLobbyScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080) {
    val paneWidth = 600

    val paneHeight = 760

    private val contentPane = Pane<StaticComponentView<*>>(
        posX = 1920 / 2 - paneWidth / 2,
        posY = 1080 / 2 - paneHeight / 2,
        width = paneWidth,
        height = paneHeight,
        visual = ColorVisual(Constants.color_background)
    )

    val backButton = Button(
        posX = 20,
        posY = 20,
        width = 80,
        height = 56,
        visual = ImageVisual("LobbyScene/Exports/Button_Back.png")
    ).apply {
        onMouseClicked = {
            app.showMenuScene(app.mainMenuScene)
        }
    }

    private val heading = Label(
        posX = paneWidth / 2 - 200 / 2,
        posY = 50,
        width = 200,
        height = 100,
        font = Constants.font_heading,
        text = "Join"
    )

    private val lobbyCodeHeading = Label(
        posX = paneWidth / 2 - 200 / 2,
        posY = 140,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Lobby Code:",
        alignment = Alignment.CENTER_LEFT
    )

    val lobbyCodeInput = TextField(
        posX = paneWidth / 2 - 200 / 2,
        posY = 210,
        width = 200,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "..."
    )

    private val serverHeading = Label(
        posX = paneWidth / 2 - 200 / 2,
        posY = 320,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Server URL:",
        alignment = Alignment.CENTER_LEFT
    )

    val serverInput = TextField(
        posX = paneWidth / 2 - 200 / 2,
        posY = 390,
        width = 200,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "..."
    )

    private val secretHeading = Label(
        posX = paneWidth / 2 - 200 / 2,
        posY = 500,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Secret:",
        alignment = Alignment.CENTER_LEFT
    )

    val secretInput = TextField(
        posX = paneWidth / 2 - 200 / 2,
        posY = 570,
        width = 200,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "..."
    )

    val joinLobbyButton = Button(
        posX = paneWidth - 80 - 20,
        posY = paneHeight - 80 - 20,
        width = 80,
        height = 80,
        visual = ImageVisual("LobbyScene/Exports/Button_Confirm.png")
    ).apply {
        onMouseClicked = {
            saveCredentials()
            app.showMenuScene(app.waitingForHostScene)
        }
    }

    init {
        background = Visual.EMPTY

        addComponents(
            contentPane
        )

        contentPane.addAll(
            backButton,
            heading,
            lobbyCodeHeading,
            lobbyCodeInput,
            serverHeading,
            serverInput,
            secretHeading,
            secretInput,
            joinLobbyButton
        )
    }

    fun saveCredentials() {
        root.fileService.saveCredentials(serverInput.text, secretInput.text)
    }

    fun loadCredentials() {
        secretInput.text = root.fileService.loadSecret()
        serverInput.text = root.fileService.loadServer()
    }
}