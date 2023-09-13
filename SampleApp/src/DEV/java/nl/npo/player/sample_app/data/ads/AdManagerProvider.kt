package nl.npo.player.sample_app.data.ads

import nl.npo.player.library.domain.ads.AdManager
import nl.npo.player.library.domain.ads.NoAdManager

object AdManagerProvider {
    fun getAdManager(): AdManager = NoAdManager()
}
