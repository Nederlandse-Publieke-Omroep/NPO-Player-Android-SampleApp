package nl.npo.player.sampleApp.tv.presentation.playback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentationtv.adapter.NPOLeanbackPlayerAdapter
import nl.npo.player.library.setAdViewGroup
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlayerViewModel
import nl.npo.player.sampleApp.tv.BaseActivity
import nl.npo.player.sampleApp.tv.presentation.selection.PlayerActivity.Companion.getSourceWrapper

/** Handles video playback with media controls. */
@AndroidEntryPoint
class PlaybackVideoFragment : VideoSupportFragment() {
    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<NPOLeanbackPlayerAdapter>
    private val playerViewModel by viewModels<PlayerViewModel>()
    private lateinit var sourceWrapper: SourceWrapper
    private lateinit var player: NPOPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? BaseActivity)?.logPageAnalytics(TAG)

        loadSourceWrapperFromIntent(activity?.intent)
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }

    private fun loadSourceWrapperFromIntent(intent: Intent?) {
        val context = context ?: return
        val activity = activity as? BaseActivity ?: return
        sourceWrapper = intent?.getSourceWrapper() ?: run {
            Log.d(
                TAG,
                "loadSourceWrapperFromIntent - intent or source is null. Finishing activity.",
            )
            activity.finish()
            return
        }

        playerViewModel.getConfiguration { playerConfig, npoPlayerColors ->
            val pageTracker = activity.pageTracker ?: return@getConfiguration
            player =
                NPOPlayerLibrary
                    .getPlayer(
                        context,
                        PlayerTagProvider.getPageTracker(pageTracker),
                        playerConfig,
                    ).apply {
                        attachToLifecycle(lifecycle)
                        setAdViewGroup(view as ViewGroup)
                    }

            val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
            val playerAdapter = NPOLeanbackPlayerAdapter(player)
            playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

            mTransportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
            mTransportControlGlue.host = glueHost
            when {
                sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig ->
                    loadStreamURL(
                        sourceWrapper.npoSourceConfig as NPOOfflineSourceConfig,
                    )

                sourceWrapper.getStreamLink -> playerViewModel.retrieveSource(sourceWrapper, ::handleTokenState)
                sourceWrapper.npoSourceConfig != null -> loadStreamURL(sourceWrapper.npoSourceConfig!!)
                else -> {
                    /** NO-OP **/
                }
            }
        }
    }

    private fun handleTokenState(retrievalState: StreamRetrievalState) {
        when (retrievalState) {
            is StreamRetrievalState.Success -> loadStreamURL(retrievalState.npoSourceConfig)

            is StreamRetrievalState.Error -> player.setPlayerError(retrievalState.error)

            StreamRetrievalState.Loading -> {
//                handleLoading()
            }

            StreamRetrievalState.NotStarted -> {
                // NO-OP
            }
        }
    }

    private fun loadStreamURL(npoSourceConfig: NPOSourceConfig) {
        mTransportControlGlue.title = npoSourceConfig.title
        mTransportControlGlue.subtitle = npoSourceConfig.description
        mTransportControlGlue.playWhenPrepared()
        playerViewModel.loadStream(player, npoSourceConfig)
    }

    companion object {
        private const val TAG = "PlaybackVideoFragment"
    }
}
