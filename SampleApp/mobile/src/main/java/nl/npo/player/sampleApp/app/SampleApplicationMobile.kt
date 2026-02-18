package nl.npo.player.sampleApp.app

import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.player.NPOPlayer

import nl.npo.player.sampleApp.shared.app.SampleApplication
import okhttp3.Dispatcher

@HiltAndroidApp
class SampleApplicationMobile : SampleApplication() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    override fun onCreate() {
        super.onCreate()

    }

}
