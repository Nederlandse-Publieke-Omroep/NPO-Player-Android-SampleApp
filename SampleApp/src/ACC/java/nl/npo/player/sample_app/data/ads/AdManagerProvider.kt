package nl.npo.player.sample_app.data.ads

import android.content.Context
import nl.npo.player.library.domain.ads.AdManager
import nl.npo.player.library.domain.ads.SterConfiguration
import nl.npo.player.library.sterads.presentation.ads.ImaAdManager

object AdManagerProvider {
    fun getAdManager(applicationContext: Context): AdManager =
        ImaAdManager(SterConfiguration(applicationContext.packageName))
}
