package nl.npo.player.sampleApp.presentation.compose

import nl.npo.player.sampleApp.shared.model.SourceWrapper


sealed class DownloadEvent {
    data class Intent(val itemId: String, val sourceWrapper: SourceWrapper) : DownloadEvent()
    data class ErrorMessage(val message:String): DownloadEvent()
}
