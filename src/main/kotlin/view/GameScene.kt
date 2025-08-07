package view

import entity.Coordinate
import entity.Player
import entity.PlayerColor
import entity.Tile
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.reposition
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.*

class GameScene(
    val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val playTiles = mutableMapOf<Tile, CardView>()

    private val players = mutableMapOf<Player, TokenView>()

    private val stepTokenList = mutableListOf<TokenView>()


    //--------------------v Left Info Pane v--------------------

    private val infoPane = Pane<ComponentView>(
        width = 480,
        height = 480,
        visual = ColorVisual.BLACK
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
        visual = ColorVisual.DARK_GRAY
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

    private val stepTokenLine = LinearLayout<TokenView>(
        width = 480,
        height = 128,
        posX = 0,
        posY = 244,
        alignment = Alignment.CENTER,
        spacing = 48,
        visual = ColorVisual.DARK_GRAY
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
        visual = ColorVisual.BLACK
    )

    init {
        background = ColorVisual.DARK_GRAY

        infoPane.addAll(playerLine, stepTokenLine)
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
            posX = 0,
            posY = 0,
            rows = currentState.boardSize,
            columns = currentState.boardSize,
            spacing = 20,
            visual = ColorVisual.LIGHT_GRAY
        )

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
                showFront()
                if(!currentState.currentPlayer.position.neighbours.contains(coordinate)) {
                    isDisabled = true
                }
                onMouseClicked = {
                    rootService.playerActionService.moveTo(
                        Coordinate(coordinate.x, coordinate.y, currentState.boardSize)
                    )
                }
            }

            playTiles[tile] = cardView
            playArea[coordinate.x, coordinate.y] = cardView
        }

        initializeScene()
        positionPlayers()
    }

    override fun refreshAfterMoveTo(from : Coordinate, to: Coordinate) {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        val playerTokenToMove = players.filter { it.key.color == currentState.currentPlayer.color }.values.firstOrNull()
        checkNotNull(playerTokenToMove)

        playerTokenToMove.apply {
            posX = getPlayerPosX(to.x).toDouble()
            posY = getPlayerPosY(to.y).toDouble()
        }

        if(currentState.currentPlayer.visitedTiles.size == 1) {
            val collapsedTileView = playTiles[currentState.getTileAt(from)]
            checkNotNull(collapsedTileView)
            collapsedTileView.apply { showBack() }
        }

        val stepToken = stepTokenList[currentState.currentPlayer.remainingMoves]
        stepToken.apply {
            posX = getPlayerPosX(from.x).toDouble()
            posY = getPlayerPosY(from.y).toDouble() - 238
        }

        currentState.getTileAt(from).position.neighbours.forEach { neighbour ->
            val neighbourTileView = playTiles[currentState.getTileAt(neighbour)]
            checkNotNull(neighbourTileView)

            neighbourTileView.apply { isDisabled = true }
        }
        if(currentState.currentPlayer.remainingMoves > 0) {
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

        stepTokenList.forEach {
            it.isVisible = false
            it.posX = 0.0
            it.posY = 0.0
        }
        stepTokenLine.clear()

        for(i in 0 until currentState.currentPlayer.remainingMoves) {
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

        greenPlayer.posX = getPlayerPosX(currentState.players.first{ it.color == PlayerColor.GREEN_SQUARE }.position.x).toDouble()
        greenPlayer.posY = getPlayerPosY(currentState.players.first{ it.color == PlayerColor.GREEN_SQUARE }.position.y).toDouble()
        orangePlayer.posX = getPlayerPosX(currentState.players.first{ it.color == PlayerColor.ORANGE_HEXAGON }.position.x).toDouble()
        orangePlayer.posY = getPlayerPosY(currentState.players.first{ it.color == PlayerColor.ORANGE_HEXAGON }.position.y).toDouble()
        if(currentState.players.size == 3) {
            yellowPlayer.posX = getPlayerPosX(currentState.players.first{ it.color == PlayerColor.YELLOW_CIRCLE }.position.x).toDouble()
            yellowPlayer.posY = getPlayerPosY(currentState.players.first{ it.color == PlayerColor.YELLOW_CIRCLE }.position.y).toDouble()
        } else if(currentState.players.size == 4) {
            yellowPlayer.posX = getPlayerPosX(currentState.players.first{ it.color == PlayerColor.YELLOW_CIRCLE }.position.x).toDouble()
            yellowPlayer.posY = getPlayerPosY(currentState.players.first{ it.color == PlayerColor.YELLOW_CIRCLE }.position.y).toDouble()
        }
    }

    private fun getTileVisual(movesOnTile: Int) : ImageVisual {
        return when (movesOnTile) {
            1 -> ImageVisual("GameScene/Tile_1.png")
            2 -> ImageVisual("GameScene/Tile_2.png")
            3 -> ImageVisual("GameScene/Tile_3.png")
            4 -> ImageVisual("GameScene/Tile_4.png")
            else -> throw IllegalArgumentException("Invalid number of moves on tile: $movesOnTile")
        }
    }

    private fun getPlayerPosX(posX : Int) : Int {
        return when (posX) {
            0 -> 660 + 48
            1 -> 660 + posX * 160 + 20 + 48
            2 -> 660 + posX * 160 + 40 + 48
            3 -> 660 + posX * 160 + 60 + 48
            else -> throw IllegalArgumentException("Invalid player position X: $posX")
        }
    }

    private fun getPlayerPosY(posY : Int) : Int {
        return when (posY) {
            0 -> 190 + 48
            1 -> 190 + posY * 160 + 20 + 48
            2 -> 190 + posY * 160 + 40 + 48
            3 -> 190 + posY * 160 + 60 + 48
            else -> throw IllegalArgumentException("Invalid player position Y: $posY")
        }
    }

    //--------------------^ Helper Functions ^--------------------
}