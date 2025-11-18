package nl.npo.player.sampleApp.presentation.compose


sealed class DownloadEvent {
    data class Intent(val itemId: String) : DownloadEvent()
}
