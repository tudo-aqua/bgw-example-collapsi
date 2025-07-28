package view

import tools.aqua.bgw.core.BoardGameApplication

class CollapsiApplication : BoardGameApplication("Callapsi") {

   private val helloScene = HelloScene()

   init {
        this.showGameScene(helloScene)
    }

}

