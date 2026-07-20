package nl.npo.player.sampleApp.shared.data.progress

import jakarta.inject.Inject
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository
import kotlin.time.Duration

class ProgressStorageRepositoryImpl
    @Inject
    constructor(
        private val progressStorageProvider: SharedPreferencesProgressStorageProvider,
    ) : ProgressStorageRepository {
        override fun getProgress(
            sourceConfig: NPOSourceConfig,
            uniqueId: String,
        ): Duration? {
            return progressStorageProvider.getProgress(sourceConfig, uniqueId)
        }

        override fun storeProgress(
            sourceConfig: NPOSourceConfig,
            uniqueId: String,
            progress: Duration,
        ) {
            progressStorageProvider.storeProgress(
                sourceConfig,
                sourceId = uniqueId,
                progress = progress,
            )
        }

        override suspend fun clearProgress(uniqueId: String) {
            progressStorageProvider.clearProgress(uniqueId)
        }
    }
