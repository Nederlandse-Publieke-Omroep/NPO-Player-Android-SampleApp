package nl.npo.player.sampleApp.presentation.compose.views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.ContentCard
import nl.npo.player.sampleApp.presentation.compose.components.Header
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(viewModel: LinksViewModel = hiltViewModel()) {
    val orange = Color(0xFFFF7A00)
    val audioItems = viewModel.urlLinkList.observeAsState(emptyList())
    val videoItems = viewModel.streamLinkList.observeAsState(emptyList())
    val loadUrl = viewModel.urlLinkList.observeAsState(emptyList())
    val loadStream = viewModel.streamLinkList.observeAsState(emptyList())
    val context = LocalContext.current
    val isLoading = loadUrl.value.isEmpty() && loadStream.value.isEmpty()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
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
                        Box(
                            modifier = Modifier.background(Color(0xFF121212)),
                        ) {
                            Header(title = stringResource(R.string.header_audio), type = AVType.AUDIO)
                        }
                    }

                    if (audioItems.value.isNotEmpty()) {
                        itemsIndexed(
                            items = audioItems.value,
                            key = { index, item -> "audio_${item.uniqueId}_$index" },
                        ) { _, item ->
                            ContentCard(
                                image = item.imageUrl ?: "",
                                contentTitle = item.title ?: "",
                                contentDescription = item.testingDescription,
                                accent = orange,
                                onClick = { context.intentPlayerActivity(item) },
                            )
                        }
                    }
                    stickyHeader {
                        Box(modifier = Modifier.background(Color(0xFF121212))) {
                            Header(title = stringResource(R.string.header_video), type = AVType.VIDEO)
                        }
                    }

                    if (videoItems.value.isNotEmpty()) {
                        itemsIndexed(
                            items = videoItems.value,
                            key = { index, item -> "video_${item.uniqueId}_$index" },
                        ) { _, item ->
                            ContentCard(
                                image = item.imageUrl ?: "",
                                contentTitle = item.title ?: "",
                                contentDescription = item.testingDescription,
                                accent = orange,
                                onClick = { context.intentPlayerActivity(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Context.intentPlayerActivity(wrapper: SourceWrapper) {
    startActivity(
        Intent(
            PlayerActivity.getStartIntent(
                packageContext = this,
                sourceWrapper = wrapper,
            ),
        ),
    )
}
