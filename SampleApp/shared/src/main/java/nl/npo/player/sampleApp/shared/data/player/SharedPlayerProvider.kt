package nl.npo.player.sampleApp.shared.data.player

import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.ext.attachToLifecycle
import nl.npo.player.library.ext.detachFromLifecycle
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
        private var playerSettings: PlayerConfigSettings? = null
        private val mutex = Mutex()

        suspend fun getPlayer(pageTracker: PlayerPageTracker): NPOPlayer {
            mutex.withLock {
                val settings = settingsRepository.getPlayerConfigSettings()
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
    }
