package gui

import entity.*
import service.*
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
import kotlin.math.*

class GameScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : BoardGameScene(1920, 1080), Refreshable {
    //region Components
    //region Board

    /**
     * The GridPane containing all tiles on the board.
     */
    private val boardGrid = GridPane<CardView>(
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

    private val maskedBackground = Label(
        posX = 0,
        posY = 0,
        width = this.width,
        height = this.height,
        visual = ImageVisual("GameScene/MaskedBackground.png")
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
        visual = ImageVisual("GameScene/GameHudBackground.png"),
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
        alignment = Alignment.CENTER,
        spacing = 40,
    )

    /**
     * The tokens on the left side of the screen that represent each player.
     * This includes inactive player tokens.
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
     * The arrow that displays the current player.
     */
    private val activePlayerArrow = Label(
        width = 84,
        height = 116,
        posY = 170,
        visual = ImageVisual("GameScene/CurrentPlayerArrow.png")
    )

    /**
     * The LinearLayout for the step tokens on the left side that show the remaining moves.
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
            visual = ImageVisual("GameScene/StepToken.png")
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
        visual = ImageVisual("GameScene/Exports/BackToMenuButtonBackground.png")
    )

    /**
     * The button below the player ranking to return to the main menu.
     */
    private val backToMenuButton = Button(
        width = 160,
        height = 90,
        posX = 65,
        posY = 65,
        visual = ImageVisual("GameScene/Exports/BackToMenuButton.png")
    ).apply {
        onMouseClicked = {
            app.showMenuScene(app.mainMenuScene)
        }
    }

    //endregion

    //region Toolbar

    private val toolbarBackground = Label(
        width = 650,
        height = 135,
        posX = 1052 - 650 / 2,
        posY = 1080 - 135,
        visual = ImageVisual("GameScene/Exports/ToolbarBg.png")
    )

    private val toolbarLayout = LinearLayout<TokenView>(
        width = 640,
        height = 50,
        posX = 1052,
        posY = 990,
        spacing = 40,
        horizontalAlignment = HorizontalAlignment.CENTER
    )

    private val undoButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("GameScene/Button_Undo.png")
    ).apply {
        onMouseClicked = {
            val game = root.currentGame

            if (!blockMovementInput && game != null && game.undoStack.isNotEmpty()
            ) {
                root.playerActionService.undo()
            }
        }
    }

