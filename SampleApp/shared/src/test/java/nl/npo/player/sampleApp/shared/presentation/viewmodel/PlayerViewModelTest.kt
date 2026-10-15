package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.data.player.SharedPlayerProvider
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.TokenResponse
import nl.npo.player.sampleApp.shared.fake.FakeSettingsRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    private val context = mockk<Context>(relaxed = true, moreInterfaces = arrayOf(PlayerApplication::class))
    private val tokenProvider = mockk<TokenProvider>()
    private val settingsRepository = FakeSettingsRepository()
    private val sharedPlayerProvider = mockk<SharedPlayerProvider>()
    private val streamLinkRepository = mockk<LinkRepository>()
    private val urlLinkRepository = mockk<LinkRepository>()
    private val player = mockk<NPOPlayer>(relaxed = true)

    private val urlSource =
        SourceWrapper(title = "URL", uniqueId = "url", getStreamLink = false, streamUrl = "https://example.com/stream.mpd")
    private val streamLinkSource = SourceWrapper(title = "StreamLink", uniqueId = "prid", getStreamLink = true)

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { (context as PlayerApplication).npoTag } returns null
        mockkObject(PlayerTagProvider)
        every { PlayerTagProvider.getPageTracker(any<PageConfiguration>()) } returns mockk()
        mockkObject(NPOCasting)
        every { NPOCasting.isCastingConnected() } returns false
        coEvery { sharedPlayerProvider.getPlayer(any()) } returns player
        coEvery { tokenProvider.createToken(any(), any()) } returns StreamInfoResult.Success(TokenResponse("token"))

        viewModel =
            PlayerViewModel(
                context = context,
                tokenProvider = tokenProvider,
                settingsRepository = settingsRepository,
                sharedPlayerProvider = sharedPlayerProvider,
                streamLinkRepository = streamLinkRepository,
                urlLinkRepository = urlLinkRepository,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `play binds the shared player and loads the source`() {
        settingsRepository.autoPlayEnabled.value = true

        viewModel.play(urlSource)

        assertEquals(player, viewModel.playbackState.value.player)
        verify {
            player.eventEmitter.addListener(any())
            player.load(match { it.uniqueId == "url" && it.autoPlay }, any(), any())
        }
    }

    @Test
    fun `play binds the same shared player only once`() {
        viewModel.play(urlSource)
        viewModel.play(urlSource)

        verify(exactly = 1) { player.eventEmitter.addListener(any()) }
        verify(exactly = 2) { player.load(any(), any(), any()) }
    }

    @Test
    fun `play rebinds when the shared player was recreated`() {
        val newPlayer = mockk<NPOPlayer>(relaxed = true)
        viewModel.play(urlSource)
        coEvery { sharedPlayerProvider.getPlayer(any()) } returns newPlayer

        viewModel.play(urlSource)

        assertEquals(newPlayer, viewModel.playbackState.value.player)
        verify {
            player.eventEmitter.removeListener(any())
            player.setTokenRefreshCallback(null)
            player.setPlayNextListener(null)
            newPlayer.eventEmitter.addListener(any())
            newPlayer.load(any(), any(), any())
        }
    }

    @Test
    fun `play closes when the source has nothing to play`() {
        viewModel.play(urlSource.copy(npoSourceConfig = null))

        assertTrue(viewModel.playbackState.value.shouldClose)
    }

    @Test
    fun `stream link error is shown and can be retried`() {
        mockkObject(NPOPlayerLibrary.StreamLink)
        coEvery { NPOPlayerLibrary.StreamLink.getNPOSourceConfig(any()) } throws
            NPOPlayerException.StreamLinkException.GeneralException("failed")

        viewModel.play(streamLinkSource)

        with(viewModel.playbackState.value) {
            assertNotNull(error)
            assertFalse(isLoading)
        }
        verify { player.setError(any()) }

        viewModel.retry()

        coVerify(exactly = 2) { tokenProvider.createToken("prid", any()) }
    }

    @Test
    fun `token refresh retrieves the source again`() {
        val tokenRefresh = slot<(Duration) -> Unit>()
        every { player.setTokenRefreshCallback(capture(tokenRefresh)) } returns Unit
        mockkObject(NPOPlayerLibrary.StreamLink)
        coEvery { NPOPlayerLibrary.StreamLink.getNPOSourceConfig(any()) } returns urlSource.npoSourceConfig!!
        viewModel.play(streamLinkSource)

        tokenRefresh.captured(30.seconds)

        coVerify(exactly = 2) { tokenProvider.createToken("prid", any()) }
        verify { player.load(match { it.startOffset == 30.0 }, any(), any()) }
    }

    @Test
    fun `play random plays a source from the stream link list`() {
        coEvery { streamLinkRepository.getSourceList() } returns listOf(urlSource)
        settingsRepository.onlyStreamLinkRandomEnabled.value = true
        viewModel.play(streamLinkSource.copy(getStreamLink = false, npoSourceConfig = urlSource.npoSourceConfig))

        viewModel.playRandom()

        verify {
            player.unload()
            player.load(match { it.uniqueId == "url" }, any(), any())
        }
        coVerify(exactly = 0) { urlLinkRepository.getSourceList() }
    }

    @Test
    fun `clearing unloads the player when not casting`() {
        viewModel.play(urlSource)

        viewModel.clear()

        verify {
            player.eventEmitter.removeListener(any())
            player.unload()
        }
    }

    @Test
    fun `clearing keeps the player loaded while casting`() {
        every { NPOCasting.isCastingConnected() } returns true
        viewModel.play(urlSource)

        viewModel.clear()

        verify(exactly = 0) { player.unload() }
    }

    private fun PlayerViewModel.clear() {
        PlayerViewModel::class.java
            .getDeclaredMethod("onCleared")
            .apply { isAccessible = true }
            .invoke(this)
    }
}
