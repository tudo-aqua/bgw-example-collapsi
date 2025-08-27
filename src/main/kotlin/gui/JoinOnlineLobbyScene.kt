package gui

import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.*

class JoinOnlineLobbyScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080) {
    val paneWidth = 1100

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

    val lobbyCodeInput = TextField(
        posX = paneWidth / 2 - 200 / 2,
        posY = 300,
        width = 200,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "code"
    )

    val secretInput = TextField(
        posX = paneWidth / 2 - 200 / 2,
        posY = 450,
        width = 200,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "secret"
    )

    val joinLobbyButton = Button(
        posX = paneWidth / 2 - 200 / 2,
        posY = 600,
        width = 200,
        height = 100,
        visual = CompoundVisual(
            ColorVisual(Color(0x555555)),
            TextVisual(
                text = "JOIN",
                font = Constants.font_label
            )
        )
    ).apply {
        onMouseClicked = {
            saveSecret()
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
            lobbyCodeInput,
            secretInput,
            joinLobbyButton
        )
    }

    fun saveSecret() {
        root.fileService.saveSecret(secretInput.text)
    }

    fun loadSecret() {
        val secret = root.fileService.loadSecret()
        if (secret != null) {
            secretInput.text = secret
        }
    }
}