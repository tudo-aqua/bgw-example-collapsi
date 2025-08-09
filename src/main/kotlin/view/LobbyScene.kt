package view

import entity.PlayerType
import service.*
import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*
import view.components.ExclusiveButtonGroup
import view.components.PlayerSetupView

class LobbyScene(
    private val root: RootService
) : MenuScene(1920, 1080), Refreshable {

    val paneWidth = 1100

    val paneHeight = 760

    private val contentPane = Pane<StaticComponentView<*>>(
        posX = 1920 / 2 - paneWidth / 2,
        posY = 1080 / 2 - paneHeight / 2,
        width = paneWidth,
        height = paneHeight,
        visual = ColorVisual(Constants.color_background)
    )

    private val playerSetupViews = List(4) { index ->
        val spacing = 40
        val width = 220
        val height = 450
        val distance = width + spacing
        PlayerSetupView(
            posX = -width / 2 + paneWidth / 2 - distance * 3 / 2 + distance * index,
            posY = 100,
            width = width,
            height = height,
            playerId = index
        ).apply {
            addButton.onMouseClicked = { addPlayer() }
            removeButton.onMouseClicked = { removePlayer() }
            typeSelection.onSelectionChanged = {
                if (playerTypes.size > index)
                    playerTypes[index] = PlayerType.values()[it]

                // Only show difficulty selection if the player type is bot.
                difficultySelection.isVisible = it == 1
            }
            difficultySelection.onSelectionChanged = { botDifficulties[index] = it }
        }
    }

    private val boardSizeSelection = ExclusiveButtonGroup(
        posX = 40,
        posY = 570,
        width = 490,
        height = 100,
        buttonCount = 3,
        buttonSize = 150,
        spacing = 5,
        imagePaths = listOf(
            "LobbyScene/Exports/Button_BoardSize_4x4",
            "LobbyScene/Exports/Button_BoardSize_5x5",
            "LobbyScene/Exports/Button_BoardSize_6x6"
        ),
        selectedIndex = 0
    ).apply {
        onSelectionChanged = { boardSize = it + 4 }
    }

    private val backButton = Button(
        posX = 20,
        posY = 20,
        width = 80,
        height = 56,
        visual = ImageVisual("LobbyScene/Exports/Button_Back.png")
    )

    private val startButton = Button(
        posX = 1100 - 80 - 20,
        posY = 760 - 80 - 20,
        width = 80,
        height = 80,
        visual = ImageVisual("LobbyScene/Exports/Button_Confirm.png")
    ).apply {
        onMouseClicked = {
            root.gameService.startNewGame(
                playerTypes,
                botDifficulties,
                boardSize
            )
        }
    }

    val playerTypes = mutableListOf<PlayerType>()

    val botDifficulties = mutableListOf<Int>()

    var boardSize = 4

    init {
        opacity = 0.0

        addComponents(
            contentPane
        )

        contentPane.addAll(playerSetupViews)
        contentPane.addAll(
            boardSizeSelection,
            backButton,
            startButton
        )

        addPlayer()
        addPlayer()

        playerSetupViews[2].addButton.isVisible = true
    }

    fun addPlayer() {
        check(playerTypes.size < 4) { "Tried to add a fifth player." }

        val index = playerTypes.size

        playerTypes.add(PlayerType.LOCAL)
        botDifficulties.add(3)

        playerSetupViews[index].setIsIncluded(true)

        // Always disable remove buttons for Player 1 and 2.
        if (index <= 1)
            playerSetupViews[index].removeButton.isVisible = false

        // Only allow removal of the latest player.
        if (index >= 1)
            playerSetupViews[index - 1].removeButton.isVisible = false

        // Only allow adding Player 4 if Player 3 was already added.
        if (index == 2)
            playerSetupViews[3].addButton.isVisible = true
    }

    fun removePlayer() {
        check(playerTypes.size > 2) { "Tried to remove the second player." }

        val index = playerTypes.lastIndex

        playerTypes.removeLast()
        botDifficulties.removeLast()

        playerSetupViews[index].setIsIncluded(false)

        // Allow removal of Player 3 if Player 4 was removed.
        if (index >= 3)
            playerSetupViews[index - 1].removeButton.isVisible = true

        // Only allow adding Player 4 if Player 3 was already added.
        if (index == 2)
            playerSetupViews[3].addButton.isVisible = false
    }
}