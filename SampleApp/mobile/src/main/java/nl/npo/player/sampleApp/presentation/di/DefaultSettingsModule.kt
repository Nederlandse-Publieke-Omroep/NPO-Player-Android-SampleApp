package nl.npo.player.sampleApp.presentation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.presentation.ext.supportsCasting
import nl.npo.player.sampleApp.shared.domain.model.DefaultSettings

@Module
@InstallIn(SingletonComponent::class)
object DefaultSettingsModule {
    @Provides
    fun provideDefaultSettings(
        @ApplicationContext context: Context,
    ): DefaultSettings {
        return DefaultSettings(
            enableCasting = context.supportsCasting,
        )
    }
}
