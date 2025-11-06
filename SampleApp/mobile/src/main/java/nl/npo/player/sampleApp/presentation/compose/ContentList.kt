package nl.npo.player.sampleApp.presentation.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.npo.player.sampleApp.presentation.compose.model.ContentItem
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalFoundationApi::class)
@Composable
 fun ContentList(
    live: List<SourceWrapper>,
    vod: List<SourceWrapper>,
    onItemClick: (SourceWrapper) -> Unit
) {
    val orange = Color(0xFFFF7A00)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 84.dp), // leave space for pill bar
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header (give it a unique key too if you want to be explicit)
        stickyHeader {
            SectionHeader("Live Video")
        }
        items(
            items = live,
            key = { item -> "live_${item.uniqueId}" }   // ← prefix keys
        ) { item ->
            ContentRow(item, accent = orange) { onItemClick(item) }
            Divider()
        }

        stickyHeader {
            SectionHeader("Video On Demand")
        }
        items(
            items = vod,
            key = { item -> "vod_${item.uniqueId}" }    // ← different prefix
        ) { item ->
            ContentRow(item, accent = orange) { onItemClick(item) }
            Divider()
        }
    }

}
