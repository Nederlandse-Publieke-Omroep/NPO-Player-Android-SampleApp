package nl.npo.player.sampleApp.presentation.model

import nl.npo.player.sampleApp.shared.model.SourceWrapper

sealed interface DownloadEvent {
    object None : DownloadEvent

    data class Error(
        val itemId: String?,
        val message: String?,
        val wrapper: SourceWrapper? = null,
    ) : DownloadEvent

    data class Delete(
        val id: String,
        val sourceWrapper: SourceWrapper,
    ) : DownloadEvent
}
