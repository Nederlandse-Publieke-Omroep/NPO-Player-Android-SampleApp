package nl.npo.player.sampleApp.tv

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint

/** Loads [PlaybackVideoFragment]. */
@AndroidEntryPoint
class PlaybackActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, PlaybackVideoFragment())
                .commit()
        }
    }
}
