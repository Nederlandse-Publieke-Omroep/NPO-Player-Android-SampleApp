package nl.npo.player.sampleApp.presentation

import androidx.appcompat.app.AppCompatActivity
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.tag.sdk.tracker.PageTracker

abstract class BaseActivity : AppCompatActivity() {
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
