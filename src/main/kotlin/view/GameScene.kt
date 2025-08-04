package view

import entity.Coordinate
import entity.Player
import entity.PlayerColor
import entity.Tile
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.reposition
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.TextVisual

class GameScene(
    val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val playTiles = mutableMapOf<Tile, CardView>()

    private val players = mutableMapOf<Player, TokenView>()

    private val infoLabel = Label(
        width = 480,
        height = 480,
        visual = ColorVisual.BLACK
    )

    private val greenPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ColorVisual.BLACK
    )

    private val orangePlayer = TokenView(
        width = 64,
        height = 64,
        visual = ColorVisual.LIGHT_GRAY
    )

    private val yellowPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ColorVisual.BLACK
    )

    private val redPlayer = TokenView(
        width = 64,
        height = 64,
        visual = ColorVisual.BLACK
    )

    /*
    private val playerPane = Pane<ComponentView>(
        width = 480,
        height = 128,
        posX = 0,
        posY = 64,
    )
     */

    init {
        background = ColorVisual.DARK_GRAY

        addComponents(infoLabel)
    }

    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentState = game.currentGame

        players.clear()

        players[currentState.players.first { it.color == PlayerColor.GREEN_SQUARE }] = greenPlayer
        players[currentState.players.first { it.color == PlayerColor.ORANGE_HEXAGON }] = orangePlayer
        if (currentState.players.size >= 3) {
            players[currentState.players.first { it.color == PlayerColor.YELLOW_CIRCLE }] = yellowPlayer
        }
        if (currentState.players.size == 4) {
            players[currentState.players.first { it.color == PlayerColor.RED_TRIANGLE }] = redPlayer
        }

        playTiles.clear()

        val playArea = GridPane<ComponentView>(
            posX = 610,
            posY = 220,
            rows = currentState.boardSize,
            columns = currentState.boardSize,
            spacing = 20,
            visual = ColorVisual.LIGHT_GRAY
        )

        addComponents(playArea, greenPlayer, orangePlayer, yellowPlayer, redPlayer)

        currentState.board.forEach { (coordinate: Coordinate, tile: Tile) ->
            val startingColor : ColorVisual = when(
                tile.startTileColor
            ) {
                PlayerColor.GREEN_SQUARE -> ColorVisual.GREEN
                PlayerColor.ORANGE_HEXAGON -> ColorVisual.ORANGE
                PlayerColor.YELLOW_CIRCLE -> ColorVisual.YELLOW
                PlayerColor.RED_TRIANGLE -> ColorVisual.RED
                else -> ColorVisual.GRAY
            }

            val cardView = CardView(
                width = 160,
                height = 160,
                posX = 0,
                posY = 0,
                front = CompoundVisual(
                    startingColor,
                    TextVisual("${tile.movesToMake}")
                ),
                back = ColorVisual.RED
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
        else {
            currentState.players[(currentState.currentPlayerIndex + 1) % currentState.players.size].position.neighbours.forEach { neighbour ->
                val neighbourTileView = playTiles[currentState.getTileAt(neighbour)]
                checkNotNull(neighbourTileView)

                neighbourTileView.apply { isDisabled = false }
            }
        }
    }

    private fun getPlayerPosX(posX : Int) : Int {
        return when (posX) {
            0 -> 658
            1 -> 838
            2 -> 1018
            3 -> 1198
            else -> throw IllegalArgumentException("Invalid player position X: $posX")
        }
    }

    private fun getPlayerPosY(posY : Int) : Int {
        return when (posY) {
            0 -> 232
            1 -> 412
            2 -> 592
            3 -> 772
            else -> throw IllegalArgumentException("Invalid player position X: $posY")
        }
    }
}