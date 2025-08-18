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
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.DEFAULT_LINEAR_LAYOUT_SPACING
import tools.aqua.bgw.visual.*
import kotlin.math.roundToInt

class GameScene(
    val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val playTiles = mutableMapOf<Tile, CardView>()

    private val players = mutableMapOf<Player, TokenView>()

    private val stepTokenList = mutableListOf<TokenView>()

    private val activePlayerLabel = mutableMapOf<PlayerColor, TokenView>()


    //--------------------v Left Info Pane v--------------------

    private val infoPane = Pane<ComponentView>(
        width = 540,
        height = 1080,
        visual = ImageVisual("GameScene/GameHudBackground.png"),
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    private val playerLine = LinearLayout<TokenView>(
        width = 460,
        height = 128,
        posX = 40,
        posY = 144,
        alignment = Alignment.CENTER,
        spacing = 40,
    )

    private val greenPlayerVisual = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P1.png")
    )

    private val orangePlayerVisual = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P2.png")
    )

    private val yellowPlayerVisual = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P3.png")
    )

    private val redPlayerVisual = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P4.png")
    )

    private val activePlayer = Label(
        width = 84,
        height = 116,
        posY = 170,
        visual = ImageVisual("GameScene/CurrentPlayerArrow.png")
    )

    private val stepTokenLine = LinearLayout<TokenView>(
        width = 460,
        height = 128,
        posX = 40,
        posY = 308,
        alignment = Alignment.CENTER,
        spacing = 40,
    )

    //--------------------^ Left Info Pane ^--------------------

    //--------------------v Player Tokens v--------------------

    private val greenPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P1.png")
    )

    private val orangePlayer = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P2.png")
    )

    private val yellowPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P3.png")
    ).apply {
        isVisible = false
    }

    private val redPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ImageVisual("GameScene/Pawn_P4.png")
    ).apply {
        isVisible = false
    }

    //--------------------^ Player Tokens ^--------------------

    private val playContainer = Pane<ComponentView>(
        width = 784,
        height = 784,
        posX = 660,
        posY = 148,
        //visual = ColorVisual.BLACK
    )

    init {
        background = ImageVisual("gameScene/Background.png")

        infoPane.addAll(activePlayer, playerLine, stepTokenLine)

        addComponents(playContainer, infoPane)
    }

    //--------------------v Refreshes v--------------------

    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        playTiles.clear()

        val playArea = GridPane<ComponentView>(
            posX = playContainer.width / 2,
            posY = playContainer.height / 2,
            rows = currentState.boardSize,
            columns = currentState.boardSize,
            spacing = 20,
            //visual = ColorVisual.LIGHT_GRAY
        ).apply {
            if (currentState.boardSize == 5) {
                spacing = 15.0
            } else if (currentState.boardSize == 6) {
                spacing = 10.0
            }
        }

        players.clear()

        players[currentState.players.first { it.color == PlayerColor.GREEN_SQUARE }] = greenPlayer
        players[currentState.players.first { it.color == PlayerColor.ORANGE_HEXAGON }] = orangePlayer
        if (currentState.players.size >= 3) {
            players[currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }] = yellowPlayer
        }
        if (currentState.players.size == 4) {
            players[currentState.players.first { it.color == PlayerColor.RED_TRIANGLE }] = redPlayer
        }
        playContainer.add(playArea)
        addComponents(greenPlayer, orangePlayer, yellowPlayer, redPlayer)

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
                        Coordinate(coordinate.x, coordinate.y, currentState.boardSize)
                    )
                }
            }

            activePlayerLabel.clear()

            currentState.players.forEach {
                when (it.color) {
                    PlayerColor.GREEN_SQUARE -> activePlayerLabel[PlayerColor.GREEN_SQUARE] = greenPlayerVisual
                    PlayerColor.ORANGE_HEXAGON -> activePlayerLabel[PlayerColor.ORANGE_HEXAGON] = orangePlayerVisual
                    PlayerColor.YELLOW_CIRCLE -> activePlayerLabel[PlayerColor.YELLOW_CIRCLE] = yellowPlayerVisual
                    PlayerColor.RED_TRIANGLE -> activePlayerLabel[PlayerColor.RED_TRIANGLE] = redPlayerVisual
                }
            }

            playTiles[tile] = cardView
            playArea[coordinate.x, coordinate.y] = cardView
        }

        initializeScene()
        positionPlayers()

        val currentLabel = activePlayerLabel[currentState.currentPlayer.color]
        checkNotNull(currentLabel)
        activePlayer.posX = currentLabel.actualPosX - 10

        if (currentState.currentPlayer.type == PlayerType.BOT && game.simulationSpeed >= 0) {
            rootService.botService.calculateTurn()

            playAnimation(DelayAnimation(((game.simulationSpeed + 1) * 1000).roundToInt()).apply {
                onFinished = { makeNextBotMove() }
            })
        }
    }

    override fun refreshAfterMoveTo(from: Coordinate, to: Coordinate) {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        val playerTokenToMove = players.filter { it.key.color == currentState.currentPlayer.color }.values.firstOrNull()
        checkNotNull(playerTokenToMove)

        playAnimation(
            MovementAnimation(
                playerTokenToMove,
                getPlayerPosX(from),
                getPlayerPosX(to),
                getPlayerPosY(from),
                getPlayerPosY(to),
                500
            ).apply {
                onFinished = {
                    playerTokenToMove.apply {
                        posX = getPlayerPosX(to)
                        posY = getPlayerPosY(to)
                    }
                }
            })

        if (currentState.currentPlayer.visitedTiles.size == 1) {
            val collapsedTileView = playTiles[currentState.getTileAt(from)]
            checkNotNull(collapsedTileView)
            collapsedTileView.apply {
                playAnimation(
                    FlipAnimation(
                        collapsedTileView,
                        collapsedTileView.frontVisual,
                        collapsedTileView.backVisual,
                        500
                    ).apply {
                        onFinished = {
                            collapsedTileView.showBack()
                        }
                    })
            }
        }

        val stepToken = stepTokenList[currentState.currentPlayer.remainingMoves]
        playAnimation(
            MovementAnimation(
                stepToken,
                stepToken.actualPosX,
                getPlayerPosX(from),
                stepToken.actualPosY,
                getPlayerPosY(from),
                500
            ).apply {
                onFinished = {
                    stepToken.offset(
                        getPlayerPosX(from) - stepToken.actualPosX,
                        getPlayerPosY(from) - stepToken.actualPosY
                    )
                }
            })

        currentState.getTileAt(from).position.neighbours.forEach { neighbour ->
            val neighbourTileView = playTiles[currentState.getTileAt(neighbour)]
            checkNotNull(neighbourTileView)

            neighbourTileView.apply { isDisabled = true }
        }
        if (currentState.currentPlayer.remainingMoves > 0) {
            currentState.getTileAt(to).position.neighbours.forEach { neighbour ->
                val neighbourTileView = playTiles[currentState.getTileAt(neighbour)]
                checkNotNull(neighbourTileView)

                neighbourTileView.apply { isDisabled = false }
            }
        }

        if (currentState.currentPlayer.remainingMoves <= 0) {
            playAnimation(DelayAnimation(1000).apply {
                onFinished = {
                    rootService.gameService.endTurn()
                }
            })
        }
    }

    override fun refreshAfterEndTurn() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        currentState.currentPlayer.position.neighbours.forEach { neighbour ->
            val neighbourTileView = playTiles[currentState.getTileAt(neighbour)]
            checkNotNull(neighbourTileView)

            neighbourTileView.apply { isDisabled = false }
        }

        val currentLabel = activePlayerLabel[currentState.currentPlayer.color]
        checkNotNull(currentLabel)
        activePlayer.posX = currentLabel.actualPosX - 10

        stepTokenList.forEach {
            it.isVisible = false
            it.posX = 0.0
            it.posY = 0.0
        }
        stepTokenLine.clear()

        for (i in 0 until currentState.currentPlayer.remainingMoves) {
            stepTokenList[i].apply {
                isVisible = true
            }
            stepTokenLine.add(stepTokenList[i])
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

    //--------------------^ Refreshes ^--------------------

    //--------------------v Helper Functions v--------------------

    fun makeNextBotMove() {
        val game = checkNotNull(rootService.currentGame) { "No game is currently running." }
        val gameState = game.currentGame
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
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        for (player in currentState.players) {
            val playerVisualToAdd = activePlayerLabel[player.color]
            checkNotNull(playerVisualToAdd)
            playerLine.add(playerVisualToAdd)
        }

        if (currentState.players.size >= 3) {
            yellowPlayer.apply { isVisible = true }
            playerLine.apply { spacing = 56.0 }
        }
        if (currentState.players.size == 4) {
            redPlayer.apply { isVisible = true }
            playerLine.apply { spacing = 48.0 }
        }

        repeat(4) {
            val stepToken = TokenView(
                width = 64,
                height = 64,
                visual = ImageVisual("GameScene/StepToken.png")
            ).apply {
                isVisible = false
            }
            stepTokenList.add(stepToken)
        }
        stepTokenLine.add(stepTokenList[0].apply { isVisible = true })
    }

    private fun positionPlayers() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        // Todo: Create getPlayerByColor function somewhere. Or maybe a player to color bi-map?
        greenPlayer.posX = getPlayerPosX(
            currentState.players.first { it.color == PlayerColor.GREEN_SQUARE }.position
        )
        greenPlayer.posY = getPlayerPosY(
            currentState.players.first { it.color == PlayerColor.GREEN_SQUARE }.position
        )
        orangePlayer.posX = getPlayerPosX(
            currentState.players.first { it.color == PlayerColor.ORANGE_HEXAGON }.position
        )
        orangePlayer.posY = getPlayerPosY(
            currentState.players.first { it.color == PlayerColor.ORANGE_HEXAGON }.position
        )

        if (currentState.players.size >= 3) {
            yellowPlayer.posX = getPlayerPosX(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
            )
            yellowPlayer.posY = getPlayerPosY(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
            )
        }
        if (currentState.players.size == 4) {
            redPlayer.posX = getPlayerPosX(
                currentState.players.first { it.color == PlayerColor.RED_TRIANGLE }.position
            )
            redPlayer.posY = getPlayerPosY(
                currentState.players.first { it.color == PlayerColor.RED_TRIANGLE }.position
            )
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
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        val currentTile = playTiles[currentState.getTileAt(position)]
        checkNotNull(currentTile)

        return currentTile.actualPosX + (currentTile.width - 64) / 2
    }

    private fun getPlayerPosY(position: Coordinate): Double {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        val currentTile = playTiles[currentState.getTileAt(position)]
        checkNotNull(currentTile)

        return currentTile.actualPosY + (currentTile.height - 64) / 2
    }

    //--------------------^ Helper Functions ^--------------------
}