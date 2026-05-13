package nl.npo.player.sampleApp.shared.data.offline.service

import android.app.Notification
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import nl.npo.player.library.data.offline.exoplayer.NPODownloadService
import nl.npo.player.sampleApp.shared.R

class TestDownloadService : NPODownloadService() {

    @OptIn(UnstableApi::class)
    override fun getDownloadNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        Log.d("OfflineDownload", "getDownloadNotification called, downloads=${downloads.size}")
        val title = "Downloading: ${downloads.getTitles()}"
        val titleShort = "Downloading ${downloads.size} item(s)"

        return NotificationCompat.Builder(applicationContext, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(androidx.mediarouter.R.drawable.ic_audiotrack_dark)
            .setColor(Color.RED)
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, R.mipmap.player_logo)
            )
            .setContentTitle(titleShort)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setOngoing(true)
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun MutableList<Download>.getTitles(): String =
        mapNotNull { download ->
            download.request.keySetId?.toString()
        }.joinToString()
}
