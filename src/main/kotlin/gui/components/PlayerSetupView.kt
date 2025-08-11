package gui.components

import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

class PlayerSetupView(
    posX: Number,
    posY: Number,
    width: Number,
    height: Number,
    val playerId: Int
) :
    Pane<StaticComponentView<*>>(
        posX = posX,
        posY = posY,
        width = width,
        height = height
    ) {

    /** Whether the player for this view would be included in the game if it starts right now. */
    var isIncluded: Boolean = false

    val pawnSize = 120

    private val pawn = Label(
        posX = this.width / 2 - pawnSize / 2,
        posY = 60,
        width = pawnSize,
        height = pawnSize,
        visual = ImageVisual("GameScene/Pawn_P${playerId + 1}.png")
    )

    private val removeButtonSize = 50

    val removeButton = Label(
        posX = this.width - removeButtonSize - 5,
        posY = 5,
        width = removeButtonSize,
        height = removeButtonSize,
        visual = ImageVisual("LobbyScene/Exports/Button_RemovePlayer.png")
    ).apply {
        isVisible = false
    }

    val addButton = Label(
        posX = this.width / 2 - 50,
        posY = 200,
        width = 100,
        height = 56,
        visual = ImageVisual("LobbyScene/Exports/Button_AddPlayer.png")
    ).apply {
        isVisible = false
    }

    val typeSelection = ExclusiveButtonGroup(
        posX = 0,
        posY = 200,
        width = this.width,
        height = 80,
        buttonCount = 2,
        buttonSize = 80,
        spacing = 10,
        imagePaths = listOf(
            "LobbyScene/Exports/Button_PlayerTypes_Local",
            "LobbyScene/Exports/Button_PlayerTypes_Bot"
        ),
        initialSelectedIndex = 0
    ).apply {
        isVisible = false
    }

    val difficultySelection = ExclusiveButtonGroup(
        posX = 0,
        posY = 300,
        width = this.width,
        height = 35,
        buttonCount = 5,
        buttonSize = 35,
        spacing = 3,
        imagePaths = listOf(
            "LobbyScene/Exports/Button_BotDifficulty_Icon",
            "LobbyScene/Exports/Button_BotDifficulty_1",
            "LobbyScene/Exports/Button_BotDifficulty_2",
            "LobbyScene/Exports/Button_BotDifficulty_3",
            "LobbyScene/Exports/Button_BotDifficulty_4"
        ),
        initialSelectedIndex = 3
    ).apply {
        buttons[0].isDisabled = true
        isVisible = false
    }

    init {
        addAll(
            pawn,
            removeButton,
            addButton,
            typeSelection,
            difficultySelection
        )
    }

    fun setIsIncluded(isIncluded: Boolean) {
        this.isIncluded = isIncluded

        visual = if (isIncluded) {
            ImageVisual("LobbyScene/Exports/PlayerGradient_P${playerId + 1}.png")
        } else {
            Visual.EMPTY
        }

        // Re-select human as the default player type. This also hides the difficulty options.
        typeSelection.selectButton(0)
        typeSelection.isVisible = isIncluded
        addButton.isVisible = !isIncluded
        removeButton.isVisible = isIncluded
    }
}