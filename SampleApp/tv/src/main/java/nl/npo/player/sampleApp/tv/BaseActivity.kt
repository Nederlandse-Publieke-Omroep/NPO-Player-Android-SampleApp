package nl.npo.player.sampleApp.tv

import androidx.fragment.app.FragmentActivity
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.tag.sdk.tracker.PageTracker

abstract class BaseActivity : FragmentActivity() {
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
