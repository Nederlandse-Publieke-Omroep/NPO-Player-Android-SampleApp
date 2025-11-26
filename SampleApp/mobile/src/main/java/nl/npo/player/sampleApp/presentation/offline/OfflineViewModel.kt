package nl.npo.player.sampleApp.presentation.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.presentation.model.DownloadEvent
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.OfflineLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject

@HiltViewModel
class OfflineViewModel
    @Inject
    constructor(
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        @URLLinkRepository private val urlLinkRepository: LinkRepository,
        @OfflineLinkRepository private val offlineLinkRepository: LinkRepository.OfflineLinkRepository,
    ) : ViewModel() {
        private val _downloadEvent = MutableStateFlow<DownloadEvent>(DownloadEvent.None)
        val downloadEvent = _downloadEvent
        private val mutableOfflineLinkList = MutableStateFlow<List<SourceWrapper>>(emptyList())

        private val streamLinkList =
            flow {
                emit(streamLinkRepository.getSourceList())
            }
        private val urlLinkList =
            flow {
                emit(urlLinkRepository.getSourceList())
            }

        val mergedSourceList =
            combine(
                streamLinkList,
                urlLinkList,
                mutableOfflineLinkList,
                ::mergeList,
            ).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

        fun mergeList(
            streamLinkList: List<SourceWrapper>,
            urlLinkList: List<SourceWrapper>,
            offlineLinkList: List<SourceWrapper>,
        ): List<SourceWrapper> =
            urlLinkList
                .union(streamLinkList)
                .filter { it.offlineDownloadAllowed }
                .map { source ->
                    offlineLinkList.firstOrNull { offlineSource ->
                        source.uniqueId == offlineSource.uniqueId
                    } ?: source
                }

        init {
            getOfflineLinkListItems()
            CoroutineScope(Dispatchers.Default).launch { }
        }

        fun onItemClicked(
            sourceWrapper: SourceWrapper,
            id: String,
            onClick: (DownloadEvent) -> Unit,
            error: (Throwable) -> Unit,
        ) {
            if (sourceWrapper.npoOfflineContent != null) {
                val offlineContent = sourceWrapper.npoOfflineContent ?: return
                if (sourceWrapper.uniqueId != id) return
                when (val downloadState = offlineContent.downloadState.value) {
                    NPODownloadState.Finished -> {
                        val offlineSource = offlineContent.getOfflineSource()
                        sourceWrapper.copy(
                            npoOfflineContent = null,
                            npoSourceConfig = offlineSource,
                        )
                        onClick(
                            DownloadEvent.Request(
                                itemId = sourceWrapper.uniqueId,
                                wrapper = sourceWrapper,
                            ),
                        )
                    }

                    is NPODownloadState.Failed -> {
                        handleDownloadState(
                            state = downloadState,
                            id = id,
                            sourceWrapper = sourceWrapper,
                        )
                        offlineContent.startOrResumeDownload()
                    }

                    is NPODownloadState.Paused -> {
                        offlineContent.startOrResumeDownload()
                    }

                    is NPODownloadState.InProgress -> {
                        offlineContent.pause()
                    }

                    is NPODownloadState.Deleting -> {
                        deleteDownloadedItem(sourceWrapper = sourceWrapper)
                    }

                    NPODownloadState.Initializing -> {
                        dismissDownloadEventDialog()
                    }
                }
            } else {
                createOfflineContent(sourceWrapper) { throwable ->
                    error(throwable)
                }
            }
        }

        private fun handleDownloadState(
            state: NPODownloadState.Failed,
            id: String,
            sourceWrapper: SourceWrapper,
        ) {
            if (sourceWrapper.uniqueId == id) {
                _downloadEvent.value =
                    DownloadEvent.Error(
                        itemId = id,
                        message = state.reason.message ?: "Download failed",
                    )
            }
        }

        fun deleteDownloadedItem(sourceWrapper: SourceWrapper) {
            _downloadEvent.value = DownloadEvent.Delete(sourceWrapper = sourceWrapper)
            deleteOfflineContent(sourceWrapper = sourceWrapper)
        }

        fun dismissDownloadEventDialog() {
            _downloadEvent.value = DownloadEvent.None
        }

        override fun onCleared() {
            mutableOfflineLinkList.value.forEach { it.npoOfflineContent?.release() }
            super.onCleared()
        }

        fun createOfflineContent(
            sourceWrapper: SourceWrapper,
            errorCallback: (Throwable) -> Unit,
        ) {
            viewModelScope.launch(
                CoroutineExceptionHandler { _, throwable ->
                    errorCallback.invoke(throwable)
                },
            ) {
                if (mutableOfflineLinkList.value.indexOfFirst { it.uniqueId == sourceWrapper.uniqueId } != -1) {
                    // Already exists. Don't create new offline content.
                    errorCallback.invoke(Exception("Offline content already exists"))
                } else {
                    val offlineContent = offlineLinkRepository.createOfflineContent(sourceWrapper)
                    mutableOfflineLinkList.value =
                        mutableOfflineLinkList.value.toMutableList().apply {
                            val newSource =
                                sourceWrapper.copy(
                                    npoOfflineContent = offlineContent,
                                )
                            add(newSource)
                        }
                }
            }
        }

        fun deleteOfflineContent(sourceWrapper: SourceWrapper) {
            val npoOfflineContent = sourceWrapper.npoOfflineContent ?: return
            viewModelScope.launch {
                offlineLinkRepository.deleteOfflineContent(npoOfflineContent)
                mutableOfflineLinkList.value =
                    mutableOfflineLinkList.value.toMutableList().apply {
                        removeIf { it.uniqueId == sourceWrapper.uniqueId }
                    }
            }
        }

        private fun getOfflineLinkListItems() =
            viewModelScope.launch {
                mutableOfflineLinkList.emit(offlineLinkRepository.getSourceList())
            }

//  private suspend fun updateMergedLinkList(
//    offlineList: List<SourceWrapper>,
//  ): List<SourceWrapper> {
//      val streamLink = streamLinkRepository.getSourceList()
//      val urlLink = urlLinkRepository.getSourceList()
//      return urlLink
//      .union(streamLink)
//      .filter { it.offlineDownloadAllowed }
//      .map { source ->
//        offlineList.firstOrNull { offlineSource ->
//          source.uniqueId == offlineSource.uniqueId
//        } ?: source
//      }
//  }
    }
