package nl.npo.player.sample_app.presentation.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
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
                                    overrideImageUrl = item.getImageUrl(source),
                                    overrideAutoPlay = item.autoPlay,
                                    overrideMetadata = source.metadata?.toMutableMap()
                                        ?.apply {
                                            set(
                                                "appletest",
                                                "true"
                                            )
                                        }, // We add this so the Cast Receiver shows the debug log when casting.
                                    overrideTitle = if (item.overrideStreamLinkTitleAndDescription) "SampleApp override title: ${item.title}" else source.title,
                                    overrideDescription = if (item.overrideStreamLinkTitleAndDescription) "SampleApp override description: ${item.testingDescription}" else source.description
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

    private fun SourceWrapper.getImageUrl(npoSourceConfig: NPOSourceConfig): String? {
        return if (preferThisImageUrlOverStreamLink) {
            imageUrl ?: npoSourceConfig.imageUrl
        } else npoSourceConfig.imageUrl ?: imageUrl
    }

    fun loadStream(npoPlayer: NPOPlayer, npoSourceConfig: NPOSourceConfig) {
        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                delay(61 * 1000)
            withContext(Dispatchers.Main) {
                npoPlayer.loadStream(npoSourceConfig)
//                npoPlayer.loadStreamWithDRMRefresh(npoSourceConfig)
//                }
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
