package nl.npo.player.sampleApp.presentation.cast

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/**
 * This is currently a wrapper around Google's cast provider. In the future we will either move
 * this wrapper to a new Cast module in the Player library if the Player team does the Chromecast
 * implementation, or move back to our own Chromecast implementation which is initialized in the
 * commented out code.
 */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setLaunchOptions(LaunchOptions.Builder().setRelaunchIfRunning(true).build())
            .setReceiverApplicationId(context.getString(getReceiverID()))
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider>? {
        return null
    }

    companion object {
        @StringRes
        fun getReceiverID(): Int {
            return nl.npo.player.library.library.R.string.npo_player_chromecast_receiver_id_production
        }
    }
}
