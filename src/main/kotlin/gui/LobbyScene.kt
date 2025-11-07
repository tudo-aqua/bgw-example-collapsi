package gui

import entity.*
import service.*
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*
import gui.components.ExclusiveButtonGroup
import gui.components.PlayerSetupView

/**
 * Scene for setting up the players and the game rules.
 *
 * Also works for online games.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 * @param root The main [RootService] containing all other services.
 */
class LobbyScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : MenuScene(1920, 1080), Refreshable {

    private val paneWidth = 1100

    private val paneHeight = 760

    private var networkMode = false

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
                    playerTypes[index] = PlayerType.entries[it]

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
            "lobbyScene/Button_BoardSize_4x4",
            "lobbyScene/Button_BoardSize_5x5",
            "lobbyScene/Button_BoardSize_6x6"
        ),
        initialSelectedIndex = 0
    ).apply {
        onSelectionChanged = { selectedIndex -> boardSize = selectedIndex + 4 }
    }

    var previousScene: MenuScene = app.mainMenuScene

    val backButton = Button(
        posX = 20,
        posY = 20,
        width = 80,
        height = 56,
        visual = ImageVisual("lobbyScene/Button_Back.png")
    ).apply {
        onMouseClicked = {
            app.hostOnlineLobbyScene.generateNewCode()
            app.showMenuScene(previousScene)
        }
    }

    private val startButton = Button(
        posX = paneWidth - 80 - 20,
        posY = paneHeight - 80 - 20,
        width = 80,
        height = 80,
        visual = ImageVisual("lobbyScene/Button_Confirm.png")
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

    val playerCount get() = playerTypes.size

    var boardSize = 4

    init {
        background = Visual.EMPTY

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

    /**
     * Adds a player in the next free slot.
     *
     * @throws IllegalStateException if there were already 4 players present.
     */
    fun addPlayer() {
        check(playerCount < 4) { "Tried to add a fifth player." }

        val index = playerTypes.size

        playerTypes.add(PlayerType.LOCAL)
        botDifficulties.add(3)

        playerSetupViews[index].difficultySelection.selectButton(3)

        updatePlayerSetupViews()
    }

    /**
     * Removes the right-most player.
     *
     * @throws IllegalStateException if there was only 1 player (1 player is the minimum).
     */
    fun removePlayer() {
        check(playerCount > 1) { "Tried to remove the first player." }

        playerTypes.removeLast()
        botDifficulties.removeLast()

        updatePlayerSetupViews()
    }

    private fun updatePlayerSetupViews() {
        check(playerCount in 1..4) { "Requires 1 to 4 players." }
        check(playerTypes.size == botDifficulties.size) { "playerTypes and botDifficulties needs to match in size." }

        for (i in 0..<4) {
            playerSetupViews[i].remotePlayer = i >= 1 && networkMode
            playerSetupViews[i].setIsIncluded(i < playerCount)
        }

        playerSetupViews[2].addButton.isVisible = !networkMode && playerCount == 2
        playerSetupViews[3].addButton.isVisible = !networkMode && playerCount == 3

        playerSetupViews[2].removeButton.isVisible = !networkMode && playerCount == 3
        playerSetupViews[3].removeButton.isVisible = !networkMode && playerCount == 4
    }

    fun setNetworkMode(networkMode: Boolean) {
        this.networkMode = networkMode

        if (!networkMode) {
            // Always require at least 2 players in local mode.
            if (playerCount == 1) {
                addPlayer()
            }
        } else {
            // Remove all but one player.
            repeat(playerCount - 1) {
                removePlayer()
            }

            playerTypes[0] = PlayerType.REMOTE
        }

        updatePlayerSetupViews()
    }
}