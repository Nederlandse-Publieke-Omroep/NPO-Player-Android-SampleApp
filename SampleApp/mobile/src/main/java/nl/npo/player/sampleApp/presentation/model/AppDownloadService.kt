package nl.npo.player.sampleApp.presentation.model

import android.app.Notification
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import nl.npo.player.library.data.offline.exoplayer.NPODownloadService

//class AppDownloadService : NPODownloadService() {
//
//    @OptIn(UnstableApi::class)
//    override fun getDownloadNotification(
//        downloads: MutableList<Download>,
//        notMetRequirements: Int,
//    ): Notification {
//        return NotificationCompat.Builder(this, "DOWNLOAD_NOTIFICATION_CHANNEL_ID")
//            .setSmallIcon(android.R.drawable.stat_sys_download)
//            .setContentTitle("Downloading")
//            .setContentText(
//                downloads.firstOrNull()?.let {
//                    "${it.percentDownloaded.toInt()}%"
//                } ?: "Starting download"
//            )
//            .setProgress(100, downloads.firstOrNull()?.percentDownloaded?.toInt() ?: 0, false)
//            .setOngoing(true)
//            .build()
//    }
//}