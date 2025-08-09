package view

import entity.*
import service.*
import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*
import view.components.PlayerSetupView
import java.awt.Color

class LobbyScene(
    private val rootService: RootService
) : MenuScene(1920, 1080), Refreshable {

    val paneWidth = 1100

    val paneHeight = 740

    private val contentPane = Pane<StaticComponentView<*>>(
        posX = 1920 / 2 - paneWidth / 2,
        posY = 1080 / 2 - paneHeight / 2,
        width = paneWidth,
        height = paneHeight,
        visual = ColorVisual(Constants.color_background)
    )

    private val playerSetupViews = List(4) {
        val spacing = 20
        val width = 220
        val height = 400
        val distance = width + spacing
        PlayerSetupView(-width / 2 + paneWidth / 2 - distance * 3 / 2 + distance * it, 100, width, height, it)
    }

    private var boardSize = 4

    private val players: List<PlayerType> = listOf(
        PlayerType.LOCAL,
        PlayerType.LOCAL
    )

    init {
        opacity = 0.0

        addComponents(
            contentPane
        )

        contentPane.addAll(playerSetupViews)
    }
}