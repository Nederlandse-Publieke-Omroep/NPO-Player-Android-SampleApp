package nl.npo.player.sample_app.presentation.cast

import android.content.Context
import androidx.annotation.StringRes
import com.bitmovin.player.casting.BitmovinCastOptionsProvider
import com.google.android.gms.cast.framework.CastOptions

/**
 * This is currently a wrapper around the BitMovin cast provider. In the future we will either move
 * this wrapper to a new Cast module in the Player library if the Player team does the Chromecast
 * implementation, or move back to our own Chromecast implementation which is initialized in the
 * commented out code.
 */
class CastOptionsProvider : BitmovinCastOptionsProvider() {
    override fun getCastOptions(context: Context): CastOptions {
        super.getCastOptions(context)
        return CastOptions.Builder()
            .with(super.getCastOptions(context))
//            .setReceiverApplicationId(context.getString(getReceiverID())) --> No longer needed as it's set by NPOCasting.initializeCasting() in the SampleApplication
            .build()
    }

    private fun CastOptions.Builder.with(castOptions: CastOptions): CastOptions.Builder {
        return this
            .setSupportedNamespaces(castOptions.supportedNamespaces)
            .setCastMediaOptions(castOptions.castMediaOptions)
            .setLaunchOptions(castOptions.launchOptions)
            .setReceiverApplicationId(castOptions.receiverApplicationId)
            .setEnableReconnectionService(castOptions.enableReconnectionService)
            .setResumeSavedSession(castOptions.resumeSavedSession)
            .setStopReceiverApplicationWhenEndingSession(castOptions.stopReceiverApplicationWhenEndingSession)
    }

    companion object {
        @StringRes
        fun getReceiverID(): Int {
            return nl.npo.player.library.library.R.string.npo_player_chromecast_receiver_id_production
        }
    }
}
