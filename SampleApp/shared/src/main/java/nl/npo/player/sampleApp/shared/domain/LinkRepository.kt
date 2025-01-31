package nl.npo.player.sampleApp.shared.domain

import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import org.jetbrains.annotations.NotNull

interface LinkRepository {
    @NotNull
    suspend fun getSourceList(): List<SourceWrapper>

    interface OfflineLinkRepository : LinkRepository {
        @Throws(NPOOfflineContentException::class)
        suspend fun createOfflineContent(sourceWrapper: SourceWrapper): NPOOfflineContent

        suspend fun deleteOfflineContent(npoOfflineContent: NPOOfflineContent)
    }
}
