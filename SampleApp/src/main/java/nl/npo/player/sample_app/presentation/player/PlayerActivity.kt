package nl.npo.player.sample_app.presentation.player

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bitmovin.player.ui.getSystemUiVisibilityFlags
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.offline.model.NPOOfflineSourceConfig
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.common.model.PlayerListener
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.media.NPOAudioTrack
import nl.npo.player.library.domain.player.model.NPOFullScreenHandler
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.model.NPOPlayerBitmovin
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sample_app.SampleApplication
import nl.npo.player.sample_app.databinding.ActivityPlayerBinding
import nl.npo.player.sample_app.extension.observeNonNull
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.model.StreamRetrievalState
import nl.npo.player.sample_app.presentation.BaseActivity
import nl.npo.player.sample_app.presentation.player.viewmodel.PlayerViewModel
import nl.npo.player.sample_app.presentation.viewmodel.LinksViewModel
import nl.npo.tag.sdk.tracker.PageTracker

const val PLAYER_SOURCE = "PLAYER_SOURCE"
const val PLAYER_OFFLINE_SOURCE = "PLAYER_OFFLINE_SOURCE"

@AndroidEntryPoint
class PlayerActivity : BaseActivity() {
    private lateinit var player: NPOPlayer
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var sourceWrapper: SourceWrapper
    private val playerViewModel by viewModels<PlayerViewModel>()
    private val linkViewModel by viewModels<LinksViewModel>()

    private val onFinishedPlaybackListener: PlayerListener = object : PlayerListener {
        override fun onPlaybackFinished(currentPosition: Double) {
            binding.btnSwitchStreams.callOnClick()
        }
    }

    private val onAudioTrackListener: PlayerListener = object : PlayerListener {
        override fun onAudioTracksChanged(
            oldAudioTracks: List<NPOAudioTrack>,
            newAudioTracks: List<NPOAudioTrack>
        ) {
            if (!fullScreenHandler.isFullscreen) setAudioStreamVisibility(newAudioTracks)
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

        loadSource(sourceWrapper)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && !fullScreenHandler.isFullscreen) {
            binding.npoVideoPlayer.setFullScreen(true)
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun loadSource(sourceWrapper: SourceWrapper) {
        val title = sourceWrapper.title
        if (!::player.isInitialized) {
            logPageAnalytics(title)
            val autoPlay = sourceWrapper.autoPlay
            player = NPOPlayerBitmovin(
                context = binding.root.context,
                npoPlayerConfig = NPOPlayerConfig(
                    autoPlayEnabled = autoPlay
                ),
                pageTracker = pageTracker?.let { PlayerTagProvider.getPageTracker(it) }
                    ?: PlayerTagProvider.getPageTracker(PageConfiguration(title))
            ).also {
                it.attachToLifecycle(lifecycle)
                it.remoteControlMediaInfoCallback = PlayerViewModel.remoteCallback
                it.eventEmitter.addListener(onFinishedPlaybackListener)
                it.eventEmitter.addListener(onAudioTrackListener)
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
        player.eventEmitter.removeListener(onFinishedPlaybackListener)
        super.onDestroy()
    }

    private fun ActivityPlayerBinding.setupViews() {
        npoVideoPlayer.apply {
            attachToLifecycle(lifecycle)
            setFullScreenHandler(fullScreenHandler)
        }
        btnSwitchStreams.setOnClickListener {
            if (!player.isAdPlaying) {
                val newSource = linkViewModel.urlLinkList.value?.union(
                    linkViewModel.streamLinkList.value ?: emptyList()
                )?.random()
                newSource?.let {
                    loadSource(it)
                }
            }
        }
        btnAudioStreams.setOnClickListener {
            player.getAudioTracks()?.let { audioTracks ->
                AlertDialog.Builder(this@PlayerActivity).setSingleChoiceItems(
                    audioTracks.map { it.language ?: it.label ?: it.id }.toTypedArray(),
                    audioTracks.indexOf(player.getSelectedAudioTrack())
                ) { dialog, which ->
                    player.selectAudioTrack(audioTracks[which])
                    dialog.dismiss()
                }.create().show()
            }
        }
    }

    private fun changePageTracker(
        title: String
    ) {
        val pageTracker =
            (application as SampleApplication).npoTag?.pageTrackerBuilder()?.withPageName(title)
                ?.build()

        player.changePageTracker(
            when (pageTracker) {
                is PageTracker -> PlayerTagProvider.getPageTracker(pageTracker)
                else -> PlayerTagProvider.getPageTracker(PageConfiguration(title))
            }
        )
    }

    private fun loadStreamURL(npoSourceConfig: NPOSourceConfig) {
        player.loadStream(npoSourceConfig = npoSourceConfig.copy(overrideTitle = "SampleApp: ${npoSourceConfig.title}"))
        binding.apply {
            loadingIndicator.isVisible = false
            retryBtn.isVisible = false
            npoVideoPlayer.attachPlayer(player)
        }
    }

    private fun handleError(throwable: Throwable?, retry: () -> Unit) {
        Log.d(
            PlayerActivity::javaClass.name, "Loading stream in player failed with result:$throwable"
        )
        throwable?.printStackTrace()
        binding.apply {
            loadingIndicator.isVisible = false
            retryBtn.isVisible = true
            retryBtn.setOnClickListener {
                retry.invoke()
            }
        }
    }

    private fun handleLoading() {
        binding.apply {
            loadingIndicator.isVisible = true
            retryBtn.isVisible = false
        }
    }

    private fun setAudioStreamVisibility(tracks: List<NPOAudioTrack>?) {
        binding.btnAudioStreams.isVisible = (tracks != null && tracks.size > 1)
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

    private fun doSystemUiVisibility(fullScreen: Boolean) {
        runOnUiThread {
            val uiParams = getSystemUiVisibilityFlags(fullScreen, true)
            window.decorView.systemUiVisibility = uiParams
        }
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
                binding.btnSwitchStreams.isVisible = true
                setAudioStreamVisibility(player.getAudioTracks())
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                doSystemUiVisibility(false)
            }
        }

        override fun onFullscreenRequested() {
            fullscreen = true
            runOnUiThread {
                binding.btnSwitchStreams.isVisible = false
                binding.btnAudioStreams.isVisible = false
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
                @Suppress("DEPRECATION") getSerializableExtra(PLAYER_SOURCE) as? SourceWrapper
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
