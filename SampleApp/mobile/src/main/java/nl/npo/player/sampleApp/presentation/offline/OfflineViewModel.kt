package nl.npo.player.sampleApp.presentation.offline

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.sampleApp.presentation.model.DownloadEvent
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository
import nl.npo.player.sampleApp.shared.domain.annotation.OfflineLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OfflineViewModel
    @Inject
    constructor(
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        @URLLinkRepository private val urlLinkRepository: LinkRepository,
        @OfflineLinkRepository private val offlineLinkRepository: LinkRepository.OfflineLinkRepository,
        private val progressStorageRepository: ProgressStorageRepository,
    ) : ViewModel() {
        private val _downloadEvent = MutableStateFlow<DownloadEvent>(DownloadEvent.None)
        val downloadEvent = _downloadEvent
        private val mutableOfflineLinkList = MutableStateFlow<List<SourceWrapper>>(emptyList())
        private val pendingCreations = mutableSetOf<String>()
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
        private val _legacyOfflineContentList = MutableStateFlow<List<NPOOfflineContent>>(emptyList())
        val legacyOfflineContentList: StateFlow<List<NPOOfflineContent>> = _legacyOfflineContentList

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
                    source.copy(
                        npoOfflineContent = offlineSource?.npoOfflineContent,
                    )
                }
        }

        init {
            viewModelScope.launch {
                getOfflineLinkListItems()
                // Legacy (Bitmovin-era) offline downloads are no longer supported by the library (7.x).
                _legacyOfflineContentList.tryEmit(emptyList())
            }
        }

        fun onItemClicked(
            sourceWrapper: SourceWrapper,
            id: String,
            onClick: () -> Unit,
            error: (Throwable) -> Unit,
        ) {
            val offlineContent = sourceWrapper.npoOfflineContent
            if (offlineContent == null) {
                createOfflineContent(
                    sourceWrapper,
                    onCreated = { createdContent ->
                        createdContent.startOrResumeDownload()
                    },
                ) { throwable ->
                    error(throwable)
                }
                return
            }

            if (sourceWrapper.uniqueId != id) return

            when (val downloadState = offlineContent.downloadState.value) {
                is NPODownloadState.Finished -> {
                    onClick()
                }

                is NPODownloadState.Failed -> {
                    // Don't auto-retry. Surface the error and let the user choose to
                    // retry (resume) or cancel via the dialog.
                    handleDownloadState(
                        state = downloadState,
                        id = id,
                        sourceWrapper = sourceWrapper,
                    )
                }

                is NPODownloadState.Paused -> {
                    offlineContent.startOrResumeDownload()
                }

                is NPODownloadState.InProgress -> {
                    offlineContent.pause()
                }

                is NPODownloadState.Deleting -> {
                    // Deletion is already in progress; ignore taps until it settles.
                }

                NPODownloadState.Initializing -> {
                    offlineContent.startOrResumeDownload()
                }
            }
        }

        fun playOfflineContent(
            wrapper: SourceWrapper,
            context: Context,
        ) {
            viewModelScope.launch {
                context.startPlayerActivity(
                    wrapper.copy(
                        npoOfflineContent = null,
                        npoSourceConfig =
                            wrapper.npoOfflineContent?.getOfflineSource()
                                ?: wrapper.npoSourceConfig,
                    ),
                )
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
                        wrapper = sourceWrapper,
                    )
            }
        }

        /** Retry a previously failed download and dismiss the error dialog. */
        fun retryDownload(event: DownloadEvent.Error) {
            dismissDownloadEventDialog()

            val id = event.itemId ?: event.wrapper?.uniqueId ?: return
            // Resolve the *live* wrapper from our own list rather than trusting the
            // (possibly stale) wrapper captured in the event.
            val wrapper =
                mutableOfflineLinkList.value.firstOrNull { it.uniqueId == id }
                    ?: event.wrapper
                    ?: return

            val content = wrapper.npoOfflineContent
            if (content != null) {
                content.startOrResumeDownload()
            } else {
                createOfflineContent(
                    wrapper,
                    onCreated = { it.startOrResumeDownload() },
                    errorCallback = {
                        _downloadEvent.value =
                            DownloadEvent.Error(
                                itemId = id,
                                message = it.message ?: "Download failed",
                                wrapper = wrapper,
                            )
                    },
                )
            }
        }

        fun cancelDownload(event: DownloadEvent.Error) {
            val id = event.itemId ?: event.wrapper?.uniqueId
            val wrapper =
                mutableOfflineLinkList.value.firstOrNull { it.uniqueId == id }
                    ?: event.wrapper
            if (wrapper?.npoOfflineContent != null) {
                deleteOfflineContent(wrapper)
            } else {
                dismissDownloadEventDialog()
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

        fun dismissLegacyDownloadDialog() {
            _legacyOfflineContentList.tryEmit(emptyList())
        }

        fun refreshLegacyDownloadList() {
            _legacyOfflineContentList.tryEmit(emptyList())
            viewModelScope.launch {
                delay(20.seconds)
                // Legacy (Bitmovin-era) offline downloads are no longer supported by the library (7.x).
                _legacyOfflineContentList.tryEmit(emptyList())
            }
        }

        override fun onCleared() {
            mutableOfflineLinkList.value.forEach { it.npoOfflineContent?.release() }
            super.onCleared()
        }

        fun createOfflineContent(
            sourceWrapper: SourceWrapper,
            onCreated: (NPOOfflineContent) -> Unit = {},
            errorCallback: (Throwable) -> Unit,
        ) {
            val id = sourceWrapper.uniqueId

            // Already creating for this id: ignore the extra tap.
            if (!pendingCreations.add(id)) return

            // Content already exists (e.g. list hasn't recomposed yet): resume it
            // instead of creating a duplicate download.
            val existing =
                mutableOfflineLinkList.value.firstOrNull { it.uniqueId == id }?.npoOfflineContent
            if (existing != null) {
                pendingCreations.remove(id)
                onCreated(existing)
                return
            }

            viewModelScope.launch {
                val offlineContent =
                    try {
                        offlineLinkRepository.createOfflineContent(sourceWrapper)
                    } catch (e: NPOOfflineContentException) {
                        pendingCreations.remove(id)
                        errorCallback(e)
                        return@launch
                    }

                mutableOfflineLinkList.value =
                    mutableOfflineLinkList.value
                        .map { item ->
                            if (item.uniqueId == sourceWrapper.uniqueId) {
                                item.copy(npoOfflineContent = offlineContent)
                            } else {
                                item
                            }
                        }.let { updated ->
                            if (updated.any { it.uniqueId == sourceWrapper.uniqueId }) {
                                updated
                            } else {
                                updated + sourceWrapper.copy(npoOfflineContent = offlineContent)
                            }
                        }

                pendingCreations.remove(id)
                onCreated(offlineContent)
            }
        }

        fun deleteOfflineContent(sourceWrapper: SourceWrapper) {
            val offlineContent = sourceWrapper.npoOfflineContent ?: return

            viewModelScope.launch {
                offlineLinkRepository.deleteOfflineContent(offlineContent)
                progressStorageRepository.clearProgress(sourceWrapper.uniqueId)
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

fun Context.startPlayerActivity(wrapper: SourceWrapper) {
    startActivity(
        Intent(
            PlayerActivity.getStartIntent(
                packageContext = this,
                sourceWrapper = wrapper,
            ),
        ),
    )
}
