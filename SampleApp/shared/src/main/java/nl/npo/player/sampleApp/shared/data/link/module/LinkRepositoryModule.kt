package nl.npo.player.sampleApp.shared.data.link.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.offline.NPOOfflineContentManager
import nl.npo.player.sampleApp.shared.data.link.OfflineContentDataRepository
import nl.npo.player.sampleApp.shared.data.link.StreamLinkDataRepository
import nl.npo.player.sampleApp.shared.data.link.URLLinkDataRepository
import nl.npo.player.sampleApp.shared.data.streamlink.StreamInfoRepository
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.annotation.OfflineLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.URLLinkRepository

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
