package nl.npo.player.sample_app.presentation.player

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.KeyCharacterMap
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.common.model.PlayerSource
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.media.NPOSubtitleTrack
import nl.npo.player.library.domain.player.model.NPOFullScreenHandler
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.notifications.NPONotificationManager
import nl.npo.player.library.setupPlayerNotificationManager
import nl.npo.player.sample_app.R
import nl.npo.player.sample_app.SampleApplication
import nl.npo.player.sample_app.databinding.ActivityPlayerBinding
import nl.npo.player.sample_app.extension.observeNonNull
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.model.StreamRetrievalState
import nl.npo.player.sample_app.presentation.BaseActivity
import nl.npo.player.sample_app.presentation.player.enums.PlaybackSpeeds
import nl.npo.player.sample_app.presentation.player.enums.PlayerSettings
import nl.npo.player.sample_app.presentation.player.viewmodel.PlayerViewModel
import nl.npo.player.sample_app.presentation.viewmodel.LinksViewModel
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

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() {
            super.onPlay()
            player?.play()
        }

        override fun onPause() {
            super.onPause()
            player?.pause()
        }

        override fun onStop() {
            super.onStop()
            player?.pause()
        }
    }

    private val mediaSession by lazy {
        MediaSession(this, MEDIA_SESSION_TAG).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }

    private val onFinishedPlaybackListener: PlayerListener = object : PlayerListener {
        override fun onPlaybackFinished(currentPosition: Double) {
            binding.btnSwitchStreams.callOnClick()
        }
    }

    private val onPlayPauseListener: PlayerListener = object : PlayerListener {
        override fun onPlaybackFinished(currentPosition: Double) {
            binding.btnPlayPause.isVisible = false
        }

        override fun onPaused(currentPosition: Double) {
            binding.btnPlayPause.apply {
                isVisible = !fullScreenHandler.isFullscreen
                setImageResource(android.R.drawable.ic_media_play)
            }
        }

        override fun onPlaying(currentPosition: Double) {
            binding.btnPlayPause.apply {
                isVisible = !fullScreenHandler.isFullscreen
                setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        override fun onSourceLoaded(currentPosition: Double, playerSource: PlayerSource) {
            binding.btnPlayPause.apply {
                isVisible = !fullScreenHandler.isFullscreen
                setImageResource(android.R.drawable.ic_media_play)
            }
        }

        override fun onSourceError(currentPosition: Double) {
            binding.btnPlayPause.isVisible = false
        }

        override fun onSourceLoad(currentPosition: Double) {
            binding.btnPlayPause.isVisible = false
        }

        override fun onCanStartPlayingBecauseSwitchedToWiFi() {
            player?.play()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupViews()
        setObservers()
        NPOCasting.updateCastingContext(this)

        sourceWrapper = intent.getSourceWrapper() ?: run {
            finish()
            return
        }

        playerViewModel.getPlayerConfig { playerConfig ->
            loadSource(sourceWrapper, playerConfig)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && !fullScreenHandler.isFullscreen) {
            binding.npoVideoPlayer.setFullScreen(true)
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun loadSource(sourceWrapper: SourceWrapper, config: NPOPlayerConfig) {
        val title = sourceWrapper.title
        if (player == null) {
            logPageAnalytics(title)

            try {
                player = NPOPlayerLibrary.getPlayer(
                    context = binding.root.context,
                    npoPlayerConfig = config,
                    pageTracker = pageTracker?.let { PlayerTagProvider.getPageTracker(it) }
                        ?: PlayerTagProvider.getPageTracker(PageConfiguration(title))
                ).apply {
                    remoteControlMediaInfoCallback = PlayerViewModel.remoteCallback
                    eventEmitter.addListener(onFinishedPlaybackListener)
                    eventEmitter.addListener(onPlayPauseListener)
                    npoNotificationManager = setupPlayerNotificationManager(
                        NOTIFICATION_CHANNEL_ID,
                        R.string.app_name,
                        R.drawable.ic_launcher_foreground,
                        NOTIFICATION_ID,
                        mediaSession.sessionToken
                    )
                    binding.npoVideoPlayer.attachPlayer(this)
                    attachToLifecycle(lifecycle)
                }
            } catch (e: NPOPlayerException.PlayerInitializationException) {
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Player Analytics not initialized correctly. ${e.message}")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Ok"
                    ) { _, _ -> finish() }
                    .show()
                return
            }
        } else {
            // Note: This is only to simulate switching pages. A normal app shouldn't need to do such a switch at stream load, only when switching to a new page with the same player..
            changePageTracker(title)
        }

        when {
            sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig -> loadStreamURL(sourceWrapper.npoSourceConfig)
            sourceWrapper.getStreamLink -> playerViewModel.retrieveSource(sourceWrapper)
            sourceWrapper.npoSourceConfig != null -> loadStreamURL(sourceWrapper.npoSourceConfig)
            else -> finish()
        }
    }

    override fun onDestroy() {
        player?.apply {
            eventEmitter.removeListener(onFinishedPlaybackListener)
            eventEmitter.removeListener(onPlayPauseListener)
        }

        npoNotificationManager?.setPlayer(null)
        mediaSession.release()
        super.onDestroy()
    }

    private fun ActivityPlayerBinding.setupViews() {
        npoVideoPlayer.apply {
            attachToLifecycle(lifecycle)
            setFullScreenHandler(fullScreenHandler)
            playerViewModel.hasCustomSettings {
                setSettingsButtonOnClickListener {
                    runOnUiThread {
                        showSettings()
                    }
                    true
                }
            }

            setPlayPauseButtonOnClickListener { isPlayPressed ->
                runOnUiThread {
                    Toast.makeText(
                        context,
                        "${if (isPlayPressed) "Play" else "Pause"} pressed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        btnSwitchStreams.setOnClickListener {
            if (player?.isAdPlaying != true) {
                linkViewModel.urlLinkList.value?.union(
                    linkViewModel.streamLinkList.value ?: emptyList()
                )?.random()?.let { newSource ->
                    playerViewModel.getPlayerConfig { config ->
                        loadSource(newSource, config)
                    }
                }
            }
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

    private fun showSettings() {
        getSettings().let { settings ->
            AlertDialog.Builder(this@PlayerActivity).setItems(
                settings.map { it.name }.toTypedArray()
            ) { dialog, which ->
                when (settings[which]) {
                    PlayerSettings.SUBTITLES -> showSubtitleDialog()
                    PlayerSettings.AUDIO_QUALITIES -> showAudioQualityDialog()
                    PlayerSettings.AUDIO_TRACKS -> showAudioTracksDialog()
                    PlayerSettings.VIDEO_QUALITIES -> showVideoQualityDialog()
                    PlayerSettings.SPEED -> showSpeedSelectionDialog()
                }
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun getSettings(): List<PlayerSettings> {
        return listOfNotNull(
            subtitleSettings(),
            audioQualitiesSettings(),
            audioTrackSettings(),
            videoQualitiesSettings(),
            PlayerSettings.SPEED
        )
    }

    private fun subtitleSettings(): PlayerSettings? {
        val tracks = player?.getSubtitleTracks() ?: return null
        return if (tracks.isNotEmpty() && !(tracks.size == 1 && tracks.contains(NPOSubtitleTrack.OFF))) PlayerSettings.SUBTITLES else null
    }

    private fun audioQualitiesSettings(): PlayerSettings? =
        if ((player?.getAudioQualities()?.size ?: 0) > 0) PlayerSettings.AUDIO_QUALITIES else null

    private fun audioTrackSettings(): PlayerSettings? =
        if ((player?.getAudioTracks()?.size ?: 0) > 0) PlayerSettings.AUDIO_TRACKS else null

    private fun videoQualitiesSettings(): PlayerSettings? =
        if ((player?.getVideoQualities()?.size ?: 0) > 0) PlayerSettings.VIDEO_QUALITIES else null

    private fun showSubtitleDialog() {
        player?.getSubtitleTracks()?.let { npoSubtitleTracks ->
            AlertDialog.Builder(this).setSingleChoiceItems(
                npoSubtitleTracks.map { it.label ?: it.id }.toTypedArray(),
                npoSubtitleTracks.indexOf(player?.getSelectedSubtitleTrack())
            ) { dialog, which ->
                player?.selectSubtitleTrack(npoSubtitleTracks[which])
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun showAudioTracksDialog() {
        player?.getAudioTracks()?.let { audioTracks ->
            AlertDialog.Builder(this).setSingleChoiceItems(
                audioTracks.map { it.label ?: it.id }.toTypedArray(),
                audioTracks.indexOf(player?.getSelectedAudioTrack())
            ) { dialog, which ->
                player?.selectAudioTrack(audioTracks[which])
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun showAudioQualityDialog() {
        player?.getAudioQualities()?.let { npoAudioQualities ->
            AlertDialog.Builder(this).setSingleChoiceItems(
                npoAudioQualities.map { it.label ?: it.id }.toTypedArray(),
                npoAudioQualities.indexOf(player?.getSelectedAudioQuality())
            ) { dialog, which ->
                player?.selectAudioQuality(npoAudioQualities[which])
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun showVideoQualityDialog() {
        player?.getVideoQualities()?.let { videoQualities ->
            AlertDialog.Builder(this).setSingleChoiceItems(
                videoQualities.map { it.label ?: it.id }.toTypedArray(),
                videoQualities.indexOf(player?.getSelectedVideoQuality())
            ) { dialog, which ->
                player?.selectVideoQuality(videoQualities[which])
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun showSpeedSelectionDialog() {
        PlaybackSpeeds.values().let { speeds ->
            AlertDialog.Builder(this).setSingleChoiceItems(
                speeds.map { "${it.name} (${it.value}x)" }.toTypedArray(),
                speeds.indexOf(
                    speeds.firstOrNull { it.value == player?.playbackSpeed }
                        ?: PlaybackSpeeds.NORMAL
                )
            ) { dialog, which ->
                player?.playbackSpeed = speeds[which].value
                dialog.dismiss()
            }.create().show()
        }
    }

    private fun changePageTracker(title: String) {
        val pageTracker =
            (application as SampleApplication).npoTag?.pageTrackerBuilder()?.withPageName(title)
                ?.build()

        player?.changePageTracker(
            when (pageTracker) {
                is PageTracker -> PlayerTagProvider.getPageTracker(pageTracker)
                else -> PlayerTagProvider.getPageTracker(PageConfiguration(title))
            }
        )
    }

    private fun loadStreamURL(npoSourceConfig: NPOSourceConfig) {
        player?.let {
            playerViewModel.loadStream(
                npoPlayer = it,
                npoSourceConfig = npoSourceConfig
            )
        }
        binding.apply {
            loadingIndicator.isVisible = false
            retryBtn.isVisible = false
        }
    }

    private fun handleError(throwable: Throwable?, retry: () -> Unit) {
        Log.d(
            PlayerActivity::class.simpleName,
            "Loading stream in player failed with result:$throwable"
        )
        throwable?.printStackTrace()
        when (throwable) {
            is NPOPlayerException.StreamLinkException -> {
                binding.apply {
                    loadingIndicator.isVisible = false
                    retryBtn.isVisible =
                        throwable is NPOPlayerException.StreamLinkException.WillComeAvailableSoonException
                    Toast.makeText(baseContext, throwable.message, Toast.LENGTH_LONG).show()
                }
            }

            else -> {
                binding.apply {
                    loadingIndicator.isVisible = false
                    retryBtn.isVisible = true
                    retryBtn.setOnClickListener {
                        retry.invoke()
                    }
                }
            }
        }
    }

    private fun handleLoading() {
        binding.apply {
            loadingIndicator.isVisible = true
            retryBtn.isVisible = false
        }
    }

    private fun setObservers() {
        playerViewModel.retrievalState.observeNonNull(this, ::handleTokenState)
    }

    private fun handleTokenState(retrievalState: StreamRetrievalState) {
        when (retrievalState) {
            is StreamRetrievalState.Success -> with(retrievalState) {
                loadStreamURL(npoSourceConfig)
            }

            is StreamRetrievalState.Error -> with(retrievalState) {
                handleError(throwable) {
                    playerViewModel.retrieveSource(sourceWrapper)
                }
            }

            StreamRetrievalState.Loading -> {
                handleLoading()
            }

            StreamRetrievalState.NotStarted -> {
                /* NO-OP */
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun doSystemUiVisibility(fullScreen: Boolean) {
        runOnUiThread {
            val uiParams = getSystemUiVisibilityFlags(fullScreen, true)
            window.decorView.systemUiVisibility = uiParams
        }
    }
    private fun getSystemUiVisibilityFlags(fullScreen: Boolean, useFullscreenLayoutFlags: Boolean): Int {
        var uiParams: Int
        if (!fullScreen) {
            uiParams = View.SYSTEM_UI_FLAG_VISIBLE
        } else if (Build.VERSION.SDK_INT >= 19) {
            uiParams = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            uiParams = View.SYSTEM_UI_FLAG_FULLSCREEN
            if (useFullscreenLayoutFlags) {
                uiParams = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            val key1 = KeyCharacterMap.deviceHasKey(4)
            val key2 = KeyCharacterMap.deviceHasKey(3)
            if (!key1 || !key2) {
                uiParams = uiParams or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                if (useFullscreenLayoutFlags) {
                    uiParams = uiParams or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                }
            }
        }
        return uiParams
    }

    private val fullScreenHandler = object : NPOFullScreenHandler {
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
                doSystemUiVisibility(false)
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
                doSystemUiVisibility(true)
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
                offlineSource = this@getSourceWrapper.getParcelableExtra(
                    PLAYER_OFFLINE_SOURCE,
                    NPOOfflineSourceConfig::class.java
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
            sourceWrapper: SourceWrapper
        ): Intent {
            return Intent(packageContext, PlayerActivity::class.java).apply {
                if (sourceWrapper.npoSourceConfig is NPOOfflineSourceConfig) {
                    putExtra(PLAYER_OFFLINE_SOURCE, sourceWrapper.npoSourceConfig as Parcelable)
                    putExtra(PLAYER_SOURCE, sourceWrapper.copy(npoSourceConfig = null))
                } else {
                    putExtra(PLAYER_SOURCE, sourceWrapper)
                }
            }
        }
    }
}
