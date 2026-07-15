package nl.npo.player.sampleApp.shared.domain

import kotlin.time.Duration

interface ProgressStorageRepository {

    fun getProgress(uniqueId: String): Duration?

    fun storeProgress(

        uniqueId: String,

        progress: Duration,

        )

    suspend fun clearProgress(uniqueId: String)
}