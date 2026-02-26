package nl.npo.player.sampleApp.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.R
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import androidx.transition.Visibility
import com.bitmovin.player.api.PlayerConfig
import com.google.android.datatransport.runtime.scheduling.persistence.EventStoreModule_PackageNameFactory.packageName
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.ext.mediaSession
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.presentation.player.PlayerBuildConfig
import nl.npo.player.sampleApp.presentation.player.PlayerRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import java.lang.runtime.ObjectMethods.bootstrap
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
@UnstableApi
class PlaybackService : MediaSessionService() {

    @Inject lateinit var repo: PlayerRepository
    private var session: MediaSession? = null



    override fun onBind(intent: Intent?): IBinder? {
        Log.d("DEBUG_INFO", "onBind action=${intent?.action}")
      return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()

       val player = repo.ensurePlayer(applicationContext)

//        ensureChannel()
//
//        // Start foreground immediately to satisfy Android’s timing rules.
//        startForeground(NOTIFICATION_ID, placeholderNotification())
//
//        notifManager =
//            PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
//                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
//                    override fun onNotificationPosted(
//                        notificationId: Int,
//                        notification: Notification,
//                        ongoing: Boolean
//                    ) {
//                        startForeground(notificationId, notification)
//                    }
//
//                    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//                        stopForeground(STOP_FOREGROUND_REMOVE)
//                        stopSelf()
//
//                    }
//                })
//                .setMediaDescriptionAdapter(mediaDescriptionAdapter())
//                .build()

//                 serviceScope.launch {
//                  repo.player.filterNotNull().collectLatest {
//                     session = it.mediaSession
//                 }

        session = player.mediaSession




    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
         return session
    }



       private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

//    fun Context.ensurePlaybackNotificationChannel() {
//        if (android.os.Build.VERSION.SDK_INT < 26) return
//
//        val channel = android.app.NotificationChannel(
//            "1001", NOTIFICATION_CHANNEL_ID,
//            android.app.NotificationManager.IMPORTANCE_LOW
//        )
//        val nm = getSystemService(android.app.NotificationManager::class.java)
//        nm.createNotificationChannel(channel)
//    }
//
    private fun placeholderNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.media3_notification_small_icon)
            .setContentTitle("Preparing playback")
            .setContentText("Starting…")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }


    @UnstableApi
    private fun mediaDescriptionAdapter(): PlayerNotificationManager.MediaDescriptionAdapter {
        return object : PlayerNotificationManager.MediaDescriptionAdapter {

            override fun getCurrentContentTitle(player: Player): CharSequence {
                val md = player.mediaMetadata
                return md.title
                    ?: md.displayTitle
                    ?: "Playing"
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                val md = player.mediaMetadata
                return md.artist
                    ?: md.albumArtist
                    ?: md.subtitle
            }

            override fun getCurrentSubText(player: Player): CharSequence? {
                val md = player.mediaMetadata
                return md.albumTitle
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
                return PendingIntent.getActivity(
                    this@PlaybackService,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return null
            }
        }
    }


}

private const val NOTIFICATION_CHANNEL_ID = "NPO-PlayerSampleApp"
private const val NOTIFICATION_ID = 1
private const val MEDIA_SESSION_TAG = "npo-player-mediaSession"
//
//
//

//    private fun ensureChannel() {
//        if (Build.VERSION.SDK_INT >= 26) {
//            val channel = NotificationChannel(
//                NOTIFICATION_CHANNEL_ID,
//                "Playback",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
//        }
//    }
//
//    private fun placeholderNotification(): Notification {
//        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.media3_notification_small_icon) // pick an actual small icon
//            .setContentTitle("Preparing playback")
//            .setContentText("Starting…")
//            .setOngoing(true)
//            .setOnlyAlertOnce(true)
//            .build()
//    }
//
//    @UnstableApi
//    override fun onUpdateNotification(
//        session: MediaSession,
//        startInForegroundRequired: Boolean,
//    ) {
//        super.onUpdateNotification(session, startInForegroundRequired)
//
////        if (startInForegroundRequired) {
////            startForeground(notification.notificationId, notification.notification)
////        } else {
////            getSystemService(NotificationManager::class.java)
////                .notify(notification.notificationId, notification.notification)
////        }
//    }
//
//
//}
//private const val NOTIFICATION_CHANNEL_ID = "playback"
//private const val NOTIFICATION_ID = 1001
