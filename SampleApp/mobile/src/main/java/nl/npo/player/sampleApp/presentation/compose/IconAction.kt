package nl.npo.player.sampleApp.presentation.compose

import android.R.attr.onClick
import android.R.attr.strokeWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.jsonwebtoken.lang.Assert.state
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.presentation.offline.OfflineViewModel
import nl.npo.player.sampleApp.shared.model.SourceWrapper


@Composable
fun DownloadActionIcon(
    currentState: LiveData<NPODownloadState>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    //val currentState by currentState?.observeAsState()
    val currentState: NPODownloadState = currentState
        ?.observeAsState(initial = NPODownloadState.Initializing)
        ?.value
        ?: NPODownloadState.Initializing

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {

        if ( currentState is NPODownloadState.InProgress) {
            CircularProgressIndicator(
                progress = currentState.progress,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            IconButton(onClick = { onClick() }) {
                val icon = when (currentState) {
                    DownloadState.Initializing -> Icons.Default.Download
                    DownloadState.Paused -> Icons.Default.Pause
                    DownloadState.Finished -> Icons.Default.PlayArrow
                    is DownloadState.Failed -> Icons.Default.Error
                    DownloadState.Deleting -> Icons.Default.Delete
                    is DownloadState.InProgress -> error("handled above")
                    else -> {Icons.Default.Star}
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        }
    }
}


@Composable
@Preview
fun PreviewIcon() {
    val test =  MutableLiveData<NPODownloadState>().apply {
        value = NPODownloadState.InProgress(0.5f)
    }
    DownloadActionIcon(
        currentState = test,
        onClick = {}
    )
}
