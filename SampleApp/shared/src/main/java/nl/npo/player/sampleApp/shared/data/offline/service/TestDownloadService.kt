package nl.npo.player.sampleApp.shared.data.offline.service

import android.app.Notification
import android.graphics.Color
import android.graphics.drawable.Icon
import com.bitmovin.player.offline.service.BitmovinDownloadState
import nl.npo.player.library.presentation.offline.NPODownloadService
import nl.npo.player.sampleApp.shared.R

class TestDownloadService : NPODownloadService() {
    override fun getForegroundNotification(downloadStates: Array<out BitmovinDownloadState>): Notification {
        val title = "Downloading: ${downloadStates.getTitles()}"
        val titleShort = "Downloading ${downloadStates.size} item(s)"
        return Notification.Builder
            .recoverBuilder(
                applicationContext,
                super.getForegroundNotification(downloadStates),
            ).setSmallIcon(androidx.mediarouter.R.drawable.ic_audiotrack_dark)
            .setColor(Color.RED)
            .setLargeIcon(
                Icon.createWithResource(applicationContext, R.mipmap.player_logo),
            ).setContentTitle(titleShort)
            .setStyle(Notification.BigTextStyle().bigText(title))
            .build()
    }

    private fun Array<out BitmovinDownloadState>.getTitles(): String =
        map { it.offlineContent.sourceConfig.title }
            .joinToString()
}
