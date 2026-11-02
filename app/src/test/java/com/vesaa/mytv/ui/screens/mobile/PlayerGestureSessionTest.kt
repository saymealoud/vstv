package com.vesaa.mytv.ui.screens.mobile

import org.junit.Assert.*
import org.junit.Test

class PlayerGestureSessionTest {
    private fun gesture(startX: Float = 100f) = PlayerGestureSession(1000f, 1000f, startX, 0.5f, 0.5f, 10f)

    @Test fun leftUpRaisesBrightnessWithoutSwitchingAxes() {
        val g = gesture()
        assertEquals(0.6f, g.drag(0f, -70f)!!, 0.001f)
        assertTrue(g.adjustsBrightness)
        g.drag(800f, 0f)
        assertEquals(PlayerGestureSession.Axis.Vertical, g.axis)
        assertTrue(g.adjustsBrightness)
    }

    @Test fun rightDownLowersVolumeAndCanMute() {
        val g = gesture(900f)
        assertFalse(g.adjustsBrightness)
        assertEquals(0.4f, g.drag(0f, 70f)!!, 0.001f)
        assertEquals(0f, g.drag(0f, 2000f)!!, 0f)
    }

    @Test fun brightnessNeverBlacksOutAndBothLevelsAreClamped() {
        assertEquals(0.01f, gesture().drag(0f, 2000f)!!, 0f)
        assertEquals(1f, gesture().drag(0f, -2000f)!!, 0f)
        assertEquals(1f, gesture(900f).drag(0f, -2000f)!!, 0f)
    }

    @Test fun horizontalSwipeCannotChangeBrightnessOrVolume() {
        val g = gesture()
        assertNull(g.drag(100f, 2f))
        assertNull(g.drag(0f, 400f))
        assertEquals(PlayerGestureSession.Axis.Horizontal, g.axis)
    }

    @Test fun jitterAndNewGesturesDoNotCarryOver() {
        val g = gesture()
        assertNull(g.drag(1f, 1f))
        assertEquals(PlayerGestureSession.Axis.Pending, g.axis)
        g.drag(0f, -80f)
        assertEquals(PlayerGestureSession.Axis.Pending, gesture().axis)
        assertEquals(0.6f, gesture().drag(0f, -70f)!!, 0.001f)
    }
}
