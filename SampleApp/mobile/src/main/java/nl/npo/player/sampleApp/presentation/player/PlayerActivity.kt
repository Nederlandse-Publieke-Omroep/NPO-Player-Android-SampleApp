package nl.npo.player.sampleApp.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.data.offline.model.NPOMedia3OfflineSourceConfig
import nl.npo.player.sampleApp.presentation.BaseActivity
import nl.npo.player.sampleApp.presentation.MainActivity
import nl.npo.player.sampleApp.presentation.compose.views.PlaybackScreen
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlaybackViewModel

const val PLAYER_SOURCE = "PLAYER_SOURCE"
const val PLAYER_OFFLINE_SOURCE = "PLAYER_OFFLINE_SOURCE"

@AndroidEntryPoint
class PlayerActivity : BaseActivity() {
    private val playbackViewModel by viewModels<PlaybackViewModel>()
    private var backstackLost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PlaybackScreen(viewModel = playbackViewModel, onClose = ::finish)
            }
        }
        if (playbackViewModel.playbackState.value.player == null) {
            intent.getSourceWrapper()?.let { logPageAnalytics(it.title.orEmpty()) }
            loadSourceWrapperFromIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadSourceWrapperFromIntent(intent)
    }

    private fun loadSourceWrapperFromIntent(intent: Intent?) {
        val sourceWrapper = intent?.getSourceWrapper() ?: return finish()
        playbackViewModel.play(sourceWrapper)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) backstackLost = true
    }

    override fun finish() {
        if (backstackLost) {
            finishAndRemoveTask()
            startActivity(
                Intent.makeRestartActivityTask(
                    ComponentName(
                        this,
                        MainActivity::class.java,
                    ),
                ),
            )
        } else {
            super.finish()
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        fun Intent.getSourceWrapper(): SourceWrapper? {
            val offlineSource: NPOMedia3OfflineSourceConfig?
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                offlineSource =
                    this@getSourceWrapper.getParcelableExtra(
                        PLAYER_OFFLINE_SOURCE,
                        NPOMedia3OfflineSourceConfig::class.java,
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

        fun getStartIntent(
            packageContext: Context,
            sourceWrapper: SourceWrapper,
        ): Intent =
            Intent(packageContext, PlayerActivity::class.java).apply {
                if (sourceWrapper.npoSourceConfig is NPOMedia3OfflineSourceConfig) {
                    putExtra(PLAYER_OFFLINE_SOURCE, sourceWrapper.npoSourceConfig as Parcelable)
                    putExtra(PLAYER_SOURCE, sourceWrapper.copy(npoSourceConfig = null))
                } else {
                    putExtra(PLAYER_SOURCE, sourceWrapper)
                }
            }
    }
}
