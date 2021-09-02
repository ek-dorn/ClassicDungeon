package com.ekdorn.classicdungeon.shared.engine.cache

import com.ekdorn.classicdungeon.shared.engine.general.ResourceLoader
import com.ekdorn.classicdungeon.shared.engine.ui.*
import com.ekdorn.classicdungeon.shared.engine.ui.BackgroundUI
import com.ekdorn.classicdungeon.shared.engine.ui.FrameUI
import com.ekdorn.classicdungeon.shared.engine.ui.LayoutUI
import com.ekdorn.classicdungeon.shared.engine.ui.WidgetUI
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*


// Layout file management
// UI pixelization in camera depending on screen ratio
// TODO: deal with NO_LAYOUT
internal object Transcender {
    private object UI {
        val UIs = mapOf(
            "BackgroundUI" to ::BackgroundUI,
            "ClipUI" to ::ClipUI,
            "FrameUI" to ::FrameUI,
            "ImageUI" to ::ImageUI,
            "LayoutUI" to ::LayoutUI,
            "TextUI" to ::TextUI
        )
    }

    @Serializable
    private data class WidgetHolder (private val cls: String, val id: String, private val value: @Contextual JsonObject) {
        private val children: List<WidgetHolder>? = null

        private val background: @Contextual JsonObject? = null


        @Transient private var widget = map(value)!!

        @Transient private var back = map(background)

        private fun map (map: JsonObject?) = map?.mapValues {
            if (it.value is JsonPrimitive) {
                val atom = it.value.jsonPrimitive
                atom.booleanOrNull ?: atom.intOrNull ?: atom.floatOrNull ?: atom.content
            } else it.value.toString()
        } as Map<String, Any>?



        fun buildWidget (): WidgetUI = UI.UIs.getValue(cls).invoke(widget).also { w ->
            if (w is LayoutUI) {
                children?.forEach { w.add(it.id, it.buildWidget()) }
                w.background = UI.UIs.getValue("FrameUI").invoke(back ?: mapOf<String, Any>()) as FrameUI?
            }
        }
    }



    private val layouts = mutableMapOf<String, WidgetHolder>()

    suspend fun load (vararg values: String) = values.forEach {
        layouts[it] = Json.decodeFromString(ResourceLoader.loadDataString("layouts/$it.ui.json"))
    }

    fun summon (layout: String) = layouts[layout]!!.buildWidget() as LayoutUI
}