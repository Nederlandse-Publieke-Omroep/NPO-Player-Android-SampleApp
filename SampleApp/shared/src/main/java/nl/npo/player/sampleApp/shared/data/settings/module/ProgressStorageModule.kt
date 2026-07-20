package nl.npo.player.sampleApp.shared.data.settings.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.npo.player.library.domain.offline.ProgressStorageProvider
import nl.npo.player.sampleApp.shared.data.progress.SharedPreferencesProgressStorageProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProgressStorageModule {
    @Provides
    @Singleton
    fun provideProgressStorageProvider(
        @ApplicationContext context: Context,
    ): ProgressStorageProvider {
        return SharedPreferencesProgressStorageProvider(
            context = context,
        )
    }
}
