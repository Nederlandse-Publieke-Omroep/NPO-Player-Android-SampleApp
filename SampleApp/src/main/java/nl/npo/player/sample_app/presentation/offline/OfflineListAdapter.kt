package nl.npo.player.sample_app.presentation.offline

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.presentation.offline.view.OfflineListItemViewHolder

class OfflineListAdapter(
    var offlineSource: List<SourceWrapper>,
    private val onItemClickListener: (SourceWrapper) -> Unit,
    private val onItemLongClickListener: (SourceWrapper) -> Boolean
) :
    RecyclerView.Adapter<OfflineListItemViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): OfflineListItemViewHolder {
        return OfflineListItemViewHolder.create(viewGroup)
    }

    override fun onBindViewHolder(
        offlineListItemViewHolder: OfflineListItemViewHolder,
        position: Int
    ) {
        offlineListItemViewHolder.bind(
            offlineSource[position],
            onItemClickListener,
            onItemLongClickListener
        )
    }

    override fun getItemCount() = offlineSource.size
}
