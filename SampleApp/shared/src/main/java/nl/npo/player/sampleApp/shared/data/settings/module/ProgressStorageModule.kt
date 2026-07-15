package nl.npo.player.sampleApp.shared.data.settings.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.data.progress.ProgressStorageRepositoryImpl
import nl.npo.player.sampleApp.shared.domain.ProgressStorageRepository
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressStorageModule {
    @Binds
    @Singleton
    abstract fun bindsProgressRepository(impl: ProgressStorageRepositoryImpl): ProgressStorageRepository
}