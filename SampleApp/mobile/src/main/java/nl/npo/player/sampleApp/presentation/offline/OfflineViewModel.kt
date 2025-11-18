package nl.npo.player.sampleApp.presentation.offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.jsonwebtoken.lang.Assert.state
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.offline.NPOOfflineContentManager
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.sampleApp.presentation.compose.DownloadEvent

import nl.npo.player.sampleApp.presentation.compose.DownloadState
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
        private val mutableStreamLinkList = MutableLiveData<List<SourceWrapper>>()
        val streamLinkList: LiveData<List<SourceWrapper>> = mutableStreamLinkList
        private val mutableURLLinkList = MutableLiveData<List<SourceWrapper>>()
        val urlLinkList: LiveData<List<SourceWrapper>> = mutableURLLinkList
        private val mutableOfflineLinkList = MutableLiveData<List<SourceWrapper>>()
        val offlineLinkList: LiveData<List<SourceWrapper>> = mutableOfflineLinkList

        private val mutableMergedLinkList = MutableLiveData<List<SourceWrapper>>()
        val mergedLinkList: LiveData<List<SourceWrapper>> = mutableMergedLinkList

        private val _toastMessage = MutableLiveData<String?>()
        val toastMessage: LiveData<String?> = _toastMessage

        private val _events = MutableSharedFlow<DownloadEvent>()
        val events = _events.asSharedFlow()

        init {
            getStreamLinkListItems()
            getUrlLinkListItems()
            getOfflineLinkListItems()


        }


    fun onItemClicked(state: NPODownloadState?, itemId: String) {
        val mergedList = mergedLinkList.value ?: return
            mergedList.map { item ->
            if (item.uniqueId == itemId) {
                 when (state) {
                    DownloadState.Initializing -> DownloadState.InProgress(progress = 0.2f)
                    is DownloadState.InProgress -> DownloadState.Paused
                    DownloadState.Paused -> DownloadState.InProgress(progress = 0.5f)
                    DownloadState.Finished -> DownloadState.Finished
                    is DownloadState.Failed -> DownloadState.InProgress(progress = 0f)
                     else -> {}
                 }

            }
        }


//        if (id != null ) {
//            when (state?.value) {
//
//                is DownloadState.InProgress -> {
//                    id.pause()
//                }
//
//                DownloadState.Paused -> {
//                    id.startOrResumeDownload()
//                }
//
//                is DownloadState.Failed -> {
//                    id.startOrResumeDownload()
//                    viewModelScope.launch {
//                        _events.emit(
//                            DownloadEvent.ShowErrorDialog(
//                                itemId = itemId,
//                                "state.value.error"
//                            )
//                        )
//                    }
//                }
//
//                DownloadState.Finished -> {
//                    id.getOfflineSource()
//                    viewModelScope.launch {
//                        _events.emit(DownloadEvent.OpenDownloadedItem(itemId = itemId))
//                    }
//                }
//
//                DownloadState.Deleting, DownloadState.Initializing -> {
//                    // NO-OP
//                }
//
//                else -> {}
//            }
//        }else
    }


    override fun onCleared() {
            mutableOfflineLinkList.value?.forEach { it.npoOfflineContent?.release() }
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
                if (mutableOfflineLinkList.value?.indexOfFirst { it.uniqueId == sourceWrapper.uniqueId } != -1) {
                    // Already exists. Don't create new offline content.
                    errorCallback.invoke(Exception("Offline content already exists"))
                } else {
                    val offlineContent = offlineLinkRepository.createOfflineContent(sourceWrapper)
                    mutableOfflineLinkList.value =
                        mutableOfflineLinkList.value?.toMutableList()?.apply {
                            val newSource =
                                sourceWrapper.copy(
                                    npoOfflineContent = offlineContent,
                                )
                            add(newSource)
                        }
                }
                updateMergedLinkList()
            }
        }

        fun deleteOfflineContent(sourceWrapper: SourceWrapper) {
            val npoOfflineContent = sourceWrapper.npoOfflineContent ?: return
            viewModelScope.launch {
                offlineLinkRepository.deleteOfflineContent(npoOfflineContent)
                mutableOfflineLinkList.value =
                    mutableOfflineLinkList.value?.toMutableList()?.apply {
                        removeIf { it.uniqueId == sourceWrapper.uniqueId }
                    }
                updateMergedLinkList()
            }
        }

        private fun getStreamLinkListItems() =
            viewModelScope.launch {
                mutableStreamLinkList.value = streamLinkRepository.getSourceList()
                updateMergedLinkList()
            }

        private fun getUrlLinkListItems() =
            viewModelScope.launch {
                mutableURLLinkList.value = urlLinkRepository.getSourceList()
                updateMergedLinkList()
            }

        private fun getOfflineLinkListItems() =
            viewModelScope.launch {
                mutableOfflineLinkList.value = offlineLinkRepository.getSourceList()
                updateMergedLinkList()
            }

        private fun updateMergedLinkList() {
            val unionList =
                (urlLinkList.value ?: emptyList())
                    .union(streamLinkList.value ?: emptyList())
                    .filter { it.offlineDownloadAllowed }
            offlineLinkList.value?.let { offlineLinkListNotNull ->
                mutableMergedLinkList.postValue(
                    unionList.map { source ->
                        offlineLinkListNotNull.firstOrNull { offlineSource ->
                            source.uniqueId == offlineSource.uniqueId
                        } ?: source
                    },
                )
            }

        }


    }

