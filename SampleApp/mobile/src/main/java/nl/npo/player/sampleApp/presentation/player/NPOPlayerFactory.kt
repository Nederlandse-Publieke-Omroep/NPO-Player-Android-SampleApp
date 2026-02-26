package nl.npo.player.sampleApp.presentation.player

import android.content.Context
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig

interface NPOPlayerFactory {
    fun create(
        context: Context,
        playerConfig: NPOPlayerConfig,
               useExoplayer: Boolean,
        pageTracker: PlayerPageTracker,
    ): NPOPlayer
}
