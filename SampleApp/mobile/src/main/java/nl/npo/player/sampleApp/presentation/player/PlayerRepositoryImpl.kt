package nl.npo.player.sampleApp.presentation.player

import android.content.Context
import android.util.Log
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.bitmovin.player.api.PlayerConfig
import com.google.android.datatransport.runtime.scheduling.SchedulingConfigModule_ConfigFactory.config
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.ext.mediaSession
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.data.settings.SettingsPreferences.Keys.useExoplayer
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val factory: NPOPlayerFactory,
    private val settingsRepository: SettingsRepository,
    ): PlayerRepository {
        private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _player = MutableStateFlow<NPOPlayer?>(null)
    override val player: StateFlow<NPOPlayer?> = _player

    @Volatile private var bootstrap: PlayerConfiguration? = null
    private val _session = MutableStateFlow<MediaSession?>(null)
    override val session: StateFlow<MediaSession?> = _session

    val pageTracker =
        (appContext as PlayerApplication)
            .npoTag
            ?.pageTrackerBuilder()
            ?.withPageName("playbackservice")
            ?.build()

    override fun provideBootstrap(b: PlayerConfiguration) {
        bootstrap = b
    }

    override  fun ensurePlayer(
        context: Context
    ): NPOPlayer {
        val current = _player.value
        val currentSession = _session.value
        val pageTracker = pageTracker ?: return player.value!!
        if (current != null ) return current
        if (currentSession != null ) return  current!!

      val player =  factory.create(
                context = appContext,
                playerConfig = NPOPlayerConfig(),
                useExoplayer = true,
                pageTracker = PlayerTagProvider.getPageTracker(pageTracker)
                ).apply {
                    _player.value = this

            }.apply { Log.d("DEBUG_INFO", "player created = $this, mediasession=${this.mediaSession}") }


        return player

    }



    override suspend fun release() {
        repoScope.cancel()
        player.value?.unload()
    }

    override fun loadStreamConfig( config: NPOSourceConfig) {
        repoScope.launch {
            val autoPlay = settingsRepository.autoPlayEnabled.first()
            val playNextType = settingsRepository.shouldShowPlayNext.first()
            player.value?.load(config.copy(overrideAutoPlay = autoPlay), playNextType)
        }
    }

    override fun loadOffline(config: NPOOfflineSourceConfig) {
        repoScope.launch {
            val autoPlay = settingsRepository.autoPlayEnabled.first()
            val playNextType = settingsRepository.shouldShowPlayNext.first()
            player.value?.load(config.copy(overrideAutoPlay = autoPlay), playNextType)
        }

    }
}
