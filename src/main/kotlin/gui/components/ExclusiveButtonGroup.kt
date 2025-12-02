package gui.components

import gui.CollapsiApplication
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.visual.ImageVisual

/**
 * Custom component for a set of buttons where exactly one of them needs to be selected at all times.
 *
 * @param posX The x position of the component.
 * @param posY The y position of the component.
 * @param width The width of the component.
 * @param height The height of the component.
 * @param buttonSize The width/height of the buttons. Buttons are always square.
 * @param spacing The spacing between the individual buttons.
 * @param imagePaths The images used for the buttons.
 * Path must lead to two images. Once when adding "_Selected.png" and one when adding "_Deselected.png".
 * @param initialSelectedIndex The index of the initially selected button.
 * @param app A reference to the primary [CollapsiApplication].
 */
class ExclusiveButtonGroup(
    posX: Number,
    posY: Number,
    width: Number,
    height: Number,
    buttonSize: Number,
    spacing: Number,
    val imagePaths: List<String>,
    initialSelectedIndex: Int,
    app: CollapsiApplication
) :
    Pane<Label>(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
    ) {

    /**
     * Amount of buttons that are shown.
     */
    val buttonCount get() = imagePaths.size

    /**
     * The buttons that are in this group.
     */
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
            // Only call selectButton if a new button was selected.
            onMouseClicked = {
                if (selectedIndex != buttonIndex) {
                    selectButton(buttonIndex)
                    app.playSound(app.clickSfx)
                }
            }
        }
    }

    /**
     * The index of the button that is currently selected.
     */
    var selectedIndex = initialSelectedIndex
        private set

    /**
     * A custom function that is called whenever the selection changes.
     *
     * This function is not triggered if the same button is pressed twice.
     *
     * The given value is the index of the newly selected button.
     */
    var onSelectionChanged: ((Int) -> Unit)? = null

    init {
        require(imagePaths.size == buttonCount) { "Button Count didn't match images supplied." }

        addAll(buttons)

        selectButton(selectedIndex)
    }

    /**
     * Selects the given button.
     *
     * Updates the images and calls [onSelectionChanged].
     *
     * @param index The index of the button to select.
     */
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