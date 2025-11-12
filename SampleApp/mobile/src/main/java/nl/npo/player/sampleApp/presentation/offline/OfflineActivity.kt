package nl.npo.player.sampleApp.presentation.offline

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.domain.offline.NPOOfflineContentManager
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.databinding.ActivityOfflineBinding
import nl.npo.player.sampleApp.presentation.BaseActivity
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import javax.inject.Inject

@AndroidEntryPoint
class OfflineActivity : BaseActivity() {
    private lateinit var binding: ActivityOfflineBinding
    private val viewModel by viewModels<OfflineViewModel>()
    private val offlineListAdapter =
        OfflineListAdapter(emptyList(), ::onItemClicked, ::onItemLongClicked)

    @Inject
    lateinit var npoOfflineContentManager: NPOOfflineContentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupViews()


        // Update the context the BitmovinCastManager is using
        // This should be done in every Activity's onCreate using the cast function
        NPOCasting.updateCastingContext(this)
        setObservers()
        logPageAnalytics("OfflineActivity")
    }

    private fun ActivityOfflineBinding.setupViews() {
        rvOffline.adapter = offlineListAdapter
    }

    private fun setObservers() {
        viewModel.mergedLinkList.observeNonNull(this) { contentUpdated(it) }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun contentUpdated(sourceList: List<SourceWrapper>) {
        offlineListAdapter.offlineSource = sourceList
        offlineListAdapter.notifyDataSetChanged()
    }

    private fun onItemClicked(sourceWrapper: SourceWrapper) {
        if (sourceWrapper.npoOfflineContent != null) {
            val offlineContent = sourceWrapper.npoOfflineContent ?: return
            when (offlineContent.downloadState.value) {
                NPODownloadState.Finished -> {
                    val offlineSource = offlineContent.getOfflineSource()
                    startActivity(
                        PlayerActivity.getStartIntent(
                            this@OfflineActivity,
                            sourceWrapper.copy(
                                npoOfflineContent = null,
                                npoSourceConfig = offlineSource,
                            ),
                        ),
                    )
                }
                is NPODownloadState.Failed, is NPODownloadState.Paused -> {
                    offlineContent.startOrResumeDownload()
                }
                is NPODownloadState.InProgress -> {
                    offlineContent.pause()
                }
                is NPODownloadState.Deleting, NPODownloadState.Initializing -> {
                    // NO_OP
                }
            }
        } else {
            viewModel.createOfflineContent(sourceWrapper) { throwable ->
                Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onItemLongClicked(sourceWrapper: SourceWrapper): Boolean =
        if (sourceWrapper.npoOfflineContent != null) {
            showDeleteDialog(sourceWrapper)
            true
        } else {
            false
        }

    private fun showDeleteDialog(sourceWrapper: SourceWrapper) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Delete offline content")
        alertDialog.setMessage("Are you sure you want to delete the offline content for \"${sourceWrapper.title}\"")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
            viewModel.deleteOfflineContent(sourceWrapper)
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }
}
