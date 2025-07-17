package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.extensions.toNPOPlayerError
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.enums.CastMediaType
import nl.npo.player.library.domain.player.model.NPOBufferConfig
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
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
                fetchAndMergeSource(item)?.let(callback)
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

        private suspend fun fetchAndMergeSource(sourceWrapper: SourceWrapper): StreamRetrievalState? {
            val isPlusUser =
                sourceWrapper.overrideIsPlusUser
                    ?: (settingsRepository.userType.first() == UserType.Plus)
            val token = createToken(sourceWrapper.uniqueId, isPlusUser) ?: return null

            return try {
                val source = NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
                val mergedSource = mergeSourceWrapperWithSource(sourceWrapper, source)
                StreamRetrievalState.Success(mergedSource, sourceWrapper)
            } catch (e: NPOPlayerException) {
                StreamRetrievalState.Error(e.toNPOPlayerError(), sourceWrapper)
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
                val playNextType = settingsRepository.shouldShowPlayNext.first()
                npoPlayer.loadStream(
                    npoSourceConfig.copy(overrideAutoPlay = autoPlay),
                    playNextType,
                )
            }
        }

        fun getConfiguration(callback: (NPOPlayerConfig, NativePlayerColors?) -> Unit) {
            viewModelScope.launch {
                val playerConfig =
                    NPOPlayerConfig(
                        shouldPauseOnSwitchToCellularNetwork = settingsRepository.pauseOnSwitchToCellularNetwork.first(),
                        shouldPauseWhenBecomingNoisy = settingsRepository.pauseWhenBecomingNoisy.first(),
                        bufferConfig = NPOBufferConfig(),
                    )

                val npoPlayerColors =
                    if (settingsRepository.styling.first() == Styling.Custom) {
                        NativePlayerColors(
                            textColor = "#FFFF0000".toColorInt(),
                            onOverlay = "#FF00FF00".toColorInt(),
                            primary = "#FF00FF00".toColorInt(),
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
