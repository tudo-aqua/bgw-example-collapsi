package view

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

class EndGameMenuScene(
    private val rootService: RootService
) : MenuScene(1920, 1080), Refreshable {

    private val endLabel = Label(
        width = 500,
        height = 200,
        posX = 1920 / 2 - 500 / 2,
        posY = 1080 / 2 - 200 / 2,
        text = "Game Over",
        visual = ColorVisual.LIGHT_GRAY
    )

    init {
        background = ColorVisual.GRAY

        addComponents(endLabel)
    }
}