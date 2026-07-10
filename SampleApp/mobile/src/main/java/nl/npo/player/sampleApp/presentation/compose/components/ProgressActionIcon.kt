package nl.npo.player.sampleApp.presentation.compose.components

import androidx.annotation.OptIn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import nl.npo.player.library.domain.offline.models.NPODownloadState

@OptIn(UnstableApi::class)
@Composable
fun ProgressActionIcon(
    downloadState: NPODownloadState?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = downloadState) {
            is NPODownloadState.InProgress -> {
                CircularProgressIndicator(
                    progress = { s.progress / 100f },
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            }

            else -> {
                IconButton(onClick = onClick) {
                    val icon: ImageVector =
                        when (downloadState) {
                            NPODownloadState.Deleting -> Icons.Default.Delete
                            is NPODownloadState.Failed -> Icons.Default.Error
                            is NPODownloadState.Finished -> Icons.Default.PlayArrow
                            NPODownloadState.Initializing -> Icons.Default.Download
                            is NPODownloadState.Paused -> Icons.Default.Pause
                            is NPODownloadState.InProgress -> error("handled above")
                            null -> Icons.Default.Download
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
            value = NPODownloadState.InProgress(0.5f, 500L)
        }
    ProgressActionIcon(
        downloadState = downloadState.collectAsState().value,
        onClick = {},
    )
}
