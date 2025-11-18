package nl.npo.player.sampleApp.presentation.compose


sealed class DownloadEvent {
    data class ShowErrorDialog(val itemId: String, val message: String?) : DownloadEvent()
    data class OpenDownloadedItem(val itemId: String) : DownloadEvent()
}
