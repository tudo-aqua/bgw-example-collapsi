package gui.components

import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.visual.ImageVisual

class ExclusiveButtonGroup(
    posX: Number,
    posY: Number,
    width: Number,
    height: Number,
    buttonCount: Int,
    buttonSize: Number,
    spacing: Number,
    val imagePaths: List<String>,
    initialSelectedIndex: Int
) :
    Pane<Label>(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
    ) {

    val buttons = List(buttonCount) { buttonIndex ->
        Label(
            posX = -buttonSize.toDouble() / 2
                    + this.width / 2
                    + (buttonIndex - (buttonCount.toDouble() - 1) / 2) * (buttonSize.toDouble() + spacing.toDouble()),
            posY = 0,
            width = buttonSize,
            height = buttonSize,
            visual = ImageVisual("${imagePaths[buttonIndex]}_Deselected.png")
        ).apply {
            onMouseClicked = { if (selectedIndex != buttonIndex) selectButton(buttonIndex) }
        }
    }

    private var selectedIndex = initialSelectedIndex

    // Not called when the user presses the same button twice.
    var onSelectionChanged: ((Int) -> Unit)? = null

    init {
        require(imagePaths.size == buttonCount) { "Button Count didn't match images supplied." }

        addAll(buttons)

        selectButton(selectedIndex)
    }

    fun selectButton(index: Int) {
        selectedIndex = index

        for ((i, button) in buttons.withIndex()) {
            if (i == selectedIndex) {
                button.visual = ImageVisual("${imagePaths[i]}_Selected.png")
            } else {
                button.visual = ImageVisual("${imagePaths[i]}_Deselected.png")
            }
        }

        onSelectionChanged?.invoke(selectedIndex)
    }
}