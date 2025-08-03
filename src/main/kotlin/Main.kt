import entity.PlayerType
import service.RootService
import view.CollapsiApplication

fun main() {
    // val root = RootService()
    // root.gameService.startNewGame(listOf(PlayerType.BOT_HARD, PlayerType.BOT_EASY), 4)

    CollapsiApplication().show()
    
    println("Application ended. Goodbye")
}