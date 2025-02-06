package nl.npo.player.sampleApp.tv.presentation.selection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.tv.R

/**
 * Details activity class that loads [VideoDetailsFragment] class.
 */
class PlayerActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, VideoDetailsFragment())
                .commitNow()
        }
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "hero"

        const val PLAYER_SOURCE = "PLAYER_SOURCE"
        const val PLAYER_OFFLINE_SOURCE = "PLAYER_OFFLINE_SOURCE"

        private const val NOTIFICATION_CHANNEL_ID = "NPO-PlayerSampleApp"
        private const val NOTIFICATION_ID = 1
        private const val MEDIA_SESSION_TAG = "npo-player-mediaSession"

        @Suppress("DEPRECATION")
        fun Intent.getSourceWrapper(): SourceWrapper? {
            val offlineSource: NPOOfflineSourceConfig?
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                offlineSource =
                    this@getSourceWrapper.getParcelableExtra(
                        PLAYER_OFFLINE_SOURCE,
                        NPOOfflineSourceConfig::class.java,
                    )
                getSerializableExtra(PLAYER_SOURCE, SourceWrapper::class.java)
            } else {
                offlineSource = this@getSourceWrapper.getParcelableExtra(PLAYER_OFFLINE_SOURCE)
                getSerializableExtra(PLAYER_SOURCE) as? SourceWrapper
            }?.let { sourceWrapper ->
                offlineSource?.let { sourceWrapper.copy(npoSourceConfig = offlineSource) }
                    ?: sourceWrapper
            }
        }

        fun <T : Activity> getStartIntent(
            packageContext: Context,
            sourceWrapper: SourceWrapper,
            optionalClass: Class<T>,
        ): Intent =
            Intent(packageContext, optionalClass).apply {
                if (sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig) {
                    putExtra(PLAYER_OFFLINE_SOURCE, sourceWrapper.npoSourceConfig as Parcelable)
                    putExtra(PLAYER_SOURCE, sourceWrapper.copy(npoSourceConfig = null))
                } else {
                    putExtra(PLAYER_SOURCE, sourceWrapper)
                }
            }
    }
}
