package nl.npo.player.sampleApp.shared.data.analytics.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.data.analytics.AnalyticsEnvironmentProviderImpl
import nl.npo.player.sampleApp.shared.domain.AnalyticsEnvironmentProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsProviderModule {
    @Binds
    @Singleton
    abstract fun bindsAnalyticsProvider(impl: AnalyticsEnvironmentProviderImpl): AnalyticsEnvironmentProvider
}
