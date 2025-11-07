package nl.npo.player.sampleApp.presentation.compose

import android.R
import android.R.attr.color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.npo.player.library.domain.common.enums.AVType


@Composable
fun SectionHeader(title: String, count: Int, type: AVType) {
//    val (icon, color) = when (type) {
//        AVType.VIDEO -> Icons.Default.PlayArrow to Color(0xFFFF6A00)
//        AVType.AUDIO -> Icons.Default.GraphicEq to Color(0xFF4DB6AC)
//        AVType.UNKNOWN  -> Icons.Default.GraphicEq to Color(0xFF4DB6AC)
//    }
    val icon = when(type) { 
        AVType.AUDIO -> Icons.Default.PlayArrow
        AVType.VIDEO -> Icons.Default.Star
        AVType.UNKNOWN -> Icons.Default.Close
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0C0C))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon.autoMirror)
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Surface(shape = CircleShape, color = Color.Black) {
         Text(text = "$count", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        }

    }
}
