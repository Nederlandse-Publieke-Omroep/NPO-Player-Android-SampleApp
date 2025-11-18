package nl.npo.player.sampleApp.presentation.compose.views

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.presentation.compose.Header
import nl.npo.player.sampleApp.presentation.compose.ContentCard
import nl.npo.player.sampleApp.presentation.compose.DownloadActionIcon
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

        val mergedList by viewModel.mergedLinkList.observeAsState(emptyList())
        val toastMessage by viewModel.toastMessage.observeAsState()


        //val LoadList = viewModel.mergedLinkList.observeAsState(emptyList())

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
                if (mergedList.isEmpty())
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    stickyHeader {
                        Header("Offline")
                        }

                    val content = mergedList.map { it.npoOfflineContent }
                    val id = content.map { it?.uniqueId }
                    if (mergedList.isNotEmpty()) {
                        itemsIndexed(
                            items = mergedList,
                            key = { index, _ -> "live_${id}_$index" }   // ← prefix keys
                        ) { _, item ->
                            val test = item.npoOfflineContent?.downloadState?.asLiveData()
                            Log.d("DEBUG_INFO","currentState=${test?.value}, item=${item.uniqueId}" )
                            ContentCard(
                                image = item.imageUrl,
                                contentTitle = item.title ?: "",
                                accent = orange,
                                trailingContent = {
                                    DownloadActionIcon(
                                        currentState = test,
                                        onClick = { viewModel.onItemClicked(item, item.uniqueId) },
                                    )
                                }
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

