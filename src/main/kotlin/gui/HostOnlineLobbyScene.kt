package gui

import gui.types.LobbyMode
import service.*
import service.network.types.ConnectionState
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.*

/**
 * The scene for inputting the code, secret, and server for starting a new online game.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 * @param root The main [RootService] containing all other services.
 */
class HostOnlineLobbyScene(
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
        text = "Host"
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
        onTextChanged = {
            text = text.uppercase()
            app.lobbyScene.lobbyCode.text = "Lobby Code: $text"
        }
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

    val hostLobbyButton = Button(
        posX = paneWidth - 80 - 20,
        posY = paneHeight - 80 - 20,
        width = 80,
        height = 80,
        visual = ImageVisual("lobbyScene/Button_Confirm.png")
    ).apply {
        onMouseClicked = {
            saveCredentials()

            app.playSound(app.clickSfx)

            root.networkService.hostGame(serverInput.text, secretInput.text, lobbyCodeInput.text)
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
            hostLobbyButton
        )
    }

    /**
     * Generates a new 4-character lobby code while avoiding ambiguous characters
     * and sets it to the lobbyCodeInput field in uppercase.
     */
    fun generateNewCode() {
        val chars = (('0'..'9') + ('a'..'z')).filter { it != 'i' && it != 'l' && it != '0' && it != 'o' }
        val code = (1..4).map { chars.random() }.joinToString("")
        lobbyCodeInput.text = code.uppercase()
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
        if (newState == ConnectionState.WAITING_FOR_GUESTS) {
            app.lobbyScene.setNetworkMode(LobbyMode.HOST, 1, 0)
            app.showMenuScene(app.lobbyScene)
        }
    }
}