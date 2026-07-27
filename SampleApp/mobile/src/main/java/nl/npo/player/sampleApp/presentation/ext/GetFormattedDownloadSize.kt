package nl.npo.player.sampleApp.presentation.ext

import android.content.Context
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.R
import kotlin.math.roundToInt

fun NPODownloadState?.getFormattedDownloadSize(context: Context): String {
    val resources = context.resources
    return when (this) {
        null -> resources.getString(R.string.download_not_started)
        is NPODownloadState.Finished ->
            resources.getString(
                R.string.download_finished,
                bytesDownloaded?.toHumanReadableSize(),
            )

        is NPODownloadState.Failed ->
            resources.getString(
                R.string.download_failed,
                bytesDownloaded?.toHumanReadableSize(),
            )

        is NPODownloadState.InProgress ->
            resources.getString(
                R.string.download_in_progress,
                progress.roundToInt(),
                bytesDownloaded.toHumanReadableSize(),
            )

        is NPODownloadState.Paused ->
            resources.getString(
                R.string.download_paused,
                progress.roundToInt(),
                bytesDownloaded.toHumanReadableSize(),
            )

        NPODownloadState.Initializing -> resources.getString(R.string.download_initializing)
        NPODownloadState.Deleting -> resources.getString(R.string.download_deleting)
    }
}
