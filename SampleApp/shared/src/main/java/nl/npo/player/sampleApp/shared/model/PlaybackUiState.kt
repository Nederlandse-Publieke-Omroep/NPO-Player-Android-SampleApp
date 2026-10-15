package nl.npo.player.sampleApp.shared.model

import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig

data class PlaybackUiState(
    val player: NPOPlayer? = null,
    val npoPlayerColors: NativePlayerColors? = null,
    val playerUIConfig: NPOPlayerUIConfig = NPOPlayerUIConfig(),
    val showCustomSettings: Boolean = false,
    val isLoading: Boolean = false,
    val error: NPOPlayerError? = null,
    val shouldClose: Boolean = false,
)
