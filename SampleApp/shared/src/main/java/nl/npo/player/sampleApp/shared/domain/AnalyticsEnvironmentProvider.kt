package nl.npo.player.sampleApp.shared.domain

import nl.npo.player.library.domain.analytics.model.AnalyticsEnvironment

interface AnalyticsEnvironmentProvider {
    suspend fun getAnalyticsEnvironment(): AnalyticsEnvironment
}
