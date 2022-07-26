package com.ekdorn.classicdungeon.shared.engine.ui

import com.ekdorn.classicdungeon.shared.gl.extensions.Camera
import com.ekdorn.classicdungeon.shared.gl.extensions.Script
import com.ekdorn.classicdungeon.shared.gl.extensions.WidgetBuffer
import com.ekdorn.classicdungeon.shared.engine.atomic.Color
import com.ekdorn.classicdungeon.shared.engine.atomic.Rectangle
import com.ekdorn.classicdungeon.shared.gl.primitives.Matrix
import com.ekdorn.classicdungeon.shared.engine.atomic.Vector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * WidgetUI - base widget for all.
 */
@Serializable
@SerialName("WidgetUI")
internal abstract class WidgetUI {
    /**
     * Types of widgets anchor alignment:
     * - START - anchor aligned to left or top
     * - CENTER - anchor aligned to center
     * - END - anchor aligned to right or bottom
     */
    @Serializable
    enum class ALIGNMENT {
        START, CENTER, END
    }


    /*
    var exists: Boolean = true
    private var alive: Boolean = true
    var active: Boolean = true
        get() = field && if (parent != null) (parent!!.active) else true
    */

    /**
     * Parent widget of this widget.
     */
    @Transient open var parent: LayoutUI? = null

    /**
     * Coordinates of the widget in pixels.
     */
    @Transient protected var coords = Vector()

    /**
     * Size of widget in pixels.
     */
    @Transient protected var metrics = Vector()

    /**
     * Model matrix of the widget.
     */
    @Transient protected var model = Matrix()

    /**
     * Whether call of updateVertices() needed on next update.
     */
    @Transient protected var dirty = false

    /**
     * GL buffer associated with the widget.
     */
    @Transient protected val buffer = WidgetBuffer()

    /**
     * Size of widget as percent of its parent.
     */
    @Transient open var dimens = Vector()
        protected set


    /**
     * Property anchor - point of widgets parent widget is bound to.
     * Zero, zero by default.
     */
    var anchor = Vector()

    /**
     * Property pixelation - scale of widget.
     * 1 texture pixel will be size of pixelation * pixelation real pixels.
     * 1 by default.
     */
    var pixelation = 1F

    /**
     * Property visible - whether the widget is visible and should be drawn.
     * True by default.
     */
    var visible = true
        // get() = field && if (parent != null) (parent!!.visible) else true

    /**
     * Property verticalAlignment - vertical alignment of widget anchor.
     * ALIGNMENT.CENTER by default.
     */
    var verticalAlignment = ALIGNMENT.CENTER

    /**
     * Property horizontalAlignment - horizontal alignment of widget anchor.
     * ALIGNMENT.CENTER by default.
     */
    var horizontalAlignment = ALIGNMENT.CENTER

    /**
     * Property speed - speed of the widget movement.
     * Zero by default.
     */
    var speed = Vector()

    /**
     * Property acceleration - acceleration of the widget movement.
     * Zero by default.
     */
    var acceleration = Vector()

    /**
     * Property angle - angle of the widget, in degrees.
     * Zero by default.
     */
    var angle = 0F

    /**
     * Property angleSpeed - angle of the widget rotation, in degrees.
     * Zero by default.
     */
    var angleSpeed = 0F

    // var origin = Vector()

    /**
     * Property ambient - color that will be added to that widgets texture color.
     * Transparent black by default.
     */
    var ambient = Color()

    /**
     * Property material - color that widgets texture color will be multiplied by.
     * White by default.
     */
    var material = Color(0xFFFFFFFFU)


    /*
    inline var center: Vector
        get () = Vector(anchor.x + metrics.x / 2, anchor.y + metrics.y / 2)
        set (v) {
            anchor.x = v.x - metrics.x / 2
            anchor.y = v.y - metrics.y / 2
        }
     */

    inline val rect: Rectangle
        get() = Rectangle(coords.x, coords.y, coords.x + metrics.x, coords.y + metrics.y)


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

        var halfDelta = (move(speed.x, acceleration.x, elapsed) - speed.x) / 2
        speed.x += halfDelta
        var delta = speed.x * elapsed
        speed.x += halfDelta

        coords.x += delta
        if (parent != null) anchor.x += delta / parentMetrics()!!.x

        halfDelta = (move(speed.y, acceleration.y, elapsed) - speed.y) / 2
        speed.y += halfDelta
        delta = speed.y * elapsed
        speed.y += halfDelta

        coords.y += delta
        if (parent != null) anchor.y += delta / parentMetrics()!!.y

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
     */
    protected open fun updateVertices () { dirty = false }

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
}
