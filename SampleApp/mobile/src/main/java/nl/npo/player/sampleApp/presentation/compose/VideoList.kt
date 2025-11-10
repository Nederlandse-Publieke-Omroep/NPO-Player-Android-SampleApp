package nl.npo.player.sampleApp.presentation.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@OptIn(ExperimentalFoundationApi::class)
@Composable
 fun VideoList(
    live: List<SourceWrapper>,
    onItemClick: () -> Unit
) {
    val orange = Color(0xFFFF7A00)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {

            stickyHeader {
                SectionHeader("Audio", AVType.AUDIO)
            }

            itemsIndexed(
                items = live,
                key = { index, item -> "live_${item.uniqueId}_$index" }   // ← prefix keys
            ) { index, item ->
                RowCard(
                    image = item.imageUrl ?: "",
                    contentInfo = item.title ?: "",
                    accent = orange,
                    onClick = { onItemClick() },
                )
            }
        }
    }
}
