package nl.npo.player.sample_app.model

import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import java.io.Serializable

data class SourceWrapper(
    val title: String,
    val testingDescription: String = "",
    val uniqueId: String,
    val getStreamLink: Boolean,
    val streamUrl: String? = null,
    val startOffset: Double = 0.0,
    val autoPlay: Boolean = false,
    val uiEnabled: Boolean = true,
    val offlineDownloadAllowed: Boolean = false,
    val imageUrl: String? = "https://cdn.npoplayer.nl/posters/default-npo-poster.png",
    val preferThisImageUrlOverStreamLink: Boolean = false,
    val overrideStreamLinkTitleAndDescription: Boolean = false,
    val npoSourceConfig: NPOSourceConfig? = if (!getStreamLink) MyNPOSourceConfig(
        title = title,
        uniqueId = uniqueId,
        streamUrl = streamUrl!!,
        startOffset = startOffset,
        autoPlay = autoPlay,
        imageUrl = imageUrl
    ) else null,
    val npoOfflineContent: NPOOfflineContent? = null,
    val asPlusUser: Boolean = true
) : Serializable {
    override fun toString(): String {
        return "$title - $uniqueId"
    }
}
