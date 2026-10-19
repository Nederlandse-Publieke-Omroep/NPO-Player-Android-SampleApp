package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.extensions.toNPOPlayerError
import nl.npo.player.library.data.offline.model.NPOMedia3OfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.enums.CastMediaType
import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.player.ui.model.PlayNextListenerResult
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.extension.getMessage
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.data.player.SharedPlayerProvider
import nl.npo.player.sampleApp.shared.data.player.getPlayerConfigSettings
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.model.PlaybackUiState
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import nl.npo.tag.sdk.tracker.PageTracker
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
        private val sharedPlayerProvider: SharedPlayerProvider,
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        @URLLinkRepository private val urlLinkRepository: LinkRepository,
    ) : ViewModel() {
        private val _playbackState = MutableStateFlow(PlaybackUiState())
        val playbackState: StateFlow<PlaybackUiState> = _playbackState
        private var currentSource: SourceWrapper? = null

        private val playerListener =
            object : PlayerListener {
                override fun onSourceLoad(source: NPOSourceConfig) {
                    // NOTE: This is not done to actually seek, but to make sure that if an app does this it won't crash. An error should be broadcasted through `onPlayerError`
                    if (!NPOCasting.isCastingConnected()) _playbackState.value.player?.seek(10000.0.seconds)
                }

                override fun onCanStartPlayingBecauseSwitchedToWiFi() {
                    _playbackState.value.player?.play()
                }

                override fun onCanStartPlayingBecauseResumingAfterCasting() {
                    _playbackState.value.player?.play()
                }

                override fun onPlayerError(
                    error: NPOPlayerError,
                    retryPossible: Boolean,
                ) {
                    Log.w("SampleAppTest", "Error: code: ${error.errorCode}: ${error.getMessage(context)}")
                }
            }

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
                is StreamInfoResult.Success -> {
                    tokenResult.data.token
                }

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
                val mergedSource =
                    sourceWrapper
                        .mergeSourceWrapperWithSource(source)
                        .copy(overrideAutoPlay = settingsRepository.autoPlayEnabled.first())
                StreamRetrievalState.Success(mergedSource, sourceWrapper)
            } catch (e: NPOPlayerException) {
                e.cause?.printStackTrace()
                StreamRetrievalState.Error(e.toNPOPlayerError(), sourceWrapper)
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
                val playerConfig = settingsRepository.getPlayerConfigSettings().toNPOPlayerConfig()

                val npoPlayerColors = getPlayerColors()
                val playerUIConfig = getPlayerUIConfig()

                callback(
                    playerConfig,
                    npoPlayerColors,
                    playerUIConfig,
                )
            }
        }

        fun play(sourceWrapper: SourceWrapper) {
            currentSource = sourceWrapper
            viewModelScope.launch {
                // Note: Updating the page tracker per stream is only to simulate switching pages. A normal app would only do this when switching to a new page with the same player.
                val player = sharedPlayerProvider.getPlayer(createPageTracker(sourceWrapper.title.orEmpty()))
                if (player != _playbackState.value.player) bindPlayer(player)
                _playbackState.update {
                    it.copy(
                        npoPlayerColors = getPlayerColors(),
                        playerUIConfig = getPlayerUIConfig(),
                        showCustomSettings = settingsRepository.showCustomSettings.first(),
                    )
                }

                val sourceConfig = sourceWrapper.npoSourceConfig
                when {
                    sourceConfig is NPOMedia3OfflineSourceConfig -> load(player, sourceConfig)
                    sourceWrapper.getStreamLink -> retrieveAndLoad(player, sourceWrapper)
                    sourceConfig != null -> load(player, sourceConfig)
                    else -> _playbackState.update { it.copy(shouldClose = true) }
                }
            }
        }

        private suspend fun getPlayerColors(): NativePlayerColors? =
            if (settingsRepository.styling.first() == Styling.Custom) {
                NativePlayerColors(
                    textColor = "#FFFF0000".toColorInt(),
                    onOverlay = "#FF00FF00".toColorInt(),
                    primary = "#FF00FF00".toColorInt(),
                )
            } else {
                null
            }

        private suspend fun getPlayerUIConfig(): NPOPlayerUIConfig =
            if (settingsRepository.chapterSkippingAlwaysFeatured.first()) {
                NPOPlayerUIConfig(
                    maximumTimeToShowChapterSkipButton = Duration.INFINITE,
                    isLockUiEnabled = true,
                )
            } else {
                NPOPlayerUIConfig(isLockUiEnabled = true)
            }

        fun retry() {
            val player = _playbackState.value.player ?: return
            val sourceWrapper = currentSource ?: return
            viewModelScope.launch { retrieveAndLoad(player, sourceWrapper) }
        }

        fun playRandom() {
            _playbackState.value.player?.unload()
            viewModelScope.launch {
                val sources =
                    if (settingsRepository.onlyStreamLinkRandomEnabled.first()) {
                        streamLinkRepository.getSourceList()
                    } else {
                        streamLinkRepository.getSourceList() + urlLinkRepository.getSourceList()
                    }
                sources.randomOrNull()?.let(::play)
            }
        }

        private fun bindPlayer(player: NPOPlayer) {
            _playbackState.value.player?.let(::unbindPlayer)
            player.apply {
                eventEmitter.addListener(playerListener)
                setTokenRefreshCallback { startOffset ->
                    val sourceWrapper = currentSource ?: return@setTokenRefreshCallback
                    viewModelScope.launch {
                        retrieveAndLoad(
                            player,
                            sourceWrapper.copy(startOffset = startOffset.toDouble(DurationUnit.SECONDS)),
                        )
                    }
                }
                setPlayNextListener { action ->
                    when (action) {
                        is PlayNextListenerResult.Triggered -> playRandom()
                    }
                }
            }
            _playbackState.update { it.copy(player = player) }
        }

        private fun unbindPlayer(player: NPOPlayer) {
            player.apply {
                eventEmitter.removeListener(playerListener)
                setTokenRefreshCallback(null)
                setPlayNextListener(null)
            }
        }

        private suspend fun retrieveAndLoad(
            player: NPOPlayer,
            sourceWrapper: SourceWrapper,
        ) {
            _playbackState.update { it.copy(isLoading = true, error = null) }
            when (val result = fetchAndMergeSource(sourceWrapper)) {
                is StreamRetrievalState.Success -> load(player, result.npoSourceConfig)
                is StreamRetrievalState.Error -> {
                    player.setError(result.error)
                    _playbackState.update { it.copy(isLoading = false, error = result.error) }
                }
                else -> _playbackState.update { it.copy(isLoading = false) }
            }
        }

        private suspend fun load(
            player: NPOPlayer,
            npoSourceConfig: NPOSourceConfig,
        ) {
            _playbackState.update { it.copy(isLoading = false, error = null) }
            player.load(
                npoSourceConfig.copy(overrideAutoPlay = settingsRepository.autoPlayEnabled.first()),
                settingsRepository.shouldShowPlayNext.first(),
            )
        }

        private fun createPageTracker(title: String): PlayerPageTracker {
            val pageTracker =
                (context as PlayerApplication)
                    .npoTag
                    ?.pageTrackerBuilder()
                    ?.withPageName(title)
                    ?.build()
            return when (pageTracker) {
                is PageTracker -> PlayerTagProvider.getPageTracker(pageTracker)
                else -> PlayerTagProvider.getPageTracker(PageConfiguration(title))
            }
        }

        override fun onCleared() {
            _playbackState.value.player?.apply {
                unbindPlayer(this)
                if (!NPOCasting.isCastingConnected()) unload()
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

internal fun SourceWrapper.mergeSourceWrapperWithSource(source: NPOSourceConfig): NPOSourceConfig =
    source.copy(
        overrideStartOffset = startOffset,
        overrideImageUrl = getImageUrl(source),
//                overrideMetadata =
//                    source.metadata
//                        ?.toMutableMap()
//                        ?.apply {
//                            set(
//                                "appletest",
//                                "true",
//                            )
//                        },
        // We add this so the Cast Receiver shows the debug log when casting.
        overrideTitle =
            if (overrideStreamLinkTitleAndDescription) {
                title
            } else {
                source.title
            },
        overrideDescription =
            if (overrideStreamLinkTitleAndDescription) {
                "SampleApp override description: $testingDescription"
            } else {
                source.description
            },
        overrideNicamContentDescription =
            overrideNicamContentDescription
                ?: source.nicamContentDescription,
    )

internal fun SourceWrapper.getImageUrl(npoSourceConfig: NPOSourceConfig): String? =
    if (preferThisImageUrlOverStreamLink) {
        imageUrl ?: npoSourceConfig.imageUrl
    } else {
        npoSourceConfig.imageUrl ?: imageUrl
    }
