package nl.npo.player.sampleApp.domain

import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.sampleApp.model.SourceWrapper

interface LinkRepository {
    suspend fun getSourceList(): List<SourceWrapper>

    interface OfflineLinkRepository : LinkRepository {
        @Throws(NPOOfflineContentException::class)
        suspend fun createOfflineContent(sourceWrapper: SourceWrapper): NPOOfflineContent

        suspend fun deleteOfflineContent(npoOfflineContent: NPOOfflineContent)
    }
}
