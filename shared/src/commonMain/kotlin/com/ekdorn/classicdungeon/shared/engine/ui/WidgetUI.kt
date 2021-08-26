package com.ekdorn.classicdungeon.shared.engine.ui

import com.ekdorn.classicdungeon.shared.dependant.gl.GLBuffer
import com.ekdorn.classicdungeon.shared.engine.glextensions.Camera
import com.ekdorn.classicdungeon.shared.engine.glextensions.Script
import com.ekdorn.classicdungeon.shared.engine.maths.Color
import com.ekdorn.classicdungeon.shared.engine.maths.Matrix
import com.ekdorn.classicdungeon.shared.engine.maths.Vector


/**
 * WidgetUI - base widget for all.
 */
internal abstract class WidgetUI (initializer: Map<String, *>) {
    /**
     * Types of widgets anchor alignment:
     * - START - anchor aligned to left or top
     * - CENTER - anchor aligned to center
     * - END - anchor aligned to right or bottom
     */
    enum class ALIGNMENT {
        START, CENTER, END
    }



    /**
     * Annotation, defining Implicit members of UI widgets.
     * Implicit members are non-property members that can not be set through layout file.
     */
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    @MustBeDocumented
    annotation class Implicit



    /*
    var exists: Boolean = true
    private var alive: Boolean = true
    var active: Boolean = true
        get() = field && if (parent != null) (parent!!.active) else true
    */
    /**
     * Parent widget of this widget.
     */
    @Implicit var parent: LayoutUI? = null
    /**
     * Coordinates of the widget in pixels.
     */
    @Implicit protected var coords = Vector()
    /**
     * Size of widget in pixels.
     */
    @Implicit protected var metrics = Vector()
    /**
     * Model matrix of the widget.
     */
    @Implicit protected var model = Matrix()

    /**
     * Whether call of updateVertices() needed on next update.
     */
    @Implicit protected var dirty = false

    /**
     * GL buffer associated with the widget.
     */
    @Implicit private val buffer = GLBuffer(GLBuffer.TYPE.COMMON)


    /**
     * Size of widget as percent of its parent.
     */
    @Implicit open var dimens = Vector()
        protected set


    /**
     * Property anchor - point of widgets parent widget is bound to.
     * Zero, zero by default.
     */
    var anchor = initializer.getOrElse("anchor") { Vector() } as Vector
    /**
     * Property pixelation - scale of widget.
     * 1 texture pixel will be size of pixelation * pixelation real pixels.
     * 1 by default.
     */
    var pixelation = initializer.getOrElse("pixelation") { 1F } as Float

    /**
     * Property visible - whether the widget is visible and should be drawn.
     * True by default.
     */
    var visible = initializer.getOrElse("visible") { true } as Boolean
        // get() = field && if (parent != null) (parent!!.visible) else true

    /**
     * Property verticalAlignment - vertical alignment of widget anchor.
     * ALIGNMENT.CENTER by default.
     */
    var verticalAlignment = ALIGNMENT.valueOf(initializer.getOrElse("verticalAlignment") { ALIGNMENT.CENTER.name } as String)
    /**
     * Property horizontalAlignment - horizontal alignment of widget anchor.
     * ALIGNMENT.CENTER by default.
     */
    var horizontalAlignment = ALIGNMENT.valueOf(initializer.getOrElse("horizontalAlignment") { ALIGNMENT.CENTER.name } as String)

    /**
     * Property speed - speed of the widget movement.
     * Zero by default.
     */
    var speed = initializer.getOrElse("speed") { Vector() } as Vector
    /**
     * Property acceleration - acceleration of the widget movement.
     * Zero by default.
     */
    var acceleration = initializer.getOrElse("acceleration") { Vector() } as Vector
    /**
     * Property angle - angle of the widget, in degrees.
     * Zero by default.
     */
    var angle = initializer.getOrElse("angle") { 0F } as Float
    /**
     * Property angleSpeed - angle of the widget rotation, in degrees.
     * Zero by default.
     */
    var angleSpeed = initializer.getOrElse("angleSpeed") { 0F } as Float
    // var origin = Vector()

    /**
     * Property material - color that will be added to that widgets texture color.
     * Transparent black by default.
     */
    var ambient = initializer.getOrElse("ambient") { Color() } as Color
    /**
     * Property material - color that widgets texture color will be multiplied by.
     * White by default.
     */
    var material = initializer.getOrElse("material") { Color(1F, 1F, 1F, 1F) } as Color



    /*
    inline var center: Vector
        get () = Vector(anchor.x + metrics.x / 2, anchor.y + metrics.y / 2)
        set (v) {
            anchor.x = v.x - metrics.x / 2
            anchor.y = v.y - metrics.y / 2
        }
     */

    /**
     * Inline property alpha - transparency of that widgets texture.
     * Zero by default.
     */
    inline var alpha: Float
        get () = material.a + ambient.a
        set (v) {
            material.a = v
            ambient.a = 0F
        }



