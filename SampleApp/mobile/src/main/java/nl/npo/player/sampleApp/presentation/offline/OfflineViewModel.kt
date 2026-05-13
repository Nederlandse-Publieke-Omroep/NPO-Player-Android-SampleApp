package nl.npo.player.sampleApp.presentation.offline

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
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

    @OptIn(UnstableApi::class)
    fun mergeList(
        streamLinkList: List<SourceWrapper>,
        urlLinkList: List<SourceWrapper>,
        offlineLinkList: List<SourceWrapper>,
        ): List<SourceWrapper> {
        val offlineById = offlineLinkList.associateBy { it.uniqueId }
        return urlLinkList
            .union(streamLinkList)
            .filter { it.offlineDownloadAllowed }
            .map { source ->
                val offlineSource = offlineById[source.uniqueId]
                val mergedSource = source.copy(
                    npoOfflineContent = offlineSource?.npoOfflineContent,
                    )
                Log.d(
                    "OFFLINECONTENT",
                    "mergeList: uniqueId=${source.uniqueId}, " +
                            "hasOfflineContent=${mergedSource.npoOfflineContent != null}, " +
                            "state=${mergedSource.npoOfflineContent?.downloadState?.value}",
                    )
                mergedSource
            }
    }

    init {
        getOfflineLinkListItems()
    }

    @OptIn(UnstableApi::class)
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
                    offlineContent.startDownload()
                }

                is NPODownloadState.Paused -> {
                    offlineContent.resumeDownload()
                }

                    is NPODownloadState.InProgress -> {
                        offlineContent.startOrResumeDownload()
                    }

                is NPODownloadState.Deleting -> {
                    onClick(
                        DownloadEvent.Delete(
                            sourceWrapper.uniqueId,
                            sourceWrapper,
                        ),
                    )
                }

                NPODownloadState.Initializing -> {}
            }
        } else {
            createOfflineContent(
                sourceWrapper,
                onCreated = { offlineContent -> offlineContent.startOrResumeDownload() })
            { throwable ->
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

    fun deleteDownloadedItem(
        id: String,
        sourceWrapper: SourceWrapper,
    ) {

        if (sourceWrapper.uniqueId == id) {
            _downloadEvent.value = DownloadEvent.Delete(id, sourceWrapper = sourceWrapper)
        }
    }

    fun dismissDownloadEventDialog() {
        _downloadEvent.value = DownloadEvent.None
    }

    override fun onCleared() {
        mutableOfflineLinkList.value.forEach { it.npoOfflineContent?.release() }
        super.onCleared()
    }

//    @OptIn(UnstableApi::class)
//    fun createOfflineContent(
//        sourceWrapper: SourceWrapper,
//        onCreated: (NPOOfflineContent) -> Unit = {},
//        errorCallback: (Throwable) -> Unit,
//        ) {
//        viewModelScope.launch(
//            CoroutineExceptionHandler { _, throwable ->
//                errorCallback.invoke(throwable)
//            },
//        ) {
//            val existingItem = mutableOfflineLinkList.value.firstOrNull {
//                it.uniqueId == sourceWrapper.uniqueId
//            }
//            if (existingItem?.npoOfflineContent != null) {
//                errorCallback(Exception("Offline content already exists"))
//                return@launch
//
//            } else {
//                val offlineContent = offlineLinkRepository.createOfflineContent(sourceWrapper)
//                onCreated(offlineContent)
//                mutableOfflineLinkList.value =
//                    mutableOfflineLinkList.value.toMutableList().apply {
//                        val newSource =
//                            sourceWrapper.copy(
//                                npoOfflineContent = offlineContent,
//                            )
//                        add(newSource)
//                    }
//            }
//        }
//    }


    @OptIn(UnstableApi::class)
    fun createOfflineContent(
        sourceWrapper: SourceWrapper,
        onCreated: (NPOOfflineContent) -> Unit = {},
        errorCallback: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            val existingItem = mutableOfflineLinkList.value.firstOrNull {
                it.uniqueId == sourceWrapper.uniqueId
            }
            if (existingItem?.npoOfflineContent != null) {
                errorCallback(Exception("Offline content already exists"))
                return@launch
            }

            val offlineContent = offlineLinkRepository.createOfflineContent(sourceWrapper)
            onCreated(offlineContent)
            mutableOfflineLinkList.value =
                mutableOfflineLinkList.value.map { item ->
                    if (item.uniqueId == sourceWrapper.uniqueId) {
                        item.copy(
                            npoOfflineContent = offlineContent,
                        )
                    } else {
                        item
                    }
                }.let { updated ->
                    if (updated.any { it.uniqueId == sourceWrapper.uniqueId }) {
                        updated
                    } else {
                        updated + sourceWrapper.copy(
                            npoOfflineContent = offlineContent,

                            )

                    }
                }
            Log.d(
                "DEUG_OFFLINE",
                "after create: " +
                        mutableOfflineLinkList.value.map {
                            "${it.uniqueId}: offline=${it.npoOfflineContent != null}"

                        }
            )
        }
    }


    fun deleteOfflineContent(sourceWrapper: SourceWrapper) {

        val offlineContent = sourceWrapper.npoOfflineContent ?: return

        viewModelScope.launch {
            offlineLinkRepository.deleteOfflineContent(offlineContent)
                mutableOfflineLinkList.value =
                mutableOfflineLinkList.value.map { item ->
                    if (item.uniqueId == sourceWrapper.uniqueId) {
                        item.copy(npoOfflineContent = null)
                    } else {
                        item
                    }
                }
            dismissDownloadEventDialog()
        }
    }

    private fun getOfflineLinkListItems() =
        viewModelScope.launch {
            mutableOfflineLinkList.emit(offlineLinkRepository.getSourceList())
        }
}
