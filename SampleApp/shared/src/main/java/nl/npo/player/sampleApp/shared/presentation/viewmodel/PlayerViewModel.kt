package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.data.player.StreamSourceResolver
import nl.npo.player.sampleApp.shared.data.player.getPlayerColors
import nl.npo.player.sampleApp.shared.data.player.getPlayerConfigSettings
import nl.npo.player.sampleApp.shared.data.player.getPlayerUIConfig
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val streamSourceResolver: StreamSourceResolver,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val isSterUIEnabled: StateFlow<Boolean> =
            settingsRepository.sterUiEnabled.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialValue = false,
            )

        fun retrieveSource(
            item: SourceWrapper,
            callback: (StreamRetrievalState) -> Unit,
        ) {
            viewModelScope.launch {
                callback(StreamRetrievalState.Loading)
                streamSourceResolver.resolve(item)?.let(callback)
            }
        }

        fun loadStream(
            npoPlayer: NPOPlayer,
            npoSourceConfig: NPOSourceConfig,
        ) {
            viewModelScope.launch {
                val autoPlay = settingsRepository.autoPlayEnabled.first()
                val playNextType = settingsRepository.shouldShowPlayNext.first()
                npoPlayer.load(
                    npoSourceConfig.copy(overrideAutoPlay = autoPlay),
                    playNextType,
                )
            }
        }

        fun getConfiguration(callback: (NPOPlayerConfig, NativePlayerColors?, NPOPlayerUIConfig) -> Unit) {
            viewModelScope.launch {
                callback(
                    settingsRepository.getPlayerConfigSettings().toNPOPlayerConfig(),
                    settingsRepository.getPlayerColors(),
                    settingsRepository.getPlayerUIConfig(),
                )
            }
        }

        fun onlyStreamLinkRandomEnabled(callback: (Boolean) -> Unit) {
            viewModelScope.launch {
                callback(settingsRepository.onlyStreamLinkRandomEnabled.first())
            }
        }
    }
