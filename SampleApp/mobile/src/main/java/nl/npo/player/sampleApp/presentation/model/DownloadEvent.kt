package nl.npo.player.sampleApp.presentation.model

import nl.npo.player.sampleApp.shared.model.SourceWrapper

sealed interface DownloadEvent {
    object None : DownloadEvent

    sealed class ItemEvent(
        open val itemId: String?,
        open val sourceWrapper: SourceWrapper?,
    ) : DownloadEvent {
        data class Error(
            override val itemId: String?,
            val message: String?,
            override val sourceWrapper: SourceWrapper? = null,
        ) : ItemEvent(itemId, sourceWrapper)

        data class Delete(
            override val itemId: String?,
            override val sourceWrapper: SourceWrapper,
        ) : ItemEvent(itemId, sourceWrapper)
    }
}
