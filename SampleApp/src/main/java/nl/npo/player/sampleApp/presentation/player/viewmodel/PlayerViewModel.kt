package nl.npo.player.sampleApp.presentation.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import nl.npo.player.library.presentation.model.NPOUiConfig
import nl.npo.player.sampleApp.domain.SettingsRepository
import nl.npo.player.sampleApp.domain.TokenProvider
import nl.npo.player.sampleApp.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.domain.model.Styling
import nl.npo.player.sampleApp.domain.model.UserType
import nl.npo.player.sampleApp.model.SourceWrapper
import nl.npo.player.sampleApp.model.StreamRetrievalState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val mutableState =
            MutableLiveData<StreamRetrievalState>(StreamRetrievalState.NotStarted)
        val retrievalState: LiveData<StreamRetrievalState> = mutableState

        fun retrieveSource(item: SourceWrapper) {
            mutableState.postValue(StreamRetrievalState.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                val isPlusUser = settingsRepository.userType.first() == UserType.Plus
                when (val result = tokenProvider.createToken(item.uniqueId, isPlusUser)) {
                    is StreamInfoResult.Success -> {
                        try {
                            val autoPlay = settingsRepository.autoPlayEnabled.first()
                            val source =
                                NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(result.data.token))
                            mutableState.postValue(
                                StreamRetrievalState.Success(
                                    source.copy(
                                        overrideStartOffset = item.startOffset,
                                        overrideImageUrl = item.getImageUrl(source),
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
                                            if (item.overrideStreamLinkTitleAndDescription) {
                                                item.title
                                            } else {
                                                source.title
                                            },
                                        overrideDescription =
                                            if (item.overrideStreamLinkTitleAndDescription) {
                                                "SampleApp override description: ${item.testingDescription}"
                                            } else {
                                                source.description
                                            },
                                        overrideNicamContentDescription =
                                            item.overrideNicamContentDescription
                                                ?: source.nicamContentDescription,
                                    ),
                                    item,
                                ),
                            )
                        } catch (throwable: NPOPlayerException) {
                            postError(throwable.toNPOPlayerError(), item)
                        }
                    } // Happy path
                    is StreamInfoResult.Error -> {
                        postError(NPOPlayerError.StreamLink.AuthorisationFailed(NPOPlayerException.AUTHORISATION_FAILED), item)
                    }
                }
            }
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

        private fun postError(
            error: NPOPlayerError,
            sourceWrapper: SourceWrapper,
        ) {
            mutableState.postValue(
                StreamRetrievalState.Error(
                    error,
                    sourceWrapper,
                ),
            )
        }

        fun getConfiguration(callback: (NPOPlayerConfig, NPOUiConfig, Boolean, NPOPlayerColors?) -> Unit) {
            viewModelScope.launch {
                val playerConfig =
                    NPOPlayerConfig(
                        shouldPauseOnSwitchToCellularNetwork = settingsRepository.pauseOnSwitchToCellularNetwork.first(),
                        shouldPauseWhenBecomingNoisy = settingsRepository.pauseWhenBecomingNoisy.first(),
                        bufferConfig = NPOBufferConfig(),
                    )

                val uiConfig =
                    if (settingsRepository.showUi.first()) {
                        NPOUiConfig.WebUi(
                            supplementalCssLocation =
                                if (settingsRepository.styling.first() == Styling.Custom) {
                                    "file:///android_asset/player_supplemental_styling.css"
                                } else {
                                    null
                                },
                        )
                    } else {
                        NPOUiConfig.Disabled
                    }
                val npoPlayerColors =
                    if (settingsRepository.styling.first() == Styling.Custom) {
                        NPOPlayerColors(textColor = 0xFFFF0000, iconColor = 0xFF00FF00, primaryColor = 0xFF00FF00)
                    } else {
                        null
                    }

                callback(
                    playerConfig,
                    uiConfig,
                    settingsRepository.showNativeUIPlayer.first(),
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
