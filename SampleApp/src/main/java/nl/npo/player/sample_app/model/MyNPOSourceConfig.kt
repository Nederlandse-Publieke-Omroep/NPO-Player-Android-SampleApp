package nl.npo.player.sample_app.model

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.enums.NPOSourceType
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.domain.stream_link.model.Subtitle

@kotlinx.serialization.Serializable
data class MyNPOSourceConfig(
    override val uniqueId: String,
    override val streamUrl: String,
    override val title: String? = null,
    override val description: String? = null,
    override val imageUrl: String? = null,
    override val metadata: Map<String, String>? = null,
    override var startOffset: Double = 0.0,
    override val avType: AVType? = null,
    override val isLiveStream: Boolean? = null,
    override val sourceType: NPOSourceType = NPOSourceType.Progressive,
    override val autoPlay: Boolean = false,
    override val subtitles: List<Subtitle> = emptyList(),
    override val thumbnailTrack: String? = null,
    override val drmToken: String? = null,
    override val durationInMillis: Long? = null
) : NPOSourceConfig
