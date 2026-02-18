package nl.npo.player.sampleApp.presentation.player

import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.compose.theme.PlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.presentation.viewmodel.UseExoplayer

data class PlayerBuildConfig(
    val playerConfig: NPOSourceConfig,
    val useExoplayer: Boolean,
    val uiConfig: NPOPlayerUIConfig,
    val colors: NativePlayerColors?
)
