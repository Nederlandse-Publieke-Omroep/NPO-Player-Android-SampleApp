package nl.npo.player.sampleApp.presentation.player

import android.app.Application
import android.content.Context
import android.media.session.PlaybackState
import com.bitmovin.player.api.PlayerConfig
import com.bitmovin.player.api.source.SourceConfig
import com.bitmovin.player.core.internal.vr.createDefaultGlRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.presentation.viewmodel.UseExoplayer
import nl.npo.tag.sdk.tracker.PageTracker
import kotlin.time.Duration.Companion.milliseconds
//
//class HostPlaybackPlayerInteractor(
//    private val appContext: Context,
//    private val application: Application
//) : PlaybackPlayerInteractor {
//
//    private val _snapshot = MutableStateFlow(PlaybackSnapshot.initial())
//    override val snapshot: StateFlow<PlaybackSnapshot> = _snapshot
//    private val config: NPOPlayerConfig = NPOPlayerConfig()
//    // private val pageTracker = PageTracker(application)
//    // val pageTracker1 = pageTracker.pageTracker
//
//    private var player: NPOPlayer? = null
//
//    private fun update(block: (PlaybackSnapshot) -> PlaybackSnapshot) {
//        _snapshot.update(block)
//    }
//
//     fun getOrCreatePlayer(
//         config: NPOPlayerConfig,
//         useExoplayer: UseExoplayer,
//         pageTracker: PageTracker,
//    ): NPOPlayer {
//         player?.let { return it }
//        return synchronized(this) {
//            player ?: NPOPlayerLibrary.getPlayer(
//                context = appContext.applicationContext,
//                npoPlayerConfig = config,
//                pageTracker = pageTracker.let
//                { PlayerTagProvider.getPageTracker(it) } /* no cast */,
//                useExoplayer = useExoplayer,
//            ).also { created -> player = created }
//        }
//    }
//
//    fun loadSource(
//        sourceWrapper: SourceWrapper,
//    ) {
//        update { s ->
//            s.copy(
//                nowPlaying = NowPlaying(
//                    title = sourceWrapper.title ?: "Playing",
//                    artist = sourceWrapper.testingDescription,
//                    artworkUri = sourceWrapper.imageUrl,
//                    mediaId = sourceWrapper.uniqueId
//                ),
//                status = nl.npo.player.library.domain.state.PlaybackState.Loading,
//                error = null
//            )
//
//        }
//        sourceWrapper.npoSourceConfig?.let { player?.load(it) }
//    }
//
//
//    override fun play() {
//        player?.play()
//        update { it.copy(playWhenReady = player?.isPlaying == true) }
//    }
//
//    override fun pause() {
//        player?.pause()
//        update { it.copy(status = nl.npo.player.library.domain.state.PlaybackState.Paused()) }
//    }
//
//    override fun stop() = Unit
//
//    override fun seekTo(positionMs: Long) {
//        player?.seek(positionMs.milliseconds)
//        update { it.copy(positionMs= positionMs) }
//    }
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//






//    val player = NPOPlayerLibrary.getPlayer(
//        context = appContext,
//        npoPlayerConfig = config,
//        pageTracker = pageTracker1?.let {
//            PlayerTagProvider.getPageTracker(it) } as PlayerPageTracker,
//        useExoplayer = useExoplayer,
//    )

//    override fun play() { player.play() }
//    override fun pause() { player.pause() }
//    override fun stop() = Unit
//
//    override fun seekTo(positionMs: Long) { player.seek(positionMs.milliseconds) }

    // Update _snapshot op basis van Exo callbacks (listener)…

