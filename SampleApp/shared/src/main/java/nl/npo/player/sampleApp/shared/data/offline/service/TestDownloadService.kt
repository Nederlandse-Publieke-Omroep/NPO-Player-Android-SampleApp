package nl.npo.player.sampleApp.shared.data.offline.service

import kotlinx.coroutines.runBlocking
import nl.npo.player.library.presentation.offline.NPODownloadService
import nl.npo.player.sampleApp.shared.app.PlayerApplication

class TestDownloadService : NPODownloadService() {
    override fun onCreate() {
        val playerApplication = application as PlayerApplication
        if (!playerApplication.isPlayerInitiatedYet()) {
            runBlocking { playerApplication.initiatePlayerLibrary(withNPOTag = false) }
        }
        super.onCreate()
    }
}
