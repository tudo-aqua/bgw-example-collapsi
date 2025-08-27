package gui

import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*

class MainMenuScene(
    private val app: CollapsiApplication
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

    private val logo = Label(
        posX = paneWidth / 2 - 350 / 2,
        posY = 100,
        width = 350,
        height = 150,
        visual = ColorVisual(Color(0xFFFFFF))
    )

    val localGameButton = Button(
        posX = paneWidth / 2 - 200 / 2,
        posY = 300,
        width = 200,
        height = 100,
        visual = CompoundVisual(
            ColorVisual(Color(0x555555)),
            TextVisual(
                text = "LOCAL",
                font = Constants.font_label
            )
        )
    ).apply {
        onMouseClicked = {
            app.lobbyScene.previousScene = app.mainMenuScene
            app.showMenuScene(app.lobbyScene)
        }
    }

    val hostGameButton = Button(
        posX = paneWidth / 2 - 200 / 2,
        posY = 450,
        width = 200,
        height = 100,
        visual = CompoundVisual(
            ColorVisual(Color(0x555555)),
            TextVisual(
                text = "HOST",
                font = Constants.font_label
            )
        )
    ).apply {
        onMouseClicked = {
            app.hostOnlineLobbyScene.generateNewCode()
            app.hostOnlineLobbyScene.loadSecret()
            app.showMenuScene(app.hostOnlineLobbyScene)
        }
    }

    val joinGameButton = Button(
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
            app.joinOnlineLobbyScene.loadSecret()
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
            localGameButton,
            hostGameButton,
            joinGameButton
        )
    }
}