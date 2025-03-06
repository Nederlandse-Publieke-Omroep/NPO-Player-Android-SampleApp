package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.extensions.toNPOPlayerError
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.enums.CastMediaType
import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.domain.player.model.NPOBufferConfig
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.player.ui.model.NPOPlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _streamRetrievalState =
            MutableStateFlow<StreamRetrievalState>(StreamRetrievalState.NotStarted)
        val streamRetrievalState = _streamRetrievalState.asLiveData()

        fun retrieveSource(item: SourceWrapper) {
            viewModelScope.launch {
                _streamRetrievalState.emit(StreamRetrievalState.Loading)
                val mergedSource = fetchAndMergeSource(item) ?: return@launch
                _streamRetrievalState.emit(StreamRetrievalState.Success(mergedSource, item))
            }
        }

        private suspend fun createToken(
            itemId: String,
            isPlusUser: Boolean,
        ): String? =
            when (val tokenResult = tokenProvider.createToken(itemId, isPlusUser)) {
                is StreamInfoResult.Success -> tokenResult.data.token
                else -> {
                    null
                }
            }

        private suspend fun fetchAndMergeSource(sourceWrapper: SourceWrapper): NPOSourceConfig? {
            val isPlusUser = sourceWrapper.overrideIsPlusUser ?: (settingsRepository.userType.first() == UserType.Plus)
            val token = createToken(sourceWrapper.uniqueId, isPlusUser) ?: return null

            return try {
                val source = NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
                mergeSourceWrapperWithSource(sourceWrapper, source)
            } catch (e: NPOPlayerException) {
                handleError(e.toNPOPlayerError(), sourceWrapper)
                null
            }
        }

        private suspend fun mergeSourceWrapperWithSource(
            sourceWrapper: SourceWrapper,
            source: NPOSourceConfig,
        ): NPOSourceConfig {
            val autoPlay = settingsRepository.autoPlayEnabled.first()
            return source.copy(
                overrideStartOffset = sourceWrapper.startOffset,
                overrideImageUrl = sourceWrapper.getImageUrl(source),
                overrideAutoPlay = autoPlay,
                overrideMetadata =
                    source.metadata
                        ?.toMutableMap()
                        ?.apply {
                            set(
                                "appletest",
                                "true",
                            )
                        },
                // We add this so the Cast Receiver shows the debug log when casting.
                overrideTitle =
                    if (sourceWrapper.overrideStreamLinkTitleAndDescription) {
                        sourceWrapper.title
                    } else {
                        source.title
                    },
                overrideDescription =
                    if (sourceWrapper.overrideStreamLinkTitleAndDescription) {
                        "SampleApp override description: ${sourceWrapper.testingDescription}"
                    } else {
                        source.description
                    },
                overrideNicamContentDescription =
                    sourceWrapper.overrideNicamContentDescription
                        ?: source.nicamContentDescription,
            )
        }

        private fun SourceWrapper.getImageUrl(npoSourceConfig: NPOSourceConfig): String? =
            if (preferThisImageUrlOverStreamLink) {
                imageUrl ?: npoSourceConfig.imageUrl
            } else {
                npoSourceConfig.imageUrl ?: imageUrl
            }

        fun loadStream(
            npoPlayer: NPOPlayer,
            npoSourceConfig: NPOSourceConfig,
        ) {
            viewModelScope.launch {
                val autoPlay = settingsRepository.autoPlayEnabled.first()
                npoPlayer.loadStream(
                    npoSourceConfig.copy(overrideAutoPlay = autoPlay),
                    settingsRepository.shouldShowPlayNext.first(),
                )
            }
        }

        private suspend fun handleError(
            error: NPOPlayerError,
            sourceWrapper: SourceWrapper,
        ) {
            _streamRetrievalState.emit(
                StreamRetrievalState.Error(
                    error,
                    sourceWrapper,
                ),
            )
        }

        fun getConfiguration(callback: (NPOPlayerConfig, NPOPlayerColors?) -> Unit) {
            viewModelScope.launch {
                val playerConfig =
                    NPOPlayerConfig(
                        shouldPauseOnSwitchToCellularNetwork = settingsRepository.pauseOnSwitchToCellularNetwork.first(),
                        shouldPauseWhenBecomingNoisy = settingsRepository.pauseWhenBecomingNoisy.first(),
                        bufferConfig = NPOBufferConfig(),
                    )

                val npoPlayerColors =
                    if (settingsRepository.styling.first() == Styling.Custom) {
                        NPOPlayerColors(
                            textColor = 0xFFFF0000,
                            iconColor = 0xFF00FF00,
                            primaryColor = 0xFF00FF00,
                            settingsTextColor = 0xFFFFEB3B,
                            settingsSurfaceColor = 0xFF770099,
                        )
                    } else {
                        null
                    }

                callback(
                    playerConfig,
                    npoPlayerColors,
                )
            }
        }

        fun hasCustomSettings(callback: () -> Unit) {
            viewModelScope.launch {
                if (settingsRepository.showCustomSettings.first()) {
                    callback()
                }
            }
        }

        fun onlyStreamLinkRandomEnabled(callback: (Boolean) -> Unit) {
            viewModelScope.launch {
                callback(settingsRepository.onlyStreamLinkRandomEnabled.first())
            }
        }

        fun shouldAddSterOverlay(callback: () -> Unit) {
            viewModelScope.launch {
                if (settingsRepository.sterUiEnabled.first()) {
                    callback()
                }
            }
        }

        companion object {
            val remoteCallback: ((NPOSourceConfig) -> CastMediaType) = { source ->
                if (source.avType == AVType.AUDIO || source.streamUrl.contains("mp3")) {
                    CastMediaType.MusicTrack
                } else if (source.avType == AVType.VIDEO) {
                    CastMediaType.Movie
                } else {
                    CastMediaType.Generic
                }
            }
        }
    }
