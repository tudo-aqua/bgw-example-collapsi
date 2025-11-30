package gui.components

import gui.CollapsiApplication
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * Custom component for the player view in the [gui.LobbyScene].
 *
 * Contains the player pawn as well as buttons for player type and bot difficulty.
 *
 * @param posX The x position of the component.
 * @param posY The y position of the component.
 * @param width The width of the component.
 * @param height The height of the component.
 * @param playerId The index of the player this view is for. 0-3.
 */
class PlayerSetupView(
    posX: Number,
    posY: Number,
    width: Number,
    height: Number,
    val playerId: Int,
    app: CollapsiApplication
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

    var remotePlayer = false

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
        visual = ImageVisual("lobbyScene/Button_RemovePlayer.png")
    ).apply {
        isVisible = false
    }

    val addButton = Label(
        posX = this.width / 2 - 50,
        posY = 200,
        width = 100,
        height = 56,
        visual = ImageVisual("lobbyScene/Button_AddPlayer.png")
    ).apply {
        isVisible = false
    }

    val typeSelection = ExclusiveButtonGroup(
        posX = 0,
        posY = 200,
        width = this.width,
        height = 80,
        buttonSize = 80,
        spacing = 10,
        imagePaths = listOf(
            "lobbyScene/Button_PlayerTypes_Local",
            "lobbyScene/Button_PlayerTypes_Bot"
        ),
        initialSelectedIndex = 0,
        app
    ).apply {
        isVisible = false
    }

    val remoteType = Label(
        posX = this.width / 2 - 80 / 2,
        posY = 200,
        width = 80,
        height = 80,
        visual = ImageVisual("lobbyScene/Button_PlayerTypes_Remote_Selected.png")
    ).apply {
        isVisible = false
    }

    val difficultySelection = ExclusiveButtonGroup(
        posX = 0,
        posY = 300,
        width = this.width,
        height = 35,
        buttonSize = 35,
        spacing = 3,
        imagePaths = listOf(
            "lobbyScene/Button_BotDifficulty_Icon",
            "lobbyScene/Button_BotDifficulty_1",
            "lobbyScene/Button_BotDifficulty_2",
            "lobbyScene/Button_BotDifficulty_3",
            "lobbyScene/Button_BotDifficulty_4"
        ),
        initialSelectedIndex = 3,
        app
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
            remoteType,
            difficultySelection
        )
    }

    /**
     * Will change whether this player is currently displayed as being included in the upcoming game.
     *
     * Only included players have the gradient behind them and can have their settings edited.
     *
     * @param isIncluded The new value for [PlayerSetupView.isIncluded].
     */
    fun setIsIncluded(isIncluded: Boolean) {
        if (this.isIncluded == isIncluded)
            return

        this.isIncluded = isIncluded

        visual = if (isIncluded) {
            ImageVisual("LobbyScene/PlayerGradient_P${playerId + 1}.png")
        } else {
            Visual.EMPTY
        }

        // Re-select human as the default player type. This also hides the difficulty options.
        typeSelection.selectButton(0)
        typeSelection.isVisible = isIncluded && !remotePlayer
        remoteType.isVisible = isIncluded && remotePlayer
        addButton.isVisible = !isIncluded && playerId >= 2
        removeButton.isVisible = isIncluded && playerId >= 2
        difficultySelection.isVisible = isIncluded && typeSelection.selectedIndex == 1
    }
}