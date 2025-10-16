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
import kotlin.math.roundToInt

class GameScene(
    private val app: CollapsiApplication,
    private val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {
    /**
     * A map with all tile positions mapped to the CardViews representing tiles.
     */
    private val tileViews = BidirectionalMap<Coordinate, CardView>()

    private var animationSpeed = 500


    //--------------------v Left Info Pane v--------------------

    private val infoPane = Pane<ComponentView>(
        width = 540,
        height = 1080,
        visual = ImageVisual("GameScene/GameHudBackground.png"),
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    /**
     * LinearLayout for the tokens on the left side that show the player order.
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
     * This includes even inactive tokens.
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

    private val activePlayerArrow = Label(
        width = 84,
        height = 116,
        posY = 170,
        visual = ImageVisual("GameScene/CurrentPlayerArrow.png")
    )

    private val stepTokenLayout = LinearLayout<TokenView>(
        width = 460,
        height = 128,
        posX = 40,
        posY = 308,
        alignment = Alignment.CENTER,
        spacing = 40,
    )

    private val stepTokenViews = List(4) {
        TokenView(
            width = 64,
            height = 64,
            visual = ImageVisual("GameScene/StepToken.png")
        ).apply {
            isVisible = false
        }
    }

    //--------------------^ Left Info Pane ^--------------------

    //--------------------v Button Pane v--------------------

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

    //--------------------^ Button Pane ^--------------------

    //--------------------v Player Tokens v--------------------

    private val playerMainTokens: Map<PlayerColor, TokenView> = (0..3).associate { index ->
        Pair(
            PlayerColor.entries[index],
            TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/Pawn_P${index + 1}.png")
            )
        )
    }

    //--------------------^ Player Tokens ^--------------------

    //--------------------v Play Ranking v--------------------

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

    private val playerRankPanes = List(4) { index ->
        Pane<TokenView>(
            width = 200,
            height = 200,
            posX = 1920,
            posY = 100 + index * 170,
            visual = ImageVisual("GameScene/PlayerRanking_${index + 1}.png")
        )
    }

    //--------------------^ Play Ranking ^--------------------

    private val backToMenuButtonPane = Pane<Button>(
        width = 300,
        height = 300,
        posX = 1920,
        posY = 780,
        visual = ImageVisual("GameScene/Exports/BackToMenuButtonBackground.png")
    )

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

    private val playContainer = Pane<ComponentView>(
        width = 784,
        height = 784,
        posX = 660,
        posY = 148,
        //visual = ColorVisual.BLACK
    )

    init {
        background = ImageVisual("gameScene/Background.png")

        infoPane.addAll(activePlayerArrow, playerOrderLayout, stepTokenLayout)
        backToMenuButtonPane.add(backToMenuButton)

        addComponents(
            playContainer,
            infoPane,
            activeSpeed,
            buttonPane,
            backToMenuButtonPane
        )

        addComponents(*playerMainTokens.values.toTypedArray())
        addComponents(*playerRankTokens.values.toTypedArray())
        addComponents(*playerRankPanes.toTypedArray())
    }

    //--------------------v Refreshes v--------------------

    override fun refreshAfterStartNewGame() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        tileViews.clear()
        playContainer.clear()

        val playArea = GridPane<ComponentView>(
            posX = playContainer.width / 2,
            posY = playContainer.height / 2,
            rows = currentState.boardSize,
            columns = currentState.boardSize,
            spacing = 20,
        ).apply {
            if (currentState.boardSize == 5) {
                spacing = 15.0
            } else if (currentState.boardSize == 6) {
                spacing = 10.0
            }
        }

        playerMainTokens[PlayerColor.YELLOW_CIRCLE]!!.isVisible = currentState.players.size >= 3
        playerMainTokens[PlayerColor.RED_TRIANGLE]!!.isVisible = currentState.players.size >= 4

        playerRankTokens.values.forEach { it.isVisible = false }

        val tokenScale = when (currentState.players.size) {
            4 -> 0.677
            3 -> 0.8
            else -> 1.0
        }

        playerMainTokens.values.forEach { it.scale = tokenScale }
        playerRankTokens.values.forEach { it.scale = tokenScale }

        playContainer.add(playArea)

        currentState.board.forEach { (coordinate: Coordinate, tile: Tile) ->
            val startingColor: ImageVisual = when (tile.startTileColor) {
                PlayerColor.GREEN_SQUARE -> ImageVisual("GameScene/Tile_P1.png")
                PlayerColor.ORANGE_HEXAGON -> ImageVisual("GameScene/Tile_P2.png")
                PlayerColor.YELLOW_CIRCLE -> ImageVisual("GameScene/Tile_P3.png")
                PlayerColor.RED_TRIANGLE -> ImageVisual("GameScene/Tile_P4.png")
                else -> getTileVisual(tile.movesToMake)
            }

            val cardView = CardView(
                width = 176, //192, //160,
                height = 176, //192, //160,
                posX = 0,
                posY = 0,
                front = startingColor,
                back = ImageVisual("GameScene/Tile_Collapsed.png")
            ).apply {
                if (currentState.boardSize == 5) {
                    width = 140.8 //153.6 //128.0
                    height = 140.8 //153.6 //128.0
                } else if (currentState.boardSize == 6) {
                    width = 119.167 //130.0 //108.33
                    height = 119.167 //130.0 //108.33
                }
                if (!tile.collapsed) showFront()
                if (!currentState.currentPlayer.position.neighbours.contains(coordinate)) {
                    isDisabled = true
                }
                onMouseClicked = {
                    rootService.playerActionService.moveTo(
                        coordinate
                    )
                }
            }

            tileViews.add(tile.position, cardView)
            playArea[coordinate.x, coordinate.y] = cardView
        }

        initializeScene()
        positionPlayers()

        val currentLabel = playerOrderTokens[currentState.currentPlayer.color]
        checkNotNull(currentLabel)
        activePlayerArrow.posX = currentLabel.actualPosX - 10

        if (currentState.currentPlayer.type == PlayerType.BOT && game.simulationSpeed >= 0) {
            rootService.botService.calculateTurn()

            playAnimation(DelayAnimation(((game.simulationSpeed + 1) * 1000).roundToInt()).apply {
                onFinished = { makeNextBotMove() }
            })
        }
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
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

        currentState.getTileAt(from).position.neighbours.forEach { neighbour: Coordinate ->
            val neighbourTileView = tileViews.forward(neighbour)
            checkNotNull(neighbourTileView)

            neighbourTileView.apply { isDisabled = true }
        }
        if (currentState.currentPlayer.remainingMoves > 0) {
            currentState.getTileAt(to).position.neighbours.forEach { neighbour: Coordinate ->
                val neighbourTileView = tileViews.forward(neighbour)
                checkNotNull(neighbourTileView)

                neighbourTileView.apply { isDisabled = false }
            }
        }

        if (currentState.currentPlayer.remainingMoves <= 0) {
            playAnimation(DelayAnimation(animationSpeed * 2).apply {
                onFinished = {
                    rootService.gameService.endTurn()
                }
            })
        }
    }

    override fun refreshAfterEndTurn() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        currentState.currentPlayer.position.neighbours.forEach { neighbour: Coordinate ->
            val neighbourTileView = tileViews.forward(neighbour)
            checkNotNull(neighbourTileView)

            neighbourTileView.apply { isDisabled = false }
        }

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
            && rootService.playerActionService.hasValidMove()
        ) {
            rootService.botService.calculateTurn()

            playAnimation(DelayAnimation((game.simulationSpeed * 1000).roundToInt()).apply {
                onFinished = { makeNextBotMove() }
            })
        }
    }

    override fun refreshAfterPlayerDied(player: Player) {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val playerTokenToRemove = playerMainTokens[player.color]
        checkNotNull(playerTokenToRemove)
        playerTokenToRemove.apply { isVisible = false }

        val playerRankToken = playerRankTokens[player.color]
        checkNotNull(playerRankToken)

        when (currentState.players.count { it.alive }) {
            3 -> {
                this.removeComponents(playerRankToken)
                playerRankPanes[3].add(playerRankToken)
                playerRankToken.apply {
                    isVisible = true
                    scale = 0.8
                }
                playerRankToken.posX = 44.0
                playerRankToken.posY = 57.0
                playAnimation(
                    MovementAnimation(
                        playerRankPanes[3],
                        playerRankPanes[3].posX,
                        playerRankPanes[3].posX - 300,
                        playerRankPanes[3].posY,
                        playerRankPanes[3].posY,
                        animationSpeed
                    ).apply {
                        onFinished = {
                            playerRankPanes[3].apply {
                                posX -= 300
                            }
                        }
                    }

                )
            }

            2 -> {
                this.removeComponents(playerRankToken)
                playerRankPanes[2].add(playerRankToken)
                playerRankToken.apply {
                    isVisible = true
                    scale = 0.8
                }
                playerRankToken.posX = 44.0
                playerRankToken.posY = 57.0
                playAnimation(
                    MovementAnimation(
                        playerRankPanes[2],
                        playerRankPanes[2].posX,
                        playerRankPanes[2].posX - 300,
                        playerRankPanes[2].posY,
                        playerRankPanes[2].posY,
                        animationSpeed
                    ).apply {
                        onFinished = {
                            playerRankPanes[2].apply {
                                posX -= 300
                            }
                        }
                    }
                )
            }

            1 -> {
                val winner = playerRankTokens[currentState.players.first { it.alive }.color]
                checkNotNull(winner)

                this.removeComponents(playerRankToken)
                playerRankPanes[1].add(playerRankToken)
                playerRankToken.apply {
                    isVisible = true
                    scale = 0.8
                }
                playerRankToken.posX = 44.0
                playerRankToken.posY = 57.0
                playAnimation(
                    MovementAnimation(
                        playerRankPanes[1],
                        playerRankPanes[1].posX,
                        playerRankPanes[1].posX - 300,
                        playerRankPanes[1].posY,
                        playerRankPanes[1].posY,
                        animationSpeed
                    ).apply {
                        onFinished = {
                            playerRankPanes[1].apply {
                                posX -= 300
                            }
                        }
                    }
                )
                this.removeComponents(winner)
                playerRankPanes[0].add(winner)
                winner.apply {
                    isVisible = true
                    scale = 0.8
                }
                winner.posX = 44.0
                winner.posY = 57.0
                playAnimation(
                    MovementAnimation(
                        playerRankPanes[0],
                        playerRankPanes[0].posX,
                        playerRankPanes[0].posX - 300,
                        playerRankPanes[0].posY,
                        playerRankPanes[0].posY,
                        animationSpeed
                    ).apply {
                        onFinished = {
                            playerRankPanes[0].apply {
                                posX -= 300
                            }
                        }
                    }
                )
            }
        }
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
    }

    //--------------------^ Refreshes ^--------------------

    //--------------------v Helper Functions v--------------------

    fun makeNextBotMove() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentState
        val originalPlayer = gameState.currentPlayer

        playAnimation(DelayAnimation((0.35 * game.simulationSpeed * 1000).roundToInt()).apply {
            onFinished = {
                rootService.botService.makeMove()

                // Move until the player switches.
                if (gameState.currentPlayer == originalPlayer && originalPlayer.remainingMoves > 0) {
                    makeNextBotMove()
                }
            }
        })
    }

    /**
     * Function to initialize the scene with information given after the start of the game.
     */
    private fun initializeScene() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        playerOrderLayout.clear()

        for (player in currentState.players) {
            val playerVisualToAdd = playerOrderTokens[player.color]
            checkNotNull(playerVisualToAdd)
            playerOrderLayout.add(playerVisualToAdd)
        }

        if (currentState.players.size >= 3) {
            playerMainTokens[PlayerColor.YELLOW_CIRCLE]!!.apply { isVisible = true }
            playerOrderLayout.apply { spacing = 56.0 }
        }
        if (currentState.players.size == 4) {
            playerMainTokens[PlayerColor.RED_TRIANGLE]!!.apply { isVisible = true }
            playerOrderLayout.apply { spacing = 48.0 }
        }

        addButtons()

        playerRankPanes.forEach { it.posX = 1920.0 }

        backToMenuButtonPane.posX = 1920.0
    }

    private fun addButtons() {
        buttonPane.addAll(
            Button(
                width = 26.67 * 1.5,
                height = 25 * 1.5,
                posX = 20,
                posY = 12.5,
                visual = ImageVisual("GameScene/undo.png")
            ).apply {
                onMouseClicked = {
                    rootService.playerActionService.undo()
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
                    rootService.playerActionService.redo()
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
                    val game = rootService.currentGame
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
                    val game = rootService.currentGame
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
                    val game = rootService.currentGame
                    checkNotNull(game)
                    game.simulationSpeed = 2.0
                    activeSpeed.posX = this.actualPosX + this.width / 2 - activeSpeed.width / 2
                    animationSpeed = 200
                }
            },
        )
    }

    private fun positionPlayers() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        for (player in currentState.players) {
            playerMainTokens[player.color]!!.posX = getPlayerPosX(player.position)
            playerMainTokens[player.color]!!.posY = getPlayerPosY(player.position)
        }
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
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentTile = tileViews.forward(position)
        checkNotNull(currentTile)

        return currentTile.actualPosX + (currentTile.width - 64) / 2
    }

    private fun getPlayerPosY(position: Coordinate): Double {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val currentState = game.currentState

        val currentTile = tileViews.forward(position)
        checkNotNull(currentTile)

        return currentTile.actualPosY + (currentTile.height - 64) / 2
    }

    //--------------------^ Helper Functions ^--------------------
}