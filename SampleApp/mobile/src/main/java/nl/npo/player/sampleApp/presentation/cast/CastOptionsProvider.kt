package nl.npo.player.sampleApp.presentation.cast

import android.content.Context
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nl.npo.player.sampleApp.shared.domain.SettingsRepository

/**
 * This is currently a wrapper around Google's cast provider. In the future we will either move
 * this wrapper to a new Cast module in the Player library if the Player team does the Chromecast
 * implementation, or move back to our own Chromecast implementation which is initialized in the
 * commented out code.
 */
class CastOptionsProvider : OptionsProvider {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CastOptionsEntryPoint {
        fun settingsRepository(): SettingsRepository
    }

    override fun getCastOptions(context: Context): CastOptions {
        val castReceiver =
            runBlocking {
                EntryPointAccessors
                    .fromApplication(context, CastOptionsEntryPoint::class.java)
                    .settingsRepository()
                    .castReceiver
                    .first()
            }
        return CastOptions.Builder()
            .setLaunchOptions(LaunchOptions.Builder().setRelaunchIfRunning(true).build())
            .setReceiverApplicationId(castReceiver.applicationId)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider>? {
        return null
    }
}
