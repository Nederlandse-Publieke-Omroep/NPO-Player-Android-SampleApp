package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.common.enums.AVType

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
        val _combinedList  = MutableStateFlow<List<SourceWrapper>>(emptyList())
        val combinedList: StateFlow<List<SourceWrapper>> = _combinedList

      init {
        getStreamLinkListItems()
        getUrlLinkListItems()

        viewModelScope.launch {
           val sourceList = buildList {
              addAll(streamLinkRepository.getSourceList())
              addAll(urlLinkRepository.getSourceList())
            }
          _combinedList.value = sourceList
        }
      }


  private fun getStreamLinkListItems() =
    viewModelScope.launch {
      mutableStreamLinkList.postValue(streamLinkRepository.getSourceList())
    }

  private fun getUrlLinkListItems() =
    viewModelScope.launch {
      mutableURLLinkList.postValue(urlLinkRepository.getSourceList())
    }

  val audioItems = combinedList.map {
      it.filter {
        sourceWrapper -> sourceWrapper.avType == AVType.AUDIO
      }
    }

  val videoItems = combinedList.map {
      it.filter {
        sourceWrapper -> sourceWrapper.avType == AVType.VIDEO
      }
    }

    }




