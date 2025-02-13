package nl.npo.player.sampleApp.tv.presentation.selection

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import nl.npo.player.sampleApp.shared.model.SourceWrapper

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any,
    ) {
        val movie = item as SourceWrapper

        viewHolder.title.text = movie.title
        viewHolder.subtitle.text = movie.testingDescription
        viewHolder.body.text = movie.testingDescription
    }
}
