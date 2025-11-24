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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.presentation.compose.components.ContentCard
import nl.npo.player.sampleApp.presentation.compose.components.SectionHeader
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel

@Composable
fun PlayerScreen(viewModel: LinksViewModel = hiltViewModel()) {
    Scaffold(containerColor = Color.Transparent) {
        val orange = Color(0xFFFF7A00)
        val audio = viewModel.urlLinkList.value
        val video = viewModel.streamLinkList.value
        val loadUrl = viewModel.urlLinkList.observeAsState(emptyList())
        val loadStream = viewModel.streamLinkList.observeAsState(emptyList())
        val context = LocalContext.current
        val isLoading = loadUrl.value.isEmpty() && loadStream.value.isEmpty()
        val audioItems = audio.orEmpty()
        val videoItems = video.orEmpty()

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(0xFF121212),
                                    Color(0xFF2C1A00),
                                    Color(0xFFFF6B00),
                                ),
                        ),
                    ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        stickyHeader {
                            SectionHeader(title = "Audio", type = AVType.AUDIO)
                        }

                        if (audioItems.isNotEmpty()) {
                            val id = audioItems.map { it.uniqueId }
                            itemsIndexed(
                                items = audioItems,
                                key = { index, _ -> "audio_${id}_$index" },
                            ) { _, item ->
                                ContentCard(
                                    image = item.imageUrl ?: "",
                                    contentTitle = item.title ?: "",
                                    contentDescription = item.testingDescription,
                                    accent = orange,
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                PlayerActivity.getStartIntent(
                                                    packageContext = context,
                                                    sourceWrapper = item,
                                                ),
                                            ),
                                        )
                                    },
                                )
                            }
                        }

                        stickyHeader {
                            SectionHeader(title = "Video", type = AVType.VIDEO)
                        }

                        if (videoItems.isNotEmpty()) {
                            val id = videoItems.map { it.uniqueId }
                            itemsIndexed(
                                items = videoItems,
                                key = { index, _ -> "video_${id}_$index" },
                            ) { _, item ->
                                ContentCard(
                                    image = item.imageUrl ?: "",
                                    contentTitle = item.title ?: "",
                                    contentDescription = item.testingDescription,
                                    accent = orange,
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                PlayerActivity.getStartIntent(
                                                    packageContext = context,
                                                    sourceWrapper = item,
                                                ),
                                            ),
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
}
