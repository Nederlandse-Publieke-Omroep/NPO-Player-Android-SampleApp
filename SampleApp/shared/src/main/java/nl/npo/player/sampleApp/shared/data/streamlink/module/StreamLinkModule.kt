package nl.npo.player.sampleApp.shared.data.streamlink.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.data.streamlink.StreamInfoRepository
import nl.npo.player.sampleApp.shared.data.streamlink.SystemTimeProvider
import nl.npo.player.sampleApp.shared.domain.TimeProvider
import nl.npo.player.sampleApp.shared.domain.TokenProvider

@Module
@InstallIn(SingletonComponent::class)
object StreamLinkModule {
    @Provides
    fun provideTimeProvider(timeProvider: SystemTimeProvider): TimeProvider = timeProvider

    @Provides
    fun provideTokenProvider(tokenProvider: StreamInfoRepository): TokenProvider = tokenProvider
}
