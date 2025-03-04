package nl.npo.player.sampleApp.tv.presentation.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.ui.model.Cue
import nl.npo.player.library.domain.player.ui.model.NPOPlayerColors
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel
    @Inject
    constructor() : ViewModel() {
        private val _player = MutableStateFlow<NPOPlayer?>(null)
        val player = _player.asStateFlow()

        private val _playerColors = MutableStateFlow(NPOPlayerColors())
        val playerColors = _playerColors.asStateFlow()
        private val _subtitles = MutableStateFlow<List<Cue>>(emptyList())
        val subtitles = _subtitles.asStateFlow()

        fun setPlayer(player: NPOPlayer) {
            _player.value = player.also(::collectPlayerStates)
        }

        fun setPlayerColors(colors: NPOPlayerColors) {
            _playerColors.value = colors
        }

        private fun collectPlayerStates(player: NPOPlayer) {
            with(player) {
                viewModelScope.launch {
                    playerStateManager.playerState.collect { state ->
                        _subtitles.emit(state.subtitleCues)
                    }
                }
            }
        }
    }
