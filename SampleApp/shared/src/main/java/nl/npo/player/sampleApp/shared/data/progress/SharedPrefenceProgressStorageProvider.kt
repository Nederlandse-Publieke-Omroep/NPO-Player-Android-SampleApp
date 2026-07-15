package nl.npo.player.sampleApp.shared.data.progress

import android.content.Context
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Singleton
class SharedPreferencesProgressStorageProvider(
    @ApplicationContext context: Context,
    ) : ProgressStorageProvider {
    private val preferences =
        context.getSharedPreferences(
            "playback_progress",
            Context.MODE_PRIVATE,
            )

    override fun getProgress(sourceId: String?): Duration? {
        val key = progressKey(sourceId)
        if (!preferences.contains(key)) {
            return null
        }

        return preferences
            .getLong(key, 0L)
            .milliseconds

    }

    override fun storeProgress(
        sourceId: String,
        progress: Duration,
        ) {
        preferences.edit {
            putLong(
                progressKey(sourceId),
                progress.inWholeMilliseconds,
                )
        }

    }

    override suspend fun clearProgress(uniqueId: String) {
        preferences.edit {
            remove(progressKey(uniqueId))

        }

    }

    private fun progressKey(uniqueId: String?): String {
        return "progress_$uniqueId"

    }

}