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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import nl.npo.player.library.domain.offline.models.NPODownloadState

@Composable
fun ProgressActionIcon(
    downloadState: StateFlow<NPODownloadState>?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val state by produceState<NPODownloadState>(
        initialValue = NPODownloadState.Initializing,
        key1 = downloadState,
    ) {
        (downloadState ?: emptyFlow()).collect { value = it }
    }

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is NPODownloadState.InProgress -> {
                CircularProgressIndicator(
                    progress = { s.progress / 100f },
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            }
            else -> {
                IconButton(onClick = onClick) {
                    val icon =
                        when (s) {
                            NPODownloadState.Deleting -> Icons.Default.Delete
                            is NPODownloadState.Failed -> Icons.Default.Error
                            NPODownloadState.Finished -> Icons.Default.PlayArrow
                            NPODownloadState.Initializing -> Icons.Default.Download
                            is NPODownloadState.Paused -> Icons.Default.Pause
                            is NPODownloadState.InProgress -> error("handled above")
                        }
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewIcon() {
    val downloadState =
        MutableStateFlow<NPODownloadState>(value = NPODownloadState.Initializing).apply {
            value = NPODownloadState.InProgress(0.5f)
        }
    ProgressActionIcon(
        downloadState = downloadState,
        onClick = {},
    )
}
