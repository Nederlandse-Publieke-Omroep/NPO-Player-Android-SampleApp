package nl.npo.player.sample_app.presentation.cast

import android.content.Context
import android.os.Build
import com.bitmovin.player.casting.BitmovinCastOptionsProvider
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.media.CastMediaOptions
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
            .apply {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                    setCastMediaOptions(
                        CastMediaOptions.Builder().setMediaSessionEnabled(false).build()
                    )
                }
            }
            .setReceiverApplicationId(context.getString(R.string.cast_receiver_id)).build()
    }
}
