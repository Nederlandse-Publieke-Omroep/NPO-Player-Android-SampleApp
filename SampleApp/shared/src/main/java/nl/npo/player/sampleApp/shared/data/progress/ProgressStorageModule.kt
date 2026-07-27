package nl.npo.player.sampleApp.shared.data.progress

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressStorageModule {
    @Binds
    abstract fun bindsProgressStorageProvider(impl: SharedPreferencesProgressStorageProvider): ProgressStorageProvider

    @Binds
    abstract fun bindsProgressStorageRepository(impl: ProgressStorageRepositoryImpl): ProgressStorageRepository
}
