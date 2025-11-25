package nl.npo.player.sampleApp.presentation.compose.views

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.ContentCard
import nl.npo.player.sampleApp.presentation.compose.components.CustomAlertDialog
import nl.npo.player.sampleApp.presentation.compose.components.Header
import nl.npo.player.sampleApp.presentation.compose.components.ProgressActionIcon
import nl.npo.player.sampleApp.presentation.model.DownloadEvent
import nl.npo.player.sampleApp.presentation.offline.OfflineViewModel
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OfflineScreen(
    modifier: Modifier = Modifier,
    viewModel: OfflineViewModel = hiltViewModel(),
) {
    val orange = Color(0xFFFF7A00)
    val mergedList by viewModel.mergedLinkList.observeAsState(emptyList())
    val context = LocalContext.current
    val downloadEvent by viewModel.downloadEvent.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    if (downloadEvent is DownloadEvent.Error) {
        val data = downloadEvent as DownloadEvent.Error
        CustomAlertDialog(
            dialogTitle = data.message ?: "",
            modifier = Modifier,
            onConfirm = viewModel::dismissDownloadEventDialog,
            onDismiss = viewModel::dismissDownloadEventDialog,
        )
    }

    if (downloadEvent is DownloadEvent.Delete) {
        val data = downloadEvent as DownloadEvent.Delete
        val msg =
            context.getString(
                R.string.delete_offline_confirmation,
                data.sourceWrapper.title,
            )
        CustomAlertDialog(
            dialogTitle = stringResource(R.string.delete_offline_title),
            dialogDescription = msg,
            modifier = modifier,
            onConfirm = viewModel::dismissDownloadEventDialog,
            onDismiss = viewModel::dismissDownloadEventDialog,
        )
    }

    Column(
        modifier =
            modifier
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
        Box(modifier = modifier.fillMaxSize()) {
            if (mergedList.isEmpty()) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    stickyHeader {
                        Box(
                            modifier = modifier.background(Color(0xFF121212)),
                        ) {
                            Header(modifier = modifier, stringResource(R.string.offline_header))
                        }
                    }

                    if (mergedList.isNotEmpty()) {
                        itemsIndexed(
                            items = mergedList,
                            key = { index, item -> "offline_${item.uniqueId}_$index" },
                        ) { _, item ->
                            val currentState =
                                item.npoOfflineContent?.downloadState?.asLiveData()
                            ContentCard(
                                image = item.imageUrl,
                                contentTitle = item.title ?: "",
                                accent = orange,
                                onLongClick = { viewModel.deleteDownloadedItem(item) },
                                trailingContent = {
                                    ProgressActionIcon(
                                        downloadState = currentState,
                                        onClick = {
                                            viewModel.onItemClicked(
                                                sourceWrapper = item,
                                                id = item.uniqueId,
                                                onClick = { context.startPlayerActivity(item) },
                                            )
                                        },
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

fun Context.startPlayerActivity(wrapper: SourceWrapper) {
    startActivity(
        Intent(
            PlayerActivity.getStartIntent(
                packageContext = this,
                sourceWrapper =
                    wrapper.copy(
                        npoOfflineContent = null,
                        npoSourceConfig = wrapper.npoOfflineContent?.getOfflineSource(),
                    ),
            ),
        ),
    )
}
