package nl.npo.player.sampleApp.presentation.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import nl.npo.player.library.domain.player.model.NPOFullScreenHandler

class ActivityFullScreenHandler(
    private val activity: Activity,
) : NPOFullScreenHandler {
    var isFullscreen by mutableStateOf(false)
        private set

    override fun onFullscreenRequested() {
        activity.runOnUiThread {
            isFullscreen = true
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            updateSystemBars()
        }
    }

    override fun onFullscreenExitRequested() {
        activity.runOnUiThread {
            isFullscreen = false
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            updateSystemBars()
        }
    }

    override fun onPause() = Unit

    override fun onResume() = updateSystemBars()

    override fun onDestroy() = Unit

    private fun updateSystemBars() {
        with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            val type = WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
            if (isFullscreen) hide(type) else show(type)
        }
    }
}
