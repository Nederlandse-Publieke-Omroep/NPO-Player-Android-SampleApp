package nl.npo.player.sampleApp.model

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import java.io.Serializable

data class SourceWrapper(
    val title: String?,
    val testingDescription: String = "",
    val uniqueId: String,
    val getStreamLink: Boolean,
    val streamUrl: String? = null,
    val startOffset: Double = 0.0,
    val offlineDownloadAllowed: Boolean = false,
    val imageUrl: String? = "https://cdn.npoplayer.nl/posters/default-npo-poster.png",
    val avType: AVType? = null,
    val preferThisImageUrlOverStreamLink: Boolean = false,
    val overrideStreamLinkTitleAndDescription: Boolean = false,
    val npoSourceConfig: NPOSourceConfig? =
        if (!getStreamLink) {
            MyNPOSourceConfig(
                title = title,
                uniqueId = uniqueId,
                streamUrl = streamUrl!!,
                startOffset = startOffset,
                imageUrl = imageUrl,
                avType = avType,
            )
        } else {
            null
        },
    val npoOfflineContent: NPOOfflineContent? = null,
) : Serializable {
    override fun toString(): String = "$title - $uniqueId"
}
