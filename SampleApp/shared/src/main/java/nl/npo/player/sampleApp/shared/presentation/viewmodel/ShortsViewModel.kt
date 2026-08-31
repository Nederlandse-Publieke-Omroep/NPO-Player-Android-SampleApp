package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOBufferConfig
import nl.npo.player.library.domain.player.model.NPORepeatMode
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.player.preload.NPOPreloadManager
import nl.npo.player.library.domain.player.preload.play
import nl.npo.player.library.domain.streamLink.model.StreamChapterType
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.shared.data.ads.AdManagerProvider
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.annotation.ACCShortsStreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.ShortsStreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject

@HiltViewModel
class ShortsViewModel
    @Inject
    constructor(
        @ShortsStreamLinkRepository private val prodShortsStreamLinkRepository: LinkRepository,
        @ACCShortsStreamLinkRepository private val accShortsStreamLinkRepository: LinkRepository,
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val mutableSourceWrapperList = MutableLiveData<List<SourceWrapper>>()
        private val mutableSourceConfig: MutableLiveData<List<NPOSourceConfig>> =
            MutableLiveData<List<NPOSourceConfig>>()
        val sourceConfig: LiveData<List<NPOSourceConfig>> = mutableSourceConfig
        private val _player = MutableStateFlow<NPOPlayer?>(null)
        val player = _player.asStateFlow()
        private var currentPreloadManager: NPOPreloadManager? = null
        private val preloadManager: StateFlow<NPOPreloadManager?> =
            combine(
                player,
                sourceConfig.asFlow(),
                settingsRepository.usePreloadManagerShorts,
            ) { npoPlayer, sourceConfigs, usePreloadManager ->

                if (!usePreloadManager || npoPlayer == null) {
                    currentPreloadManager?.release()
                    currentPreloadManager = null
                    return@combine null
                }

                currentPreloadManager?.let { manager ->
                    val startIndex =
                        (sourceConfigs.size - sizeOfValidSourceConfigs)
                            .coerceAtLeast(0)
                    sourceConfigs
                        .drop(startIndex)
                        .forEachIndexed { index, config ->
                            manager.addSource(
                                config,
                                startIndex + index,
                            )
                        }
                    return@combine manager
                }
                NPOPlayerLibrary.getPreloadManager().also { manager ->
                    manager.setSources(sourceConfigs)
                    currentPreloadManager = manager
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )
        private var sizeOfValidSourceConfigs = 0

        init {
            viewModelScope.launch {
                mutableSourceWrapperList.asFlow().collect { list ->
                    val mutableList = list.toMutableList()
                    mutableSourceConfig.postValue(
                        mutableList
                            .mapNotNull { sourceWrapper ->
                                fetchSourceConfig(sourceWrapper)
                            }.also { sizeOfValidSourceConfigs = it.size },
                    )
                }
            }

            getStreamLinkListItems()
        }

        fun initPlayer(context: Context) {
            viewModelScope.launch(Dispatchers.Main) {
                val pageTracker: PlayerPageTracker =
                    PlayerTagProvider.getPageTracker(
                        PageConfiguration("Shorts"),
                    )
                val playerConfig =
                    NPOPlayerConfig(
                        shouldPauseOnSwitchToCellularNetwork = settingsRepository.pauseOnSwitchToCellularNetwork.first(),
                        shouldPauseWhenBecomingNoisy = settingsRepository.pauseWhenBecomingNoisy.first(),
                        bufferConfig = NPOBufferConfig(),
                        allowedToSkipFollowingChapterTypes =
                            if (settingsRepository.chapterSkippingEnabled.first()) {
                                listOf(
                                    StreamChapterType.IDENT,
                                    StreamChapterType.INTRO,
                                    StreamChapterType.RECAP,
                                    StreamChapterType.CREDITS,
                                )
                            } else {
                                emptyList()
                            },
                    )
                _player.emit(
                    NPOPlayerLibrary
                        .getPlayer(
                            npoPlayerConfig = playerConfig,
                            pageTracker = pageTracker,
                            adManager = AdManagerProvider.getAdManager(),
                        ).also { player ->
                            player.setTokenRefreshCallback { playbackStoppedAt ->
                                viewModelScope.launch {
                                    var newSourceConfig =
                                        player.lastLoadedSource?.refreshSourceConfig() ?: return@launch
                                    newSourceConfig =
                                        newSourceConfig.copy(overrideStartOffset = playbackStoppedAt.inWholeSeconds.toDouble())
                                    withContext(Dispatchers.Main) {
                                        player.load(newSourceConfig)
                                    }
                                }
                            }
                            player.setRepeatMode(NPORepeatMode.ONE)
                        },
                )
            }
        }

        fun setCurrentPageIndex(index: Int) {
            viewModelScope.launch {
                if (settingsRepository.usePreloadManagerShorts.first()) {
                    val loader = preloadManager.first() ?: return@launch

                    extendSourceConfigListIfNeeded(index)

                    withContext(Dispatchers.Main) {
                        player.value?.let { loader.play(index, it) }
                    }
                } else {
                    player.first()?.load(sourceConfig.value!![index])
                }
            }
        }

        private fun extendSourceConfigListIfNeeded(index: Int) {
            val sourceConfigList = mutableSourceConfig.value
            if (sourceConfigList != null && index + 2 >= sourceConfigList.size) {
                if (sizeOfValidSourceConfigs == 0) return
                val subList = sourceConfigList.subList(0, sizeOfValidSourceConfigs)
                val mutableSourceConfigList = sourceConfigList.toMutableList()
                mutableSourceConfigList.addAll(subList)
                mutableSourceConfig.postValue(mutableSourceConfigList.toList())
            }
        }

        private fun getStreamLinkListItems() =
            viewModelScope.launch {
                val sourceList =
                    if (settingsRepository.environment.first() == Environment.Acceptance) {
                        accShortsStreamLinkRepository.getSourceList()
                    } else {
                        prodShortsStreamLinkRepository.getSourceList()
                    }
                mutableSourceWrapperList.postValue(sourceList.filter { it.isShort && it.getStreamLink })
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

        private suspend fun fetchSourceConfig(sourceWrapper: SourceWrapper): NPOSourceConfig? {
            val isPlusUser =
                sourceWrapper.overrideIsPlusUser
                    ?: (settingsRepository.userType.first() == UserType.Plus)
            val token = createToken(sourceWrapper.uniqueId, isPlusUser) ?: return null

            return try {
                val source = NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
                sourceWrapper.mergeSourceWrapperWithSource(source).copy(overrideAutoPlay = true)
            } catch (e: NPOPlayerException) {
                e.printStackTrace()
                null
            }
        }

        private suspend fun NPOSourceConfig.refreshSourceConfig(): NPOSourceConfig? {
            val isPlusUser = (settingsRepository.userType.first() == UserType.Plus)
            val token = createToken(uniqueId, isPlusUser) ?: return null

            return try {
                NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
            } catch (e: NPOPlayerException) {
                e.printStackTrace()
                null
            }
        }
    }
