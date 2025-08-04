package view

import entity.PlayerType
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

class ProtoMenu(
    private val rootService: RootService
) : MenuScene(1920, 1080), Refreshable {

    private var gridSize = 4

    private val players: List<PlayerType> = listOf(
        PlayerType.LOCAL,
        PlayerType.LOCAL
    )

    private val headLine = Label(
        width = 1920,
        height = 100,
        posX = 0,
        posY = 50,
        text = "Collapsi",
        alignment = Alignment.CENTER,
        visual = Visual.EMPTY,
        font = Font(size = 60)
    )

    private val toggleFour = Button(
        width = 200,
        height = 100,
        posX = 560,
        posY = 250,
        text = "4x4",
        visual = ColorVisual.BLUE
    ).apply {
        onMouseClicked = {
            toggleFour()
        }
    }

    private val toggleFive = Button(
        width = 200,
        height = 100,
        posX = 860,
        posY = 250,
        text = "5x5",
        visual = ColorVisual.LIGHT_GRAY
    ).apply {
        onMouseClicked = {
            toggleFive()
        }
    }

    private val toggleSix = Button(
        width = 200,
        height = 100,
        posX = 1160,
        posY = 250,
        text = "6x6",
        visual = ColorVisual.LIGHT_GRAY
    ).apply {
        onMouseClicked = {
            toggleSix()
        }
    }

    private val startButton = Button(
        width = 200,
        height = 100,
        posX = 860,
        posY = 490,
        text = "Start Game"
    ).apply {
        onMouseClicked = {
            rootService.gameService.startNewGame(players, players.map { 1.0 }, gridSize)
        }
    }

    init {
        background = ColorVisual.DARK_GRAY

        addComponents(
            headLine,
            toggleFour,
            toggleFive,
            toggleSix,
            startButton
        )
    }

    private fun toggleFour() {
        gridSize = 4
        toggleFour.visual = ColorVisual.BLUE
        toggleFive.visual = ColorVisual.LIGHT_GRAY
        toggleSix.visual = ColorVisual.LIGHT_GRAY
    }

    private fun toggleFive() {
        gridSize = 5
        toggleFive.visual = ColorVisual.BLUE
        toggleFour.visual = ColorVisual.LIGHT_GRAY
        toggleSix.visual = ColorVisual.LIGHT_GRAY
    }

    private fun toggleSix() {
        gridSize = 6
        toggleSix.visual = ColorVisual.BLUE
        toggleFour.visual = ColorVisual.LIGHT_GRAY
        toggleFive.visual = ColorVisual.LIGHT_GRAY
    }
}