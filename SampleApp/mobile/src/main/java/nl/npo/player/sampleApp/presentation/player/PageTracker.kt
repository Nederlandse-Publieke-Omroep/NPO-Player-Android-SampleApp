package nl.npo.player.sampleApp.presentation.player

import android.app.Application
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.tag.sdk.tracker.PageTracker

class PageTracker(private val application: Application) {
    var pageTracker: PageTracker? = null

    fun logPageAnalytics(pageName: String) {
        if (pageTracker == null) {
            (application as PlayerApplication).npoTag?.apply {
                pageTracker = pageTrackerBuilder().withPageName(pageName).build()
            }
        }
        pageTracker?.pageView()
    }
}
