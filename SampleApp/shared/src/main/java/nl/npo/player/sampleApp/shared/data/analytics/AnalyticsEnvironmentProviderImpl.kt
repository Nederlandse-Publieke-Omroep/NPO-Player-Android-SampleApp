package nl.npo.player.sampleApp.shared.data.analytics

import kotlinx.coroutines.flow.firstOrNull
import nl.npo.player.library.domain.analytics.model.AnalyticsEnvironment
import nl.npo.player.sampleApp.shared.data.extensions.toAnalyticsEnvironment
import nl.npo.player.sampleApp.shared.data.settings.SettingsPreferences
import nl.npo.player.sampleApp.shared.domain.AnalyticsEnvironmentProvider
import javax.inject.Inject

class AnalyticsEnvironmentProviderImpl
    @Inject
    constructor(
        private val prefs: SettingsPreferences,
    ) : AnalyticsEnvironmentProvider {
        override suspend fun getAnalyticsEnvironment(): AnalyticsEnvironment =
            prefs.environment
                .firstOrNull()
                ?.toDomain()
                ?.toAnalyticsEnvironment()
                ?: AnalyticsEnvironment.PROD
    }
