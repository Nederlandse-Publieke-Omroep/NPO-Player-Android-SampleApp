package nl.npo.player.sampleApp.presentation.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@OptIn(ExperimentalFoundationApi::class)
@Composable
 fun ContentList(
    live: List<SourceWrapper>,
    vod: List<SourceWrapper>,
    onItemClick: (SourceWrapper) -> Unit
) {
    val orange = Color(0xFFFF7A00)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp),  // 👈 visible window
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            stickyHeader {
                Header("Video")
            }
            itemsIndexed(
                items = live,
                key = { index, item -> "live_${item.uniqueId}_$index" }   // ← prefix keys
            ) { index, item ->
                RowCard(
                    image = item.imageUrl ?: "",
                    contentInfo = item.title ?: "",
                    accent = orange,
                    onClick = { onItemClick(item) },
                )
            }
        }

        Spacer(Modifier.height(50.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp),  // 👈 visible window
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            stickyHeader {
                Header("Audio")
            }
            itemsIndexed(
                items = vod,
                key = { index, item -> "live_${item.uniqueId}_$index" }    // ← different prefix
            ) { index, item ->
                RowCard(
                    image = item.imageUrl ?: "",
                    contentInfo = item.testingDescription,
                    accent = orange,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}




@Composable
 fun Header(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = Color(0xFFBDBDBD),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }

}
