package nl.npo.player.sampleApp.presentation.compose.views

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.presentation.compose.RowCard
import nl.npo.player.sampleApp.presentation.compose.SectionHeader
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun PlayerScreen(
    viewModel: LinksViewModel = hiltViewModel(),
) {
    Scaffold(
        containerColor = Color.Transparent) {

        val orange = Color(0xFFFF7A00)
        val audio = viewModel.urlLinkList.value
        val video = viewModel.streamLinkList.value
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF121212), // near black / charcoal
                            Color(0xFF2C1A00), // deep brown-orange glow base
                            Color(0xFFFF6B00)  // accent orange glow at the bottom
                        )
                    )
                )
        ) {

            Box(modifier = Modifier.fillMaxSize()) {
//                if (audio.isNullOrEmpty() && video.isNullOrEmpty())
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .size(40.dp),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                else
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {

                        stickyHeader {
                            SectionHeader("Audio", AVType.AUDIO)
                        }

                        if (audio != null) {
                            val id = audio.map { it.uniqueId }
                            itemsIndexed(
                                items = audio,
                                key = { index, _ -> "live_${id}_$index" }   // ← prefix keys
                            ) { _, item ->
                                RowCard(
                                    image = item.imageUrl ?: "",
                                    contentTitle = item.title ?: "",
                                    contentDescription = item.testingDescription,
                                    accent = orange,
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                PlayerActivity.getStartIntent
                                                    (context, item)
                                            )
                                        )
                                    },
                                )
                            }
                        }

                        stickyHeader {
                            SectionHeader("Video", type = AVType.VIDEO)
                        }

                        if (video != null) {
                            val id = video.map { it.uniqueId }
                            itemsIndexed(
                                items = video,
                                key = { index, _ -> "vod_${id}_$index" }
                            ) { _, item ->
                                RowCard(
                                    image = item.imageUrl ?: "",
                                    contentTitle = item.title ?: "",
                                    contentDescription = item.testingDescription,
                                    accent = orange,
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                PlayerActivity.getStartIntent
                                                    (context, item)
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }
            }
        }
    }
}


