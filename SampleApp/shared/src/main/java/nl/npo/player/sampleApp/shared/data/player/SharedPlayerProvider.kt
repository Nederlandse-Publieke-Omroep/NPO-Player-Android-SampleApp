package nl.npo.player.sampleApp.shared.data.player

import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.first
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOBufferConfig
import nl.npo.player.library.domain.streamLink.model.StreamChapterType
import nl.npo.player.library.ext.attachToLifecycle
import nl.npo.player.library.ext.detachFromLifecycle
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.shared.data.ads.AdManagerProvider
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPlayerProvider
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val progressStorageProvider: ProgressStorageProvider,
    ) {
        private val processLifecycle get() = ProcessLifecycleOwner.get().lifecycle
        private var player: NPOPlayer? = null
        private var playerSettings: PlayerSettings? = null

        suspend fun getPlayer(pageTracker: PlayerPageTracker): NPOPlayer {
            val settings = settingsRepository.getPlayerSettings()
            player?.let { current ->
                if (settings == playerSettings) {
                    current.updatePageTracker(pageTracker)
                    return current
                }
                current.detachFromLifecycle(processLifecycle)
                current.destroy()
            }

            return NPOPlayerLibrary
                .getPlayer(
                    npoPlayerConfig = settings.toNPOPlayerConfig(),
                    pageTracker = pageTracker,
                    adManager = AdManagerProvider.getAdManager(),
                    progressStorageProvider = progressStorageProvider,
                ).also {
                    it.attachToLifecycle(processLifecycle)
                    player = it
                    playerSettings = settings
                }
        }
    }

internal data class PlayerSettings(
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

internal suspend fun SettingsRepository.getPlayerSettings() =
    PlayerSettings(
        pauseOnSwitchToCellularNetwork = pauseOnSwitchToCellularNetwork.first(),
        pauseWhenBecomingNoisy = pauseWhenBecomingNoisy.first(),
        chapterSkippingEnabled = chapterSkippingEnabled.first(),
    )
