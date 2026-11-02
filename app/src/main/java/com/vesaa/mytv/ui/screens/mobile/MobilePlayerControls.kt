package com.vesaa.mytv.ui.screens.mobile

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesaa.mytv.activities.LeanbackActivity
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun MobilePlayerControls(
    activity: LeanbackActivity,
    channelName: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChannels: () -> Unit,
    onMenu: () -> Unit,
    onSources: () -> Unit,
    onLineSwipe: (Boolean) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val audio = remember(activity) { activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var visible by remember { mutableStateOf(true) }
    var touchLocked by rememberSaveable { mutableStateOf(false) }
    var orientationLocked by rememberSaveable { mutableStateOf(activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    var interaction by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val latestLineSwipe by rememberUpdatedState(onLineSwipe)
    val latestMenu by rememberUpdatedState(onMenu)
    val latestSources by rememberUpdatedState(onSources)
    LaunchedEffect(visible, interaction, dragging) {
        if (visible && !dragging) {
            delay(5000)
            visible = false
        }
    }
    LaunchedEffect(feedback, dragging) {
        if (feedback != null && !dragging) {
            delay(800)
            feedback = null
        }
    }
    fun rotation(orientation: Int) {
        activity.requestedOrientation = orientation
        orientationLocked = orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        interaction++
    }
    Box(Modifier.fillMaxSize()) {
        // A sibling above the video owns touch input, leaving remote key handling intact.
        Box(Modifier.fillMaxSize()
            .pointerInput(touchLocked) {
                detectTapGestures(
                    onTap = { visible = !visible; interaction++ },
                    onDoubleTap = { if (!touchLocked) latestMenu() },
                    onLongPress = { if (!touchLocked) latestSources() },
                )
            }
            .pointerInput(touchLocked, configuration.orientation) {
                if (touchLocked) return@pointerInput
                var session: PlayerGestureSession? = null
                var volumeMax = 1
                detectDragGestures(
                    onDragStart = { position ->
                        volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                        val windowBrightness = activity.window.attributes.screenBrightness
                        val brightness = if (windowBrightness >= 0) windowBrightness else
                            Settings.System.getInt(activity.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f
                        session = PlayerGestureSession(
                            size.width.toFloat(), size.height.toFloat(), position.x, brightness,
                            audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / volumeMax,
                            viewConfiguration.touchSlop,
                        )
                        dragging = true
                        interaction++
                    },
                    onDragCancel = { session = null; dragging = false; feedback = null },
                    onDragEnd = {
                        session?.let {
                            if (it.axis == PlayerGestureSession.Axis.Horizontal &&
                                kotlin.math.abs(it.dx) >= 64.dp.toPx()) latestLineSwipe(it.dx > 0)
                        }
                        session = null
                        dragging = false
                        interaction++
                    },
                ) { change, amount ->
                    change.consume()
                    val gesture = session ?: return@detectDragGestures
                    val level = gesture.drag(amount.x, amount.y) ?: return@detectDragGestures
                    if (gesture.adjustsBrightness) {
                        // Window-only brightness: no WRITE_SETTINGS permission and no global setting changes.
                        activity.window.attributes = activity.window.attributes.apply { screenBrightness = level }
                        feedback = "亮度 ${(level * 100).roundToInt()}%"
                    } else {
                        val target = (level * volumeMax).roundToInt().coerceIn(0, volumeMax)
                        val changed = runCatching {
                            audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                        }.isSuccess
                        val actual = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                        feedback = if (changed) "音量 ${(actual * 100f / volumeMax).roundToInt()}%" else "系统限制音量调整"
                    }
                }
            })
        if (feedback != null) {
            Text(feedback.orEmpty(), color = Color.White, fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp)).padding(20.dp))
        }
        if (visible) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.75f)).navigationBarsPadding().padding(8.dp)) {
                if (touchLocked) {
                    TextButton(onClick = { touchLocked = false; interaction++ }) { Text("解锁屏幕", color = Color.White) }
                } else {
                    Text(channelName.ifBlank { "直播播放器" }, color = Color.White, maxLines = 1)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Control("上一台") { onPrevious(); interaction++ }
                        Control("选台", onChannels)
                        Control("下一台") { onNext(); interaction++ }
                        Control("菜单", onMenu)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Control("横/竖屏") {
                            rotation(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                        }
                        Control(if (orientationLocked) "自动旋转" else "锁定旋转") {
                            rotation(if (orientationLocked) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                else ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                        }
                        if (activity.supportsPip()) Control("小窗") { activity.enterPlayerPip() }
                        Control("锁屏") { touchLocked = true; interaction++ }
                    }
                    Text("左侧上下滑调亮度 · 右侧上下滑调音量", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun Control(label: String, action: () -> Unit) {
    TextButton(onClick = action, contentPadding = PaddingValues(horizontal = 6.dp)) {
        Text(label, color = Color.White, fontSize = 13.sp, maxLines = 1)
    }
}
