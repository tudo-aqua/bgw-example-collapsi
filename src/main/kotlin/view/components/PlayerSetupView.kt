package view.components

import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.visual.ImageVisual

class PlayerSetupView(posX: Number, posY: Number, width: Number, height: Number, playerId: Int) :
    Pane<StaticComponentView<*>>(
        posX, posY, width, height,
        visual = ImageVisual("LobbyScene/Exports/PlayerGradient_P${playerId + 1}.png")
    ) {

    val pawnSize = 120

    private val pawn = Label(
        posX = this.width / 2 - pawnSize / 2,
        posY = 60,
        width = pawnSize,
        height = pawnSize,
        visual = ImageVisual("GameScene/Pawn_P${playerId + 1}.png")
    )

    val removeButtonSize = 50

    private val removeButton = Button(
        posX = this.width - removeButtonSize - 5,
        posY = 5,
        width = removeButtonSize,
        height = removeButtonSize,
        visual = ImageVisual("LobbyScene/Exports/Button_RemovePlayer.png")
    )

    init {
        addAll(
            pawn,
            removeButton
        )
    }
}