    /**
     * Method for receiving metrics of this widgets parent.
     * Property itself has protected modifier and can not be used in children.
     */
    protected fun parentMetrics () = parent?.metrics

    /**
     * Method for receiving coords of this widgets parent.
     * Property itself has protected modifier and can not be used in children.
     */
    protected fun parentCoords () = parent?.coords

    /**
     * Method for moving widget depending on its speed and acceleration.
     * @param speed widget speed
     * @param acceleration widget acceleration
     * @param time time elapsed
     */
    private fun move (speed: Float, acceleration: Float, time: Int) = speed + acceleration * time



    /**
     * Function triggered on each update to update widgets inner properties.
     * It is triggered after translate() and before draw().
     * @param elapsed time elapsed since last update
     * @see translate translation method
     * @see draw draw function
     */
    open fun update (elapsed: Int) {
        if (dirty) updateVertices()

        val halfDeltaX = (move(speed.x, acceleration.x, elapsed) - speed.x) / 2
        speed.x += halfDeltaX
        anchor.x += speed.x * elapsed
        speed.x += halfDeltaX

        val halfDeltaY = (move(speed.y, acceleration.y, elapsed) - speed.y) / 2
        speed.y += halfDeltaY
        anchor.y += speed.y * elapsed
        speed.y += halfDeltaY

        angle += angleSpeed * elapsed % 360

        model.toIdentity()
        model.translate(coords.x, -coords.y)
        model.scale(metrics.x, metrics.y)
        // model.translate(origin.x, origin.y) // Needed?
        model.rotate(angle)
        // model.translate(-origin.x, -origin.y) // Needed?
    }

    /**
     * Function triggered on each update to draw this widget on canvas.
     * It is triggered after translate() and update().
     * @see translate translation method
     * @see draw draw function
     */
    open fun draw () {
        // println("Camera: ${Camera.UI}")
        // println("Model: $model")

        Script.setCamera(Camera.UI)
        Script.setModel(model)

        Script.setMaterial(material)
        Script.setAmbient(ambient)

        buffer.bind()
    }



    /**
     * Function triggered each update to define this widget coords (1) and metrics (2).
     * It is triggered first, before update() and draw().
     * @param parentCoords coordinates of parent widget
     * @param parentMetrics metrics of parent widget
     * @see coords coordinates of the widget in parent
     * @see metrics metrics of the widget in pixels
     * @see update update function
     * @see draw draw function
     */
    open fun translate (parentCoords: Vector, parentMetrics: Vector) {
        coords = parentCoords + Vector(when (horizontalAlignment) {
            ALIGNMENT.START -> parentMetrics.x * anchor.x
            ALIGNMENT.CENTER -> parentMetrics.x * anchor.x - metrics.x / 2
            ALIGNMENT.END -> parentMetrics.x * anchor.x - metrics.x
        }, when (verticalAlignment) {
            ALIGNMENT.START -> parentMetrics.y * anchor.y
            ALIGNMENT.CENTER -> parentMetrics.y * anchor.y - metrics.y / 2
            ALIGNMENT.END -> parentMetrics.y * anchor.y - metrics.y
        })
    }

    /**
     * Function for updating widget vertices on GL view.
     * After vertices are updated, they should be loaded into buffer.
     * @see updateBuffer updating vertices buffer
     */
    protected open fun updateVertices () { dirty = false }

    /**
     * Function for loading given vertices and textures array into this widgets buffer.
     * @param vertices array containing coordinates of this widget
     * @param textures array containing texture points associated with this widget coordinates
     */
    protected fun updateBuffer (vertices: FloatArray, textures: FloatArray) {
        val size = vertices.size + textures.size
        buffer.fillDynamic(FloatArray(size) {
            if ((it / 2) % 2 == 0) vertices[(it / 2) + (it % 2) - (it / 2) % 2]
            else textures[(it / 2) + (it % 2) - (it / 2) % 2]
        })
    }



    /*
    fun kill () {
        alive = false
        exists = false
    }
    fun revive () {
        alive = true
        exists = true
    }
    */

    /**
     * Method for clearing this widget and deleting its buffer.
     */
    open fun delete () = buffer.delete()



    fun resetColor () {
        material.apply { r = 1F; g = 1F; b = 1F; a = 1F }
        ambient.apply { r = 0F; g = 0F; b = 0F; a = 0F }
    }

    fun multiplyColor (color: Color) {
        ambient.apply { r = 0F; g = 0F; b = 0F; a = 0F }
        material.apply { r = color.r; g = color.g; b = color.b; a = color.a }
    }

    fun addColor (color: Color) {
        material.apply { r = 0F; g = 0F; b = 0F; a = 0F }
        ambient.apply { r = color.r; g = color.g; b = color.b; a = color.a }
    }
}
