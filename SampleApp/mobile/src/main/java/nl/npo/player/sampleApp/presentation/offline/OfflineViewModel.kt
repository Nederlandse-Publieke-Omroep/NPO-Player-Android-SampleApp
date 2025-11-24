package nl.npo.player.sampleApp.presentation.offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.exception.NPOOfflineErrorCode
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
        private val _itemId = MutableStateFlow<String?>(null)
        val itemId: StateFlow<String?> = _itemId

        init {
            getStreamLinkListItems()
            getUrlLinkListItems()
            getOfflineLinkListItems()
        }

        fun onItemClicked(
            sourceWrapper: SourceWrapper,
            id: String,
        ) {
            if (sourceWrapper.npoOfflineContent != null) {
                val offlineContent = sourceWrapper.npoOfflineContent ?: return
                if (sourceWrapper.uniqueId != id) return
                when (offlineContent.downloadState.value) {
                    NPODownloadState.Finished -> {
                        val offlineSource = offlineContent.getOfflineSource()
                        sourceWrapper.copy(
                            npoOfflineContent = null,
                            npoSourceConfig = offlineSource,
                        )
                        viewModelScope.launch {
                            _events.emit(
                                value =
                                    DownloadEvent.Request(
                                        itemId = id,
                                        wrapper = sourceWrapper,
                                    ),
                            )
                        }
                    }

                    is NPODownloadState.Failed -> {
                        handleDownloadState(
                            state = offlineContent.downloadState.value,
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
                    is NPODownloadState.Deleting, NPODownloadState.Initializing -> {
                        // NO_OP
                    }
                }
            } else {
                createOfflineContent(sourceWrapper) { throwable ->
                    showError(throwable.message)
                }
            }
        }

        fun handleDownloadState(
            state: NPODownloadState?,
            id: String,
            sourceWrapper: SourceWrapper,
        ) {
            if (sourceWrapper.uniqueId == id) {
                if (state is NPODownloadState.Failed) {
                    val error = state.reason as? NPOOfflineContentException.DownloadError
                    val isFailedDownload = error?.errorCode == NPOOfflineErrorCode.DownloadFailed

                    if (isFailedDownload) {
                        viewModelScope.launch {
                            _events.emit(
                                DownloadEvent.Error(
                                    itemId = id,
                                    message = state.reason.message ?: "Download failed",
                                ),
                            )
                        }
                    }
                }
            }
        }

        fun onDialogItemClicked(sourceWrapper: SourceWrapper) {
            _itemId.value = sourceWrapper.uniqueId
        }

        fun dismissDialogItem() {
            _itemId.value = null
        }

        fun showError(message: String?) {
            _toastMessage.value = message
        }

        fun onToastShown() {
            _toastMessage.value = null
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
