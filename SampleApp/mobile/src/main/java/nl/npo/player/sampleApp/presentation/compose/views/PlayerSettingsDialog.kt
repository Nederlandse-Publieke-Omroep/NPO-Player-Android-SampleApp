package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.media.NPOPlaybackSpeed
import nl.npo.player.library.domain.player.media.NPOSubtitleTrack
import nl.npo.player.sampleApp.presentation.player.enums.PlaybackSpeeds
import nl.npo.player.sampleApp.presentation.player.enums.PlayerSettings

@Composable
fun PlayerSettingsDialog(
    player: NPOPlayer,
    onDismiss: () -> Unit,
) {
    var selectedSetting by remember { mutableStateOf<PlayerSettings?>(null) }

    when (selectedSetting) {
        null -> {
            val settings = remember(player) { player.availableSettings() }
            OptionsDialog(
                options = settings.map { it.name },
                onSelect = { selectedSetting = settings[it] },
                onDismiss = onDismiss,
            )
        }

        PlayerSettings.SUBTITLES -> {
            val tracks = player.availableSubtitleTracks
            OptionsDialog(
                options = tracks.map { it.label ?: it.id },
                selectedIndex = tracks.indexOf(player.selectedSubtitleTrack),
                onSelect = { player.setSelectedSubtitleTrack(tracks[it]) },
                onDismiss = onDismiss,
            )
        }

        PlayerSettings.AUDIO_TRACKS -> {
            val tracks = player.availableAudioTracks
            OptionsDialog(
                options = tracks.map { it.label ?: it.id },
                selectedIndex = tracks.indexOf(player.selectedAudioTrack),
                onSelect = { player.setSelectedAudioTrack(tracks[it]) },
                onDismiss = onDismiss,
            )
        }

        PlayerSettings.AUDIO_QUALITIES -> {
            val qualities = player.availableAudioQualities
            OptionsDialog(
                options = qualities.map { it.label ?: it.id },
                selectedIndex = qualities.indexOf(player.selectedAudioQuality),
                onSelect = { player.setSelectedAudioQuality(qualities[it]) },
                onDismiss = onDismiss,
            )
        }

        PlayerSettings.VIDEO_QUALITIES -> {
            val qualities = player.availableVideoQualities
            OptionsDialog(
                options = qualities.map { it.label ?: it.id },
                selectedIndex = qualities.indexOf(player.selectedVideoQuality),
                onSelect = { player.setSelectedVideoQuality(qualities[it]) },
                onDismiss = onDismiss,
            )
        }

        PlayerSettings.SPEED -> {
            val speeds = PlaybackSpeeds.entries
            OptionsDialog(
                options = speeds.map { "${it.name} (${it.value}x)" },
                selectedIndex =
                    speeds.indexOf(
                        speeds.firstOrNull { it.value == player.playbackSpeed.speed } ?: PlaybackSpeeds.NORMAL,
                    ),
                onSelect = { player.setPlaybackSpeed(NPOPlaybackSpeed(speeds[it].value)) },
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun OptionsDialog(
    options: List<String>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    selectedIndex: Int? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            LazyColumn(modifier = if (selectedIndex != null) Modifier.selectableGroup() else Modifier) {
                itemsIndexed(options) { index, option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (selectedIndex != null) {
                                        Modifier.selectable(
                                            selected = index == selectedIndex,
                                            role = Role.RadioButton,
                                        ) {
                                            onSelect(index)
                                            onDismiss()
                                        }
                                    } else {
                                        Modifier.clickable { onSelect(index) }
                                    },
                                ).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectedIndex != null) {
                            RadioButton(selected = index == selectedIndex, onClick = null)
                        }
                        Text(text = option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
    )
}

private fun NPOPlayer.availableSettings(): List<PlayerSettings> =
    listOfNotNull(
        PlayerSettings.SUBTITLES.takeIf {
            val tracks = availableSubtitleTracks
            tracks.isNotEmpty() && !(tracks.size == 1 && tracks.contains(NPOSubtitleTrack.OFF))
        },
        PlayerSettings.AUDIO_QUALITIES.takeIf { availableAudioQualities.size > 1 },
        PlayerSettings.AUDIO_TRACKS.takeIf { availableAudioTracks.isNotEmpty() },
        PlayerSettings.VIDEO_QUALITIES.takeIf { availableVideoQualities.size > 1 },
        PlayerSettings.SPEED,
    )

@Preview
@Composable
private fun OptionsDialogMenuPreview() {
    MaterialTheme {
        OptionsDialog(
            options = PlayerSettings.entries.map { it.name },
            onSelect = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun OptionsDialogSelectionPreview() {
    MaterialTheme {
        OptionsDialog(
            options = PlaybackSpeeds.entries.map { "${it.name} (${it.value}x)" },
            selectedIndex = PlaybackSpeeds.entries.indexOf(PlaybackSpeeds.NORMAL),
            onSelect = {},
            onDismiss = {},
        )
    }
}
