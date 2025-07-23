package nl.npo.player.sampleApp.tv.presentation.playback

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.sampleApp.shared.presentation.viewmodel.SettingsViewModel
import nl.npo.player.sampleApp.tv.BaseActivity

/** Loads [PlaybackVideoFragment] or [NativePlaybackVideoFragment]. */
@AndroidEntryPoint
class PlaybackActivity : BaseActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logPageAnalytics(TAG)
        // TODO: Do we need a switch or should it always be native UI?
        showUI(true)
    }

    private fun showUI(showNativeUI: Boolean) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                if (showNativeUI) ComposePlaybackVideoFragment() else PlaybackVideoFragment(),
            ).commit()
    }

    companion object {
        private const val TAG = "PlaybackActivity"
    }
}
