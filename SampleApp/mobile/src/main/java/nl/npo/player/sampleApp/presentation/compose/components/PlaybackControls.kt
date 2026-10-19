package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.npo.player.library.domain.state.PlaybackState
import nl.npo.player.sampleApp.R

@Composable
fun PlaybackControls(
    showRetry: Boolean,
    playbackState: PlaybackState?,
    onRetry: () -> Unit,
    onSwitchStreams: () -> Unit,
    onPlayPause: () -> Unit,
) {
    val accent = colorResource(R.color.orange)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        ControlButton(
            icon = Icons.Default.Shuffle,
            label = stringResource(R.string.playback_random),
            color = colorResource(R.color.white),
            onClick = onSwitchStreams,
        )
        if (showRetry) {
            ControlButton(
                icon = Icons.Default.Refresh,
                label = stringResource(R.string.retry_btn_title),
                color = accent,
                onClick = onRetry,
            )
        }
        if (playbackState != null && playbackState.isWithin(PlaybackState.Loaded, PlaybackState.Paused())) {
            val isPlaying = playbackState == PlaybackState.Playing
            ControlButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                label = stringResource(if (isPlaying) R.string.playback_pause else R.string.playback_play),
                color = accent,
                onClick = onPlayPause,
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Text(text = label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlaybackControlsPlayingPreview() {
    MaterialTheme {
        PlaybackControls(
            showRetry = false,
            playbackState = PlaybackState.Playing,
            onRetry = {},
            onSwitchStreams = {},
            onPlayPause = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PlaybackControlsErrorPreview() {
    MaterialTheme {
        PlaybackControls(
            showRetry = true,
            playbackState = null,
            onRetry = {},
            onSwitchStreams = {},
            onPlayPause = {},
        )
    }
}
