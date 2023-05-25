package nl.npo.player.sample_app.presentation.offline.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sample_app.databinding.ListItemOfflineBinding
import nl.npo.player.sample_app.model.SourceWrapper

class OfflineListItemViewHolder private constructor(
    private val binding: ListItemOfflineBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var sourceWrapper: SourceWrapper? = null
    private var viewHolderScope: CoroutineScope? = null

    fun bind(
        sourceWrapper: SourceWrapper,
        onItemClickListener: (SourceWrapper) -> Unit,
        onItemLongClickListener: (SourceWrapper) -> Boolean
    ) {
        this.sourceWrapper = sourceWrapper
        binding.tvTitle.text = sourceWrapper.title
        sourceWrapper.npoOfflineContent?.apply {
            getViewHolderScope().launch {
                downloadState.onEach { handleProgress() }.collect()
            }
            handleProgress()
        } ?: run {
            handleProgress()
        }
        binding.root.setOnClickListener { this.sourceWrapper?.let { onItemClickListener.invoke(it) } }
        binding.root.setOnLongClickListener {
            this.sourceWrapper?.let {
                onItemLongClickListener.invoke(
                    it
                )
            } ?: false
        }
    }

    private fun getViewHolderScope(): CoroutineScope {
        viewHolderScope?.cancel("New binding, old binding removed.")
        return CoroutineScope(Dispatchers.Main).also {
            viewHolderScope = it
        }
    }

    private fun handleProgress() {
        val npoDownloadState = sourceWrapper?.npoOfflineContent?.downloadState?.value
        binding.apply {
            when (npoDownloadState) {
                is NPODownloadState.Failed -> {
                    cpiOffline.isVisible = false
                    ivStatus.apply {
                        setImageResource(android.R.drawable.stat_notify_error)
                        isVisible = true
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
            val binding = ListItemOfflineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return OfflineListItemViewHolder(binding)
        }
    }
}
