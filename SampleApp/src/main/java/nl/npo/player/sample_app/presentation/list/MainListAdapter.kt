package nl.npo.player.sample_app.presentation.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.presentation.list.view.MainListItemViewHolder

class MainListAdapter(
    var offlineSource: List<SourceWrapper>,
    private val onItemClickListener: (SourceWrapper) -> Unit
) : RecyclerView.Adapter<MainListItemViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): MainListItemViewHolder {
        return MainListItemViewHolder.create(viewGroup)
    }

    override fun onBindViewHolder(
        offlineListItemViewHolder: MainListItemViewHolder,
        position: Int
    ) {
        offlineListItemViewHolder.bind(
            offlineSource[position],
            onItemClickListener
        )
    }

    override fun getItemCount() = offlineSource.size
}
