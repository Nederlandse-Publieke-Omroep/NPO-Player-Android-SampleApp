package nl.npo.player.sampleApp.presentation.offline

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sampleApp.presentation.offline.view.OfflineListItemViewHolder
import nl.npo.player.sampleApp.shared.model.SourceWrapper

class OfflineListAdapter(
    var offlineSource: List<SourceWrapper>,
    private val onItemClickListener: (SourceWrapper) -> Unit,
    private val onItemLongClickListener: (SourceWrapper) -> Boolean,
) : RecyclerView.Adapter<OfflineListItemViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int,
    ): OfflineListItemViewHolder = OfflineListItemViewHolder.create(viewGroup)

    override fun onBindViewHolder(
        offlineListItemViewHolder: OfflineListItemViewHolder,
        position: Int,
    ) {
        offlineListItemViewHolder.bind(
            offlineSource[position],
            onItemClickListener,
            onItemLongClickListener,
        )
    }

    override fun getItemCount() = offlineSource.size
}
