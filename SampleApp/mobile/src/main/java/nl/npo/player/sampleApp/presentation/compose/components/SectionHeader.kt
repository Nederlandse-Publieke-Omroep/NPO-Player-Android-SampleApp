package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.npo.player.library.domain.common.enums.AVType

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    type: AVType? = null,
) {
    val icon =
        when (type) {
            AVType.AUDIO -> Icons.Default.AudioFile
            AVType.VIDEO -> Icons.Default.VideoFile
            AVType.UNKNOWN -> Icons.Default.Unarchive
            else -> Icons.Default.OfflinePin
        }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212).copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, "header", tint = Color.White)

        Text(
            title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier.width(8.dp))
    }
}
