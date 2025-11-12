package nl.npo.player.sampleApp.presentation.compose.views

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
internal fun PlayerScreen(
    viewModel: LinksViewModel = hiltViewModel(),
) {
    Scaffold {
        val orange = Color(0xFFFF7A00)
        val audio = viewModel.urlLinkList.value
        val video = viewModel.streamLinkList.value
        val context = LocalContext.current

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


//
//                    stickyHeader {
//                        SectionHeader("Live", AVType.AUDIO)
//                    }
//
//                    if (audio != null && video != null ) {
//                            itemsIndexed(
//                                items = audio,
//                                key = { index, _ -> "live_${id}_$index" }   // ← prefix keys
//                            ) { _, item ->
//                                RowCard(
//                                    image = item.imageUrl ?: "" ,
//                                    contentTitle = item.title ?: "",
//                                    contentDescription = item.testingDescription,
//                                    accent = orange,
//                                    onClick = {
//                                        context.startActivity(
//                                            Intent(PlayerActivity.getStartIntent
//                                                (context, item)
//                                            )
//                                        )
//                                    },
//                                )
//                            }
//                        }
//                    }


                    stickyHeader {
                        SectionHeader("Audio", AVType.AUDIO)
                    }

                    if (audio != null ) {
                        val id = audio.map { it.uniqueId }
                        itemsIndexed(
                            items = audio,
                            key = { index, _ -> "live_${id}_$index" }   // ← prefix keys
                        ) { _, item ->
                            RowCard(
                                image = item.imageUrl ?: "" ,
                                contentTitle = item.title ?: "",
                                contentDescription = item.testingDescription,
                                accent = orange,
                                onClick = {
                                    context.startActivity(
                                        Intent(PlayerActivity.getStartIntent
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
                        ) { _,item ->
                            RowCard(
                                image = item.imageUrl ?: "",
                                contentTitle = item.title ?: "" ,
                                contentDescription = item.testingDescription,
                                accent = orange,
                                onClick = { context.startActivity(
                                    Intent(PlayerActivity.getStartIntent
                                        (context, item)
                                    )
                                ) },
                            )
                        }
                    }
                }
            }
    }
}


