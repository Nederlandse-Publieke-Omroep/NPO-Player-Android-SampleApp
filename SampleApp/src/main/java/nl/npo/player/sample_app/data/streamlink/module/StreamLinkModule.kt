package nl.npo.player.sample_app.data.streamlink.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.common.enums.Environment
import nl.npo.player.sample_app.BuildConfig
import nl.npo.player.sample_app.data.streamlink.StreamInfoRepository
import nl.npo.player.sample_app.data.streamlink.SystemTimeProvider
import nl.npo.player.sample_app.domain.TimeProvider
import nl.npo.player.sample_app.domain.TokenProvider

@Module
@InstallIn(SingletonComponent::class)
object StreamLinkModule {

    @Provides
    fun provideNPOOfflineContentManager(): Environment =
        when (BuildConfig.FLAVOR) {
            "DEV" -> Environment.TEST
            "ACC" -> Environment.ACC
            else -> Environment.PROD
        }

    @Provides
    fun provideTimeProvider(timeProvider: SystemTimeProvider): TimeProvider = timeProvider

    @Provides
    fun provideTokenProvider(tokenProvider: StreamInfoRepository): TokenProvider = tokenProvider
}
