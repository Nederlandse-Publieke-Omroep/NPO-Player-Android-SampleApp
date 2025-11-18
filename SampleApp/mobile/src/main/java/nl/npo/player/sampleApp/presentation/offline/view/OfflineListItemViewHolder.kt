package nl.npo.player.sampleApp.presentation.offline.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.exception.NPOOfflineErrorCode
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.databinding.ListItemOfflineBinding
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.model.SourceWrapper

class OfflineListItemViewHolder private constructor(
    private val binding: ListItemOfflineBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private var sourceWrapper: SourceWrapper? = null
    private var downloadStateLiveData: LiveData<NPODownloadState>? = null

    fun bind(
        sourceWrapper: SourceWrapper,
        onItemClickListener: (SourceWrapper) -> Unit,
        onItemLongClickListener: (SourceWrapper) -> Boolean,
    ) {
        this.sourceWrapper = sourceWrapper
        binding.itemTitle.text = sourceWrapper.title
        handleProgress()
        downloadStateLiveData?.removeObservers(binding.root.context as LifecycleOwner)
        sourceWrapper.npoOfflineContent?.apply {
            downloadStateLiveData = downloadState.asLiveData()
            downloadStateLiveData?.observeNonNull(
                binding.root.context as LifecycleOwner,
                ::handleProgress,
            )
        }
        binding.root.setOnClickListener { this.sourceWrapper?.let { onItemClickListener.invoke(it) } }
        binding.root.setOnLongClickListener {
            this.sourceWrapper?.let {
                onItemLongClickListener.invoke(
                    it,
                )
            } ?: false
        }
    }

    private fun handleProgress(npoDownloadState: NPODownloadState? = null) {
        binding.apply {
            when (npoDownloadState) {
                is NPODownloadState.Failed -> {
                    cpiOffline.isVisible = false
                    ivStatus.apply {
                        setImageResource(android.R.drawable.stat_notify_error)
                        isVisible = true
                    }
                    if ((npoDownloadState.reason as? NPOOfflineContentException.DownloadError)?.errorCode ==
                        NPOOfflineErrorCode.DownloadFailed
                    ) {
                        AlertDialog
                            .Builder(binding.root.context)
                            .setTitle(npoDownloadState.reason.message)
                            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }

                NPODownloadState.Finished -> {
                    cpiOffline.isVisible = false
                    ivStatus.apply {
                        setImageResource(android.R.drawable.ic_media_play)
                        isVisible = true
                    }
                }

                is NPODownloadState.InProgress -> {
                    cpiOffline.setProgress(npoDownloadState.progress)
                    ivStatus.isVisible = false
                }

                NPODownloadState.Initializing -> cpiOffline.progress = 0
                is NPODownloadState.Paused -> {
                    cpiOffline.isVisible = false
                    ivStatus.apply {
                        setImageResource(android.R.drawable.ic_media_pause)

                        isVisible = true
                    }
                }

                is NPODownloadState.Deleting -> {
                    cpiOffline.apply {
                        isIndeterminate = true
                        isVisible = true
                    }
                    ivStatus.isVisible = false
                }

                null -> {
                    cpiOffline.isVisible = false
                    ivStatus.apply {
                        setImageResource(android.R.drawable.stat_sys_download)
                        isVisible = true
                    }
                }
            }
        }
    }

    private fun CircularProgressIndicator.setProgress(progress: Float) {
        isIndeterminate = false
        this.progress = progress.toInt()
        isVisible = true
    }

    companion object {
        fun create(parent: ViewGroup): OfflineListItemViewHolder {
            val binding =
                ListItemOfflineBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            return OfflineListItemViewHolder(binding)
        }
    }
}
