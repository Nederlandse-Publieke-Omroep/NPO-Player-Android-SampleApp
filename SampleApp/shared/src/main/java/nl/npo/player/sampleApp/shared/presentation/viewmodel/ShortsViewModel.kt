package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.streamLink.model.StreamChapterType
import nl.npo.player.library.general.NPOPreloadManager
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject

@HiltViewModel
class ShortsViewModel
    @Inject
    constructor(
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val mutableSourceWrapperList = MutableLiveData<List<SourceWrapper>>()
        private val mutableSourceConfig: MutableLiveData<List<NPOSourceConfig>> =
            MutableLiveData<List<NPOSourceConfig>>()
        val sourceConfig: LiveData<List<NPOSourceConfig>> = mutableSourceConfig
        private val _player = MutableStateFlow<NPOPlayer?>(null)
        val player = _player.asStateFlow()
        val preloadManager: Flow<NPOPreloadManager?> =
            combine(player, sourceConfig.asFlow()) { npoPlayer, npoSourceConfigList ->
                return@combine if (npoPlayer != null) {

                    val loader = NPOPlayerLibrary.getPreloadManager(npoPlayer)
                    loader.setSources(npoSourceConfigList)
                    loader
                } else {
                    null
                }
            }

        init {
            viewModelScope.launch {
                mutableSourceWrapperList.asFlow().collect { list ->
                    val mutableList = list.toMutableList()
                    mutableList.addAll(list)
                    mutableList.addAll(list)
                    mutableSourceConfig.postValue(
                        mutableList.mapNotNull { sourceWrapper ->
                            fetchSourceConfig(sourceWrapper)
                        },
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
                val useExoplayer: UseExoplayer = settingsRepository.useExoplayer.first()

                _player.emit(
                    NPOPlayerLibrary
                        .getPlayer(
                            context = context,
                            npoPlayerConfig = playerConfig,
                            pageTracker = pageTracker,
                            useExoplayer = useExoplayer,
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
                        },
                )
            }
        }

        fun setCurrentPageIndex(index: Int) {
            viewModelScope.launch {
                val loader = preloadManager.first() ?: return@launch
                withContext(Dispatchers.Main) {
                    loader.play(index)
                }
                // TODO: Make settings option to load directly instead of loading through the pre-loader
//                player.first()?.load(sourceConfig.value!![index])
            }
        }

        private fun getStreamLinkListItems() =
            viewModelScope.launch {
                mutableSourceWrapperList.postValue(streamLinkRepository.getSourceList().filter { it.isShort && it.getStreamLink })
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
                sourceWrapper.mergeSourceWrapperWithSource(source, settingsRepository)
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
