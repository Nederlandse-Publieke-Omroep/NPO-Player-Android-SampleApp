package nl.npo.player.sampleApp.presentation.player

import android.content.Context
import android.util.Log
import com.google.android.datatransport.runtime.scheduling.SchedulingConfigModule_ConfigFactory.config
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.ext.mediaSession
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.presentation.viewmodel.UseExoplayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val factory: NPOPlayerFactory,
    ): PlayerRepository {

    private val _player = MutableStateFlow<NPOPlayer?>(null)
    override val player: StateFlow<NPOPlayer?> = _player


    private var lastConfig: NPOPlayerConfig? = null
    private var lastUseExo: Boolean? = null

    override suspend fun ensurePlayer(
        context: Context,
        playerConfig: NPOPlayerConfig,
        npoPlayerColors: NativePlayerColors?,
        useExoplayer: Boolean,
        playerUIConfig: NPOPlayerUIConfig,
        pageTracker: PlayerPageTracker,
    ): NPOPlayer {
            val current = _player.value
            val shouldRebuild =
                current == null || lastConfig != playerConfig || lastUseExo != useExoplayer

            factory.create(
                context = appContext,
                playerConfig = playerConfig,
                npoPlayerColors = npoPlayerColors,
                useExoplayer = useExoplayer,
                playerUIConfig,
                pageTracker
                ).apply {
                    _player.value = this
            }.apply { Log.d("NPOPlayerFactoryImpl", "player created = $this") }

            lastConfig = playerConfig
            lastUseExo = useExoplayer
return player.value!!

    }

    override suspend fun release() {
        TODO("Not yet implemented")
    }

    override fun loadStreamConfig(config: NPOSourceConfig) {
        TODO("Not yet implemented")
    }

    override fun loadOffline(config: NPOOfflineSourceConfig) {
        TODO("Not yet implemented")
    }
}
