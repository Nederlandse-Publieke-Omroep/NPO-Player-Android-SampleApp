package nl.npo.player.sampleApp.presentation.compose.views

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.domain.state.AdState
import nl.npo.player.library.presentation.extension.getMessage
import nl.npo.player.sampleApp.presentation.compose.components.KeepScreenOn
import nl.npo.player.sampleApp.presentation.compose.components.PlaybackControls
import nl.npo.player.sampleApp.presentation.compose.components.PlaybackPlayer
import nl.npo.player.sampleApp.presentation.compose.components.rememberIsInPictureInPicture
import nl.npo.player.sampleApp.presentation.player.ActivityFullScreenHandler
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlaybackViewModel

@Composable
fun PlaybackScreen(
    viewModel: PlaybackViewModel,
    onClose: () -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity
    val context = LocalContext.current
    val state by viewModel.playbackState.collectAsStateWithLifecycle()
    val isSterUIEnabled by viewModel.isSterUIEnabled.collectAsStateWithLifecycle()
    val fullScreenHandler = remember(activity) { ActivityFullScreenHandler(activity) }
    val isInPictureInPicture by rememberIsInPictureInPicture(activity)
    var showSettings by remember { mutableStateOf(false) }

    KeepScreenOn()
    LifecycleResumeEffect(key1 = activity) {
        NPOCasting.updateCastingContext(activity)
        onPauseOrDispose { }
    }
    LaunchedEffect(state.shouldClose) {
        if (state.shouldClose) onClose()
    }
    LaunchedEffect(state.error) {
        state.error?.let { Toast.makeText(context, it.getMessage(context), Toast.LENGTH_LONG).show() }
    }

    val player = state.player
    val playerState = player?.playerState?.collectAsStateWithLifecycle()?.value
    val isAdPlaying = playerState?.adState is AdState.Playing
    val showControls = !fullScreenHandler.isFullscreen && !isInPictureInPicture && !isAdPlaying

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            if (player != null) {
                PlaybackPlayer(
                    player = player,
                    state = state,
                    isSterUIEnabled = isSterUIEnabled,
                    fullScreenHandler = fullScreenHandler,
                    onBack = { activity.onBackPressedDispatcher.onBackPressed() },
                    onCustomSettingsClick = { showSettings = true },
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if (showControls) {
            PlaybackControls(
                showRetry = state.error?.allowRetry == true,
                playbackState = playerState?.playbackState,
                onRetry = viewModel::retry,
                onSwitchStreams = viewModel::playRandom,
                onPlayPause = { player?.apply { if (isPlaying) pause() else play() } },
            )
        }
    }

    if (showSettings && player != null) {
        PlayerSettingsDialog(player = player, onDismiss = { showSettings = false })
    }
}
