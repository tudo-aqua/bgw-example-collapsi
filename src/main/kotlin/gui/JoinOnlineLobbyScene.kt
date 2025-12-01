package gui

import gui.types.LobbyMode
import service.FileService
import service.Refreshable
import service.RootService
import service.network.types.ConnectionState
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.DialogType
import tools.aqua.bgw.visual.*

/**
 * The scene for inputting the code, secret, and server for joining an online game.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 * @param root The main [RootService] containing all other services.
 */
class JoinOnlineLobbyScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080), Refreshable {
    val paneWidth = 600

    val paneHeight = 760

    val inputWidth = 300

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
        visual = ImageVisual("lobbyScene/Button_Back.png")
    ).apply {
        onMouseClicked = {
            app.showMenuScene(app.mainMenuScene)
            app.playSound(app.clickSfx)
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

    private val headingLine = Label(
        posX = paneWidth / 2 - 250 / 2,
        posY = 140,
        width = 250,
        height = 5,
        visual = ImageVisual("menuScenes/HeadingLine.png")
    )

    private val lobbyCodeHeading = Label(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 140,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Lobby Code:",
        alignment = Alignment.CENTER_LEFT
    )

    val lobbyCodeInput = TextField(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 210,
        width = inputWidth,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "..."
    ).apply {
        onTextChanged = { text = text.uppercase() }
    }

    private val serverHeading = Label(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 320,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Server URL:",
        alignment = Alignment.CENTER_LEFT
    )

    val serverInput = TextField(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 390,
        width = inputWidth,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        prompt = "..."
    )

    private val secretHeading = Label(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 500,
        width = 220,
        height = 100,
        font = Constants.font_inputLabel,
        text = "Secret:",
        alignment = Alignment.CENTER_LEFT
    )

    val secretInput = TextField(
        posX = paneWidth / 2 - inputWidth / 2,
        posY = 570,
        width = inputWidth,
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
        visual = ImageVisual("lobbyScene/Button_Confirm.png")
    ).apply {
        onMouseClicked = {
            if (lobbyCodeInput.text.trim().isEmpty()) {
                app.showDialog("Invalid Lobby Code", "Please enter a lobby code.", DialogType.ERROR)
            } else {
                saveCredentials()

                app.playSound(app.clickSfx)

                root.networkService.joinGame(serverInput.text, secretInput.text, lobbyCodeInput.text)
            }
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
            headingLine,
            lobbyCodeHeading,
            lobbyCodeInput,
            serverHeading,
            serverInput,
            secretHeading,
            secretInput,
            joinLobbyButton
        )
    }

    /**
     * Saves the server URL and secret from the input fields using the [FileService].
     */
    fun saveCredentials() {
        root.fileService.saveCredentials(serverInput.text, secretInput.text)
    }

    /**
     * Loads the secret and server URL from the [FileService] and sets them to the input fields to save the user
     * from having to input them multiple times.
     */
    fun loadCredentials() {
        secretInput.text = root.fileService.loadSecret()
        serverInput.text = root.fileService.loadServer()
    }

    override fun refreshAfterConnectionStateChange(newState: ConnectionState) {
        if (newState == ConnectionState.WAITING_FOR_INIT) {
            val client = checkNotNull(root.networkService.currentClient) { "No client found." }
            val color = checkNotNull(client.color) { "Client didn't have color assigned." }
            val playerId = color.ordinal

            app.lobbyScene.setNetworkMode(LobbyMode.GUEST, playerId + 1, playerId)
            app.showMenuScene(app.lobbyScene)
        }
    }
}