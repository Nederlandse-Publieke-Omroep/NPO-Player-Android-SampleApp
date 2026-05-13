package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.state.PlaybackState
import nl.npo.player.library.presentation.PlayerUI
import nl.npo.player.library.presentation.compose.Poster
import nl.npo.player.library.presentation.compose.components.seekbar.Seekbar
import nl.npo.player.library.presentation.compose.components.seekbar.SeekbarState
import nl.npo.player.library.presentation.compose.state.NPOPlayerUIState
import nl.npo.player.library.presentation.compose.state.collectPlaybackStateAsState
import nl.npo.player.library.presentation.compose.state.rememberNPOPlayerUIState
import nl.npo.player.library.presentation.compose.theme.NPOPlayerTheme
import nl.npo.player.library.presentation.model.NPOPlayerUIAction
import nl.npo.player.sampleApp.shared.presentation.viewmodel.ShortsViewModel
import kotlin.time.Duration

@Composable
fun PreloadScreen(viewModel: ShortsViewModel = hiltViewModel()) {
    val sourceConfigs by viewModel.sourceConfig.asFlow().collectAsState(initial = emptyList())
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val player =
        viewModel.player.collectAsState().value ?: run {
            viewModel.initPlayer(LocalContext.current)
            return
        }
    val playerUIState = rememberNPOPlayerUIState(player = player)
    DisposableEffect(player) {
        onDispose {
            player.unload()
        }
    }

    if (sourceConfigs.isEmpty()) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    } else {
        val pagerState =
            rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0f,
            ) { sourceConfigs.count() }
        LaunchedEffect(pagerState) {
            // Collect from the pager state a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.currentPage }.collect { page ->
                player.unload()
                currentPageIndex = page
                viewModel.setCurrentPageIndex(page)
            }
        }
        val fling =
            PagerDefaults.flingBehavior(
                state = pagerState,
            )

        VerticalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = fling,
            key = { index ->
                sourceConfigs[index].uniqueId
            },
        ) { pagerIndex ->

            val npoSourceConfig = sourceConfigs[pagerIndex]
            if (currentPageIndex == pagerIndex) {
                ShortVideo(playerUIState, npoSourceConfig)
            } else {
                Poster(npoSourceConfig.imageUrl)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShortVideo(
    playerUIState: NPOPlayerUIState,
    npoSourceConfig: NPOSourceConfig,
) {
    val playbackState by playerUIState.collectPlaybackStateAsState()

    val progressState by playerUIState.progressState.collectAsState()
    val duration by remember { derivedStateOf { progressState.duration } }
    val currentPosition by remember { derivedStateOf { progressState.currentPosition } }
    val bufferPosition by remember { derivedStateOf { progressState.bufferPosition } }

    val seekbarState =
        remember(duration, currentPosition, bufferPosition) {
            SeekbarState(
                progress = currentPosition,
                bufferProgress = bufferPosition,
                range = Duration.ZERO..duration,
                onProgressChangeStart = {
                    playerUIState.handleAction(NPOPlayerUIAction.OnPlayer.ScrubbingStarted)
                },
                onProgressChanging = {
                    playerUIState.handleAction(NPOPlayerUIAction.OnPlayer.Scrubbing(it))
                },
                onProgressChangeFinished = { seekPosition ->
                    playerUIState.handleAction(
                        NPOPlayerUIAction.OnPlayer.SeekTo(seekPosition),
                    )
                    playerUIState.handleAction(NPOPlayerUIAction.OnPlayer.ScrubbingFinished)
                },
                onProgressChangeCancelled = {
                    playerUIState.handleAction(NPOPlayerUIAction.OnPlayer.ScrubbingFinished)
                },
            )
        }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        PlayerUI.Surface(modifier = Modifier, playerUIState)
        Seekbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = seekbarState,
            colors =
                NPOPlayerTheme.colors.seekbarColors.run {
                    copy(trackColor = trackColor.copy(0.24f))
                },
        )

        if (playbackState.isBefore(PlaybackState.Playing)) {
            Poster(npoSourceConfig.imageUrl)
        }
//            PlayerUI.Overlay(
//                modifier = Modifier.fillMaxSize(),
//                state = playerState,
//                components = DefaultMobilePlayerComponents(),
//                sceneOverlays = MobileSceneRenderer(NoAdOverlayRenderer),
//                typography = PlayerTypography.mobile(),
//                npoPlayerColors = NativePlayerColors().toPlayerColors(),
//            )
    }
}
