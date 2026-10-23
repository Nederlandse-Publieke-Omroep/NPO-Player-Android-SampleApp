package nl.npo.player.sampleApp.presentation.compose.views

import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseHelper
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseState
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.ContentCard
import nl.npo.player.sampleApp.presentation.compose.components.CustomAlertDialog
import nl.npo.player.sampleApp.presentation.compose.components.Header
import nl.npo.player.sampleApp.presentation.compose.components.ProgressActionIcon
import nl.npo.player.sampleApp.presentation.ext.getFormattedDownloadSize
import nl.npo.player.sampleApp.presentation.model.DownloadEvent
import nl.npo.player.sampleApp.presentation.offline.OfflineViewModel
import kotlin.time.Duration

@Composable
fun OfflineScreen(
    viewModel: OfflineViewModel =
        hiltViewModel(
            checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            },
            "OfflineScreen",
        ),
) {
    val orange = Color(0xFFFF7A00)
    val mergedList by viewModel.downloadableItemList.collectAsState()
    val context = LocalContext.current
    val downloadEvent by viewModel.downloadEvent.collectAsState()

    when (downloadEvent) {
        is DownloadEvent.ItemEvent.Error -> {
            val error = downloadEvent as DownloadEvent.ItemEvent.Error
            CustomAlertDialog(
                dialogTitle = stringResource(R.string.download_error_title),
                dialogDescription =
                    stringResource(
                        R.string.download_error_body,
                        error.sourceWrapper?.title.orEmpty(),
                        error.message.orEmpty(),
                    ),
                confirmButtonText = stringResource(R.string.download_error_retry),
                onConfirm = { viewModel.retryDownload(error) },
                onDismiss = { viewModel.cancelAndDeleteDownload(error) },
            )
        }

        is DownloadEvent.ItemEvent.Delete -> {
            val downloadEvent = downloadEvent as DownloadEvent.ItemEvent.Delete
            val msg =
                context.getString(
                    R.string.delete_offline_confirmation,
                    downloadEvent.sourceWrapper.title,
                )
            CustomAlertDialog(
                dialogTitle = stringResource(R.string.delete_offline_title),
                dialogDescription = msg,
                onConfirm = {
                    viewModel.cancelAndDeleteDownload(downloadEvent)
                },
                onDismiss = viewModel::dismissDownloadEventDialog,
            )
        }

        else -> {}
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
            if (mergedList.isEmpty()) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                return@Box
            }
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                stickyHeader {
                    Header(
                        modifier = Modifier,
                        title = stringResource(R.string.offline_header),
                    )
                }
                itemsIndexed(
                    items = mergedList,
                    key = { index, downloadableItem -> "offline_${downloadableItem.sourceWrapper.uniqueId}_$index" },
                ) { _, downloadableItem ->
                    val item = downloadableItem.sourceWrapper
                    val state by downloadableItem.downloadState.collectAsState()

                    val licenseState by downloadableItem.drmLicenseState.collectAsState()
                    val drmLicenseExpiration by downloadableItem.drmLicenseExpiration.collectAsState()
                    var failureNotified by rememberSaveable(item.uniqueId) { mutableStateOf(false) }
                    val isFailed = state is NPODownloadState.Failed

                    LaunchedEffect(isFailed) {
                        if (isFailed && !failureNotified) {
                            failureNotified = true
                            Toast
                                .makeText(
                                    context,
                                    context.getString(
                                        R.string.download_error_toast,
                                        item.title.orEmpty(),
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else if (!isFailed) {
                            failureNotified = false
                        }
                    }

                    ContentCard(
                        image = item.imageUrl,
                        contentTitle = item.title.orEmpty(),
                        contentDescription = "${state.getFormattedDownloadSize(context)}${
                            licenseState.getContentDescription(
                                drmLicenseExpiration,
                                { id, formatArgs ->
                                    formatArgs?.let {
                                        context.getString(
                                            id,
                                            formatArgs,
                                        )
                                    } ?: context.getString(id)
                                },
                            )
                        }",
                        accent = orange,
                        onClick = {
                            downloadableItem.onItemClicked(
                                onClick = { sourceWrapper ->
                                    viewModel.playOfflineContent(
                                        sourceWrapper,
                                        context,
                                    )
                                },
                                error = {
                                    Toast
                                        .makeText(
                                            context,
                                            it.message
                                                ?: context.getString(R.string.download_error_unknown),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                            )
                        },
                        onLongClick = { viewModel.showDeleteDialog(item) },
                        trailingContent = { onAction ->

                            ProgressActionIcon(
                                downloadState = state,
                                onClick = onAction,
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun NPOOfflineLicenseState?.getContentDescription(
    drmLicenseExpiration: NPOOfflineLicenseHelper.DRMLicenseExpiration?,
    stringRetriever: (id: Int, formatArgs: Any?) -> String,
): String =
    when (this) {
        is NPOOfflineLicenseState.Finished,
        is NPOOfflineLicenseState.FinishedButExpired,
        -> {
            drmLicenseExpiration.toStyledText(stringRetriever)
        }

        is NPOOfflineLicenseState.InProgress -> {
            stringRetriever(R.string.download_drm_content_description_in_progress, null)
        }

        is NPOOfflineLicenseState.Failed -> {
            stringRetriever(R.string.download_drm_content_description_failed, null)
        }

        else -> {
            ""
        }
    }

private fun NPOOfflineLicenseHelper.DRMLicenseExpiration?.toStyledText(stringRetriever: (id: Int, formatArgs: Any?) -> String): String =
    when (this?.drmExpiration) {
        Duration.ZERO, null -> {
            stringRetriever(
                R.string.download_drm_content_description_finished_expired,
                null,
            )
        }

        else -> {
            stringRetriever(R.string.download_drm_content_description_finished, drmExpiration)
        }
    }
