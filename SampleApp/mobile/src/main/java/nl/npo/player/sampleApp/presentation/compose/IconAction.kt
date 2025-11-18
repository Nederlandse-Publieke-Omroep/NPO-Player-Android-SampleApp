package nl.npo.player.sampleApp.presentation.compose

import android.R.attr.onClick
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import nl.npo.player.library.domain.offline.models.NPODownloadState


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

    Log.d("DEBUG_ACTION_ICON", "currentState=$currentState")
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
                    is DownloadState.InProgress -> error("handled above")
                    NPODownloadState.Deleting -> Icons.Default.Delete
                    is NPODownloadState.Failed -> Icons.Default.Error
                    NPODownloadState.Finished -> Icons.Default.PlayArrow
                    NPODownloadState.Initializing ->Icons.Default.Download
                    is NPODownloadState.Paused -> Icons.Default.Pause
                    else  -> Icons.Default.Close
                }
                Icon(
                    imageVector = icon,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
    }
}


//@Composable
//@Preview
//fun PreviewIcon() {
//    val test =  MutableLiveData<NPODownloadState>().apply {
//        value = NPODownloadState.InProgress(0.5f)
//    }
//    DownloadActionIcon(
//        currentState = test,
//        onClick = {}
//    )
//}
