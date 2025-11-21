package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.npo.player.library.domain.offline.models.NPODownloadState

@Composable
fun DownloadActionIcon(
    downloadState: LiveData<NPODownloadState>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Note: observing LiveData at item-level is intentional.
// Each card needs to update independently when its download state changes.
// Observing at screen-level caused missed recompositions,
// Used subscribe here to ensure accurate progress/icon updates.
    val currentState: NPODownloadState =
        downloadState
            ?.observeAsState(initial = NPODownloadState.Initializing)
            ?.value
            ?: NPODownloadState.Initializing

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (currentState is NPODownloadState.InProgress) {
            val uiProgress =
                remember(currentState.progress) {
                    val raw = currentState.progress.coerceIn(0f, 1f)
                    (raw * 0.9f).coerceIn(0f, 0.9f)
                }
            CircularProgressIndicator(
                progress = { uiProgress },
                strokeWidth = 2.dp,
                modifier = modifier.size(24.dp),
            )
        } else {
            IconButton(onClick = { onClick() }) {
                val icon =
                    when (currentState) {
                        is NPODownloadState.InProgress -> error("handled above")
                        NPODownloadState.Deleting -> Icons.Default.Delete
                        is NPODownloadState.Failed -> Icons.Default.Error
                        NPODownloadState.Finished -> Icons.Default.PlayArrow
                        NPODownloadState.Initializing -> Icons.Default.Download
                        is NPODownloadState.Paused -> Icons.Default.Pause
                    }
                Icon(
                    imageVector = icon,
                    tint = Color.White,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewIcon() {
    val test =
        MutableLiveData<NPODownloadState>().apply {
            value = NPODownloadState.InProgress(0.5f)
        }
    DownloadActionIcon(
        downloadState = test,
        onClick = {},
    )
}
