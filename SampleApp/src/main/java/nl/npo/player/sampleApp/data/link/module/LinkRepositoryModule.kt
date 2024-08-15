package nl.npo.player.sampleApp.data.link.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.offline.NPOOfflineContentManager
import nl.npo.player.sampleApp.data.link.OfflineContentDataRepository
import nl.npo.player.sampleApp.data.link.StreamLinkDataRepository
import nl.npo.player.sampleApp.data.link.URLLinkDataRepository
import nl.npo.player.sampleApp.data.streamlink.StreamInfoRepository
import nl.npo.player.sampleApp.domain.LinkRepository
import nl.npo.player.sampleApp.domain.SettingsRepository
import nl.npo.player.sampleApp.domain.annotation.OfflineLinkRepository
import nl.npo.player.sampleApp.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.domain.annotation.URLLinkRepository

@Module
@InstallIn(SingletonComponent::class)
object LinkRepositoryModule {
    @StreamLinkRepository
    @Provides
    fun provideStreamLinkRepo(): LinkRepository = StreamLinkDataRepository

    @URLLinkRepository
    @Provides
    fun provideURLLinkRepo(): LinkRepository = URLLinkDataRepository

    @OfflineLinkRepository
    @Provides
    fun provideOfflineLinkRepo(
        npoOfflineContentManager: NPOOfflineContentManager,
        streamInfoRepository: StreamInfoRepository,
        settingsRepository: SettingsRepository,
    ): LinkRepository.OfflineLinkRepository =
        OfflineContentDataRepository(
            npoOfflineContentManager,
            streamInfoRepository,
            settingsRepository,
        )
}
