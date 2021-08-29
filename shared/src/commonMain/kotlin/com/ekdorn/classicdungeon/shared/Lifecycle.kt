package com.ekdorn.classicdungeon.shared

import com.ekdorn.classicdungeon.shared.gl.wrapper.GLFunctions
import com.ekdorn.classicdungeon.shared.engine.Game
import com.ekdorn.classicdungeon.shared.engine.generics.Assigned
import com.ekdorn.classicdungeon.shared.engine.generics.TextureCache
import com.ekdorn.classicdungeon.shared.engine.utils.Event
import kotlinx.coroutines.*


object Lifecycle {
    internal val onResume = Event<Unit>()
    internal val onPause = Event<Unit>()

    val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Initializing features (blocking MAIN thread), needed for application to start (e.g. display metrics, GL surface, UI textures, etc.)
     * @throws com.ekdorn.classicdungeon.shared.engine.ResourceNotFoundException if any resources are not loaded.
     */
    suspend fun start (width: Int, height: Int) {
        Input.onResized.add {
            GLFunctions.portal(it.w, it.h)
            false
        }

        GLFunctions.setup()
        Assigned.assigned.forEach { it.gameStarted() }
        Input.onResized(width, height)

        TextureCache.init("notex")
        Game.splash(width, height)
        Game.update()

        awaitAll(scope.async { delay(2000) }, scope.async {
            TextureCache.load("font", "chrome")
            TextureCache.loadAtlas("bee", List(16) { it }, 16)
        })
        Game.start()
    }

    /**
     * Resuming game process async (e.g. sound, time measuring, etc.)
     */
    fun resume () {
        Game.resume()
        onResume.fire(Unit)
    }

    /**
     * Screen update, runs each frame in GL thread sync
     */
    fun update () {
        Game.update()
    }

    /**
     * Pausing game processes async (e.g. sound, time measuring, etc.)
     */
    fun pause () {
        Game.pause()
        onPause.fire(Unit)
    }

    /**
     * Ceasing game functionality (without ability to resume, in MAIN thread) (e.g. freeing resources, auto-saving, closing server connection, etc.)
     */
    fun end () {
        Game.end()
        Assigned.assigned.forEach { it.gameEnded() }
    }
}
