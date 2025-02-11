package nl.npo.player.sampleApp.tv.presentation.playback

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.presentation.viewmodel.SettingsViewModel
import nl.npo.player.sampleApp.tv.BaseActivity

/** Loads [PlaybackVideoFragment] or [NativePlaybackVideoFragment]. */
@AndroidEntryPoint
class PlaybackActivity : BaseActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel.showNativeUI.observeNonNull(this, ::showUI)
    }

    private fun showUI(showNativeUI: Boolean) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                if (showNativeUI) NativePlaybackVideoFragment() else PlaybackVideoFragment(),
            ).commit()
    }
}
