package nl.npo.player.sampleApp.tv.presentation.playback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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
import nl.npo.player.library.presentation.compose.NativeSubtitleView
import nl.npo.player.library.presentation.compose.PlayerSurface
import nl.npo.player.library.presentation.compose.theme.Dimens
import nl.npo.player.library.presentation.compose.theme.toPlayerColors
import nl.npo.player.library.presentation.extension.getMessage
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
        setObservers()
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
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(isPlaying) {
            focusRequester.requestFocus()
        }

        MaterialTheme {
            Box(Modifier.fillMaxSize()) {
                PlayerSurface(
                    player = player,
                    canShowAds = true,
                    modifier = Modifier.align(Alignment.Center),
                )

                NativeSubtitleView(
                    subtitleCues = subtitleCues,
                    modifier = Modifier.padding(bottom = Dimens.PaddingMedium),
                    visible = true,
                    textDefaultColor = playerColors.toPlayerColors().textColor,
                )

                (playbackState as? PlaybackState.Error)?.let { playbackStateError ->
                    Text(
                        text = playbackStateError.error.getMessage(LocalContext.current),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center,
                                ).padding(60.dp),
                    )
                }

                IconButton(
                    onClick = {
                        if (isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    },
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .focusRequester(focusRequester),
                ) {
                    Icon(
                        painter =
                            painterResource(
                                if (isPlaying) {
                                    nl.npo.player.library.presentation.R.drawable.npo_player_ic_pause
                                } else {
                                    nl.npo.player.library.presentation.R.drawable.npo_player_ic_play
                                },
                            ),
                        contentDescription =
                            stringResource(
                                if (isPlaying) {
                                    R.string.content_description_tv_play
                                } else {
                                    R.string.content_description_tv_pause
                                },
                            ),
                    )
                }
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
                            playbackViewModel.setPlayerColors(npoPlayerColors)
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

                sourceWrapper.getStreamLink -> playerViewModel.retrieveSource(sourceWrapper, ::handleTokenState)
                sourceWrapper.npoSourceConfig != null -> loadStreamURL(sourceWrapper.npoSourceConfig!!)
                else -> {
                    /** NO-OP **/
                }
            }
        }
    }

    private fun setObservers() {
//        playerViewModel.streamRetrievalState.observeNonNull(this, ::handleTokenState)
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