    private val redoButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("GameScene/Button_Redo.png")
    ).apply {
        onMouseClicked = {
            val game = root.currentGame

            if (!blockMovementInput && game != null && game.redoStack.isNotEmpty()
            ) {
                root.playerActionService.redo()
            }
        }
    }

    private val pauseButton = TokenView(
        width = 50,
        height = 50,
        visual = ImageVisual("GameScene/Button_Pause.png")
    ).apply {
        onMouseClicked = {
            setSimulationSpeed(0)
        }
    }

    private val speedButtons = List(3) { index ->
        TokenView(
            width = 25 * (index + 1),
            height = 50,
            visual = ImageVisual("GameScene/Button_Speed_${index + 1}.png")
        ).apply {
            onMouseClicked = {
                setSimulationSpeed(index + 1)
            }
        }
    }

    private val activeSpeedArrow = Label(
        width = 50,
        height = 50,
        posX = 1052,
        posY = 1030,
        visual = ImageVisual("GameScene/Exports/ToolbarArrow.png")
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    //endregion
    //endregion

    //region Animations

    /**
     * This multiplier is applied to every delay and animation
     * to speed everything up depending on the simulation speed.
     *
     * This value will always be at most 1, to prevent animations from not playing when the game is paused.
     *
     * @see [CollapsiGame.simulationSpeed]
     */
    val delayMultiplier get() = 1 / max(root.currentGame?.simulationSpeed ?: 1.0, 1.0)

    /**
     * This is how long the animation for each move lasts.
     */
    val moveAnimationDuration get() = (500 * delayMultiplier).roundToInt()

    /**
     * This is how long the game waits before ending a player's turn.
     */
    val endTurnDelay get() = (1000 * delayMultiplier).roundToInt()

    /**
     * True if the bot was supposed to make a move, but couldn't, due to the game being paused.
     * Once the simulation is unpaused, the bot will make its move.
     *
     * @see [CollapsiGame.simulationSpeed]
     */
    var performBotMoveOnUnpause = false

    /**
     * True if an animation is playing which should prevent a local player from making moves.
     */
    var blockMovementInput = false

    //endregion

    //region Functions

    init {
        background = ImageVisual("gameScene/Background.png")

        addComponents(boardGrid)

        addComponents(*playerMainPawns.values.toTypedArray())
        addComponents(*playerDuplicatePawns.values.toTypedArray())

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

        toolbarLayout.addAll(
            undoButton,
            redoButton,
            pauseButton,
            speedButtons[0],
            speedButtons[1],
            speedButtons[2]
        )
        toolbarLayout.posX = 1052.0 - toolbarLayout.width / 2
    }

    //region Refreshes

    override fun refreshAfterStartNewGame() {
        loadScene()
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Move player's main pawn.
        val currentPlayerPawn = playerMainPawns.getValue(currentState.currentPlayer.color)

        var mainFromX = getPlayerPosX(from)
        var mainFromY = getPlayerPosY(from)

        val distance = 200

        if (abs(from.x - to.x) > 1 || abs(from.y - to.y) > 1) {
            val currentPlayerDuplicatePawn = playerDuplicatePawns.getValue(currentState.currentPlayer.color)

            var duplicateToX = getPlayerPosX(to)
            var duplicateToY = getPlayerPosY(to)

            if (from.x == 0 && to.x == currentState.boardSize - 1) {
                mainFromX = getPlayerPosX(to) + distance
                duplicateToX = getPlayerPosX(from) - distance
            }
            if (from.x == currentState.boardSize - 1 && to.x == 0) {
                mainFromX = getPlayerPosX(to) - distance
                duplicateToX = getPlayerPosX(from) + distance
            }
            if (from.y == 0 && to.y == currentState.boardSize - 1) {
                mainFromY = getPlayerPosY(to) + distance
                duplicateToY = getPlayerPosY(from) - distance
            }
            if (from.y == currentState.boardSize - 1 && to.y == 0) {
                mainFromY = getPlayerPosY(to) - distance
                duplicateToY = getPlayerPosY(from) + distance
            }

            currentPlayerDuplicatePawn.posX = getPlayerPosX(from)
            currentPlayerDuplicatePawn.posY = getPlayerPosY(from)
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
        }

        blockMovementInput = true

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

        // Wait and then end the turn.
        if (currentState.currentPlayer.remainingMoves <= 0) {
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

        // Hide all step tokens.
        stepTokenViews.forEach { it.isVisible = false }
        stepTokenLayout.clear()

        for (i in 0 until currentState.currentPlayer.remainingMoves) {
            stepTokenViews[i].apply {
                isVisible = true
                posX = 0.0 // Reset position within layout.
                posY = 0.0
            }
            stepTokenLayout.add(stepTokenViews[i])
        }

        if (currentState.currentPlayer.type == PlayerType.BOT
            && root.playerActionService.hasValidMove()
        ) {
            root.botService.calculateTurn()

            performNextBotMove()
        }
    }

    override fun refreshAfterPlayerDied(player: Player) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val alivePlayerCount = currentState.players.count { it.alive }

        showPlayerRank(player, alivePlayerCount + 1)
    }

    override fun refreshAfterGameEnd(winner: Player) {
        // How "Menu" Button.
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
        setSimulationSpeed(0)
        updateAllInstant()
    }

    override fun refreshAfterRedo() {
        setSimulationSpeed(0)
        updateAllInstant()
    }

    override fun refreshAfterLoad() {
        loadScene()

        updateAllInstant()
    }

    //endregion

    //region Initializers

    private fun loadScene() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        initializeBoard()
        initializePlayers()
        initializeLeftInfoPane()
        initializePlayerRanking()

        if (currentState.currentPlayer.type == PlayerType.BOT) {
            root.botService.calculateTurn()

            setSimulationSpeed(0)
            performBotMoveOnUnpause = true
        } else {
            setSimulationSpeed(1)
        }
    }

    private fun initializeBoard() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        // Init Grid.

        repeat(boardGrid.rows) { boardGrid.removeRow(0) }
        repeat(boardGrid.columns) { boardGrid.removeColumn(0) }

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
                back = ImageVisual("GameScene/Tile_Collapsed.png")
            ).apply {
                if (!tile.collapsed)
                    showFront()
                
                onMouseClicked = {
                    val game = root.currentGame

                    if (!blockMovementInput
                        && game != null
                        && root.playerActionService.canMoveTo(position)
                        && game.currentState.currentPlayer.type == PlayerType.LOCAL
                    ) {
                        root.playerActionService.moveTo(position)
                    }
                }
            }

            tileViews.add(tile.position, cardView)
            boardGrid[position.x, position.y] = cardView
        }
    }

    private fun initializePlayers() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        playerMainPawns.getValue(PlayerColor.YELLOW_CIRCLE).isVisible = currentState.players.size >= 3
        playerMainPawns.getValue(PlayerColor.RED_TRIANGLE).isVisible = currentState.players.size >= 4

        playerRankTokens.values.forEach { it.isVisible = false }

        val tokenScale = when (currentState.boardSize) {
            4 -> 1.0
            5 -> 0.8
            6 -> 0.677
            else -> throw IllegalStateException("Board size out of range.")
        }

        playerMainPawns.values.forEach { it.scale = tokenScale }
        playerDuplicatePawns.values.forEach { it.scale = tokenScale }
        playerRankTokens.values.forEach { it.scale = tokenScale }

        playerMainPawns.values.forEach { it.isVisible = true }

        for (player in currentState.players) {
            playerMainPawns.getValue(player.color).posX = getPlayerPosX(player.position)
            playerMainPawns.getValue(player.color).posY = getPlayerPosY(player.position)
        }
    }

    private fun initializeLeftInfoPane() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        playerOrderLayout.clear()

        for (player in currentState.players) {
            val playerVisualToAdd = playerOrderTokens[player.color]
            checkNotNull(playerVisualToAdd)
            playerOrderLayout.add(playerVisualToAdd)
        }

        if (currentState.players.size >= 3) {
            playerMainPawns.getValue(PlayerColor.YELLOW_CIRCLE).apply { isVisible = true }
            playerOrderLayout.apply { spacing = 56.0 }
        }
        if (currentState.players.size == 4) {
            playerMainPawns.getValue(PlayerColor.RED_TRIANGLE).apply { isVisible = true }
            playerOrderLayout.apply { spacing = 48.0 }
        }

        val currentPlayerOrderToken = playerOrderTokens.getValue(currentState.currentPlayer.color)
        activePlayerArrow.posX = currentPlayerOrderToken.actualPosX - 10

        // First player starts with 1 step token.
        stepTokenViews.forEach { it.isVisible = false }
        stepTokenLayout.clear()

        stepTokenViews[0].isVisible = true
        stepTokenLayout.add(stepTokenViews[0])
    }

    private fun initializePlayerRanking() {
        playerRankPanes.forEach { it.posX = 1920.0 }
        playerRankPanes.forEach { it.clear() }

        backToMenuButtonPane.posX = 1920.0
    }

    //endregion

    //region Helpers

    /**
     * Updates all tiles, players, and other visuals in the current game.
     * This is only used for undo/redo and load.
     */
    private fun updateAllInstant() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState
        val currentPlayer = currentState.currentPlayer

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

        activePlayerArrow.posX = playerOrderTokens.getValue(currentPlayer.color).actualPosX - 10

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

    private fun setSimulationSpeed(index: Int) {
        val game = root.currentGame ?: return

        // Ignore button presses after game end.

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
        game.simulationSpeed = when (index) {
            0 -> 0.0
            1 -> 1.0
            2 -> 1.5
            3 -> 2.5
            else -> throw IndexOutOfBoundsException("Index out of bounds.")
        }

        if (game.simulationSpeed > 0 && performBotMoveOnUnpause) {
            performNextBotMove()
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

    private fun performNextBotMove() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val originalPlayer = gameState.currentPlayer

        if (game.simulationSpeed == 0.0) {
            performBotMoveOnUnpause = true

            return
        }

        performBotMoveOnUnpause = false

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

    private fun getPlayerPosX(position: Coordinate): Double {
        val currentTile = tileViews.forward(position)

        return currentTile.actualPosX + (currentTile.width - 64) / 2
    }

    private fun getPlayerPosY(position: Coordinate): Double {
        val currentTile = tileViews.forward(position)

        return currentTile.actualPosY + (currentTile.height - 64) / 2
    }

    //endregion
    //endregion
}