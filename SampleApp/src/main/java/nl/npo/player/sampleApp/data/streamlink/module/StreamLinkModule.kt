package nl.npo.player.sampleApp.data.streamlink.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.common.enums.Environment
import nl.npo.player.sampleApp.BuildConfig
import nl.npo.player.sampleApp.data.streamlink.StreamInfoRepository
import nl.npo.player.sampleApp.data.streamlink.SystemTimeProvider
import nl.npo.player.sampleApp.domain.TimeProvider
import nl.npo.player.sampleApp.domain.TokenProvider

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
