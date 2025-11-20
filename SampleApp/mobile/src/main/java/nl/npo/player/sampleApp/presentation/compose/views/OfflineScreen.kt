package nl.npo.player.sampleApp.presentation.compose.views

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import nl.npo.player.sampleApp.presentation.compose.Header
import nl.npo.player.sampleApp.presentation.compose.ContentCard
import nl.npo.player.sampleApp.presentation.compose.CustomALertDialog
import nl.npo.player.sampleApp.presentation.compose.DownloadActionIcon
import nl.npo.player.sampleApp.presentation.compose.DownloadEvent
import nl.npo.player.sampleApp.presentation.offline.OfflineViewModel
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
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
        val dialogItemId  by viewModel.dialogItemId.collectAsState()
        val context = LocalContext.current
        var dialogData by remember { mutableStateOf<DownloadEvent.ErrorMessage?>(null) }

        LaunchedEffect(toastMessage) {
            toastMessage?.let { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }

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
                            val currentState = item.npoOfflineContent?.downloadState?.asLiveData()
                            ContentCard(
                                image = item.imageUrl,
                                contentTitle = item.title ?: "",
                                accent = orange,
                                onLongClick = { viewModel.onDialogItemClicked(item) },
                                trailingContent = {
                                    DownloadActionIcon(
                                        currentState = currentState,
                                        onClick = { viewModel.onItemClicked(item, item.uniqueId) },
                                    )
                                }
                            )
                            if (dialogItemId == item.uniqueId) {
                                CustomALertDialog(
                                    dialogTitle = "Delete offline content",
                                    dialogDescription = "Are you sure you want to delete the offline content for \"${item.title}\"",
                                    modifier = Modifier,
                                    onConfirm = {
                                        viewModel.deleteOfflineContent(item)
                                        viewModel.dismissDialogItem()
                                    },
                                    onDismiss = viewModel::dismissDialogItem
                                )
                            }

                            LaunchedEffect(viewModel) {
                                viewModel.events.collect { event -> when(event) {
                                    is DownloadEvent.Intent ->
                                        context.startActivity(
                                            Intent(PlayerActivity.getStartIntent
                                                (context, item
                                                .copy(npoOfflineContent = null, npoSourceConfig =
                                                    item.npoOfflineContent?.getOfflineSource())))
                                        )
                                        is DownloadEvent.ErrorMessage -> dialogData = event
                                }
                                }
                            }

                            dialogData?.let { data ->
                                CustomALertDialog(
                                    dialogTitle = data.message,
                                    modifier = Modifier,
                                    onConfirm = {
                                       dialogData = null
                                    },
                                    onDismiss = { dialogData = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
 }


