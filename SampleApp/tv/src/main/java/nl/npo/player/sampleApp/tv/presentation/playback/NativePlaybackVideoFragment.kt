package nl.npo.player.sampleApp.tv.presentation.playback

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.media.NPOSubtitleTrack
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.state.PlaybackState
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.components.PlayerIconButton
import nl.npo.player.library.presentation.compose.theme.PlayerColors
import nl.npo.player.library.presentation.compose.theme.toPlayerColors
import nl.npo.player.library.presentation.tv.compose.components.TvPlayerTopBar
import nl.npo.player.library.presentation.tv.compose.scenes.v2.TvPlayerComponents
import nl.npo.player.library.presentation.tv.compose.shareable.experimental.NPOPlayerUIState
import nl.npo.player.library.presentation.tv.compose.shareable.experimental.TVSceneRenderer
import nl.npo.player.library.presentation.tv.compose.shareable.experimental.collectStreamInfoAsState
import nl.npo.player.library.presentation.tv.compose.view.NPOVideoPlayerView
import nl.npo.player.library.sterads.presentation.ui.TvSterOverlayRenderer
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlayerViewModel
import nl.npo.player.sampleApp.tv.BaseActivity
import nl.npo.player.sampleApp.tv.R
import nl.npo.player.sampleApp.tv.presentation.selection.PlayerActivity.Companion.getSourceWrapper

/** Handles video playback with media controls. */
@AndroidEntryPoint
class NativePlaybackVideoFragment : Fragment() {
    private val playerViewModel by viewModels<PlayerViewModel>()
    private val playbackViewModel by viewModels<PlaybackViewModel>()
    private lateinit var sourceWrapper: SourceWrapper
    private lateinit var player: NPOPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? BaseActivity)?.logPageAnalytics(TAG)

        loadSourceWrapperFromIntent(activity?.intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ContentRoot(playbackViewModel)
            }
        }

    @Composable
    private fun ContentRoot(viewModel: PlaybackViewModel) {
        val player = viewModel.player.collectAsState().value ?: return

        val playerState by player.playerStateManager.playerState.collectAsState()
        val playerColors by viewModel.playerColors.collectAsState()
        val subtitleCues by viewModel.subtitles.collectAsState()

        val playbackState by remember { derivedStateOf { playerState.playbackState } }
        val isPlaying by remember(playbackState) { mutableStateOf(playbackState is PlaybackState.Playing) }

        MaterialTheme {
            val isPreview = LocalInspectionMode.current
            var view: NPOVideoPlayerView? = null
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                    factory = { context ->
                        NPOVideoPlayerView(
                            context = context,
                        ).apply {
                            if (!isPreview) {
                                attachPlayer(
                                    npoPlayer = player,
                                    npoPlayerColors = PlayerColors(),
                                    sceneOverlays =
                                        TVSceneRenderer(
                                            adsOverlayRenderer =
                                                TvSterOverlayRenderer(
                                                    toolbar = {},
                                                ),
                                        ),
                                    components =
                                        CustomPlayerComponents(
                                            onBackPressed = { activity?.onBackPressed() },
                                        ),
                                )
                            }
                        }.also { view = it }
                    },
                )
            }
        }
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
            player =
                NPOPlayerLibrary
                    .getPlayer(
                        context,
                        PlayerTagProvider.getPageTracker(activity.pageTracker!!),
                        playerConfig,
                    ).apply {
                        attachToLifecycle(lifecycle)
                        playbackViewModel.setPlayer(this)
                        if (npoPlayerColors != null) {
                            playbackViewModel.setPlayerColors(npoPlayerColors.toPlayerColors())
                        }
                        eventEmitter.addListener(
                            object : PlayerListener {
                                override fun onPlaying(
                                    currentPosition: Double,
                                    isAd: Boolean,
                                ) {
                                    if (player.getSelectedSubtitleTrack() == NPOSubtitleTrack.OFF) {
                                        player
                                            .getSubtitleTracks()
                                            ?.firstOrNull { it != NPOSubtitleTrack.OFF }
                                            ?.let {
                                                player.selectSubtitleTrack(it)
                                            }
                                    }
                                }
                            },
                        )
                    }

            when {
                sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig ->
                    loadStreamURL(
                        sourceWrapper.npoSourceConfig as NPOOfflineSourceConfig,
                    )

                sourceWrapper.getStreamLink ->
                    playerViewModel.retrieveSource(
                        sourceWrapper,
                        ::handleTokenState,
                    )

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
                // NO-OP
            }

            StreamRetrievalState.NotStarted -> {
                // NO-OP
            }
        }
    }

    private fun loadStreamURL(npoSourceConfig: NPOSourceConfig) {
        playerViewModel.loadStream(player, npoSourceConfig)
    }

    companion object {
        private const val TAG = "NativePlaybackVideoFragment"
    }
}

class CustomPlayerComponents(
    val onBackPressed: () -> Unit,
) : TvPlayerComponents() {
    @Composable
    override fun TopControlsBar(
        modifier: Modifier,
        playerState: NPOPlayerUIState,
    ) {
        val info by playerState.collectStreamInfoAsState()
        TvPlayerTopBar(
            modifier = Modifier,
            title = info.title,
            description = info.description,
            backButton = {
                PlayerIconButton(
                    onClick = {
                        onBackPressed()
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.npo_player_ic_arrow_left),
                        stringResource(R.string.player_close),
                    )
                }
            },
        )
    }
}
