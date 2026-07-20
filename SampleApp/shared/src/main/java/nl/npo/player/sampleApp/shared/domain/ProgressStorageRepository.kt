package nl.npo.player.sampleApp.shared.domain

import nl.npo.player.library.domain.player.model.NPOSourceConfig
import kotlin.time.Duration

interface ProgressStorageRepository {
    fun getProgress(
        sourceConfig: NPOSourceConfig,
        uniqueId: String,
    ): Duration?

    fun storeProgress(
        sourceConfig: NPOSourceConfig,
        uniqueId: String,
        progress: Duration,
    )

    suspend fun clearProgress(uniqueId: String)
}
