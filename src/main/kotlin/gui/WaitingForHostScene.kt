package gui

import gui.components.ExclusiveButtonGroup
import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.*

/**
 * The scene after joining a game. Active while waiting for the host to start the game.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 */
class WaitingForHostScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080) {
    val paneWidth = 900

    val paneHeight = 380

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
            app.showMenuScene(app.joinOnlineLobbyScene)
            app.playSound(app.clickSfx)
        }
    }

    val waitingLabel = Label(
        posX = paneWidth / 2 - 600 / 2,
        posY = paneHeight / 2 - 100 / 2,
        width = 600,
        height = 100,
        visual = ColorVisual(Color(0x777777)),
        font = Constants.font_input,
        text = "Waiting for host to start the game..."
    )

    val difficultySelection = ExclusiveButtonGroup(
        posX = paneWidth / 2 - 220 / 2,
        posY = paneHeight / 2 - 35 / 2 + 100,
        width = 220,
        height = 35,
        buttonCount = 6,
        buttonSize = 35,
        spacing = 3,
        imagePaths = listOf(
            "lobbyScene/Button_BotDifficulty_Icon",
            "lobbyScene/Button_BotDifficulty_0",
            "lobbyScene/Button_BotDifficulty_1",
            "lobbyScene/Button_BotDifficulty_2",
            "lobbyScene/Button_BotDifficulty_3",
            "lobbyScene/Button_BotDifficulty_4"
        ),
        initialSelectedIndex = 1,
        app
    ).apply {
        buttons[0].isDisabled = true
        onSelectionChanged = {
            root.networkService.setBotDifficultyOfClient(it - 1)
        }
    }

    init {
        background = Visual.EMPTY

        addComponents(
            contentPane
        )

        contentPane.addAll(
            backButton,
            waitingLabel,
            difficultySelection
        )
    }
}