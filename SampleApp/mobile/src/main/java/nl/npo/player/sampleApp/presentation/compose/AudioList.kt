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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
fun AudioList(
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
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {

//            section("Videos", live) { video ->
//                RowCard(image = video.imageUrl?: "",
//                    contentInfo = video.title ?: "",
//                    accent = orange,
//                ) {onItemClick(video)}
//            }
//
//
//            section("Audio", vod) { audio ->
//                RowCard(image = audio.imageUrl?: "",
//                    contentInfo = audio.title ?: "",
//                    accent = orange,
//                ) {onItemClick(audio)}
//            }

            stickyHeader {
                Header("Video")
            }
            itemsIndexed(
                items = vod,
                key = { index, item -> "vod_${item.uniqueId}_$index" }   // ← prefix keys
            ) { index, item ->
                RowCard(
                    image = item.imageUrl ?: "",
                    contentInfo = item.title ?: "",
                    accent = orange,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }


//fun <T> LazyListScope.section(
//    title: String,
//    items: List<T>,
//    content: @Composable (T) -> Unit
//) {
//    // Section title (header)
//    item {
//        Text(
//            text = title,
//            color = Color.White,
//            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier
//                .padding(vertical = 8.dp, horizontal = 16.dp)
//        )
//    }
//
//    // Section items
//    items(items) { element ->
//        content(element)
//    }
//
//    // Optional: space after section
//    item { Spacer(Modifier.height(16.dp)) }
//}


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
}
