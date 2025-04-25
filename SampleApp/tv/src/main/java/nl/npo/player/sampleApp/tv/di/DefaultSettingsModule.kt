package nl.npo.player.sampleApp.tv.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.domain.model.DefaultSettings

@Module
@InstallIn(SingletonComponent::class)
object DefaultSettingsModule {
    @Provides
    fun provideDefaultSettings(): DefaultSettings {
        return DefaultSettings(
            enableCasting = false,
        )
    }
}
