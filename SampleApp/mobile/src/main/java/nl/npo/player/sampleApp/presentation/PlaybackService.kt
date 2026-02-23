package nl.npo.player.sampleApp.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.R
import androidx.media3.ui.PlayerNotificationManager
import androidx.transition.Visibility
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.ext.mediaSession
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.presentation.player.PlayerBuildConfig
import nl.npo.player.sampleApp.presentation.player.PlayerRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
@UnstableApi
class PlaybackService : MediaSessionService() {

    @Inject lateinit var repo: PlayerRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var lastTracker: PlayerPageTracker? = null

    private var sessionPlayer: Player? = null // Media3 bridge
    private var session: MediaSession? = null
    private lateinit var notifManager: PlayerNotificationManager
    private var currentConfig: PlayerBuildConfig? = null


    fun loadStreamConfig(config: NPOSourceConfig) {
        Log.d("PlaybackService", "Service loading stream: $config")
         repo.player.value?.load(config)
            repo.player.value?.play()

    }

    fun loadOffline(config: NPOOfflineSourceConfig) {
        Log.d("PlaybackService", "Service loading offline: $config")
        repo.player.value?.load(config)
        repo.player.value?.play()
    }
    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
        fun loadStreamConfig(config: NPOSourceConfig) = this@PlaybackService.loadStreamConfig(config)
        fun loadOffline(config: NPOOfflineSourceConfig) = this@PlaybackService.loadOffline(config)

        fun loadAndPlay(
            sourceWrapper: SourceWrapper,
            context: Context,
            playerConfig: NPOPlayerConfig,
            npoPlayerColors: NativePlayerColors?,
            useExoplayer: Boolean,
            playerUIConfig: NPOPlayerUIConfig,
            pageTracker: PlayerPageTracker

        ) {
            serviceScope.launch {
                repo.player.filterNotNull().first().apply {
                    sourceWrapper.npoSourceConfig?.let { load(it) }
                    play()
                }
            }
        }

        fun loadStreamConfig(
            sourceWrapper: SourceWrapper,
            context: Context,
            playerConfig: NPOPlayerConfig,
            npoPlayerColors: NativePlayerColors?,
            useExoplayer: Boolean,
            playerUIConfig: NPOPlayerUIConfig,
            pageTracker: PlayerPageTracker,
            title: String
        ) {
            Log.d("PlaybackService", "loadStreamConfig() title=$title type=${sourceWrapper::class.java.simpleName}")

            serviceScope.launch {
                val player = repo.player.filterNotNull().first()
                runCatching {
                    sourceWrapper.npoSourceConfig?.let { player.load(it) }
                    player.play()
                }.onFailure {
                    Log.e("PlaybackService", "loadStreamConfig(): load/play failed", it)
                }
            }
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
       return  binder
    }

    override fun onDestroy() {
        serviceScope.launch { repo.release() }
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()

        ensureChannel()

        startForeground(NOTIFICATION_ID, placeholderNotification())

        notifManager =
            PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(
                        notificationId: Int,
                        notification: Notification,
                        ongoing: Boolean
                    ) {
                        startForeground(notificationId, notification)
                    }

                    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                })
                .setMediaDescriptionAdapter(mediaDescriptionAdapter())
                .build()

        serviceScope.launch {
            repo.player
                .filterNotNull()
                .collectLatest { core ->
                    val providedSession = core.mediaSession
                        ?: run {
                            Log.e("PlaybackService", "core player does not provide MediaSession")
                            return@collectLatest
                        }

                    session = providedSession
                    notifManager.setMediaSessionToken(providedSession.platformToken)
                    notifManager.setPlayer(providedSession.player)
                }
        }
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session


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
