package nl.npo.player.sampleApp.tv.presentation.playback

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.presentation.compose.theme.PlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel
    @Inject
    constructor() : ViewModel() {
        private val _player = MutableStateFlow<NPOPlayer?>(null)
        val player = _player.asStateFlow()

        private val _playerColors = MutableStateFlow(PlayerColors())
        val playerColors = _playerColors.asStateFlow()
        private val _customPlayerUI = MutableStateFlow<Boolean>(false)
        val customPlayerUI = _customPlayerUI.asStateFlow()
        private val _playerUIConfig = MutableStateFlow(NPOPlayerUIConfig())
        val playerUIConfig = _playerUIConfig.asStateFlow()

        fun setPlayer(player: NPOPlayer) {
            _player.value = player
        }

        fun setPlayerColors(colors: PlayerColors) {
            _playerColors.value = colors
            _customPlayerUI.tryEmit(true)
        }

        fun setPlayerUIConfig(uIConfig: NPOPlayerUIConfig) {
            _playerUIConfig.value = uIConfig
        }
    }
