package nl.npo.player.sampleApp.model

import nl.npo.player.library.domain.player.model.NPOSourceConfig

sealed class StreamRetrievalState {
    object NotStarted : StreamRetrievalState()

    object Loading : StreamRetrievalState()

    class Error(val throwable: Throwable?, val item: SourceWrapper) : StreamRetrievalState()

    class Success(val npoSourceConfig: NPOSourceConfig, val item: SourceWrapper) :
        StreamRetrievalState()
}
