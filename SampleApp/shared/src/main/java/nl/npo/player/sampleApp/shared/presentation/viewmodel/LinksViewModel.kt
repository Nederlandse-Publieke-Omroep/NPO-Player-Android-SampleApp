package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject

@HiltViewModel
class LinksViewModel
    @Inject
    constructor(
        @StreamLinkRepository private val streamLinkRepository: LinkRepository,
        @URLLinkRepository private val urlLinkRepository: LinkRepository,
    ) : ViewModel() {
        private val mutableStreamLinkList = MutableLiveData<List<SourceWrapper>>()
        val streamLinkList: LiveData<List<SourceWrapper>> = mutableStreamLinkList
        private val mutableURLLinkList = MutableLiveData<List<SourceWrapper>>()
        val urlLinkList: LiveData<List<SourceWrapper>> = mutableURLLinkList

        init {
            getStreamLinkListItems()
            getUrlLinkListItems()
        }

        private fun getStreamLinkListItems() =
            viewModelScope.launch {
                mutableStreamLinkList.postValue(streamLinkRepository.getSourceList()!!)
            }

        private fun getUrlLinkListItems() =
            viewModelScope.launch {
                mutableURLLinkList.postValue(urlLinkRepository.getSourceList()!!)
            }
    }
