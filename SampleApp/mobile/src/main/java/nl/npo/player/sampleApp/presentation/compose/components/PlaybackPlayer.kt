package nl.npo.player.sampleApp.presentation.compose.components

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import nl.npo.player.library.domain.player.NPOPlayer
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
import nl.npo.player.library.presentation.mobile.compose.components.DefaultMobilePlayerComponents
import nl.npo.player.library.presentation.mobile.compose.components.MobilePlayerTopBar
import nl.npo.player.library.presentation.mobile.compose.scene.MobileSceneRenderer
import nl.npo.player.library.presentation.mobile.compose.theme.mobile
import nl.npo.player.library.presentation.pip.DefaultNPOPictureInPictureHandler
import nl.npo.player.library.sterads.presentation.ui.MobileSterOverlayRenderer
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.player.ActivityFullScreenHandler
import nl.npo.player.sampleApp.shared.model.PlaybackUiState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaybackPlayer(
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

    val customSettingsLabel = stringResource(R.string.open_custom_settings)
    LaunchedEffect(uiState, state.playerUIConfig, state.showCustomSettings) {
        uiState.setUIConfig(state.playerUIConfig)
        uiState.setFullScreenHandler(fullScreenHandler)
        uiState.setPipHandler(pipHandler)
        if (state.showCustomSettings) {
            uiState.settings.setSettingsOverride(
                listOf(
                    object : SettingType.Custom {
                        override val id: String = "custom_settings"
                        override val label: String = customSettingsLabel
                    },
                ),
            )
            uiState.settings.setCustomSettingsClickListener { onCustomSettingsClick() }
        } else {
            uiState.settings.setSettingsOverride(null)
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
