package nl.npo.player.sample_app.data.link

import nl.npo.player.sample_app.domain.LinkRepository
import nl.npo.player.sample_app.domain.annotation.StreamLinkRepository
import nl.npo.player.sample_app.model.SourceWrapper

@StreamLinkRepository
object StreamLinkDataRepository : LinkRepository {
    private val streamLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102",
                testingDescription = "Playback Live DVR from startpos",
                startOffset = -(10 * 60.0),
                getStreamLink = true,
                uniqueId = "LI_NL1_4188102",
                autoPlay = true,
                asPlusUser = false,
                imageUrl = "https://cdn.npoplayer.nl/posters/npo1_afbeelding.jpg"
            ),
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102",
                testingDescription = "Playback Live DVR - NO UI",
                uniqueId = "LI_NL1_4188102",
                getStreamLink = true,
                autoPlay = true,
                asPlusUser = false,
                uiEnabled = false,
                imageUrl = "https://cdn.npoplayer.nl/posters/npo1_afbeelding.jpg"
            ),
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102",
                testingDescription = "Playback Live DVR (DRM)",
                uniqueId = "LI_NL1_4188102",
                getStreamLink = true,
                autoPlay = true,
                asPlusUser = false,
                uiEnabled = true,
                imageUrl = "https://cdn.npoplayer.nl/posters/npo1_afbeelding.jpg"
            ),
            SourceWrapper(
                title = "Visual Radio 1: LI_RADIO1_300877",
                testingDescription = "Playback Live NO-DVR (DRM)",
                uniqueId = "LI_RADIO1_300877",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "Playback VOD (DRM)",
                uniqueId = "AT_300002877",
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = false
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "Poster",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = true
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "Playback VOD from startpos",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg"
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246",
                testingDescription = "Playback VOD",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                autoPlay = true,
                offlineDownloadAllowed = true,
                asPlusUser = true,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg"
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "editTitle & editDescription",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                overrideStreamLinkTitleAndDescription = true
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "NICAM information 1",
                uniqueId = "AT_300002877",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = true
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246",
                testingDescription = "NICAM information 2",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                autoPlay = true,
                offlineDownloadAllowed = true,
                asPlusUser = false,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg"
            ),
            SourceWrapper(
                title = "Teledoc Campus: AT_2031723",
                testingDescription = "Choose subtitling language",
                uniqueId = "AT_2031723",
                getStreamLink = true,
                autoPlay = false,
                asPlusUser = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/608874"
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877",
                testingDescription = "Plus content as Start User (not allowed)",
                uniqueId = "AT_300002877",
                getStreamLink = true,
                autoPlay = false,
                asPlusUser = false,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg"
            ),
            SourceWrapper(
                title = "Karen Piri: POW_05275080",
                testingDescription = "",
                uniqueId = "POW_05275080",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/Karen-Pirie-Key_Art_cdn_header-1673600845.jpg",
                uiEnabled = true
            ),
            SourceWrapper(
                title = "Karen Piri: POW_05275080 - NO UI",
                testingDescription = "",
                uniqueId = "POW_05275080",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/Karen-Pirie-Key_Art_cdn_header-1673600845.jpg",
                uiEnabled = false
            ),
            SourceWrapper(
                title = "Op1 - POMS_BV_20012834",
                testingDescription = "Segment 1 (POMS)",
                uniqueId = "POMS_BV_20012834",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/op1_cdn_header-1677598703.jpg"
            ),
            SourceWrapper(
                title = "Op1 POMS_BV_20012835",
                testingDescription = "Segment 2 (POMS)",
                uniqueId = "POMS_BV_20012835",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://images.npo.nl/header/2560x1440/op1_cdn_header-1677598703.jpg",
            ),
            SourceWrapper(
                title = "Radio 1: LI_RA1_8167349",
                testingDescription = "Playback Audio",
                uniqueId = "LI_RA1_8167349",
                getStreamLink = true,
                startOffset = -(10 * 60.0),
                autoPlay = true
            ),
            SourceWrapper(
                title = "Radio 2: LI_RA2_8167353",
                testingDescription = "",
                uniqueId = "LI_RA2_8167353",
                getStreamLink = true,
                autoPlay = false,
                imageUrl = "https://broadcast-images.nporadio.nl/w_1200/s3-nporadio2/956a1ae4-dca0-4fa8-9e17-15c0c965290f.jpg"
            ),
            SourceWrapper(
                title = "Sterren NL: LI_RA2_837085 - NO UI",
                testingDescription = "",
                uniqueId = "LI_RA2_837085",
                getStreamLink = true,
                autoPlay = true,
                imageUrl = "https://www.nporadio5.nl/sterrennl/images/blue-diamonds.webp",
                uiEnabled = false
            ),
            SourceWrapper(
                title = "Soul&Jazz: LI_RA6_837069",
                testingDescription = "",
                uniqueId = "LI_RA6_837069",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "NOS Journaal: POW_05467390",
                testingDescription = "",
                uniqueId = "POW_05467390",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Visual Radio 3FM: LI_3FM_300881",
                testingDescription = "",
                uniqueId = "LI_3FM_300881",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "Radio 3FM: LI_3FM_8167356",
                testingDescription = "",
                uniqueId = "LI_3FM_8167356",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "FUNX ICECAST: LI_FUNX_837073",
                testingDescription = "",
                uniqueId = "LI_FUNX_837073",
                getStreamLink = true,
                autoPlay = false
            ),
            SourceWrapper(
                title = "POW_05216736 - Age restriction - As START user",
                testingDescription = "",
                uniqueId = "POW_05216736",
                getStreamLink = true,
                autoPlay = true,
                asPlusUser = false
            ),
            SourceWrapper(
                title = "POW_05216736 - Age restriction - As PLUS user",
                testingDescription = "",
                uniqueId = "POW_05216736",
                getStreamLink = true,
                autoPlay = true,
                asPlusUser = true
            ),
            SourceWrapper(
                title = "De Laatste Walvisvaarders: PREPR_RA1_17306750",
                testingDescription = "",
                uniqueId = "PREPR_RA1_17306750",
                getStreamLink = true,
                offlineDownloadAllowed = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1072/c1072x603/s1072x603>/2012559.jpg",
                autoPlay = false
            ),
            SourceWrapper(
                title = "NOS Noord-, Zuidlijn: POW_03900426",
                testingDescription = "Pre-roll ad (STER)",
                uniqueId = "POW_03900426",
                getStreamLink = true,
                asPlusUser = false,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1071323",
                autoPlay = false
            ),
            SourceWrapper(
                title = "Jacobine op 2: KN_1728228",
                testingDescription = "Pre-roll ad (STER) - 2 Ads",
                uniqueId = "KN_1728228",
                getStreamLink = true,
                asPlusUser = false,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1724048",
                autoPlay = false
            ),
            SourceWrapper(
                title = "Wie is de Mol - Seizoen 2024 - Afl. 3",
                testingDescription = "",
                uniqueId = "AT_300010866",
                getStreamLink = true,
                asPlusUser = false,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/2070960",
                autoPlay = false
            )
        )
    }

    override suspend fun getSourceList() = streamLinkSourceList
}
