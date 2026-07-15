package nl.npo.player.sampleApp.shared.data.progress

import jakarta.inject.Inject
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository
import kotlin.time.Duration

class ProgressStorageRepositoryImpl @Inject constructor(
    private val progressStorageProvider: SharedPreferencesProgressStorageProvider,
    ) : ProgressStorageRepository {

    override fun getProgress(uniqueId: String): Duration? {
        return progressStorageProvider.getProgress(uniqueId)

    }

    override fun storeProgress(
        uniqueId: String,
        progress: Duration,
        ) {
        progressStorageProvider.storeProgress(
            sourceId = uniqueId,
            progress = progress,
            )
    }

    override suspend fun clearProgress(uniqueId: String) {
        progressStorageProvider.clearProgress(uniqueId)
    }
}