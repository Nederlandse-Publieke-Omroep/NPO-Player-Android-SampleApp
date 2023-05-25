package nl.npo.player.sample_app.presentation.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.enums.CastMediaType
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sample_app.domain.TokenProvider
import nl.npo.player.sample_app.domain.model.StreamInfoResult
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.model.StreamRetrievalState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val tokenProvider: TokenProvider
) : ViewModel() {
    private val mutableState =
        MutableLiveData<StreamRetrievalState>(StreamRetrievalState.NotStarted)
    val retrievalState: LiveData<StreamRetrievalState> = mutableState

    fun retrieveSource(item: SourceWrapper) {
        mutableState.postValue(StreamRetrievalState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = tokenProvider.createToken(item.uniqueId, item.asPlusUser)) {
                is StreamInfoResult.Success -> {
                    try {
                        val source =
                            NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(result.data.token))
                        mutableState.postValue(
                            StreamRetrievalState.Success(
                                source.copy(
                                    overrideStartOffset = item.startOffset,
                                    overrideImageUrl = source.imageUrl ?: item.imageUrl
                                ),
                                item
                            )
                        )
                    } catch (throwable: NPOPlayerException) {
                        postError(throwable, item)
                    }
                } // Happy path
                is StreamInfoResult.Error -> {
                    postError(result.exception, item)
                }
            }
        }
    }

    private fun postError(throwable: Throwable, sourceWrapper: SourceWrapper) {
        mutableState.postValue(
            StreamRetrievalState.Error(
                throwable, sourceWrapper
            )
        )
    }

    companion object {
        val remoteCallback: ((NPOSourceConfig) -> CastMediaType) = { source ->
            if (source.avType == AVType.AUDIO || source.streamUrl.contains("mp3")) CastMediaType.MusicTrack
            else if (source.avType == AVType.VIDEO) CastMediaType.Movie
            else CastMediaType.Generic
        }
    }
}
