package nl.npo.player.sampleApp.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.error.NPOPlayerError
import nl.npo.player.library.domain.player.media.NPOSubtitleTrack
import nl.npo.player.library.domain.player.model.NPOFullScreenHandler
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.player.ui.model.NPOPlayerColors
import nl.npo.player.library.domain.player.ui.model.PlayNextListenerResult
import nl.npo.player.library.domain.state.StoppedPlayingReason
import nl.npo.player.library.domain.state.StreamOptions
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.extension.getMessage
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.notifications.NPONotificationManager
import nl.npo.player.library.presentation.pip.DefaultNPOPictureInPictureHandler
import nl.npo.player.library.presentation.pip.NPOPictureInPictureHandler
import nl.npo.player.library.setupPlayerNotificationManager
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.databinding.ActivityPlayerBinding
import nl.npo.player.sampleApp.presentation.BaseActivity
import nl.npo.player.sampleApp.presentation.MainActivity
import nl.npo.player.sampleApp.presentation.ext.isGooglePlayServicesAvailable
import nl.npo.player.sampleApp.presentation.player.enums.PlaybackSpeeds
import nl.npo.player.sampleApp.presentation.player.enums.PlayerSettings
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlayerViewModel
import nl.npo.tag.sdk.tracker.PageTracker

const val PLAYER_SOURCE = "PLAYER_SOURCE"
const val PLAYER_OFFLINE_SOURCE = "PLAYER_OFFLINE_SOURCE"

@AndroidEntryPoint
class PlayerActivity : BaseActivity() {
    private var player: NPOPlayer? = null
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var sourceWrapper: SourceWrapper
    private val playerViewModel by viewModels<PlayerViewModel>()
    private val linkViewModel by viewModels<LinksViewModel>()
    private var npoNotificationManager: NPONotificationManager? = null
    private var backstackLost = false
    private var pipHandler: NPOPictureInPictureHandler? = null

    private val onPlayPauseListener: PlayerListener =
        object : PlayerListener {
            override fun onPlaybackFinished(currentPosition: Double) {
                binding.btnPlayPause.isVisible = false
            }

            override fun onPaused(
                currentPosition: Double,
                isAd: Boolean,
                stoppedPlayingReason: StoppedPlayingReason,
            ) {
                binding.btnPlayPause.apply {
                    isVisible = !fullScreenHandler.isFullscreen
                    setImageResource(android.R.drawable.ic_media_play)
                }
            }

            override fun onPlaying(
                currentPosition: Double,
                isAd: Boolean,
            ) {
                binding.btnPlayPause.apply {
                    isVisible = !fullScreenHandler.isFullscreen
                    setImageResource(android.R.drawable.ic_media_pause)
                }
            }

            override fun onSourceLoaded(
                currentPosition: Double,
                source: NPOSourceConfig,
                streamOptions: StreamOptions,
                maxTimeShift: Double,
            ) {
                binding.btnPlayPause.apply {
                    isVisible = !fullScreenHandler.isFullscreen
                    setImageResource(android.R.drawable.ic_media_play)
                }
            }

            override fun onSourceError(
                currentPosition: Double,
                error: NPOPlayerError,
                retryPossible: Boolean,
            ) {
                binding.btnPlayPause.isVisible = false
            }

            override fun onSourceLoad(
                currentPosition: Double,
                source: NPOSourceConfig,
            ) {
                // NOTE: This is not done to actually seek, but to make sure that if an app does this it won't crash. An error should be broadcasted through `onPlayerError`
                if (!NPOCasting.isCastingConnected()) player?.seekOrTimeShift(10000.0)

                binding.btnPlayPause.isVisible = false
            }

            override fun onCanStartPlayingBecauseSwitchedToWiFi() {
                player?.play()
            }

            override fun onPlayerError(
                currentPosition: Double,
                error: NPOPlayerError,
                retryPossible: Boolean,
            ) {
                Log.w(
                    "SampleAppTest",
                    "Error: code: ${error.errorCode}: ${error.getMessage(this@PlayerActivity)}",
                )
            }
        }

