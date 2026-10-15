package nl.npo.player.sampleApp.presentation.compose.views

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.state.AdState
import nl.npo.player.library.domain.state.PlaybackState
import nl.npo.player.library.presentation.PlayerUI
import nl.npo.player.library.presentation.compose.ads.NoAdOverlayRenderer
import nl.npo.player.library.presentation.compose.components.PlayerIcon
import nl.npo.player.library.presentation.compose.components.PlayerIconButton
import nl.npo.player.library.presentation.compose.models.SettingType
import nl.npo.player.library.presentation.compose.state.NPOPlayerUIState
import nl.npo.player.library.presentation.compose.state.rememberNPOPlayerUIState
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.compose.theme.PlayerTypography
import nl.npo.player.library.presentation.compose.theme.toPlayerColors
import nl.npo.player.library.presentation.extension.getMessage
import nl.npo.player.library.presentation.mobile.compose.components.DefaultMobilePlayerComponents
import nl.npo.player.library.presentation.mobile.compose.components.MobilePlayerTopBar
import nl.npo.player.library.presentation.mobile.compose.scene.MobileSceneRenderer
import nl.npo.player.library.presentation.mobile.compose.theme.mobile
import nl.npo.player.library.presentation.pip.DefaultNPOPictureInPictureHandler
import nl.npo.player.library.sterads.presentation.ui.MobileSterOverlayRenderer
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.CastButton
import nl.npo.player.sampleApp.presentation.ext.isGooglePlayServicesAvailable
import nl.npo.player.sampleApp.presentation.player.ActivityFullScreenHandler
import nl.npo.player.sampleApp.shared.model.PlaybackUiState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlayerViewModel

@Composable
fun PlaybackScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity
    val context = LocalContext.current
    val state by viewModel.playbackState.collectAsStateWithLifecycle()
    val isSterUIEnabled by viewModel.isSterUIEnabled.collectAsStateWithLifecycle()
    val fullScreenHandler = remember(activity) { ActivityFullScreenHandler(activity) }
    val isInPictureInPicture by rememberIsInPictureInPicture(activity)
    val isCastButtonAvailable = remember(activity) { NPOCasting.isCastingEnabled && activity.isGooglePlayServicesAvailable() }
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
                Player(
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
            if (isCastButtonAvailable && !isInPictureInPicture && !isAdPlaying) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp),
                ) {
                    CastButton()
                }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Player(
    player: NPOPlayer,
    state: PlaybackUiState,
    isSterUIEnabled: Boolean,
    fullScreenHandler: ActivityFullScreenHandler,
    onBack: () -> Unit,
    onCustomSettingsClick: () -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity
    val pipHandler = remember(activity, player) { DefaultNPOPictureInPictureHandler(activity, player) }
    val uiState = rememberNPOPlayerUIState(player)

    DisposableEffect(activity, pipHandler) {
        val onUserLeaveHint = Runnable { if (player.isPlaying) pipHandler.enterPictureInPicture() }
        activity.addOnUserLeaveHintListener(onUserLeaveHint)
        onDispose { activity.removeOnUserLeaveHintListener(onUserLeaveHint) }
    }
    LifecycleResumeEffect(key1 = pipHandler) {
        if (!activity.isInPictureInPictureMode) pipHandler.exitPictureInPicture()
        onPauseOrDispose { }
    }

    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && !fullScreenHandler.isFullscreen) {
            uiState.setFullScreen(true)
        }
    }

    LaunchedEffect(uiState, state.playerUIConfig, state.showCustomSettings) {
        uiState.setUIConfig(state.playerUIConfig)
        uiState.setFullScreenHandler(fullScreenHandler)
        uiState.setPipHandler(pipHandler)
        if (state.showCustomSettings) {
            uiState.settings.setSettingsOverride(
                listOf(
                    object : SettingType.Custom {
                        override val id: String = "custom_settings"
                        override val label: String = "Open custom settings"
                    },
                ),
            )
            uiState.settings.setCustomSettingsClickListener { onCustomSettingsClick() }
        }
    }

    val sceneOverlays =
        remember(player, isSterUIEnabled) {
            MobileSceneRenderer(
                if (isSterUIEnabled) {
                    MobileSterOverlayRenderer(
                        onBackAction = onBack,
                        castButton = { CastButton() },
                    )
                } else {
                    NoAdOverlayRenderer
                },
            )
        }
    val components =
        remember(player, state.npoPlayerColors) {
            if (state.npoPlayerColors != null) {
                CustomPlayerComponents(onBack)
            } else {
                DefaultMobilePlayerComponents()
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        PlayerUI.Surface(
            modifier = Modifier.fillMaxSize(),
            state = uiState,
        )
        PlayerUI.Overlay(
            modifier = Modifier.fillMaxSize(),
            state = uiState,
            typography = PlayerTypography.mobile(),
            sceneOverlays = sceneOverlays,
            npoPlayerColors = (state.npoPlayerColors ?: NativePlayerColors()).toPlayerColors(),
            components = components,
        )
    }
}

@Composable
private fun PlaybackControls(
    showRetry: Boolean,
    playbackState: PlaybackState?,
    onRetry: () -> Unit,
    onSwitchStreams: () -> Unit,
    onPlayPause: () -> Unit,
) {
    val accent = colorResource(R.color.orange)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        ControlButton(
            icon = Icons.Default.Shuffle,
            label = stringResource(R.string.playback_random),
            color = colorResource(R.color.white),
            onClick = onSwitchStreams,
        )
        if (showRetry) {
            ControlButton(
                icon = Icons.Default.Refresh,
                label = stringResource(R.string.retry_btn_title),
                color = accent,
                onClick = onRetry,
            )
        }
        if (playbackState != null && playbackState.isWithin(PlaybackState.Loaded, PlaybackState.Paused())) {
            val isPlaying = playbackState == PlaybackState.Playing
            ControlButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                label = stringResource(if (isPlaying) R.string.playback_pause else R.string.playback_play),
                color = accent,
                onClick = onPlayPause,
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Text(text = label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
private fun rememberIsInPictureInPicture(activity: ComponentActivity): State<Boolean> {
    val isInPictureInPicture = remember(activity) { mutableStateOf(activity.isInPictureInPictureMode) }
    DisposableEffect(activity) {
        val listener = Consumer<PictureInPictureModeChangedInfo> { isInPictureInPicture.value = it.isInPictureInPictureMode }
        activity.addOnPictureInPictureModeChangedListener(listener)
        onDispose { activity.removeOnPictureInPictureModeChangedListener(listener) }
    }
    return isInPictureInPicture
}

private class CustomPlayerComponents(
    val onBackPressed: () -> Unit,
) : DefaultMobilePlayerComponents() {
    @Composable
    override fun TopControlsBar(
        modifier: Modifier,
        playerUIState: NPOPlayerUIState,
    ) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics { isTraversalGroup = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerIconButton(onClick = onBackPressed) {
                PlayerIcon(
                    painterResource(R.drawable.npo_player_ic_arrow_left),
                    stringResource(R.string.player_close),
                )
            }

            MobilePlayerTopBar(
                playerUiState = playerUIState,
                modifier = Modifier,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlaybackControlsPlayingPreview() {
    MaterialTheme {
        PlaybackControls(
            showRetry = false,
            playbackState = PlaybackState.Playing,
            onRetry = {},
            onSwitchStreams = {},
            onPlayPause = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlaybackControlsErrorPreview() {
    MaterialTheme {
        PlaybackControls(
            showRetry = true,
            playbackState = null,
            onRetry = {},
            onSwitchStreams = {},
            onPlayPause = {},
        )
    }
}
