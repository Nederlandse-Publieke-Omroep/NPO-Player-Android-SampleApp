package nl.npo.player.sampleApp.presentation.compose

sealed class DownloadState {
    data object Initializing : DownloadState()
    data class InProgress(val progress: Float?) : DownloadState() // 0f..1f or null
    data object Paused : DownloadState()
    data object Finished : DownloadState()
    data class Failed(val error: String?) : DownloadState()
    data object Deleting : DownloadState()
}
