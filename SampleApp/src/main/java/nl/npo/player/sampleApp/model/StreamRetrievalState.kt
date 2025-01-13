package nl.npo.player.sampleApp.model

import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.domain.player.model.NPOSourceConfig

sealed class StreamRetrievalState {
    object NotStarted : StreamRetrievalState()

    object Loading : StreamRetrievalState()

    class Error(val error: NPOPlayerError, val item: SourceWrapper) : StreamRetrievalState()

    class Success(val npoSourceConfig: NPOSourceConfig, val item: SourceWrapper) :
        StreamRetrievalState()
}
