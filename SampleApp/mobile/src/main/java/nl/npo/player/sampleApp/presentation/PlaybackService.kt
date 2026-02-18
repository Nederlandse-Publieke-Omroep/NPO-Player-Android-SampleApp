package nl.npo.player.sampleApp.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
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
import com.bitmovin.player.api.source.SourceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker


import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.sampleApp.presentation.player.PlayerBuildConfig
import nl.npo.player.sampleApp.shared.data.settings.SettingsPreferences.Keys.useExoplayer
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.tag.sdk.tracker.PageTracker
import kotlin.jvm.java

@UnstableApi
class PlaybackService : MediaSessionService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

//    private lateinit var session: MediaSession
//    private lateinit var notifManager: PlayerNotificationManager
//    private lateinit var config: SourceWrapper
//    private var player: NPOPlayer? = null


    private var corePlayer: NPOPlayer? = null
    private var sessionPlayer: Player? = null // Media3 bridge
    private lateinit var session: MediaSession
    private lateinit var notifManager: PlayerNotificationManager
    private var currentConfig: PlayerBuildConfig? = null

    inner class LocalBinder : Binder() {
        fun getCorePlayer(): NPOPlayer? = corePlayer

        fun ensurePlayer(buildConfig: PlayerBuildConfig, pageTracker: PageTracker) {
            Log.d("PlaybackService", "ensurePlayer() called")

            if (corePlayer != null && currentConfig == buildConfig) {
                Log.d("PlaybackService", "ensurePlayer(): already exists")
                return
            }

            // release old if any
            runCatching { corePlayer?.destroy() }

            // build new
            try {
                corePlayer = NPOPlayerLibrary.getPlayer(
                    context = applicationContext,
                    pageTracker = PlayerTagProvider.getPageTracker(pageTracker),
                    useExoplayer = buildConfig.useExoplayer,
                )
                currentConfig = buildConfig
                Log.d("PlaybackService", "ensurePlayer(): CREATED core=${corePlayer != null}")
            } catch (t: Throwable) {
                corePlayer = null
                currentConfig = null
                Log.e("PlaybackService", "ensurePlayer(): getPlayer FAILED", t)
            }
        }

        fun loadStreamConfig(
            sourceConfig: SourceWrapper,
            buildConfig: PlayerBuildConfig,
            pageTracker: PageTracker,
            title: String
        ) {
            Log.d("PlaybackService", "loadStreamConfig() title=$title type=${sourceConfig::class.java.simpleName}")

            ensurePlayer(buildConfig, pageTracker)

            val p = corePlayer
            if (p == null) {
                Log.e("PlaybackService", "loadStreamConfig(): corePlayer is NULL after ensurePlayer")
                return
            }

            // THE MOMENT OF TRUTH
            runCatching {
                sourceConfig.npoSourceConfig?.let { p.load(it) }
                p.play()
            }.onFailure {
                Log.e("PlaybackService", "loadStreamConfig(): load/play failed", it)
            }
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        // MediaSessionService has its own binding behavior;
        // but you can still return your binder for your app's binding intent.
        return binder
    }
    override fun onCreate() {
        super.onCreate()
ensureChannel()
        startForeground(NOTIFICATION_ID, placeholderNotification())

        notifManager = PlayerNotificationManager.Builder(
            this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID
        ).build()

        notifManager.setPlayer(null)
    }

    private fun ensurePlayer(config: PlayerBuildConfig, pageTracker: PageTracker) {
        if (corePlayer != null && currentConfig == config && sessionPlayer != null && session != null) return

        // 1) Detach notification from old player
        notifManager.setPlayer(null)

        // 2) Release old session/player
        session.release()
        sessionPlayer?.release() // only if your bridge needs releasing
        sessionPlayer = null
        corePlayer?.destroy()   // whatever your SDK uses
        corePlayer = null

        // 3) Build new SDK core player
        val newCore = NPOPlayerLibrary.getPlayer(
            context = applicationContext,
            pageTracker = PlayerTagProvider.getPageTracker(pageTracker) ,
            useExoplayer = config.useExoplayer
        )

        // 4) Build/get Media3 bridge Player (THIS MUST EXIST)
        val newSessionPlayer: Player =
            NPOPlayerLibrary.getMedia3BridgePlayer( // ideal: exposed by SDK
                core = newCore,
                scope = serviceScope,
                looper = Looper.getMainLooper()
            )

        corePlayer = newCore
        sessionPlayer = newSessionPlayer
        currentConfig = config

        // 5) Build MediaSession on the Media3 player
        session = MediaSession.Builder(this, newSessionPlayer).build()

        // 6) Hook notification to the Media3 player
        notifManager.setPlayer(newSessionPlayer)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        notifManager.setPlayer(null)
        session?.release()
        corePlayer?.destroy()
        sessionPlayer?.release()
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
////    notifManager =
////    PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
////    .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
////        override fun getCurrentContentTitle(player: Player) =
////            config.npoSourceConfig?.toMediaItem()?.mediaMetadata?.title ?: "Playing "
////        // ?: "Playing"
////        //player.mediaMetadata.title ?: "Playing"
////
////        override fun createCurrentContentIntent(player: Player): PendingIntent? =
////            PendingIntent.getActivity(
////                this@PlaybackService,
////                0,
////                Intent(this@PlaybackService, PlayerActivity::class.java),
////                PendingIntent.FLAG_IMMUTABLE
////            )
////
////        override fun getCurrentContentText(player: Player) =
////            config.npoSourceConfig?.toMediaItem()?.mediaMetadata?.artist
////        //playbackRouter.snapshot.value.nowPlaying?.artist
////        // player.mediaMetadata.artist
////
////        override fun getCurrentLargeIcon(
////            player: Player,
////            callback: PlayerNotificationManager.BitmapCallback
////        ): Bitmap? = null
////    })
////    .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
////        override fun onNotificationPosted(
////            notificationId: Int,
////            notification: Notification,
////            ongoing: Boolean
////        ) {
////            // replace placeholder with real media notification
////            startForeground(notificationId, notification)
////        }
////
////        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
////            stopForeground(STOP_FOREGROUND_REMOVE)
////            stopSelf()
////        }
////    })
////    .build()
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
