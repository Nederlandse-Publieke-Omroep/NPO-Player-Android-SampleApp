package nl.npo.player.sampleApp.presentation.player

import android.content.Context
import androidx.media3.session.MediaSession
import com.bitmovin.player.api.offline.OfflineSourceConfig
import com.bitmovin.player.api.source.SourceConfig
import kotlinx.coroutines.flow.StateFlow
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig

interface PlayerRepository {
    val player: StateFlow<NPOPlayer?>
    val session: StateFlow<MediaSession?>


//    fun getPlayer(
//        context: Context,
//        playerConfig: NPOPlayerConfig,
//        npoPlayerColors: NativePlayerColors?,
//        useExoplayer: Boolean,
//        playerUIConfig: NPOPlayerUIConfig,
//        pageTracker: PlayerPageTracker,
//    ): NPOPlayer

    fun ensurePlayer(context: Context, ): NPOPlayer
    suspend fun release()
    fun provideBootstrap(b: PlayerConfiguration)
    fun loadStreamConfig(config: NPOSourceConfig)
    fun loadOffline(config: NPOOfflineSourceConfig)
}
