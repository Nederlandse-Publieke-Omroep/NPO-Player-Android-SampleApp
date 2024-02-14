package nl.npo.player.sample_app.presentation.list.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import nl.npo.player.sample_app.databinding.ListItemMainBinding
import nl.npo.player.sample_app.model.SourceWrapper

class MainListItemViewHolder private constructor(
    private val binding: ListItemMainBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var sourceWrapper: SourceWrapper? = null

    fun bind(
        sourceWrapper: SourceWrapper,
        onItemClickListener: (SourceWrapper) -> Unit
    ) {
        this.sourceWrapper = sourceWrapper
        binding.itemTitle.text = sourceWrapper.title
        binding.description.text = sourceWrapper.testingDescription
        Glide.with(binding.ivPoster.context)
            .load(sourceWrapper.imageUrl)
            .into(binding.ivPoster)
        binding.root.setOnClickListener { this.sourceWrapper?.let { onItemClickListener.invoke(it) } }
    }

    companion object {
        fun create(parent: ViewGroup): MainListItemViewHolder {
            val binding = ListItemMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MainListItemViewHolder(binding)
        }
    }
}
