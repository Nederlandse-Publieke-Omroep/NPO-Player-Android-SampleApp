package nl.npo.player.sampleApp.shared.data.offline.service

import kotlinx.coroutines.runBlocking
import nl.npo.player.library.presentation.offline.NPODownloadService
import nl.npo.player.sampleApp.shared.app.SampleApplication

class TestDownloadService : NPODownloadService() {
    override fun initializePlayerLibraryNow() {
        // NOTE: library initialization needs to happen directly so we need to block the thread until it is finished
        runBlocking {
            (applicationContext as? SampleApplication)?.initiatePlayerLibrary(withNPOTag = true)
        }
    }
}
