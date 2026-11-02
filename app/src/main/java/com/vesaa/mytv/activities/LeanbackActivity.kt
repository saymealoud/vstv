package com.vesaa.mytv.activities

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vesaa.mytv.ui.utils.isTelevision
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.vesaa.mytv.ui.LeanbackApp
import com.vesaa.mytv.ui.screens.leanback.toast.LeanbackToastState
import com.vesaa.mytv.ui.theme.LeanbackTheme
import com.vesaa.mytv.ui.utils.HttpServer
import com.vesaa.mytv.ui.utils.SP

open class LeanbackActivity : ComponentActivity() {
    var pipVisible by mutableStateOf(false)
        private set
    private var playbackEligible = false
    private var playbackAspectRatio = 16f / 9f
    private var lastPipParamsKey: Triple<Boolean, Float, Boolean>? = null

    fun supportsPip(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    fun updatePlaybackForPip(eligible: Boolean, aspectRatio: Float) {
        playbackEligible = eligible
        playbackAspectRatio = if (aspectRatio.isFinite()) aspectRatio.coerceIn(0.42f, 2.39f) else 16f / 9f
        val key = Triple(eligible, playbackAspectRatio, SP.uiPipMode)
        if (supportsPip() && lastPipParamsKey != key) {
            runCatching { setPictureInPictureParams(pipParams()) }
            lastPipParamsKey = key
        }
    }

    private fun pipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational((playbackAspectRatio * 1000).toInt(), 1000))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(playbackEligible && SP.uiPipMode)
        }
        return builder.build()
    }

    fun enterPlayerPip(showFailure: Boolean = true) {
        if (!supportsPip() || !playbackEligible || isFinishing) {
            if (showFailure) Toast.makeText(this, "当前设备不支持小窗，或尚未开始单屏播放", Toast.LENGTH_SHORT).show()
            return
        }
        val entered = runCatching { enterPictureInPictureMode(pipParams()) }.getOrDefault(false)
        if (!entered && showFailure) {
            Toast.makeText(this, "无法进入小窗，请在系统应用设置中允许画中画", Toast.LENGTH_LONG).show()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (SP.uiPipMode && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPlayerPip(showFailure = false)
        }
    }

    override fun onPictureInPictureModeChanged(inPip: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(inPip, newConfig)
        pipVisible = inPip
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTelevision()) {
            requestedOrientation = savedInstanceState?.getInt("playerOrientation")
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        try { enableEdgeToEdge() } catch (_: Exception) { /* 部分低端 ROM 不支持 */ }
        setContent {
            // 隐藏状态栏、导航栏
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, window.decorView).let { insetsController ->
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // 屏幕常亮
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            LeanbackTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    LeanbackApp(
                        onBackPressed = {
                            // 勿使用 exitProcess：会立刻杀进程，SP.apply() 尚未落盘，导致「上次频道」等偏好丢失。
                            finish()
                        },
                    )
                }
            }
        }

        HttpServer.start(applicationContext, showToast = {
            LeanbackToastState.I.showToast(it, id = "httpServer")
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("playerOrientation", requestedOrientation)
        super.onSaveInstanceState(outState)
    }
}
