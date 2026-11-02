package com.vesaa.mytv.ui.screens.mobile

import kotlin.math.abs

/** One drag owns one axis and the side where it started, even after crossing the centre. */
class PlayerGestureSession(
    private val width: Float,
    private val height: Float,
    private val startX: Float,
    private val startBrightness: Float,
    private val startVolume: Float,
    private val threshold: Float,
) {
    enum class Axis { Pending, Vertical, Horizontal }
    var axis = Axis.Pending
        private set
    var dx = 0f
        private set
    private var dy = 0f
    val adjustsBrightness get() = startX < width / 2f

    fun drag(x: Float, y: Float): Float? {
        dx += x
        dy += y
        if (axis == Axis.Pending && maxOf(abs(dx), abs(dy)) >= threshold) {
            axis = if (abs(dy) >= abs(dx)) Axis.Vertical else Axis.Horizontal
        }
        if (axis != Axis.Vertical) return null
        val initial = if (adjustsBrightness) startBrightness else startVolume
        val minimum = if (adjustsBrightness) 0.01f else 0f
        return (initial - dy / (height * 0.7f).coerceAtLeast(1f)).coerceIn(minimum, 1f)
    }
}
