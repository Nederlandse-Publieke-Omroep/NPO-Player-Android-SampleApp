package nl.npo.player.sampleApp.shared.data.ads

import android.content.Context
import nl.npo.player.library.domain.ads.AdManager
import nl.npo.player.library.domain.ads.SterConfiguration
import nl.npo.player.library.sterads.presentation.ads.VastAdManager

object AdManagerProvider {
    fun getAdManager(applicationContext: Context): AdManager = VastAdManager(SterConfiguration(applicationContext.packageName))
}
