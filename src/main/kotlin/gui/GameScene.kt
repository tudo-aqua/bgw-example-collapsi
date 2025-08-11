package gui

import entity.Coordinate
import entity.Player
import entity.PlayerColor
import entity.Tile
import service.RootService
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.*
import javax.swing.text.Position

class GameScene(
    val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val playTiles = mutableMapOf<Tile, CardView>()

    private val players = mutableMapOf<Player, TokenView>()

    private val stepTokenList = mutableListOf<TokenView>()

    private val activePlayerLabel = mutableMapOf<PlayerColor, TokenView>()


    //--------------------v Left Info Pane v--------------------

    private val infoPane = Pane<ComponentView>(
        width = 480,
        height = 1080,
        visual = ImageVisual("GameScene/GameHudBackground.png"),
    ).apply {
        isFocusable = false
        isDisabled = true
    }

    private val playerLine = LinearLayout<TokenView>(
        width = 480,
        height = 128,
        posX = 0,
        posY = 64,
        alignment = Alignment.CENTER,
        spacing = 64,
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
        //posX = 134,
        posY = 90,
        visual = ImageVisual("GameScene/CurrentPlayerArrow.png")
    )

    private val stepTokenLine = LinearLayout<TokenView>(
        width = 480,
        height = 128,
        posX = 0,
        posY = 244,
        alignment = Alignment.CENTER,
        spacing = 48,
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
        width = 720,
        height = 720,
        posX = 660,
        posY = 190,
        //visual = ColorVisual.BLACK
    )

    init {
        background = ImageVisual("gameScene/Background.png")

        infoPane.addAll(activePlayer, playerLine, stepTokenLine)
        playerLine.addAll(greenPlayerVisual, orangePlayerVisual)

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
            val startingColor : ImageVisual = when (tile.startTileColor) {
                PlayerColor.GREEN_SQUARE -> ImageVisual("GameScene/Tile_P1.png")
                PlayerColor.ORANGE_HEXAGON -> ImageVisual("GameScene/Tile_P2.png")
                PlayerColor.YELLOW_CIRCLE -> ImageVisual("GameScene/Tile_P3.png")
                PlayerColor.RED_TRIANGLE -> ImageVisual("GameScene/Tile_P4.png")
                else -> getTileVisual(tile.movesToMake)
            }

            val cardView = CardView(
                width = 160,
                height = 160,
                posX = 0,
                posY = 0,
                front = startingColor,
                back = ImageVisual("GameScene/Tile_Collapsed.png")
            ).apply {
                if (currentState.boardSize == 5) {
                    width = 128.0
                    height = 128.0
                } else if (currentState.boardSize == 6) {
                    width = 108.33
                    height = 108.33
                }
                if ( !tile.collapsed) showFront()
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
    }

    override fun refreshAfterMoveTo(from : Coordinate, to: Coordinate) {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        val playerTokenToMove = players.filter { it.key.color == currentState.currentPlayer.color }.values.firstOrNull()
        checkNotNull(playerTokenToMove)

        playerTokenToMove.apply {
            posX = getPlayerPosX(to)
            posY = getPlayerPosY(to)
        }

        if (currentState.currentPlayer.visitedTiles.size == 1) {
            val collapsedTileView = playTiles[currentState.getTileAt(from)]
            checkNotNull(collapsedTileView)
            collapsedTileView.apply { showBack() }
        }

        val stepToken = stepTokenList[currentState.currentPlayer.remainingMoves]
        stepToken.offset(getPlayerPosX(from) - stepToken.actualPosX, getPlayerPosY(from) - stepToken.actualPosY)

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
    }

    //--------------------^ Refreshes ^--------------------

    //--------------------v Helper Functions v--------------------

    /**
     * Function to initialize the scene with information given after the start of the game.
     */
    private fun initializeScene() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        if (currentState.players.size >= 3) {
            playerLine.add(yellowPlayerVisual)
            playerLine.apply { spacing = 56.0 }
        }
        if (currentState.players.size == 4) {
            playerLine.add(redPlayerVisual)
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

        if (currentState.players.size == 3) {
            yellowPlayer.posX = getPlayerPosX(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
            )
            yellowPlayer.posY = getPlayerPosY(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
            )
        } else if (currentState.players.size == 4) {
            yellowPlayer.posX = getPlayerPosX(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
            )
            yellowPlayer.posY = getPlayerPosY(
                currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }.position
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