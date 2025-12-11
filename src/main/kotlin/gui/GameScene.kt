package gui

import entity.*
import service.*
import service.network.types.ConnectionState
import tools.aqua.bgw.animation.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.HorizontalAlignment
import tools.aqua.bgw.visual.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.concurrent.thread
import kotlin.math.*

/**
 * The main [BoardGameScene] where the Collapsi game is played.
 *
 * @param app The main [CollapsiApplication] containing all other scenes.
 * @param root The main [RootService] containing all other services.
 */
class GameScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : BoardGameScene(1920, 1080), Refreshable {
    //region Components
    //region Board

    /**
     * The GridPane containing all tiles on the board.
     *
     * This GridPane is emptied and refilled between games.
     */
    private var boardGrid = GridPane<CardView>(
        posX = 1052,
        posY = 500,
        rows = 0,
        columns = 0,
        spacing = 20,
        layoutFromCenter = true
    )

    /**
     * A map with all tile positions mapped to their CardViews.
     */
    private val tileViews = BidirectionalMap<Coordinate, CardView>()

    /**
     * The main pawns ([TokenView]s) for each player color.
     *
     * This map is not reinitialized between games.
     */
    private val playerMainPawns: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            ).apply {
                isDisabled = true // To prevent pawns from blocking clicks to the tiles below.
            }
        )
    }

    /**
     * The duplicate pawns ([TokenView]s) for each player color used for wrap-around animations.
     * These are hidden off-screen when not in use.
     *
     * This map is not reinitialized between games.
     */
    private val playerDuplicatePawns: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                posX = -200, // Hide off-screen.
                posY = -200,
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            )
        )
    }

    /**
     * A copy of the background with a hole where the board is. It is rendered on top of the board and pawns.
     * This creates the effect of pawns fading out when doing the wrap-around animation.
     */
    private val maskedBackground = Label(
        posX = 0,
        posY = 0,
        width = this.width,
        height = this.height,
        visual = ImageVisual("gameScene/MaskedBackground.png")
    ).apply {
        isDisabled = true // Don't block mouse button presses.
    }

    //endregion

    //region Left Info Pane

    /**
     * The info pane on the left side of the screen.
     * It displays the current player, player order, and step counter.
     */
    private val infoPane = Pane<ComponentView>(
        width = 540,
        height = 1080,
        visual = ImageVisual("gameScene/GameHudBackground.png"),
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    /**
     * The LinearLayout for the tokens on the left side that show the player order.
     */
    private val playerOrderLayout = LinearLayout<TokenView>(
        width = 460,
        height = 128,
        posX = 40,
        posY = 144,
        alignment = Alignment.CENTER
    )

    /**
     * The tokens on the left side of the screen that represent each player.
     * This includes inactive (hidden) player tokens.
     *
     * This map is not reinitialized between games.
     */
    private val playerOrderTokens: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            )
        )
    }

    /**
     * The arrow that displays the current player on the left side of the screen.
     */
    private val activePlayerArrow = Label(
        width = 84,
        height = 116,
        posY = 170,
        visual = ImageVisual("gameScene/CurrentPlayerArrow.png")
    )

    /**
     * The LinearLayout for the step tokens on the left side that show the remaining moves.
     *
     * Step tokens are added at the start of the turn and are not removed when used, but instead moved to the tile.
     * This is to prevent the other tokens from shifting position during a turn.
     */
    private val stepTokenLayout = LinearLayout<TokenView>(
        width = 460,
        height = 128,
        posX = 40,
        posY = 308,
        alignment = Alignment.CENTER,
        spacing = 40,
    )

    /**
     * The step tokens on the left side of the screen that represent the remaining moves.
     * This includes inactive step tokens.
     *
     * This map is not reinitialized between games.
     */
    private val stepTokenViews = List(4) {
        TokenView(
            width = 64,
            height = 64,
            visual = ImageVisual("gameScene/StepToken.png")
        ).apply {
            isVisible = false
        }
    }

    //endregion

    //region Player Ranking

    /**
     * The ranking panes on the right side of the screen that represent the ranks of dead/victorious players.
     * This map includes a pane for all four possible ranks, even if there are fewer players.
     *
     * This map is not reinitialized between games.
     */
    private val playerRankPanes = List(4) { index ->
        Pane<TokenView>(
            width = 200,
            height = 200,
            posX = 1920,
            posY = 100 + index * 170,
            visual = ImageVisual("GameScene/PlayerRanking_${index + 1}.png")
        )
    }

    /**
     * The player tokens on the right side of the screen that represent the dead/victorious players.
     * This map includes a token for all four possible players, even those that are not in-game.
     * Tokens are added to the panes in [showPlayerRank] when their rank is decided.
     *
     * This map is not reinitialized between games.
     */
    private val playerRankTokens: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            )
        )
    }

    //endregion

    //region Menu Button

    /**
     * The pane for the button below the player ranking to return to the main menu.
     */
    private val backToMenuButtonPane = Pane<Button>(
        width = 300,
        height = 300,
        posX = 1920,
        posY = 780,
        visual = ImageVisual("gameScene/BackToMenuButtonBackground.png")
    )

    /**
     * The button below the player ranking to return to the main menu.
     */
    private val backToMenuButton = Button(
        width = 160,
        height = 90,
        posX = 65,
        posY = 65,
        visual = ImageVisual("gameScene/BackToMenuButton.png")
    ).apply {
        onMouseClicked = {
            app.playSound(app.clickSfx)

            if (root.networkService.connectionState != ConnectionState.DISCONNECTED) {
                // Scene will change in refresh in Application.
                root.networkService.disconnect()
            } else {
                app.mainMenuScene.updateButtons()
                app.showMenuScene(app.mainMenuScene)
            }
        }
    }

    //endregion

    //region Toolbar

    /**
     * The background for the toolbar at the bottom of the screen.
     */
    private val toolbarBackground = Label(
        width = 756,
        height = 135,
        posX = boardGrid.posX - 756 / 2,
        posY = 1080 - 135,
        visual = ImageVisual("gameScene/ToolbarBg.png")
    )

    /**
     * The layout for the toolbar at the bottom of the screen that contains all toolbar buttons (speed, undo, redo).
     */
    private val toolbarLayout = LinearLayout<TokenView>(
        width = 756,
        height = 50,
        posX = boardGrid.posX,
        posY = 990,
        spacing = 40,
        horizontalAlignment = HorizontalAlignment.CENTER
    )

    /**
     * The undo button in the toolbar at the bottom.
     */
    private val saveButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("gameScene/Button_Save.png")
    ).apply {
        onMouseClicked = {
            val game = root.currentGame

            if (game != null) {
                root.fileService.saveGame()

                app.playSound(app.clickSfx)
            }
        }
    }

    /**
     * The undo button in the toolbar at the bottom.
     */
    private val undoButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("gameScene/Button_Undo.png")
    ).apply {
        onMouseClicked = {
            val game = root.currentGame

            if (!blockMovementInput && game != null && game.undoStack.isNotEmpty()) {
                root.playerActionService.undo()
                app.playSound(app.clickSfx)
            }
        }
    }

    /**
     * The redo button in the toolbar at the bottom.
     */
    private val redoButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("gameScene/Button_Redo.png")
    ).apply {
        onMouseClicked = {
            val game = root.currentGame

            if (!blockMovementInput && game != null && game.redoStack.isNotEmpty()) {
                root.playerActionService.redo()
                app.playSound(app.clickSfx)
            }
        }
    }

    /**
     * The pause button in the toolbar at the bottom.
     */
    private val pauseButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("gameScene/Button_Pause.png")
    ).apply {
        onMouseClicked = {
            if (root.currentGame != null) {
                setSimulationSpeed(0)
                app.playSound(app.clickSfx)
            }
        }
    }

    /**
     * The speed buttons in the toolbar at the bottom.
     * The exact speed values are set in [setSimulationSpeed].
     */
    private val speedButtons = List(3) { index ->
        TokenView(
            width = 25 * (index + 1),
            height = 50,
            visual = ImageVisual("GameScene/Button_Speed_${index + 1}.png")
        ).apply {
            onMouseClicked = {
                if (root.currentGame != null) {
                    setSimulationSpeed(index + 1)
                    app.playSound(app.clickSfx)
                }
            }
        }
    }

    /**
     * The arrow that indicates the current simulation speed in the toolbar at the bottom.
     */
    private val activeSpeedArrow = Label(
        width = 50,
        height = 50,
        posX = toolbarLayout.posX,
        posY = 1030,
        visual = ImageVisual("gameScene/ToolbarArrow.png")
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    //endregion
    //endregion

    //region Animation Variables

    /**
     * The speed multiplier for the bot and animations.
     *
     * 0 means the game is paused, 1 is normal speed, 2 is double speed, etc.
     *
     * Mind that a value below 1.0 will only affect the bot. Animation speed is always at least 1.0.
     */
    private var simulationSpeed = 1.0

    /**
     * This multiplier is applied to every delay and animation
     * to speed everything up depending on the simulation speed.
     *
     * This value will always be at most 1, to prevent animations from not playing when the game is paused.
     */
    private val delayMultiplier get() = 1 / max(simulationSpeed, 1.0)

    /**
     * This is how long the animation for each move lasts.
     */
    private val moveAnimationDuration get() = (500 * delayMultiplier).roundToInt()

    /**
     * This is how long the game waits before ending a player's turn.
     */
    private val endTurnDelay get() = (1000 * delayMultiplier).roundToInt()

    /**
     * True if the bot was supposed to make a move, but couldn't, due to the game being paused.
     * Once the simulation is unpaused, the bot will make its move.
     */
    private var performBotMoveOnUnpause = false

    /**
     * True if an animation is playing which should prevent a local player from making moves.
     */
    private var blockMovementInput = false

    //endregion

    //region Audio

    private val cardFlipSfx = listOf(
        "audio/gameScene/CardFlip1.ogg",
        "audio/gameScene/CardFlip2.ogg",
        "audio/gameScene/CardFlip3.ogg"
    )

    private val tokenPlaceSfx = listOf(
        "audio/gameScene/TokenPlace1.ogg",
        "audio/gameScene/TokenPlace2.ogg",
        "audio/gameScene/TokenPlace3.ogg"
    )

    private val playerChangeSfx = "audio/gameScene/PlayerChange.ogg"

    //endregion

    //region Functions

    init {
        background = ImageVisual("gameScene/Background.png")

        addComponents(boardGrid)

        addComponents(*playerMainPawns.values.toTypedArray())
        addComponents(*playerDuplicatePawns.values.toTypedArray())

        // Note how maskedBackground is added after the board and pawns to render it on top of them.
        addComponents(maskedBackground)

        addComponents(
            infoPane,
            toolbarBackground,
            toolbarLayout,
            activeSpeedArrow,
            backToMenuButtonPane
        )

        addComponents(*playerRankPanes.toTypedArray())

        infoPane.addAll(activePlayerArrow, playerOrderLayout, stepTokenLayout)
        backToMenuButtonPane.add(backToMenuButton)
    }

    //region Refreshes

    override fun refreshAfterStartNewGame() {
        loadScene()
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Block input during movement animation.
        blockMovementInput = true

        // Move player's main pawn.
        movePlayerPawn(from, to, currentState.currentPlayer.color)

        // Collapse tile (flip it).
        if (currentState.currentPlayer.visitedTiles.size == 1) {
            val collapsedTileView = tileViews.forward(from)
            collapsedTileView.apply {
                playAnimation(
                    FlipAnimation(
                        collapsedTileView,
                        collapsedTileView.frontVisual,
                        collapsedTileView.backVisual,
                        moveAnimationDuration
                    ).apply {
                        onFinished = {
                            collapsedTileView.showBack()
                        }
                    }
                )
            }

            app.playSound(cardFlipSfx)
        }

        // Move step token to tile.
        val stepToken = stepTokenViews[currentState.currentPlayer.remainingMoves]
        playAnimation(
            MovementAnimation(
                stepToken,
                stepToken.actualPosX,
                getPlayerPosX(from),
                stepToken.actualPosY,
                getPlayerPosY(from),
                moveAnimationDuration
            ).apply {
                onFinished = {
                    stepToken.offset(
                        getPlayerPosX(from) - stepToken.actualPosX,
                        getPlayerPosY(from) - stepToken.actualPosY
                    )

                    blockMovementInput = false
                }
            })

        app.playSound(tokenPlaceSfx)

        // Wait and then end the turn automatically if this is not an online player.
        if (currentState.currentPlayer.remainingMoves <= 0 && currentState.currentPlayer.type != PlayerType.REMOTE) {
            playAnimation(DelayAnimation(endTurnDelay).apply {
                onFinished = {
                    root.gameService.endTurn()
                }
            })
        }
    }

    override fun refreshAfterEndTurn() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentLabel = playerOrderTokens.getValue(currentState.currentPlayer.color)

        // Move the current player arrow.
        playAnimation(
            MovementAnimation(
                activePlayerArrow,
                activePlayerArrow.actualPosX,
                currentLabel.actualPosX - 10,
                activePlayerArrow.actualPosY,
                activePlayerArrow.actualPosY,
                (250 * delayMultiplier).roundToInt()
            ).apply {
                onFinished = {
                    activePlayerArrow.posX = currentLabel.actualPosX - 10
                }
            }
        )

        app.playSound(playerChangeSfx)

        // Hide all step tokens.
        stepTokenViews.forEach { it.isVisible = false }
        stepTokenLayout.clear()

        // Show step tokens according to the new current player's starting moves.
        for (i in 0 until currentState.currentPlayer.remainingMoves) {
            stepTokenViews[i].apply {
                isVisible = true
                posX = 0.0 // Reset position within layout.
                posY = 0.0
            }
            stepTokenLayout.add(stepTokenViews[i])
        }

        // Start bot move if the next player is a bot.
        if (currentState.currentPlayer.type == PlayerType.BOT
            && root.playerActionService.hasValidMove()
        ) {
            // Calculate the next turn without blocking the main thread.
            thread {
                root.botService.calculateTurn()

                performNextBotMove()
            }
        }
    }

    override fun refreshAfterPlayerDied(player: Player) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val alivePlayerCount = currentState.players.count { it.alive }

        showPlayerRank(player, alivePlayerCount + 1)

        // Wait and then end the turn automatically if this is not an online player.
        if (currentState.currentPlayer.type != PlayerType.REMOTE) {
            playAnimation(DelayAnimation(endTurnDelay).apply {
                onFinished = {
                    root.gameService.endTurn()
                }
            })
        }
    }

    override fun refreshAfterGameEnd(winner: Player) {
        // Show "Menu" Button.
        playAnimation(
            MovementAnimation(
                backToMenuButtonPane,
                backToMenuButtonPane.posX,
                1550,
                backToMenuButtonPane.posY,
                backToMenuButtonPane.posY,
                (500 * delayMultiplier).roundToInt()
            ).apply {
                onFinished = {
                    backToMenuButtonPane.posX = 1550.0
                }
            }
        )

        showPlayerRank(winner, 1)
    }

    override fun refreshAfterUndo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        if (game.currentState.players.any { it.type == PlayerType.BOT }) {
            setSimulationSpeed(0)
        }

        updateAllInstant()
    }

    override fun refreshAfterRedo() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }

        if (game.currentState.players.any { it.type == PlayerType.BOT }) {
            setSimulationSpeed(0)
        }

        updateAllInstant()
    }

    override fun refreshAfterLoad() {
        loadScene()

        updateAllInstant()
    }

    //endregion

    //region Initializers

    /**
     * Loads all components and visuals for the current game.
     * Called at the start of a new game or after loading a game.
     */
    private fun loadScene() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        initializeBoard()
        initializePlayers()
        initializeLeftInfoPane()
        initializeToolbar()
        initializePlayerRanking()

        if (currentState.currentPlayer.type == PlayerType.BOT) {
            // Calculate the first turn without blocking the main thread.
            thread {
                root.botService.calculateTurn()

                performNextBotMove()
            }
        }
    }

    /**
     * Initializes the board GridPane and all tile CardViews for the current game.
     */
    private fun initializeBoard() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Init GridPane.

        // Reset GridPane.
        removeComponents(boardGrid)
        boardGrid = GridPane(
            posX = 1052,
            posY = 500,
            rows = 0,
            columns = 0,
            spacing = 20,
            layoutFromCenter = true
        )
        addComponents(boardGrid)
        boardGrid.toBack()
        boardGrid.addRows(0, currentState.boardSize)
        boardGrid.addColumns(0, currentState.boardSize)

        boardGrid.spacing = when (currentState.boardSize) {
            4 -> 20.0
            5 -> 15.0
            6 -> 10.0
            else -> throw IllegalStateException("Board size out of range.")
        }

        // Init Tiles.

        tileViews.clear()

        val tileSize = when (currentState.boardSize) {
            4 -> 176.0
            5 -> 140.8
            6 -> 119.167
            else -> throw IllegalStateException("Board size out of range.")
        }

        for ((position: Coordinate, tile: Tile) in currentState.board) {
            val frontVisual: ImageVisual = if (tile.startTileColor == null) {
                ImageVisual("GameScene/Tile_${tile.movesToMake}.png")
            } else {
                ImageVisual("GameScene/Tile_P${tile.startTileColor.ordinal + 1}.png")
            }

            val cardView = CardView(
                width = tileSize,
                height = tileSize,
                front = frontVisual,
                back = ImageVisual("gameScene/Tile_Collapsed.png")
            ).apply {
                if (!tile.collapsed)
                    showFront()

                onMouseClicked = { tryMoveTo(tileViews.backward(this)) }
            }

            tileViews.add(tile.position, cardView)
            boardGrid[position.x, position.y] = cardView
        }
    }

    /**
     * Initializes the player pawns for the current game.
     */
    private fun initializePlayers() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Scale player tokens according to board size.
        val tokenScale = when (currentState.boardSize) {
            4 -> 1.0
            5 -> 0.8
            6 -> 0.677
            else -> throw IllegalStateException("Board size out of range.")
        }

        playerMainPawns.values.forEach { it.scale = tokenScale }
        playerDuplicatePawns.values.forEach { it.scale = tokenScale }

        // Show only player tokens that are in the game.
        playerMainPawns.values.forEach { it.isVisible = true }
        playerMainPawns.getValue(PlayerColor.YELLOW_CIRCLE).isVisible = currentState.players.size >= 3
        playerMainPawns.getValue(PlayerColor.RED_TRIANGLE).isVisible = currentState.players.size >= 4

        // Init player pawn positions.
        for (player in currentState.players) {
            playerMainPawns.getValue(player.color).posX = getPlayerPosX(player.position)
            playerMainPawns.getValue(player.color).posY = getPlayerPosY(player.position)
        }
    }

    /**
     * Initializes the info pane on the left side of the screen for the current game.
     */
    private fun initializeLeftInfoPane() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Only show player order tokens for players that are in the game.
        playerOrderLayout.clear()
        for (player in currentState.players) {
            val playerVisualToAdd = playerOrderTokens.getValue(player.color)
            playerOrderLayout.add(playerVisualToAdd)
        }

        val spacing = when (currentState.players.size) {
            2 -> 72.0
            3 -> 56.0
            4 -> 48.0
            else -> throw IllegalStateException("Player count out of range.")
        }
        playerOrderLayout.spacing = spacing

        // Initialize current player arrow position.
        val currentPlayerOrderToken = playerOrderTokens.getValue(currentState.currentPlayer.color)
        activePlayerArrow.posX = currentPlayerOrderToken.actualPosX - 10

        // Initialize step tokens.
        // First player starts with 1 step token.
        stepTokenViews.forEach { it.isVisible = false }
        stepTokenLayout.clear()

        stepTokenViews[0].isVisible = true
        stepTokenLayout.add(stepTokenViews[0])
    }

    /**
     * Initializes the toolbar at the bottom of the screen for the current game.
     */
    private fun initializeToolbar() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        toolbarLayout.clear()

        // Save/Undo/Redo is disabled in online games.
        if (!game.isOnlineGame) {
            toolbarLayout.add(saveButton)
            toolbarLayout.add(undoButton)
            toolbarLayout.add(redoButton)
        }

        // Pausing means stopping bots and is therefore unnecessary for games without bots.
        if (currentState.players.any { it.type == PlayerType.BOT }) {
            toolbarLayout.add(pauseButton)
        }

        toolbarLayout.addAll(speedButtons)

        toolbarLayout.posX = boardGrid.posX - toolbarLayout.width / 2

        // Pause if the first player is a bot.
        if (currentState.currentPlayer.type == PlayerType.BOT) {
            setSimulationSpeed(0)
        } else {
            setSimulationSpeed(1)
        }
    }

    /**
     * Initializes the player ranking panes on the right side of the screen for the current game.
     */
    private fun initializePlayerRanking() {
        playerRankPanes.forEach { it.posX = 1920.0 }
        playerRankPanes.forEach { it.clear() }

        playerRankTokens.values.forEach { it.isVisible = false }

        backToMenuButtonPane.posX = 1920.0
    }

    //endregion

    //region Instant Updates

    /**
     * Updates all tiles, players, and other visuals in the current game without any animations.
     * This is only used for undo/redo and load.
     */
    private fun updateAllInstant() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState
        val currentPlayer = currentState.currentPlayer

        updateBoardInstant()
        updateStepTokensInstant()
        updatePlayerRankingInstant()

        activePlayerArrow.posX = playerOrderTokens.getValue(currentPlayer.color).actualPosX - 10
    }

    /**
     * Updates all tiles in the current game without any animations.
     *
     * @see updateAllInstant
     */
    private fun updateBoardInstant() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        for (player in currentState.players) {
            val pawn = playerMainPawns.getValue(player.color)

            pawn.apply {
                posX = getPlayerPosX(player.position)
                posY = getPlayerPosY(player.position)
                isVisible = player.alive
            }
        }

        for ((position, tile) in currentState.board) {
            val view = tileViews.forward(position)

            // Simply "view.showBack()" would be better, but a bug in BGW is preventing this.
            if (tile.collapsed && view.visual != view.backVisual)
                playAnimation(
                    FlipAnimation(
                        view,
                        view.frontVisual,
                        view.backVisual,
                        0
                    ).apply {
                        onFinished = { view.showBack() }
                    }
                )

            if (!tile.collapsed && view.visual != view.frontVisual)
                playAnimation(
                    FlipAnimation(
                        view,
                        view.backVisual,
                        view.frontVisual,
                        0
                    ).apply {
                        onFinished = { view.showFront() }
                    }
                )
        }
    }

    /**
     * Updates the step tokens on the left side in the current game without any animations.
     *
     * @see updateAllInstant
     */
    private fun updateStepTokensInstant() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState
        val currentPlayer = currentState.currentPlayer

        stepTokenLayout.clear()
        for (i in stepTokenViews.indices) {
            val stepTokenView = stepTokenViews[i]
            val isVisible = i < currentPlayer.remainingMoves + currentPlayer.visitedTiles.size

            stepTokenView.isVisible = isVisible

            if (!isVisible)
                continue

            stepTokenLayout.add(stepTokenView)

            stepTokenView.apply {
                posX = 0.0
                posY = 0.0
            }
        }

        for (i in 0 until currentPlayer.remainingMoves + currentPlayer.visitedTiles.size) {
            val stepTokenView = stepTokenViews[i]

            if (i < currentPlayer.remainingMoves)
                continue

            stepTokenView.apply {
                posX = getPlayerPosX(
                    currentPlayer.visitedTiles[i - currentPlayer.remainingMoves]
                ) - stepTokenView.actualPosX
                posY = getPlayerPosY(
                    currentPlayer.visitedTiles[i - currentPlayer.remainingMoves]
                ) - stepTokenView.actualPosY
            }
        }
    }

    /**
     * Updates the player ranking on the right side in the current game without any animations.
     *
     * @see updateAllInstant
     */
    private fun updatePlayerRankingInstant() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        playerRankPanes.forEach { it.posX = 1920.0 }
        playerRankPanes.forEach { it.clear() }

        backToMenuButtonPane.posX = 1920.0

        for (rank in 0 until 4) {
            val player = currentState.players.find { it.rank == rank }
            val rankPane = playerRankPanes[rank]

            rankPane.clear()

            if (player == null) {
                rankPane.posX = 1920.0
            } else {
                rankPane.posX = 1920.0 - 300.0

                val rankToken = playerRankTokens.getValue(player.color)
                rankPane.add(rankToken)

                rankToken.posX = 44.0
                rankToken.posY = 57.0
            }
        }
    }

    //endregion

    //region Helpers

    /**
     * Sets the simulation speed of the current game.
     *
     * If the game is unpaused and a bot was supposed to make a move, it will do so.
     *
     * @param index The index of the speed to set, where 0 = paused, 1-3 = increasing speeds.
     */
    private fun setSimulationSpeed(index: Int) {
        require(index in 0..3) { "Index out of bounds." }

        // Set arrow position.
        val button = if (index == 0) pauseButton else speedButtons[index - 1]
        val toX = button.actualPosX + button.width / 2 - activeSpeedArrow.width / 2
        playAnimation(
            MovementAnimation(
                activeSpeedArrow,
                activeSpeedArrow.posX,
                toX,
                activePlayerArrow.posY,
                activePlayerArrow.posY,
                100
            ).apply {
                onFinished = {
                    activeSpeedArrow.posX = toX
                }
            }
        )

        // Adjust simulation speed.
        simulationSpeed = when (index) {
            0 -> 0.0
            1 -> 1.0
            2 -> 1.5
            3 -> 2.5
            else -> throw IndexOutOfBoundsException("Index out of bounds.")
        }

        if (simulationSpeed > 0 && performBotMoveOnUnpause) {
            performNextBotMove()
        }
    }

    /**
     * Animates the movement of the current player's pawn from one tile to another.
     *
     * If the pawn needs to wrap around the board, a duplicate pawn is used for the animation.
     *
     * @param from The starting [Coordinate] of the move. This should be the current position of the given player.
     * @param to The destination [Coordinate] of the move.
     * @param playerColor The [PlayerColor] of the player whose pawn is being moved.
     */
    private fun movePlayerPawn(from: Coordinate, to: Coordinate, playerColor: PlayerColor) {
        val maxBoardCoordinate = boardGrid.columns - 1

        val currentPlayerPawn = playerMainPawns.getValue(playerColor)

        var mainFromX = getPlayerPosX(from)
        var mainFromY = getPlayerPosY(from)

        // Move the player's duplicate pawn for wrap-around animation if needed.
        // In this case, the main pawn always moves from off-screen to the destination and the duplicate
        // moves from the original position to off-screen.
        if (abs(from.x - to.x) > 1 || abs(from.y - to.y) > 1) {
            val distance = 200
            val currentPlayerDuplicatePawn = playerDuplicatePawns.getValue(playerColor)

            var duplicateToX = getPlayerPosX(to)
            var duplicateToY = getPlayerPosY(to)

            // These are the four possible wrap-around cases.
            if (from.x == 0 && to.x == maxBoardCoordinate) {
                mainFromX = getPlayerPosX(to) + distance
                duplicateToX = getPlayerPosX(from) - distance
            } else if (from.x == maxBoardCoordinate && to.x == 0) {
                mainFromX = getPlayerPosX(to) - distance
                duplicateToX = getPlayerPosX(from) + distance
            } else if (from.y == 0 && to.y == maxBoardCoordinate) {
                mainFromY = getPlayerPosY(to) + distance
                duplicateToY = getPlayerPosY(from) - distance
            } else if (from.y == maxBoardCoordinate && to.y == 0) {
                mainFromY = getPlayerPosY(to) - distance
                duplicateToY = getPlayerPosY(from) + distance
            }

            currentPlayerDuplicatePawn.posX = getPlayerPosX(from)
            currentPlayerDuplicatePawn.posY = getPlayerPosY(from)

            // Move the duplicate pawn from the original position to off-screen.
            playAnimation(
                MovementAnimation(
                    currentPlayerDuplicatePawn,
                    getPlayerPosX(from),
                    duplicateToX,
                    getPlayerPosY(from),
                    duplicateToY,
                    moveAnimationDuration
                ).apply {
                    onFinished = {
                        currentPlayerDuplicatePawn.posX = -200.0
                        currentPlayerDuplicatePawn.posY = -200.0
                    }
                }
            )
        }

        // Move the main pawn from off-screen to the destination.
        currentPlayerPawn.posX = mainFromX
        currentPlayerPawn.posY = mainFromY
        playAnimation(
            MovementAnimation(
                currentPlayerPawn,
                mainFromX,
                getPlayerPosX(to),
                mainFromY,
                getPlayerPosY(to),
                moveAnimationDuration
            ).apply {
                onFinished = {
                    currentPlayerPawn.posX = getPlayerPosX(to)
                    currentPlayerPawn.posY = getPlayerPosY(to)
                }
            })
    }

    /**
     * Translates a tile coordinate to the X position where the player's pawn should be placed.
     *
     * @param position The tile coordinate.
     * @return The X position for the player's pawn.
     */
    private fun getPlayerPosX(position: Coordinate): Double {
        val currentTile = tileViews.forward(position)

        return currentTile.actualPosX + (currentTile.width - 64) / 2
    }

    /**
     * Translates a tile coordinate to the Y position where the player's pawn should be placed.
     *
     * @param position The tile coordinate.
     * @return The Y position for the player's pawn.
     */
    private fun getPlayerPosY(position: Coordinate): Double {
        val currentTile = tileViews.forward(position)

        return currentTile.actualPosY + (currentTile.height - 64) / 2
    }

    /**
     * Called when a tile is clicked. Attempts to move the current player to the clicked tile.
     *
     * Does not throw an error if the move is invalid.
     *
     * @param position The [Coordinate] of the [CardView] that was clicked.
     */
    private fun tryMoveTo(position: Coordinate) {
        val game = root.currentGame ?: return

        if (!blockMovementInput
            && root.playerActionService.canMoveTo(position)
            && game.currentState.currentPlayer.type == PlayerType.LOCAL
        ) {
            root.playerActionService.moveTo(position)
        }
    }

    /**
     * Plays the animation when a player gets placed in the ranking on death or victory.
     *
     * @param player The player that is being ranked.
     * @param rank The rank of the player (1-4th).
     */
    private fun showPlayerRank(player: Player, rank: Int) {
        val mainToken = playerMainPawns.getValue(player.color)
        mainToken.isVisible = false

        val rankToken = playerRankTokens.getValue(player.color)

        val rankPane = playerRankPanes[rank - 1]
        rankPane.add(rankToken)

        rankToken.apply {
            isVisible = true
            scale = 0.8
        }
        rankToken.posX = 44.0
        rankToken.posY = 57.0

        playAnimation(
            MovementAnimation(
                rankPane,
                rankPane.posX,
                rankPane.posX - 300,
                rankPane.posY,
                rankPane.posY,
                (500 * delayMultiplier).roundToInt()
            ).apply {
                onFinished = {
                    rankPane.apply {
                        posX -= 300
                    }
                }
            }
        )
    }

    /**
     * Performs the next move for the bot player.
     * If the bot has remaining moves, this function will continue to call itself until the turn ends.
     */
    private fun performNextBotMove() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val originalPlayer = gameState.currentPlayer

        if (simulationSpeed == 0.0) {
            performBotMoveOnUnpause = true

            return
        }

        performBotMoveOnUnpause = false

        // Make the next move after a short delay.
        playAnimation(DelayAnimation(moveAnimationDuration + 50).apply {
            onFinished = {
                root.botService.makeMove()

                // Move until the player switches.
                if (gameState.currentPlayer == originalPlayer && originalPlayer.remainingMoves > 0) {
                    performNextBotMove()
                }
            }
        })
    }

    //endregion
    //endregion
}