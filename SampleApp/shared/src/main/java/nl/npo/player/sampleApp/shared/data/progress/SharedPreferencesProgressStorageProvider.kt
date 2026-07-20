package nl.npo.player.sampleApp.shared.data.progress

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SharedPreferencesProgressStorageProvider
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ProgressStorageProvider {
        private val preferences =
            context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            )

        override fun getProgress(
            sourceConfig: NPOSourceConfig,
            sourceId: String?,
        ): Duration? {
            if (!sourceConfig.isOfflineSource()) return null

            val uniqueId = sourceId ?: sourceConfig.uniqueId

            val progress =
                if (preferences.contains(uniqueId)) {
                    preferences
                        .getLong(uniqueId, 0L)
                        .milliseconds
                } else {
                    null
                }

            return progress
        }

        override fun storeProgress(
            sourceConfig: NPOSourceConfig,
            sourceId: String,
            progress: Duration,
        ) {
            if (!sourceConfig.isOfflineSource()) return

            preferences.edit {
                putLong(
                    sourceId,
                    progress.inWholeMilliseconds,
                )
            }
        }

        override suspend fun clearProgress(sourceId: String) {
            preferences.edit {
                remove(sourceId)
            }
        }

        private companion object {
            const val PREFERENCES_NAME = "playback_progress"
        }
    }