    private val castStateListener: CastStateListener =
        CastStateListener { state ->
            binding.mediaRouteButton.isVisible = state != CastState.NO_DEVICES_AVAILABLE
        }

    private val retryListener: (Double) -> Unit = {
        playerViewModel.retrieveSource(
            sourceWrapper.copy(startOffset = it),
            ::handleTokenState,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupViews()
        setObservers()
        loadSourceWrapperFromIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        NPOCasting.updateCastingContext(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadSourceWrapperFromIntent(intent)
    }

    private fun loadSourceWrapperFromIntent(intent: Intent?) {
        sourceWrapper = intent?.getSourceWrapper() ?: run {
            finish()
            return
        }

        playerViewModel.getConfiguration { playerConfig, npoPlayerColors ->
            loadSource(sourceWrapper, playerConfig, npoPlayerColors)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && !fullScreenHandler.isFullscreen) {
            binding.npoVideoPlayerNative.setFullScreen(true)
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun loadSource(
        sourceWrapper: SourceWrapper,
        playerConfig: NPOPlayerConfig,
        npoPlayerColors: NPOPlayerColors?,
    ) {
        val title = sourceWrapper.title
        if (player == null) {
            logPageAnalytics(title ?: "")

            try {
                player =
                    NPOPlayerLibrary
                        .getPlayer(
                            context = binding.root.context,
                            npoPlayerConfig = playerConfig,
                            pageTracker =
                                pageTracker?.let { PlayerTagProvider.getPageTracker(it) }
                                    ?: PlayerTagProvider.getPageTracker(
                                        PageConfiguration(
                                            title ?: "",
                                        ),
                                    ),
                        ).apply {
                            val defaultPipHandler =
                                DefaultNPOPictureInPictureHandler(
                                    this@PlayerActivity,
                                    this,
                                ).also {
                                    pipHandler = it
                                }
                            remoteControlMediaInfoCallback = PlayerViewModel.remoteCallback
                            eventEmitter.addListener(onPlayPauseListener)
                            npoNotificationManager =
                                setupPlayerNotificationManager(
                                    NOTIFICATION_CHANNEL_ID,
                                    R.string.app_name,
                                    R.drawable.ic_launcher_foreground,
                                    NOTIFICATION_ID,
                                )
                            attachToLifecycle(lifecycle)
                            setTokenRefreshCallback(retryListener)
                            playNextListener = { action ->
                                when (action) {
                                    is PlayNextListenerResult.Triggered -> playRandom()
                                }
                            }

                            val player = this
                            binding.npoVideoPlayerNative.apply {
                                attachPlayer(
                                    npoPlayer = player,
                                    npoPlayerColors = npoPlayerColors ?: NPOPlayerColors(),
                                )
                                setFullScreenHandler(fullScreenHandler)
                                enablePictureInPictureSupport(defaultPipHandler)

                                playerViewModel.hasCustomSettings {
                                    setSettingsButtonOnClickListener {
                                        showSettings()
                                        setSettingsButtonState(true)
                                    }
                                }
                            }
                            binding.npoVideoPlayerNative.isVisible
                        }
            } catch (e: NPOPlayerException.PlayerInitializationException) {
                AlertDialog
                    .Builder(this)
                    .setTitle("Error")
                    .setMessage("Player Analytics not initialized correctly. ${e.message}")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Ok",
                    ) { _, _ -> finish() }
                    .show()
                return
            }
        } else {
            // Note: This is only to simulate switching pages. A normal app shouldn't need to do such a switch at stream load, only when switching to a new page with the same player..
            changePageTracker(title ?: "")
        }

        when {
            sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig ->
                loadStreamURL(
                    sourceWrapper.npoSourceConfig as NPOOfflineSourceConfig,
                )

            sourceWrapper.getStreamLink ->
                playerViewModel.retrieveSource(
                    sourceWrapper,
                    ::handleTokenState,
                )

            sourceWrapper.npoSourceConfig != null -> loadStreamURL(sourceWrapper.npoSourceConfig!!)
            else -> finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player?.isPlaying != true) return

        pipHandler?.enterPictureInPicture()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            binding.mediaRouteButton.isVisible = false
        } else {
            backstackLost = true
            val castContext = CastContext.getSharedInstance(this@PlayerActivity)
            binding.mediaRouteButton.isVisible =
                castContext.castState != CastState.NO_DEVICES_AVAILABLE
        }
    }

    override fun finish() {
        if (backstackLost) {
            finishAndRemoveTask()
            startActivity(
                Intent.makeRestartActivityTask(
                    ComponentName(
                        this,
                        MainActivity::class.java,
                    ),
                ),
            )
        } else {
            super.finish()
        }
    }

    override fun onDestroy() {
        player?.apply {
            eventEmitter.removeListener(onPlayPauseListener)
        }
        binding.npoVideoPlayerNative.onDestroy()

        npoNotificationManager?.setPlayer(null)
        if (isGooglePlayServicesAvailable()) {
            CastContext
                .getSharedInstance(this@PlayerActivity)
                .removeCastStateListener(castStateListener)
        }
        super.onDestroy()
    }

    private fun ActivityPlayerBinding.setupViews() {
        setupCastButton()

        btnSwitchStreams.setOnClickListener {
            playRandom()
        }
        btnPlayPause.setOnClickListener {
            player?.apply {
                if (isPlaying) {
                    pause()
                } else {
                    play()
                }
            }
        }
    }

    private fun ActivityPlayerBinding.setupCastButton() {
        if (!NPOCasting.isCastingEnabled || !isGooglePlayServicesAvailable()) {
            mediaRouteButton.isVisible = false
            return
        }

        val castContext = CastContext.getSharedInstance(this@PlayerActivity)
        castContext.addCastStateListener(castStateListener)
        mediaRouteButton.isVisible = castContext.castState != CastState.NO_DEVICES_AVAILABLE
        CastButtonFactory.setUpMediaRouteButton(this@PlayerActivity, mediaRouteButton)
    }

    private fun playRandom() {
        playerViewModel.onlyStreamLinkRandomEnabled { enabled ->
            if (enabled) {
                linkViewModel.streamLinkList.value
            } else {
                linkViewModel.streamLinkList.value?.union(
                    linkViewModel.urlLinkList.value ?: emptyList(),
                )
            }?.filter { it.avType != player?.npoSourceConfig?.avType }
                ?.random()
                ?.let { newSource ->
                    playerViewModel.getConfiguration { config, npoPlayerColors ->
                        loadSource(newSource, config, npoPlayerColors)
                    }
                }
        }
    }

    private fun showSettings() {
        getSettings().let { settings ->
            AlertDialog
                .Builder(this@PlayerActivity)
                .setItems(
                    settings.map { it.name }.toTypedArray(),
                ) { dialog, which ->
                    when (settings[which]) {
                        PlayerSettings.SUBTITLES -> showSubtitleDialog()
                        PlayerSettings.AUDIO_QUALITIES -> showAudioQualityDialog()
                        PlayerSettings.AUDIO_TRACKS -> showAudioTracksDialog()
                        PlayerSettings.VIDEO_QUALITIES -> showVideoQualityDialog()
                        PlayerSettings.SPEED -> showSpeedSelectionDialog()
                    }
                    dialog.dismiss()
                }.setOnDismissListener {
                    binding.npoVideoPlayerNative.setSettingsButtonState(false)
                }.create()
                .show()
        }
    }

    private fun getSettings(): List<PlayerSettings> =
        listOfNotNull(
            subtitleSettings(),
            audioQualitiesSettings(),
            audioTrackSettings(),
            videoQualitiesSettings(),
            PlayerSettings.SPEED,
        )

    private fun subtitleSettings(): PlayerSettings? {
        val tracks = player?.getSubtitleTracks() ?: return null
        return if (tracks.isNotEmpty() && !(tracks.size == 1 && tracks.contains(NPOSubtitleTrack.OFF))) PlayerSettings.SUBTITLES else null
    }

    private fun audioQualitiesSettings(): PlayerSettings? =
        if ((player?.getAudioQualities()?.size ?: 0) > 1) PlayerSettings.AUDIO_QUALITIES else null

    private fun audioTrackSettings(): PlayerSettings? = if ((player?.getAudioTracks()?.size ?: 0) > 0) PlayerSettings.AUDIO_TRACKS else null

    private fun videoQualitiesSettings(): PlayerSettings? =
        if ((player?.getVideoQualities()?.size ?: 0) > 1) PlayerSettings.VIDEO_QUALITIES else null

    private fun showSubtitleDialog() {
        player?.getSubtitleTracks()?.let { npoSubtitleTracks ->
            AlertDialog
                .Builder(this)
                .setSingleChoiceItems(
                    npoSubtitleTracks.map { it.label ?: it.id }.toTypedArray(),
                    npoSubtitleTracks.indexOf(player?.getSelectedSubtitleTrack()),
                ) { dialog, which ->
                    player?.selectSubtitleTrack(npoSubtitleTracks[which])
                    dialog.dismiss()
                }.create()
                .show()
        }
    }

    private fun showAudioTracksDialog() {
        player?.getAudioTracks()?.let { audioTracks ->
            AlertDialog
                .Builder(this)
                .setSingleChoiceItems(
                    audioTracks.map { it.label ?: it.id }.toTypedArray(),
                    audioTracks.indexOf(player?.getSelectedAudioTrack()),
                ) { dialog, which ->
                    player?.selectAudioTrack(audioTracks[which])
                    dialog.dismiss()
                }.create()
                .show()
        }
    }

    private fun showAudioQualityDialog() {
        player?.getAudioQualities()?.let { npoAudioQualities ->
            AlertDialog
                .Builder(this)
                .setSingleChoiceItems(
                    npoAudioQualities.map { it.label ?: it.id }.toTypedArray(),
                    npoAudioQualities.indexOf(player?.getSelectedAudioQuality()),
                ) { dialog, which ->
                    player?.selectAudioQuality(npoAudioQualities[which])
                    dialog.dismiss()
                }.create()
                .show()
        }
    }

    private fun showVideoQualityDialog() {
        player?.getVideoQualities()?.let { videoQualities ->
            AlertDialog
                .Builder(this)
                .setSingleChoiceItems(
                    videoQualities.map { it.label ?: it.id }.toTypedArray(),
                    videoQualities.indexOf(player?.getSelectedVideoQuality()),
                ) { dialog, which ->
                    player?.selectVideoQuality(videoQualities[which])
                    dialog.dismiss()
                }.create()
                .show()
        }
    }

    private fun showSpeedSelectionDialog() {
        PlaybackSpeeds.entries.let { speeds ->
            AlertDialog
                .Builder(this)
                .setSingleChoiceItems(
                    speeds.map { "${it.name} (${it.value}x)" }.toTypedArray(),
                    speeds.indexOf(
                        speeds.firstOrNull { it.value == player?.playbackSpeed }
                            ?: PlaybackSpeeds.NORMAL,
                    ),
                ) { dialog, which ->
                    player?.playbackSpeed = speeds[which].value
                    dialog.dismiss()
                }.create()
                .show()
        }
    }

    private fun changePageTracker(title: String) {
        val pageTracker =
            (application as PlayerApplication)
                .npoTag
                ?.pageTrackerBuilder()
                ?.withPageName(title)
                ?.build()

        player?.changePageTracker(
            when (pageTracker) {
                is PageTracker -> PlayerTagProvider.getPageTracker(pageTracker)
                else -> PlayerTagProvider.getPageTracker(PageConfiguration(title))
            },
        )
    }

    private fun loadStreamURL(npoSourceConfig: NPOSourceConfig) {
        player?.let {
            playerViewModel.loadStream(
                npoPlayer = it,
                npoSourceConfig = npoSourceConfig,
            )
        }
        binding.apply {
            loadingIndicator.isVisible = false
            retryBtn.isVisible = false
        }
    }

    private fun handleError(
        error: NPOPlayerError,
        retry: () -> Unit,
    ) {
        Log.d(
            PlayerActivity::class.simpleName,
            "Loading stream in player failed with result:${error.getMessage(this@PlayerActivity)}",
        )
        player?.setPlayerError(error)
        binding.loadingIndicator.isVisible = false

        with(binding.retryBtn) {
            isVisible = error.allowRetry
            setOnClickListener {
                retry.invoke()
            }
        }

        Toast.makeText(baseContext, error.getMessage(this@PlayerActivity), Toast.LENGTH_LONG).show()
    }

    private fun handleLoading() {
        binding.apply {
            loadingIndicator.isVisible = true
            retryBtn.isVisible = false
        }
    }

    private fun setObservers() {
        // Initialize the link lists even though we don't do anything with the changes yet.
        linkViewModel.urlLinkList.observeNonNull(this) {}
        linkViewModel.streamLinkList.observeNonNull(this) {}
    }

    private fun handleTokenState(retrievalState: StreamRetrievalState) {
        when (retrievalState) {
            is StreamRetrievalState.Success -> loadStreamURL(retrievalState.npoSourceConfig)

            is StreamRetrievalState.Error ->
                handleError(retrievalState.error) {
                    playerViewModel.retrieveSource(sourceWrapper, ::handleTokenState)
                }

            StreamRetrievalState.Loading -> {
                handleLoading()
            }

            StreamRetrievalState.NotStarted -> {
                // NO-OP
            }
        }
    }

    private fun doSystemUiVisibility(fullScreen: Boolean) {
        runOnUiThread {
            with(WindowCompat.getInsetsController(window, window.decorView)) {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                val type =
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                if (fullScreen) {
                    hide(type)
                } else {
                    show(type)
                }
            }
        }
    }

    private val fullScreenHandler =
        object : NPOFullScreenHandler {
            private var fullscreen = false
            override val isFullscreen: Boolean get() = fullscreen

            override fun onDestroy() {
                // DO nothing
            }

            override fun onFullscreenExitRequested() {
                fullscreen = false
                runOnUiThread {
                    binding.apply {
                        btnSwitchStreams.isVisible = true
                        btnPlayPause.isVisible = true
                    }
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    doSystemUiVisibility(fullscreen)
                }
            }

            override fun onFullscreenRequested() {
                fullscreen = true
                runOnUiThread {
                    binding.apply {
                        btnSwitchStreams.isVisible = false
                        btnPlayPause.isVisible = false
                    }
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    doSystemUiVisibility(fullscreen)
                }
            }

            override fun onPause() {
                // Do nothing
            }

            override fun onResume() {
                doSystemUiVisibility(isFullscreen)
            }
        }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "NPO-PlayerSampleApp"
        private const val NOTIFICATION_ID = 1
        private const val MEDIA_SESSION_TAG = "npo-player-mediaSession"

        @Suppress("DEPRECATION")
        fun Intent.getSourceWrapper(): SourceWrapper? {
            val offlineSource: NPOOfflineSourceConfig?
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                offlineSource =
                    this@getSourceWrapper.getParcelableExtra(
                        PLAYER_OFFLINE_SOURCE,
                        NPOOfflineSourceConfig::class.java,
                    )
                getSerializableExtra(PLAYER_SOURCE, SourceWrapper::class.java)
            } else {
                offlineSource = this@getSourceWrapper.getParcelableExtra(PLAYER_OFFLINE_SOURCE)
                getSerializableExtra(PLAYER_SOURCE) as? SourceWrapper
            }?.let { sourceWrapper ->
                offlineSource?.let { sourceWrapper.copy(npoSourceConfig = offlineSource) }
                    ?: sourceWrapper
            }
        }

        fun getStartIntent(
            packageContext: Context,
            sourceWrapper: SourceWrapper,
        ): Intent =
            Intent(packageContext, PlayerActivity::class.java).apply {
                if (sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig) {
                    putExtra(PLAYER_OFFLINE_SOURCE, sourceWrapper.npoSourceConfig as Parcelable)
                    putExtra(PLAYER_SOURCE, sourceWrapper.copy(npoSourceConfig = null))
                } else {
                    putExtra(PLAYER_SOURCE, sourceWrapper)
                }
            }
    }
}
