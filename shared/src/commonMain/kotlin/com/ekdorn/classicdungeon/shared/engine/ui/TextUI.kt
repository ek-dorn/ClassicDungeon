package com.ekdorn.classicdungeon.shared.engine.ui

import com.ekdorn.classicdungeon.shared.engine.glextensions.Script
import com.ekdorn.classicdungeon.shared.engine.lib.toFloatArray
import com.ekdorn.classicdungeon.shared.engine.maths.Rectangle
import com.ekdorn.classicdungeon.shared.engine.maths.Vector
import com.ekdorn.classicdungeon.shared.engine.utils.ImageFont


internal class TextUI (initializer: Map<String, *> = hashMapOf<String, Any>()): ResizableUI(hashMapOf<String, Any>()) {
    enum class ALIGNMENT {
        START, CENTER, END, FILL
    }



    @Implicit private var textLength = 0


    var font = ImageFont.MEDIUM
        set (v) {
            dirty = true
            field = v
        }

    var multiline = true
        set (v) {
            dirty = true
            field = v
        }

    var text = ""
        set (v) {
            dirty = true
            field = v.replace("\t", "    ")
        }

    var textAlignment = ALIGNMENT.CENTER
        set (v) {
            dirty = true
            field = v
        }


    init {
        stretchY = false

        font = ImageFont.valueOf(initializer.getOrElse("font") { font.name } as String)
        multiline = initializer.getOrElse("multiline") { multiline } as Boolean
        text = initializer.getOrElse("text") { text } as String
        textAlignment = ALIGNMENT.valueOf(initializer.getOrElse("textAlignment") { textAlignment.name } as String)
        updateVertices()
    }



    private fun MutableList<String>.iterateLines (iterating: (Int) -> Float): Float {
        var maxPast = 0F
        var iterator = 0
        do {
            if (isEmpty()) break
            val past = iterating(iterator)
            if (past > maxPast) maxPast = past
            iterator++
        } while (iterator < size)
        return maxPast
    }



    override fun draw () {
        super.draw()
        font.texture.bind()
        Script.setTexture(font.texture)
        Script.drawMultiple(textLength)
        font.texture.release()
    }



    override fun translate (parentCoords: Vector, parentMetrics: Vector) {
        if (dirty) updateVertices()
        super.translate(parentCoords, parentMetrics)
    }

    override fun updateVertices () {
        super.updateVertices()
        if (parent == null) return

        val vertices = mutableListOf<Rectangle>()
        val textures = mutableListOf<Rectangle>()

        if (!multiline) text.replace('\n', ' ')

        // Object, representing correct text metrics, even if width was not set explicitly.
        val pseudoMetrics = Vector(if (dimens.x == 0F) parentMetrics()!!.x else metrics.x, parentMetrics()!!.y)
        // Space char (' ') metrics.
        val space = font.texture.image.metrics * font[' ']!!.metrics * pixelation / pseudoMetrics

        // Lines, with no empty.
        val lines = text.split('\n').filter { it.isNotEmpty() }.toMutableList()
        val maxPast = lines.iterateLines { index ->
            // Offset from the beginning of the line in width percent.
            var past = 0F

            val lineVertices = mutableListOf<Rectangle>()
            val lineTextures = mutableListOf<Rectangle>()

            // Words of the line
            val words = lines[index].split(' ')
            for (word in words.withIndex()) {
                // Offset from the beginning of the line in width percent.
                var wordPast = 0F
                val wordVertices = mutableListOf<Rectangle>()
                val wordTextures = mutableListOf<Rectangle>()

                // Adding char vertices rect and texture rect, NB! chars have const height.
                for (char in word.value) {
                    val ch = font[char]!!
                    wordTextures.add(ch)

                    val charWidth = font.width * ch.width * pixelation / pseudoMetrics.x
                    wordVertices.add(Rectangle(wordPast, 0F, wordPast + charWidth, -1F))
                    wordPast += charWidth
                }

                // If new line width exceeds view width, flushing remaining of the line as a next line.
                if (multiline && (past > 0) && (past + wordPast > 1)) {
                    lines.add(index + 1, words.subList(word.index, words.size).joinToString(" "))
                    break
                } else {
                    // Adding space if not at the end of the line.
                    if (word.index != words.size - 1) {
                        wordTextures.add(font[' ']!!)
                        wordVertices.add(Rectangle(wordPast, 0F, wordPast + space.x, -1F))
                        wordPast += space.x
                    }

                    // Translating word to the line ebd.
                    wordVertices.forEach { it.translate(x = past) }
                    past += wordPast
                }

                lineVertices.addAll(wordVertices)
                lineTextures.addAll(wordTextures)
            }

            // Remainder of space left on the line.
            val rem = 1 - past
            // Moving line coords to appropriate height.
            lineVertices.forEach { it.translate(y = -index.toFloat()) }
            vertices.addAll(when (textAlignment) {
                ALIGNMENT.START -> lineVertices
                ALIGNMENT.CENTER -> lineVertices.map { it.apply { horizontal += rem / 2 } }
                ALIGNMENT.END -> lineVertices.map { it.apply { horizontal += rem } }
                ALIGNMENT.FILL -> {
                    // Finding all space symbol indexes.
                    val spaces = lineTextures.indices.filter { lineTextures[it] == font[' ']!! }
                    // Const to add to char x.
                    var sp = 0F
                    lineVertices.mapIndexed { ind, rect -> spaces.indexOfFirst { it == ind }.let {
                        // For regular chars, moving along x by sp.
                        if (it == -1) rect.apply { horizontal += sp }
                        // For spaces, moving left coordinate along x by sp and right - by sp + rem / count of spaces.
                        // From now on sp = sp + rem / count of spaces.
                        else rect.apply { left += sp; sp += rem / spaces.size; right += sp }
                    } }
                }
            })
            textures.addAll(lineTextures)
            past
        }

        textLength = vertices.size
        // Dividing all y coords by lines number, to fit in new dimens.y.
        vertices.forEach { it.vertical /= lines.size.toFloat() }
        dimens = Vector(if (dimens.x == 0F) maxPast else dimens.x, lines.size * space.y)

        updateBuffer(2, vertices.toFloatArray(), textures.toFloatArray())
    }
}
