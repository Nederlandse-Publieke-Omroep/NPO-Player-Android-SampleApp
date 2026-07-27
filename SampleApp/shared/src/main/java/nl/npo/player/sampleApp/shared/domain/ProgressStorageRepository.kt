package nl.npo.player.sampleApp.shared.domain

import nl.npo.player.library.domain.player.model.NPOSourceConfig
import kotlin.time.Duration

interface ProgressStorageRepository {
    suspend fun getProgress(
        sourceConfig: NPOSourceConfig,
        uniqueId: String,
    ): Duration?

    suspend fun storeProgress(
        sourceConfig: NPOSourceConfig,
        uniqueId: String,
        progress: Duration,
    )

    suspend fun clearProgress(uniqueId: String)
}
