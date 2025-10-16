package gui

import entity.Coordinate
import entity.Player
import entity.PlayerColor
import entity.PlayerType
import entity.Tile
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.FlipAnimation
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.visual.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.math.roundToInt

class GameScene(
    private val app: CollapsiApplication,
    private val root: RootService
) : BoardGameScene(1920, 1080), Refreshable {
    //region Board

    /**
     * The GridPane containing all tiles on the board.
     */
    private val boardGrid = GridPane<ComponentView>(
        posX = 1052,
        posY = 540,
        rows = 0,
        columns = 0,
        spacing = 20,
    )

    /**
     * A map with all tile positions mapped to their CardViews.
     */
    private val tileViews = BidirectionalMap<Coordinate, CardView>()

    private val playerMainTokens: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            ).apply {
                isDisabled = true
            }
        )
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

    //region Button Pane

    private val buttonPane = Pane<Button>(
        width = 640,
        height = 90,
        posX = 800,
        posY = 990,
        //visual = ColorVisual.GRAY
    )

    private val activeSpeed = Label(
        width = 6,
        height = 20,
        posX = 1032.75,
        posY = 1060,
        visual = ColorVisual.WHITE
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    //endregion

    private var animationSpeed = 500

    init {
        background = ImageVisual("gameScene/Background.png")

        infoPane.addAll(activePlayerArrow, playerOrderLayout, stepTokenLayout)
        backToMenuButtonPane.add(backToMenuButton)

        addComponents(
            boardGrid,
            infoPane,
            activeSpeed,
            buttonPane,
            backToMenuButtonPane
        )

        addComponents(*playerMainTokens.values.toTypedArray())
        addComponents(*playerRankPanes.toTypedArray())
    }

    //--------------------v Refreshes v--------------------

    override fun refreshAfterStartNewGame() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        initializeBoard()
        initializePlayers()
        initializeLeftInfoPane()
        initializePlayerRanking()
        initializeButtons()

        // Start the bot.
        if (currentState.currentPlayer.type == PlayerType.BOT && game.simulationSpeed >= 0) {
            root.botService.calculateTurn()

            playAnimation(DelayAnimation(((game.simulationSpeed + 1) * 1000).roundToInt()).apply {
                onFinished = { makeNextBotMove() }
            })
        }
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val playerTokenToMove = playerMainTokens[currentState.currentPlayer.color]
        checkNotNull(playerTokenToMove)

        playAnimation(
            MovementAnimation(
                playerTokenToMove,
                getPlayerPosX(from),
                getPlayerPosX(to),
                getPlayerPosY(from),
                getPlayerPosY(to),
                animationSpeed
            ).apply {
                onFinished = {
                    playerTokenToMove.apply {
                        posX = getPlayerPosX(to)
                        posY = getPlayerPosY(to)
                    }
                }
            })

        if (currentState.currentPlayer.visitedTiles.size == 1) {
            val collapsedTileView = tileViews.forward(from)
            checkNotNull(collapsedTileView)
            collapsedTileView.apply {
                playAnimation(
                    FlipAnimation(
                        collapsedTileView,
                        collapsedTileView.frontVisual,
                        collapsedTileView.backVisual,
                        animationSpeed
                    ).apply {
                        onFinished = {
                            collapsedTileView.showBack()
                        }
                    })
            }
        }

        val stepToken = stepTokenViews[currentState.currentPlayer.remainingMoves]
        playAnimation(
            MovementAnimation(
                stepToken,
                stepToken.actualPosX,
                getPlayerPosX(from),
                stepToken.actualPosY,
                getPlayerPosY(from),
                animationSpeed
            ).apply {
                onFinished = {
                    stepToken.offset(
                        getPlayerPosX(from) - stepToken.actualPosX,
                        getPlayerPosY(from) - stepToken.actualPosY
                    )
                }
            })

        if (currentState.currentPlayer.remainingMoves <= 0) {
            playAnimation(DelayAnimation(animationSpeed * 2).apply {
                onFinished = {
                    root.gameService.endTurn()
                }
            })
        }
    }

    override fun refreshAfterEndTurn() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentLabel = playerOrderTokens[currentState.currentPlayer.color]
        checkNotNull(currentLabel)
        activePlayerArrow.posX = currentLabel.actualPosX - 10

        stepTokenViews.forEach {
            it.isVisible = false
            it.posX = 0.0
            it.posY = 0.0
        }
        stepTokenLayout.clear()

        for (i in 0 until currentState.currentPlayer.remainingMoves) {
            stepTokenViews[i].apply {
                isVisible = true
            }
            stepTokenLayout.add(stepTokenViews[i])
        }

        if (currentState.currentPlayer.type == PlayerType.BOT
            && game.simulationSpeed >= 0
            && root.playerActionService.hasValidMove()
        ) {
            root.botService.calculateTurn()

            playAnimation(DelayAnimation((game.simulationSpeed * 1000).roundToInt()).apply {
                onFinished = { makeNextBotMove() }
            })
        }
    }

    override fun refreshAfterPlayerDied(player: Player) {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val alivePlayerCount = currentState.players.count { it.alive }

        showPlayerRank(player, alivePlayerCount + 1)
    }

    override fun refreshAfterGameEnd(winner: Player) {
        playAnimation(
            MovementAnimation(
                backToMenuButtonPane,
                backToMenuButtonPane.posX,
                1550,
                backToMenuButtonPane.posY,
                backToMenuButtonPane.posY,
                animationSpeed
            )
        )

        showPlayerRank(winner, 1)
    }

    /**
     * Plays the animation when a player gets placed in the ranking on death or victory.
     *
     * @param player The player that is being ranked.
     * @param rank The rank of the player (1-4th).
     */
    private fun showPlayerRank(player: Player, rank: Int) {
        val mainToken = playerMainTokens[player.color]
        checkNotNull(mainToken)
        mainToken.apply { isVisible = false }

        val rankToken = playerRankTokens[player.color]
        checkNotNull(rankToken)

        val pane = playerRankPanes[rank - 1]
        pane.add(rankToken)

        rankToken.apply {
            isVisible = true
            scale = 0.8
        }
        rankToken.posX = 44.0
        rankToken.posY = 57.0

        playAnimation(
            MovementAnimation(
                pane,
                pane.posX,
                pane.posX - 300,
                pane.posY,
                pane.posY,
                animationSpeed
            ).apply {
                onFinished = {
                    pane.apply {
                        posX -= 300
                    }
                }
            }
        )
    }

    //--------------------^ Refreshes ^--------------------

    //--------------------v Helper Functions v--------------------

    fun makeNextBotMove() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val originalPlayer = gameState.currentPlayer

        playAnimation(DelayAnimation((0.35 * game.simulationSpeed * 1000).roundToInt()).apply {
            onFinished = {
                root.botService.makeMove()

                // Move until the player switches.
                if (gameState.currentPlayer == originalPlayer && originalPlayer.remainingMoves > 0) {
                    makeNextBotMove()
                }
            }
        })
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

        for ((position: Coordinate, tile: Tile) in currentState.board) {
            val frontVisual: ImageVisual = if (tile.startTileColor == null) {
                getTileVisual(tile.movesToMake)
            } else {
                ImageVisual("GameScene/Tile_P${tile.startTileColor.ordinal + 1}.png")
            }

            val size = when (currentState.boardSize) {
                4 -> 176.0
                5 -> 140.8
                6 -> 119.167
                else -> throw IllegalStateException("Board size out of range.")
            }

            val cardView = CardView(
                width = size,
                height = size,
                front = frontVisual,
                back = ImageVisual("GameScene/Tile_Collapsed.png")
            ).apply {
                if (!tile.collapsed)
                    showFront()

                onMouseClicked = {
                    if (root.playerActionService.canMoveTo(position))
                        root.playerActionService.moveTo(position)
                }
            }

            tileViews.add(tile.position, cardView)
            boardGrid[position.x, position.y] = cardView
        }
    }

    private fun initializePlayers() {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        playerMainTokens.getValue(PlayerColor.YELLOW_CIRCLE).isVisible = currentState.players.size >= 3
        playerMainTokens.getValue(PlayerColor.RED_TRIANGLE).isVisible = currentState.players.size >= 4

        playerRankTokens.values.forEach { it.isVisible = false }

        val tokenScale = when (currentState.boardSize) {
            4 -> 1.0
            5 -> 0.8
            6 -> 0.677
            else -> throw IllegalStateException("Board size out of range.")
        }

        playerMainTokens.values.forEach { it.scale = tokenScale }
        playerRankTokens.values.forEach { it.scale = tokenScale }

        for (player in currentState.players) {
            playerMainTokens.getValue(player.color).posX = getPlayerPosX(player.position)
            playerMainTokens.getValue(player.color).posY = getPlayerPosY(player.position)
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
            playerMainTokens.getValue(PlayerColor.YELLOW_CIRCLE).apply { isVisible = true }
            playerOrderLayout.apply { spacing = 56.0 }
        }
        if (currentState.players.size == 4) {
            playerMainTokens.getValue(PlayerColor.RED_TRIANGLE).apply { isVisible = true }
            playerOrderLayout.apply { spacing = 48.0 }
        }

        val currentPlayerOrderToken = playerOrderTokens.getValue(currentState.currentPlayer.color)
        activePlayerArrow.posX = currentPlayerOrderToken.actualPosX - 10
    }

    private fun initializePlayerRanking() {
        playerRankPanes.forEach { it.posX = 1920.0 }
        backToMenuButtonPane.posX = 1920.0
    }

    private fun initializeButtons() {
        buttonPane.addAll(
            Button(
                width = 26.67 * 1.5,
                height = 25 * 1.5,
                posX = 20,
                posY = 12.5,
                visual = ImageVisual("GameScene/undo.png")
            ).apply {
                onMouseClicked = {
                    root.playerActionService.undo()
                }
            },
            Button(
                width = 26.67 * 1.5,
                height = 25 * 1.5,
                posX = 120,
                posY = 12.5,
                visual = ImageVisual("GameScene/redo.png")
            ).apply {
                onMouseClicked = {
                    root.playerActionService.redo()
                }
            },
            Button(
                width = 19.64 * 1.5,
                height = 25 * 1.5,
                posX = 220,
                posY = 12.5,
                visual = ImageVisual("GameScene/speed_one.png")
            ).apply {
                onMouseClicked = {
                    val game = root.currentGame
                    checkNotNull(game)
                    game.simulationSpeed = 0.0
                    activeSpeed.posX = this.actualPosX + this.width / 2 - activeSpeed.width / 2
                    animationSpeed = 1000
                }
            },
            Button(
                width = 39.58 * 1.5,
                height = 25 * 1.5,
                posX = 320,
                posY = 12.5,
                visual = ImageVisual("GameScene/speed_two.png")
            ).apply {
                onMouseClicked = {
                    val game = root.currentGame
                    checkNotNull(game)
                    game.simulationSpeed = 1.0
                    activeSpeed.posX = this.actualPosX + this.width / 2 - activeSpeed.width / 2
                    animationSpeed = 500
                }
            },
            Button(
                width = 60.58 * 1.5,
                height = 25 * 1.5,
                posX = 420,
                posY = 12.5,
                visual = ImageVisual("GameScene/speed_three.png")
            ).apply {
                onMouseClicked = {
                    val game = root.currentGame
                    checkNotNull(game)
                    game.simulationSpeed = 2.0
                    activeSpeed.posX = this.actualPosX + this.width / 2 - activeSpeed.width / 2
                    animationSpeed = 200
                }
            },
        )
    }

    private fun getTileVisual(movesOnTile: Int): ImageVisual {
        return when (movesOnTile) {
            1 -> ImageVisual("GameScene/Tile_1.png")
            2 -> ImageVisual("GameScene/Tile_2.png")
            3 -> ImageVisual("GameScene/Tile_3.png")
            4 -> ImageVisual("GameScene/Tile_4.png")
            else -> throw IllegalArgumentException("Invalid number of moves on tile: $movesOnTile")
        }
    }

    private fun getPlayerPosX(position: Coordinate): Double {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentTile = tileViews.forward(position)
        checkNotNull(currentTile)

        return currentTile.actualPosX + (currentTile.width - 64) / 2
    }

    private fun getPlayerPosY(position: Coordinate): Double {
        val game = checkNotNull(root.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentTile = tileViews.forward(position)
        checkNotNull(currentTile)

        return currentTile.actualPosY + (currentTile.height - 64) / 2
    }

    //--------------------^ Helper Functions ^--------------------
}