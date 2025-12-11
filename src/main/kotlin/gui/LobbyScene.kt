package gui

import entity.*
import service.*
import gui.types.*
import gui.components.*
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.*
import tools.aqua.bgw.visual.*
import service.network.ConnectionState
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.dialog.DialogType
import kotlin.math.max

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
            playerId = index,
            app = app
        ).apply {
            addButton.onMouseClicked = {
                addPlayer()
                app.playSound(app.clickSfx)
            }
            removeButton.onMouseClicked = {
                removePlayer()
                app.playSound(app.clickSfx)
            }
            typeSelection.onSelectionChanged = {
                changeTypeOfPlayer(index, it)
            }
            difficultySelection.onSelectionChanged = {
                changeDifficultyOfPlayer(index, it)
            }
        }
    }

    private val boardSizeSelection = ExclusiveButtonGroup(
        posX = 40,
        posY = 570,
        width = 490,
        height = 100,
        buttonSize = 150,
        spacing = 5,
        imagePaths = listOf(
            "lobbyScene/Button_BoardSize_4x4",
            "lobbyScene/Button_BoardSize_5x5",
            "lobbyScene/Button_BoardSize_6x6"
        ),
        initialSelectedIndex = 0,
        app
    ).apply {
        onSelectionChanged = { selectedIndex -> boardSize = selectedIndex + 4 }
    }

    private val backButton = Button(
        posX = 20,
        posY = 20,
        width = 80,
        height = 56,
        visual = ImageVisual("lobbyScene/Button_Back.png")
    ).apply {
        onMouseClicked = {
            app.playSound(app.clickSfx)

            if (root.networkService.connectionState != ConnectionState.DISCONNECTED) {
                // Scene will change in refresh in Application.
                root.networkService.disconnect()
            } else {
                app.hostOnlineLobbyScene.generateNewCode()
                app.showMenuScene(app.mainMenuScene)
            }
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
            tryStartGame()

            app.playSound(app.clickSfx)
        }
    }

    val lobbyCode = Label(
        posX = 550,
        posY = 570,
        width = 400,
        height = 150,
        text = "Lobby Code: ERROR",
        alignment = Alignment.CENTER,
        font = Constants.font_lobbyCode
    ).apply {
        isVisible = false
    }

    val waitingLabel = Label(
        posX = paneWidth / 2 - 700 / 2,
        posY = paneHeight - 200,
        width = 700,
        height = 100,
        visual = ColorVisual(Color(0x555555)),
        font = Constants.font_input,
        text = "Waiting for the host to start the game..."
    ).apply {
        isVisible = false
    }

    /**
     * Currently selected player types.
     */
    val playerTypes = mutableListOf<PlayerType>()

    val botDifficulties = mutableListOf<Int>()

    val playerCount get() = playerTypes.size

    /** Whether this is a hosted game with online players or a fully local game. */
    private var lobbyMode = LobbyMode.LOCAL

    private val networkMode
        get() = lobbyMode != LobbyMode.LOCAL

    /** If [lobbyMode] is [LobbyMode.HOST] or [LobbyMode.GUEST], this is the index of the local player for this app. */
    private var localPlayerIndex: Int? = null

    /**
     * Currently selected size of the board.
     *
     * @see GameState.boardSize
     */
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
            startButton,
            lobbyCode,
            waitingLabel
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

        val playerType = if (!networkMode || localPlayerIndex == index) {
            PlayerType.LOCAL
        } else {
            PlayerType.REMOTE
        }

        playerTypes.add(playerType)
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
        playerTypes.removeLast()
        botDifficulties.removeLast()

        // Auto select smaller board size for convenience.
        if (boardSize == playerCount + 3) {
            boardSizeSelection.selectButton(max(playerCount - 2, 0))
        }

        updatePlayerSetupViews()
    }

    /**
     * Updates the [PlayerSetupView]s to account for player count and network mode.
     */
    private fun updatePlayerSetupViews() {
        check(playerCount <= 4) { "Passed 4 player limit." }
        check(playerTypes.size == botDifficulties.size) { "playerTypes and botDifficulties needs to match in size." }

        for (i in 0..<4) {
            playerSetupViews[i].remotePlayer = playerTypes.getOrElse(i) { PlayerType.LOCAL } == PlayerType.REMOTE
            playerSetupViews[i].setIsIncluded(i < playerCount)
        }

        playerSetupViews[2].addButton.isVisible = !networkMode && playerCount == 2
        playerSetupViews[3].addButton.isVisible = !networkMode && playerCount == 3

        playerSetupViews[2].removeButton.isVisible = !networkMode && playerCount == 3
        playerSetupViews[3].removeButton.isVisible = !networkMode && playerCount == 4

        // Disable buttons for illegal board sizes and enable the rest.
        for (i in 0..<3) {
            boardSizeSelection.setButtonEnabled(i, i >= playerCount - 2)
        }

        // Auto select bigger board size if necessary.
        if (boardSize < playerCount + 2) {
            boardSizeSelection.selectButton(max(playerCount - 2, 0))
        }
    }

    /**
     * Enables or disabled network mode.
     *
     * Sets player count to 1 in network mode or to 2 in local mode.
     *
     * @param networkMode Whether this is a hosted online game or a fully local game.
     *
     * @see networkMode
     */
    fun setNetworkMode(lobbyMode: LobbyMode, desiredPlayerCount: Int, localPlayerIndex: Int?) {
        // Don't reset players if we go from local to local.
        if (this.lobbyMode == LobbyMode.LOCAL && lobbyMode == LobbyMode.LOCAL)
            return

        require((localPlayerIndex == null) == (lobbyMode == LobbyMode.LOCAL))
        { "localPlayerIndex should be null only for local games." }

        this.lobbyMode = lobbyMode
        this.localPlayerIndex = localPlayerIndex

        // Remove additional players.
        repeat(playerCount) {
            removePlayer()
        }

        // Add minimum players.
        repeat(desiredPlayerCount) {
            addPlayer()
        }

        startButton.isVisible = lobbyMode != LobbyMode.GUEST
        boardSizeSelection.isVisible = lobbyMode != LobbyMode.GUEST
        lobbyCode.isVisible = lobbyMode == LobbyMode.HOST
        waitingLabel.isVisible = lobbyMode == LobbyMode.GUEST
    }

    private fun changeTypeOfPlayer(playerIndex: Int, typeIndex: Int) {
        if (playerTypes.size <= playerIndex || (networkMode && playerIndex != localPlayerIndex))
            return

        playerTypes[playerIndex] = PlayerType.entries[typeIndex]

        // Only show difficulty selection if the player type is bot.
        playerSetupViews[playerIndex].difficultySelection.isVisible = typeIndex == 1

        if (networkMode && playerIndex == localPlayerIndex) {
            val difficulty = if (playerTypes[playerIndex] == PlayerType.LOCAL) 0 else botDifficulties[playerIndex]
            root.networkService.setBotDifficultyOfClient(difficulty)
        }
    }

    private fun changeDifficultyOfPlayer(playerIndex: Int, difficultyIndex: Int) {
        botDifficulties[playerIndex] = difficultyIndex

        if (networkMode && playerIndex == localPlayerIndex && playerTypes[playerIndex] == PlayerType.BOT) {
            root.networkService.setBotDifficultyOfClient(difficultyIndex)
        }
    }

    private fun tryStartGame() {
        if (playerCount < 2) {
            app.showDialog("Too few players", "Collapsi requires at least 2 players.", DialogType.ERROR)
            return
        }

        when (lobbyMode) {
            LobbyMode.LOCAL -> root.gameService.startNewGame(
                playerTypes,
                botDifficulties,
                boardSize
            )

            LobbyMode.HOST -> root.networkService.startNewHostedGame(
                playerTypes,
                botDifficulties,
                boardSize
            )

            LobbyMode.GUEST -> throw IllegalStateException("Cannot start a game in a joined lobby.")
        }
    }

    override fun refreshAfterPlayerJoined() {
        addPlayer()
    }
}