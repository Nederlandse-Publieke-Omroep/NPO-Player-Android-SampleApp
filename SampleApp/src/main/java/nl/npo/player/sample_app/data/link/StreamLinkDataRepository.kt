package nl.npo.player.sample_app.data.link

import nl.npo.player.sample_app.domain.LinkRepository
import nl.npo.player.sample_app.domain.annotation.StreamLinkRepository
import nl.npo.player.sample_app.model.SourceWrapper

@StreamLinkRepository
object StreamLinkDataRepository : LinkRepository {
    private val streamLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102 (Start 10 minuten terug in de live stream) - Start User",
                startOffset = -(10 * 60.0),
                getStreamLink = true,
                uniqueId = "LI_NL1_4188102",
                autoPlay = true,
                asPlusUser = false
            ),
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102 (Start live) - Start User - NO UI",
                uniqueId = "LI_NL1_4188102",
                getStreamLink = true,
                autoPlay = true,
                asPlusUser = false,
                uiEnabled = false
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 - (Start 10 minuten in the episode)",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg"
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246 - Start user",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                autoPlay = false,
                offlineDownloadAllowed = true,
                asPlusUser = false,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg"
            ),
            SourceWrapper(
                title = "Karen Piri: POW_05275080 - NO UI",
                uniqueId = "POW_05275080",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/Karen-Pirie-Key_Art_cdn_header-1673600845.jpg",
                uiEnabled = false
            ),
            SourceWrapper(
                title = "Fragment: Op1 - POMS_BV_20012834",
                uniqueId = "POMS_BV_20012834",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/op1_cdn_header-1677598703.jpg"
            ),
            SourceWrapper(
                title = "Fragment: Op1 POMS_BV_20012835",
                uniqueId = "POMS_BV_20012835",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/op1_cdn_header-1677598703.jpg",
            ),
            SourceWrapper(
                title = "Radio 1: LI_RA1_8167349",
                uniqueId = "LI_RA1_8167349",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Radio 2: LI_RA2_8167353",
                uniqueId = "LI_RA2_8167353",
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://broadcast-images.nporadio.nl/w_1200/s3-nporadio2/956a1ae4-dca0-4fa8-9e17-15c0c965290f.jpg"
            ),
            SourceWrapper(
                title = "Sterren NL: LI_RA2_837085 - NO UI",
                uniqueId = "LI_RA2_837085",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://www.nporadio5.nl/sterrennl/images/blue-diamonds.webp",
                uiEnabled = false
            ),
            SourceWrapper(
                title = "Soul&Jazz: LI_RA6_837069",
                uniqueId = "LI_RA6_837069",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "NOS Journaal: POW_05467390",
                uniqueId = "POW_05467390",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Visual Radio 3FM: LI_3FM_300881",
                uniqueId = "LI_3FM_300881",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Radio 3FM: LI_3FM_8167356",
                uniqueId = "LI_3FM_8167356",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "FUNX ICECAST: LI_FUNX_837073",
                uniqueId = "LI_FUNX_837073",
                getStreamLink = true,
                autoPlay = false
            )
        )
    }

    override suspend fun getSourceList() = streamLinkSourceList
}
