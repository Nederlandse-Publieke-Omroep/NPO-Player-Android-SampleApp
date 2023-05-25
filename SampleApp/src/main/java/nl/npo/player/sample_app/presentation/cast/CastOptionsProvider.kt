package nl.npo.player.sample_app.presentation.cast

import android.content.Context
import com.bitmovin.player.casting.BitmovinCastOptionsProvider
import com.google.android.gms.cast.framework.CastOptions
import nl.npo.player.sample_app.R

/**
 * This is currently a wrapper around the BitMovin cast provider. In the future we will either move
 * this wrapper to a new Cast module in the Player library if the Player team does the Chromecast
 * implementation, or move back to our own Chromecast implementation which is initialized in the
 * commented out code.
 */
class CastOptionsProvider : BitmovinCastOptionsProvider() {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_receiver_id)).build()
    }
}
