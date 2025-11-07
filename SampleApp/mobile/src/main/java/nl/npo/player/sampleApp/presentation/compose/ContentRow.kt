package nl.npo.player.sampleApp.presentation.compose

import android.R.attr.description
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@Composable
fun RowCard(
    contentInfo: String,
    accent: Color,
    image: String,
    onClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // thumbnail placeholder
            Thumbnail(
            imageUrl = image,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
        )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                text = contentInfo,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = contentInfo,
                color = accent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

















//@Composable
// fun ContentRow(
//    item: SourceWrapper,
//    accent: Color,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 10.dp)
//            .clickable(onClick = onClick),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//
//        Thumbnail(
//            imageUrl = item.imageUrl,
//            modifier = Modifier
//                .size(64.dp)
//                .clip(RoundedCornerShape(12.dp))
//        )
//
//        Spacer(Modifier.width(12.dp))
//
//        Column(Modifier.weight(1f)) {
//            Text(
//                text = item.title ?: "",
//                color = Color.White,
//                style = MaterialTheme.typography.titleMedium,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//            Text(
//                text = item.testingDescription,
//                color = accent,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.SemiBold,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//            }
//        }
//    }

