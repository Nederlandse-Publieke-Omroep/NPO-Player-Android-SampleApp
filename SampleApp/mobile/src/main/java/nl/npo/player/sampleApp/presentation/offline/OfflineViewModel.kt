package nl.npo.player.sampleApp.presentation.offline

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.presentation.model.DownloadEvent
import nl.npo.player.sampleApp.presentation.model.DownloadableItem
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository
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
        private val progressStorageRepository: ProgressStorageRepository,
    ) : ViewModel() {
        private val _downloadEvent = MutableStateFlow<DownloadEvent>(DownloadEvent.None)
        val downloadEvent = _downloadEvent

        val downloadableItemList: StateFlow<List<DownloadableItem>> =
            flow {
                val streamLinkList = streamLinkRepository.getSourceList()
                val urlLinkList = urlLinkRepository.getSourceList()
                val offlineLinkList = offlineLinkRepository.getSourceList()
                val mergedList = mergeList(streamLinkList, urlLinkList, offlineLinkList)
                emit(mergedList.map { DownloadableItem(it, viewModelScope, offlineLinkRepository) })
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

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

        /** Retry a previously failed download and dismiss the error dialog. */
        fun retryDownload(event: DownloadEvent.Error) {
            Log.d("SampleAppTest", "OfflineViewModel - retryDownload($event: DownloadEvent.Error)")
            dismissDownloadEventDialog()

            val id = event.itemId ?: event.wrapper?.uniqueId ?: return
            // Resolve the *live* wrapper from our own list rather than trusting the
            // (possibly stale) wrapper captured in the event.
            val downloadableItem =
                downloadableItemList.value
                    .firstOrNull { it.sourceWrapper.uniqueId == id } ?: return
            downloadableItem.retryDownload(errorCallback = {
                _downloadEvent.value =
                    DownloadEvent.Error(
                        itemId = id,
                        message = it.message ?: "Download failed",
                        wrapper = downloadableItem.sourceWrapper,
                    )
            })
        }

        fun cancelDownload(event: DownloadEvent.Error) {
            val id = event.itemId ?: event.wrapper?.uniqueId
            downloadableItemList.value.firstOrNull { it.sourceWrapper.uniqueId == id }?.deleteOfflineContent()
            dismissDownloadEventDialog()
        }

        fun deleteDownloadedItem(sourceWrapper: SourceWrapper) {
            _downloadEvent.value = DownloadEvent.Delete(sourceWrapper.uniqueId, sourceWrapper = sourceWrapper)
            viewModelScope.launch {
                progressStorageRepository.clearProgress(sourceWrapper.uniqueId)
            }
        }

        fun dismissDownloadEventDialog() {
            _downloadEvent.value = DownloadEvent.None
        }

        override fun onCleared() {
            downloadableItemList.value.forEach { it.sourceWrapper.npoOfflineContent?.release() }
            super.onCleared()
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
