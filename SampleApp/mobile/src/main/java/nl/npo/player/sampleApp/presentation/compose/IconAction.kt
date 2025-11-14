package nl.npo.player.sampleApp.presentation.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import nl.npo.player.library.domain.offline.models.NPODownloadState

fun downloadActionIcon(state: NPODownloadState?): ImageVector? =
    when (state) {
        NPODownloadState.Initializing -> Icons.Default.Download
        is NPODownloadState.Failed  -> Icons.Default.Warning
        NPODownloadState.Finished  -> Icons.Default.PlayArrow
        is NPODownloadState.Paused   -> Icons.Default.Pause
        is NPODownloadState.InProgress -> Icons.Default.Alarm   // still spinner
        is NPODownloadState.Deleting -> null // still spinner
        else -> Icons.Default.Home
    }
