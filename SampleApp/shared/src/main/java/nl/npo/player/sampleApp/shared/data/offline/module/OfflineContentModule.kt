package nl.npo.player.sampleApp.shared.data.offline.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.offline.NPOOfflineContentManager

@Module
@InstallIn(SingletonComponent::class)
object OfflineContentModule {
    @Provides
    fun provideNPOOfflineContentManager(): NPOOfflineContentManager = NPOPlayerLibrary.Offline.getContentManager()
}
