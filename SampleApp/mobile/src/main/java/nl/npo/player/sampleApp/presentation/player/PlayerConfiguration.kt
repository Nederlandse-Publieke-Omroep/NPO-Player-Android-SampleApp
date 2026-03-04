package nl.npo.player.sampleApp.presentation.player

import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig

data class PlayerConfiguration(
   val playerConfig: NPOPlayerConfig,
   val npoPlayerColors: NativePlayerColors?,
   val useExoplayer: Boolean,
   val playerUIConfig: NPOPlayerUIConfig,
    val pageTracker: PlayerPageTracker?
)
