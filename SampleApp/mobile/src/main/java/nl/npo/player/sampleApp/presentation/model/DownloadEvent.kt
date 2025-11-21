package nl.npo.player.sampleApp.presentation.model

import nl.npo.player.sampleApp.shared.model.SourceWrapper

sealed interface DownloadEvent {
    data class Request(
        val itemId: String,
        val wrapper: SourceWrapper,
    ) : DownloadEvent

    data class Error(
        val itemId: String?,
        val message: String?,
    ) : DownloadEvent
}
