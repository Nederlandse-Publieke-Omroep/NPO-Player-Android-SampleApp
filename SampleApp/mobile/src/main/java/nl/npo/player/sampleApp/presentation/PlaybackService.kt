package nl.npo.player.sampleApp.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.R
import androidx.media3.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.presentation.player.PlayerBuildConfig
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import kotlin.jvm.java

@UnstableApi
class PlaybackService : MediaSessionService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)



    private var corePlayer: NPOPlayer? = null
    private var sessionPlayer: Player? = null // Media3 bridge
    private var session: MediaSession? = null
    private lateinit var notifManager: PlayerNotificationManager
    private var currentConfig: PlayerBuildConfig? = null

    inner class LocalBinder : Binder() {
        fun getCorePlayer(): NPOPlayer? = corePlayer

        fun loadAndPlay(
            sourceConfig: NPOSourceConfig,
            config: PlayerBuildConfig,
            pageTracker: PlayerPageTracker
        ) {
            ensurePlayer(config, pageTracker)
            corePlayer?.load(sourceConfig)
            corePlayer?.play()
        }

        fun loadStreamConfig(
            sourceConfig: SourceWrapper,
            buildConfig: PlayerBuildConfig,
            pageTracker: PlayerPageTracker,
            title: String
        ) {
            Log.d("PlaybackService", "loadStreamConfig() title=$title type=${sourceConfig::class.java.simpleName}")

            ensurePlayer(buildConfig, pageTracker)

            val player = corePlayer
            if (player == null) {
                Log.e("PlaybackService", "loadStreamConfig(): corePlayer is NULL after ensurePlayer")
                return
            }

            // THE MOMENT OF TRUTH
            runCatching {
                sourceConfig.npoSourceConfig?.let { player.load(it) }
                player.play()
            }.onFailure {
                Log.e("PlaybackService", "loadStreamConfig(): load/play failed", it)
            }
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }
    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForeground(NOTIFICATION_ID, placeholderNotification() )
        notifManager =
            PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
                .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player) =
                        corePlayer?.lastLoadedSource?.title ?: "Playing"

                    override fun createCurrentContentIntent(player: Player): PendingIntent? =
                        PendingIntent.getActivity(
                            this@PlaybackService,
                            0,
                            Intent(this@PlaybackService, PlayerActivity::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )

                    override fun getCurrentContentText(player: Player) =
                        corePlayer?.lastLoadedSource?.description

                    override fun getCurrentLargeIcon(
                        player: Player,
                        callback: PlayerNotificationManager.BitmapCallback
                    ): Bitmap? = null
                })
                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(
                        notificationId: Int,
                        notification: Notification,
                        ongoing: Boolean
                    ) {
                        // replace placeholder with real media notification
                        startForeground(notificationId, notification)
                    }

                    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                })
                .build()

        notifManager.setPlayer(null)

    }

    private fun ensurePlayer(config: PlayerBuildConfig, pageTracker: PlayerPageTracker) {
        if (corePlayer != null && currentConfig == config && sessionPlayer != null && session != null) return

        val oldSession = session
        val oldPlayer = sessionPlayer
        val oldCore = corePlayer

        // Build NEW first
        val newCore = NPOPlayerLibrary.getPlayer(
            context = applicationContext,
            pageTracker = pageTracker,
            useExoplayer = config.useExoplayer
        )
        val newSessionPlayer = NPOPlayerLibrary.getMedia3BridgePlayer(
            core = newCore,
            scope = serviceScope,
            looper = Looper.getMainLooper()
        )
        val newSession = MediaSession.Builder(this, newSessionPlayer).build()

        // Swap references
        corePlayer = newCore
        sessionPlayer = newSessionPlayer
        session = newSession
        currentConfig = config

        // Attach notification to NEW player (no gap)
        notifManager.setPlayer(newSessionPlayer)

        // Release old AFTER swap
        oldSession?.release()
        oldPlayer?.release()
        oldCore?.destroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        runCatching { notifManager.setPlayer(null) }
        runCatching { session?.release() }
        session = null
        runCatching { sessionPlayer?.release() }
        sessionPlayer = null
        runCatching { corePlayer?.destroy() }
        corePlayer = null
        serviceScope.cancel()
        super.onDestroy()
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
//
    private fun placeholderNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.media3_notification_small_icon) // pick an actual small icon
            .setContentTitle("Preparing playback")
            .setContentText("Starting…")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
}
private const val NOTIFICATION_CHANNEL_ID = "playback"
private const val NOTIFICATION_ID = 1001
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
