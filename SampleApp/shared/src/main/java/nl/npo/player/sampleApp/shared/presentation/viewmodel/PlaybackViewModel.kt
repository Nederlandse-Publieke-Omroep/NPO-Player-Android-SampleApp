package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.offline.model.NPOMedia3OfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.player.ui.model.PlayNextListenerResult
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.extension.getMessage
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.data.player.SharedPlayerProvider
import nl.npo.player.sampleApp.shared.data.player.StreamSourceResolver
import nl.npo.player.sampleApp.shared.data.player.getPlayerColors
import nl.npo.player.sampleApp.shared.data.player.getPlayerUIConfig
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.shared.model.PlaybackUiState
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import nl.npo.tag.sdk.tracker.PageTracker
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@HiltViewModel
class PlaybackViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
        private val sharedPlayerProvider: SharedPlayerProvider,
        private val streamSourceResolver: StreamSourceResolver,
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        @URLLinkRepository private val urlLinkRepository: LinkRepository,
    ) : ViewModel() {
        private val _playbackState = MutableStateFlow(PlaybackUiState())
        val playbackState: StateFlow<PlaybackUiState> = _playbackState
        private var currentSource: SourceWrapper? = null
        private var loadJob: Job? = null

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

        fun play(sourceWrapper: SourceWrapper) {
            currentSource = sourceWrapper
            launchLoad {
                // Note: Updating the page tracker per stream is only to simulate switching pages. A normal app would only do this when switching to a new page with the same player.
                val player = sharedPlayerProvider.getPlayer(createPageTracker(sourceWrapper.title.orEmpty()))
                if (player != _playbackState.value.player) bindPlayer(player)
                _playbackState.update {
                    it.copy(
                        npoPlayerColors = settingsRepository.getPlayerColors(),
                        playerUIConfig = settingsRepository.getPlayerUIConfig(),
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

        fun retry() {
            val player = _playbackState.value.player ?: return
            val sourceWrapper = currentSource ?: return
            launchLoad { retrieveAndLoad(player, sourceWrapper) }
        }

        fun onErrorShown() {
            _playbackState.update { it.copy(error = null) }
        }

        fun playRandom() {
            _playbackState.value.player?.unload()
            launchLoad {
                val sources =
                    if (settingsRepository.onlyStreamLinkRandomEnabled.first()) {
                        streamLinkRepository.getSourceList()
                    } else {
                        streamLinkRepository.getSourceList() + urlLinkRepository.getSourceList()
                    }
                sources.randomOrNull()?.let(::play)
            }
        }

        private fun launchLoad(block: suspend () -> Unit) {
            loadJob?.cancel()
            loadJob = viewModelScope.launch { block() }
        }

        private fun bindPlayer(player: NPOPlayer) {
            _playbackState.value.player?.let(::unbindPlayer)
            player.apply {
                eventEmitter.addListener(playerListener)
                setTokenRefreshCallback { startOffset ->
                    val sourceWrapper = currentSource ?: return@setTokenRefreshCallback
                    launchLoad {
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
            _playbackState.update { it.copy(isLoading = true, error = null, canRetry = false) }
            when (val result = streamSourceResolver.resolve(sourceWrapper)) {
                is StreamRetrievalState.Success -> load(player, result.npoSourceConfig)
                is StreamRetrievalState.Error -> {
                    player.setError(result.error)
                    _playbackState.update {
                        it.copy(isLoading = false, error = result.error, canRetry = result.error.allowRetry)
                    }
                }
                else -> Unit
            }
        }

        private suspend fun load(
            player: NPOPlayer,
            npoSourceConfig: NPOSourceConfig,
        ) {
            _playbackState.update { it.copy(isLoading = false, error = null, canRetry = false) }
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
    }
