package nl.npo.player.sampleApp.shared.data.player

import androidx.core.graphics.toColorInt
import kotlinx.coroutines.flow.first
import nl.npo.player.library.domain.player.model.NPOBufferConfig
import nl.npo.player.library.domain.streamLink.model.StreamChapterType
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.model.Styling
import kotlin.time.Duration

internal data class PlayerConfigSettings(
    val pauseOnSwitchToCellularNetwork: Boolean,
    val pauseWhenBecomingNoisy: Boolean,
    val chapterSkippingEnabled: Boolean,
) {
    fun toNPOPlayerConfig() =
        NPOPlayerConfig(
            shouldPauseOnSwitchToCellularNetwork = pauseOnSwitchToCellularNetwork,
            shouldPauseWhenBecomingNoisy = pauseWhenBecomingNoisy,
            bufferConfig = NPOBufferConfig(),
            allowedToSkipFollowingChapterTypes =
                if (chapterSkippingEnabled) {
                    listOf(
                        StreamChapterType.IDENT,
                        StreamChapterType.INTRO,
                        StreamChapterType.RECAP,
                        StreamChapterType.CREDITS,
                    )
                } else {
                    emptyList()
                },
        )
}

internal suspend fun SettingsRepository.getPlayerConfigSettings() =
    PlayerConfigSettings(
        pauseOnSwitchToCellularNetwork = pauseOnSwitchToCellularNetwork.first(),
        pauseWhenBecomingNoisy = pauseWhenBecomingNoisy.first(),
        chapterSkippingEnabled = chapterSkippingEnabled.first(),
    )

internal suspend fun SettingsRepository.getPlayerColors(): NativePlayerColors? =
    if (styling.first() == Styling.Custom) {
        NativePlayerColors(
            textColor = "#FFFF0000".toColorInt(),
            onOverlay = "#FF00FF00".toColorInt(),
            primary = "#FF00FF00".toColorInt(),
        )
    } else {
        null
    }

internal suspend fun SettingsRepository.getPlayerUIConfig(): NPOPlayerUIConfig =
    if (chapterSkippingAlwaysFeatured.first()) {
        NPOPlayerUIConfig(
            maximumTimeToShowChapterSkipButton = Duration.INFINITE,
            isLockUiEnabled = true,
        )
    } else {
        NPOPlayerUIConfig(isLockUiEnabled = true)
    }
