package com.ekdorn.classicdungeon.shared.dependant

actual object GLFunctions {
    actual enum class FILTERING_MODE {
        NEAREST, LINEAR
    }

    actual enum class WRAPPING_MODE {
        NEAREST, LINEAR
    }

    actual fun generate(): Int {
        TODO("Not yet implemented")
    }

    actual fun activate(texture: Int) {
    }

    actual fun bind(texture: Int) {
    }

    actual fun release(texture: Int) {
    }

    actual fun delete(texture: Int) {
    }

    actual fun filter(
        minification: FILTERING_MODE,
        magnification: FILTERING_MODE
    ) {
    }

    actual fun wrap(
        s: WRAPPING_MODE,
        t: WRAPPING_MODE
    ) {
    }

    actual fun image(w: Int, h: Int, data: ByteArray) {
    }
}