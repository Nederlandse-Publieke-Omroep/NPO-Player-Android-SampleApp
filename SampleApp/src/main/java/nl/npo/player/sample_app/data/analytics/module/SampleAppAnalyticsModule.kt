package nl.npo.player.sample_app.data.analytics.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.analytics.model.AnalyticsEnvironment
import nl.npo.player.sample_app.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object SampleAppAnalyticsModule {

    @Provides
    fun provideNPOOfflineContentManager(): AnalyticsEnvironment =
        when (BuildConfig.FLAVOR) {
            "DEV" -> AnalyticsEnvironment.DEV
            "ACC" -> AnalyticsEnvironment.PREPROD
            else -> AnalyticsEnvironment.PROD
        }
}
