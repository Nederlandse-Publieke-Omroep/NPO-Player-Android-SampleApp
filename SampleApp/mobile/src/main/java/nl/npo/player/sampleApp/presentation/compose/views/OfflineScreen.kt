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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asLiveData
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
fun OfflineScreen(viewModel: OfflineViewModel = hiltViewModel()) {
  Scaffold(containerColor = Color.Transparent) {
    val orange = Color(0xFFFF7A00)
    val mergedList by viewModel.mergedLinkList.observeAsState(emptyList())
    val itemId by viewModel.itemId.collectAsState()
    val context = LocalContext.current
    val downloadError by viewModel.downloadError.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
      toastMessage?.let { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      }
    }


    if (downloadError is DownloadEvent.Error) {
      val data = downloadError as DownloadEvent.Error
      CustomAlertDialog(
        dialogTitle = data.message ?: "",
        modifier = Modifier,
        onConfirm = viewModel::dismissErrorDialog,
        onDismiss = viewModel::dismissErrorDialog,
      )
    }

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
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
          ) {

            stickyHeader {
              Box(
                modifier = Modifier
                  .background(Color(0xFF121212)) // or your gradient

              ) {
                Header("Offline")
              }
            }

            if (mergedList.isNotEmpty()) {
              itemsIndexed(
                items = mergedList,
                key = { index, item -> "live_${item.uniqueId}_$index" },
              ) { _, item ->
                val currentState =
                  item.npoOfflineContent?.downloadState?.asLiveData()
                ContentCard(
                  image = item.imageUrl,
                  contentTitle = item.title ?: "",
                  accent = orange,
                  onLongClick = { viewModel.onDialogItemClicked(sourceWrapper = item) },
                  trailingContent = {
                    ProgressActionIcon(
                      downloadState = currentState,
                      onClick = {
                        viewModel.onItemClicked(
                          sourceWrapper = item,
                          id = item.uniqueId,
                          onClick = { context.startPlayerActivity(item) }

                        )
                      },
                    )
                  },
                )
                if (itemId == item.uniqueId) {
                  CustomAlertDialog(
                    dialogTitle = "Delete offline content",
                    dialogDescription = "Are you sure you want to delete the offline content for \"${item.title}\"",
                    modifier = Modifier,
                    onConfirm = {
                      viewModel.deleteOfflineContent(sourceWrapper = item)
                      viewModel.dismissDialogItem()
                    },
                    onDismiss = viewModel::dismissDialogItem,
                  )
                }
              }
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
        sourceWrapper = wrapper.copy(
          npoOfflineContent = null,
          npoSourceConfig = wrapper.npoOfflineContent?.getOfflineSource(),
        ),
      )
    )
  )
}
