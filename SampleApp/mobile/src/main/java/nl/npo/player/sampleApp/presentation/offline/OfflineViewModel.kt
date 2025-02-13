package nl.npo.player.sampleApp.presentation.offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
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

        init {
            getStreamLinkListItems()
            getUrlLinkListItems()
            getOfflineLinkListItems()
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
                mutableStreamLinkList.value = streamLinkRepository.getSourceList()!!
                updateMergedLinkList()
            }

        private fun getUrlLinkListItems() =
            viewModelScope.launch {
                mutableURLLinkList.value = urlLinkRepository.getSourceList()!!
                updateMergedLinkList()
            }

        private fun getOfflineLinkListItems() =
            viewModelScope.launch {
                mutableOfflineLinkList.value = offlineLinkRepository.getSourceList()!!
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
