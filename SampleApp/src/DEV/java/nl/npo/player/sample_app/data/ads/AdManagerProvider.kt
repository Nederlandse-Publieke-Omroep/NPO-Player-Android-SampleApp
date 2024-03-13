package nl.npo.player.sample_app.data.ads

import android.content.Context
import nl.npo.player.library.domain.ads.AdManager
import nl.npo.player.library.domain.ads.NoAdManager

object AdManagerProvider {
    fun getAdManager(_context: Context): AdManager = NoAdManager()
}
