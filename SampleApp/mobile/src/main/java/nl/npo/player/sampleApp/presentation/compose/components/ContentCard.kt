
package nl.npo.player.sampleApp.presentation.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    contentTitle: String,
    contentDescription: String? = null,
    accent: Color,
    image: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { onLongClick() },
                ),
    ) {
        Row(
          modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Thumbnail(
                imageUrl = image,
                modifier =
                  modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier.width(12.dp))

            Column(modifier.weight(1f)) {
                Text(
                    text = contentTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = contentDescription ?: "",
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (trailingContent != null) {
                trailingContent()
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun CardPreview() {
    ContentCard(
        contentTitle = "Audio Stream",
        contentDescription = "TestAudio",
        accent = Color.Cyan,
        trailingContent = {
        },
        onClick = {},
    )
}
