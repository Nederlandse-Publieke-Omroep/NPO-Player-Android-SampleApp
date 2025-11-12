package nl.npo.player.sampleApp.presentation.compose.views

import android.annotation.SuppressLint
import android.widget.ProgressBar
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.npo.player.sampleApp.presentation.compose.Header
import nl.npo.player.sampleApp.presentation.compose.RowCard
import nl.npo.player.sampleApp.presentation.compose.SectionHeader
import nl.npo.player.sampleApp.presentation.offline.OfflineViewModel
import kotlin.collections.map

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun OfflineScreen(
    viewModel: OfflineViewModel = hiltViewModel(),
) {
    Scaffold(containerColor = Color.Transparent) {
        val orange = Color(0xFFFF7A00)
        val offline = viewModel.offlineLinkList.value
        val list = viewModel.mergedLinkList.value
        val context = LocalContext.current
        val toastMessage by viewModel.toastMessage.observeAsState()

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
//                if (list.isNullOrEmpty())
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
                        Header("Offline")
                        }

                    if (offline != null ) {
                        val content = offline.map { it.npoOfflineContent }
                        val id = content.map { it?.uniqueId }
                        itemsIndexed(
                            items = list,
                            key = { index, _ -> "live_${id}_$index" }   // ← prefix keys
                        ) { _, item ->
                            RowCard(
                                image = null,
                                contentTitle = item.title ?: "",
                                accent = orange,
                                icon = Icons.Default.Download,
                                onClick = { viewModel.onItemClicked(item) },
                            )

                            toastMessage?.let { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
            }
        }
    }
 }

