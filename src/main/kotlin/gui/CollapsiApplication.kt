package gui

import service.*
import service.network.ConnectionState
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.util.Font
import java.io.BufferedInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

/**
 * Implementation of the BGW [BoardGameApplication] for the game "Collapsi".
 */
class CollapsiApplication : BoardGameApplication("Collapsi"), Refreshable {

    private val root = RootService()

    val mainMenuScene: MainMenuScene = MainMenuScene(this, root)

    val lobbyScene = LobbyScene(this, root)

    val hostOnlineLobbyScene = HostOnlineLobbyScene(this, root)

    val joinOnlineLobbyScene = JoinOnlineLobbyScene(this, root)

    val waitingForHostScene = WaitingForHostScene(this, root)

    val gameScene = GameScene(this, root)

    private val consoleRefreshable = ConsoleRefreshable(root)

    val clickSfx = listOf(
        "audio/ui/Click1.ogg",
        "audio/ui/Click2.ogg",
        "audio/ui/Click3.ogg"
    )

    init {
        loadFont("fonts/RussoOne-Regular.ttf", "RussoOne", Font.FontWeight.NORMAL)

        root.addRefreshables(
            this,
            lobbyScene,
            gameScene,
            consoleRefreshable,
            hostOnlineLobbyScene,
            joinOnlineLobbyScene
        )

        showGameScene(gameScene)
        showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(500)
    }

    override fun refreshAfterLoad() {
        hideMenuScene(500)
    }

    override fun refreshAfterConnectionStateChange(newState: ConnectionState) {
        if (newState == ConnectionState.DISCONNECTED) {
            showMenuScene(mainMenuScene)
        }
    }

    /**
     * Plays a sound effect randomly from the provided paths. Ogg files recommended.
     *
     * @param paths The paths to the sound effects. One of them will be chosen at random.
     *
     * @throws IllegalArgumentException If the provided paths are empty.
     */
    fun playSound(paths: List<String>) {
        require(paths.isNotEmpty()) { "No sounds provided." }

        val randomIndex = (0 until paths.size).random()
        playSound(paths[randomIndex])
    }

    /**
     * Plays a sound effect from the provided path. Ogg files recommended.
     *
     * @param path The path to the sound effect.
     *
     * @throws IllegalArgumentException If the provided path does not lead to a valid sound effect file.
     */
    fun playSound(path: String) {
        Thread {
            val inputStream = this::class.java.classLoader.getResourceAsStream(path)
            checkNotNull(inputStream) { "Sound not found at path: $path." }

            BufferedInputStream(inputStream).use { input ->
                val baseStream = AudioSystem.getAudioInputStream(input)
                val baseFormat = baseStream.format

                // Convert to PCM format so the AudioSystem can handle it.
                val decodedFormat = AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.sampleRate,
                    16,
                    baseFormat.channels,
                    baseFormat.channels * 2,
                    baseFormat.sampleRate,
                    baseFormat.isBigEndian
                )

                val decodedStream = AudioSystem.getAudioInputStream(decodedFormat, baseStream)
                val clip = AudioSystem.getClip()
                clip.open(decodedStream)
                clip.start()
            }
        }.start()
    }
}

