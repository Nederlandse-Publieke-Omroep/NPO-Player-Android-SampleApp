package nl.npo.player.sampleApp.shared.data.link

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.streamLink.model.Nicam
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.model.MyNicamContentDescription
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@StreamLinkRepository
object StreamLinkDataRepository : LinkRepository {
    private val streamLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "Audio description test: AD_DASH_20241209",
                testingDescription = "Audio description test",
                uniqueId = "AD_DASH_20241209",
                getStreamLink = true,
                imageUrl = "https://assets-start.npo.nl/resources/2024/07/26/ed3443af-3829-4e33-a952-c37356d87fb3.jpg?dimensions=375x375",
                preferThisImageUrlOverStreamLink = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Audio transcriptie test: WO_VPRO_20308566 (ALLEEN OP ACC ENVIRONMENT)",
                testingDescription = "Audio transcriptie test",
                uniqueId = "WO_VPRO_20308566",
                getStreamLink = true,
                imageUrl = "https://assets-start.npo.nl/resources/2024/07/26/ed3443af-3829-4e33-a952-c37356d87fb3.jpg?dimensions=375x375",
                preferThisImageUrlOverStreamLink = true,
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102",
                testingDescription = "Playback Live DVR from startpos (10 minutes offset from live)",
                startOffset = -(10 * 60.0),
                getStreamLink = true,
                uniqueId = "LI_NL1_4188102",
                imageUrl = "https://cdn.npoplayer.nl/posters/npo1_afbeelding.jpg",
                avType = AVType.VIDEO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Live NPO1: LI_NL1_4188102",
                testingDescription = "Playback Live DVR (DRM)",
                uniqueId = "LI_NL1_4188102",
                getStreamLink = true,
                imageUrl = "https://cdn.npoplayer.nl/posters/npo1_afbeelding.jpg",
                avType = AVType.VIDEO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Visual Radio 1: LI_RADIO1_300877",
                testingDescription = "Playback Live NO-DVR (DRM)",
                uniqueId = "LI_RADIO1_300877",
                getStreamLink = true,
                avType = AVType.VIDEO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877",
                testingDescription = "Playback VOD (DRM)",
                uniqueId = "AT_300002877",
                getStreamLink = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = false,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877",
                testingDescription = "Poster + Playback VOD from startpos (10 minutes offset from start)",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = true,
                avType = AVType.VIDEO,
                offlineDownloadAllowed = true,
            ),
            SourceWrapper(
                title = "Beste Zangers - s17-e1 - Karin Bloemen",
                testingDescription = "Playback VOD (No DRM)",
                uniqueId = "AT_300009512",
                getStreamLink = true,
                startOffset = (60.3 * 25),
                imageUrl = "https://images.poms.omroep.nl/image/s512/2202469",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "De Joodse Raad: VPWON_1333874",
                testingDescription = "Skippable Chapters",
                uniqueId = "VPWON_1333874",
                getStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2091245",
                preferThisImageUrlOverStreamLink = false,
                avType = AVType.VIDEO,
                offlineDownloadAllowed = true,
            ),
            SourceWrapper(
                title = "De Joodse Raad: VPWON_1333875",
                testingDescription = "Skippable Chapters",
                uniqueId = "VPWON_1333875",
                getStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2096138",
                preferThisImageUrlOverStreamLink = false,
                avType = AVType.VIDEO,
                offlineDownloadAllowed = false,
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription =
                    "Edited Title & edited Description.\n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla pellentesque" +
                        " magna quam, vel egestas magna lacinia vel. Sed ullamcorper tortor eget elit tincidunt," +
                        " non vestibulum tellus mattis. Nullam diam nisi, dapibus eget lacinia at, eleifend et justo." +
                        " Nulla bibendum velit id felis condimentum aliquam. Fusce nulla magna, suscipit sit amet velit et, " +
                        "condimentum tempor arcu. Fusce sollicitudin est sed eros sodales, eu bibendum ante eleifend. " +
                        "In eget vehicula ex. Pellentesque dictum ipsum vel odio vestibulum pulvinar. Etiam faucibus " +
                        "vel sem eu efficitur. Sed ultrices eleifend interdum. In malesuada consequat felis, congue" +
                        " posuere ex sollicitudin ac. Sed pharetra facilisis est rhoncus consectetur. Sed quam diam, " +
                        "sagittis ut nulla a, rutrum condimentum augue. In nec porttitor quam.\n" +
                        "\n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus aliquet, elit maximus " +
                        "lacinia bibendum, sem odio laoreet est, vel facilisis mi odio a libero. Fusce commodo augue " +
                        "quis turpis lacinia, non consequat libero malesuada. Pellentesque habitant morbi tristique" +
                        " senectus et netus et malesuada fames ac turpis egestas. Etiam eleifend dui lacinia sapien " +
                        "lacinia, at eleifend sem eleifend. Curabitur ullamcorper viverra elit at ultricies. Duis " +
                        "consectetur quis dui sed aliquam. Vivamus sem lorem, pharetra gravida purus in, scelerisque" +
                        " euismod magna. Donec quis lorem congue, pellentesque turpis ac, dictum nulla. Maecenas " +
                        "accumsan diam massa, ut dignissim dolor pellentesque nec. Nunc ut neque eu neque condimentum" +
                        " interdum. Etiam eget ante enim.\n" +
                        "\n" +
                        "Praesent eget velit non eros interdum molestie ac in nibh. Aliquam laoreet ut justo eu" +
                        " consequat. Nam id sagittis ante. Nulla venenatis efficitur commodo. Curabitur nec fringilla " +
                        "urna. Phasellus non felis ut mauris lobortis ultrices. Aenean pulvinar, nunc et fermentum " +
                        "cursus, ipsum eros tristique felis, eu convallis leo ante in velit.\n" +
                        "\n" +
                        "Vivamus vel hendrerit lectus, non pellentesque eros. Duis et sagittis ligula. Fusce vitae " +
                        "ex a lectus sagittis imperdiet eu ut orci. Donec at lectus eget nulla rhoncus faucibus " +
                        "quis ac erat. Curabitur metus sem, dictum ac lacus sed, cursus molestie leo. Quisque " +
                        "euismod convallis posuere. Integer in lacus eget risus finibus vulputate. Suspendisse " +
                        "blandit urna risus, vel scelerisque ipsum dictum id.\n" +
                        "\n" +
                        "Praesent sed posuere metus. Interdum et malesuada fames ac ante ipsum primis in faucibus. " +
                        "In maximus scelerisque dolor, in molestie arcu fermentum ornare. Proin placerat viverra " +
                        "diam, eget egestas enim lobortis vitae. Duis dapibus pharetra augue, eget varius nisi " +
                        "auctor quis. Proin non turpis et libero vulputate aliquet. Pellentesque condimentum, eros " +
                        "aliquam tincidunt pretium, nibh sapien tincidunt nisl, quis fermentum nisl dolor eu mi. Sed " +
                        "eu condimentum leo, ut sagittis leo. Curabitur semper dictum augue, a mattis mi molestie " +
                        "ultrices. Sed fringilla sem velit, non luctus nunc condimentum eget. Mauris gravida auctor " +
                        "arcu in faucibus. Proin congue elit leo, vel facilisis turpis tempus non. Integer iaculis " +
                        "eget magna eu maximus. Vivamus placerat libero sed sem tincidunt rhoncus.",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                overrideStreamLinkTitleAndDescription = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = null,
                testingDescription = "No Title & editedDescription",
                uniqueId = "AT_300002877",
                startOffset = 10 * 60.0,
                getStreamLink = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                overrideStreamLinkTitleAndDescription = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Flikken Maastricht: AT_300002877 ",
                testingDescription = "NICAM information 1",
                uniqueId = "AT_300002877",
                getStreamLink = true,
                imageUrl = "https://www.assets.avrotros.nl/user_upload/_processed_/f/a/csm_Flikken-Maastricht-1280-quiz_2279c2e700.jpg",
                preferThisImageUrlOverStreamLink = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246",
                testingDescription = "NICAM information 2",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                offlineDownloadAllowed = true,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246",
                testingDescription = "Override NICAM information",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg",
                avType = AVType.VIDEO,
                overrideNicamContentDescription =
                    MyNicamContentDescription(
                        ageRating = Nicam.Age.AGE_TOUS,
                        warnings = listOf(Nicam.Warning.UNRATED),
                    ),
            ),
            SourceWrapper(
                title = "Teledoc Campus: AT_2031723",
                testingDescription = "Choose subtitling language",
                uniqueId = "AT_2031723",
                getStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/608874",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Op1 - POMS_BV_20012834",
                testingDescription = "Segment 1 (POMS)",
                uniqueId = "POMS_BV_20012834",
                getStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1812849",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Op1 POMS_BV_20012835",
                testingDescription = "Segment 2 (POMS)",
                uniqueId = "POMS_BV_20012835",
                getStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1812845",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "NOS Noord-, Zuidlijn: POW_03900426",
                testingDescription = "Pre-roll ad (STER)",
                uniqueId = "POW_03900426",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1071323",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Jacobine op 2: KN_1728228",
                testingDescription = "Pre-roll ad (STER) - 2 Ads",
                uniqueId = "KN_1728228",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/1724048",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "POW_05216736 - Age restriction",
                testingDescription = "As START user should give error warning. As PLUS user should play.",
                uniqueId = "POW_05216736",
                getStreamLink = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Radio 1: LI_RA1_8167349",
                testingDescription = "Playback Audio",
                uniqueId = "LI_RA1_8167349",
                getStreamLink = true,
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Radio 1: LI_RA1_8167349",
                testingDescription = "Playback Audio - startPos 10 minutes ago",
                uniqueId = "LI_RA1_8167349",
                getStreamLink = true,
                startOffset = -(10 * 60.0),
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Nederland Zingt: VPWON_1336246",
                testingDescription = "Playback VOD (No DRM)",
                uniqueId = "VPWON_1336246",
                getStreamLink = true,
                imageUrl = "https://nederlandzingt-eo.cdn.eo.nl/w_1260/4ofj4w3bqf8w-feest.jpg",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Karen Pirie: POW_05275080",
                testingDescription = "",
                uniqueId = "POW_05275080",
                getStreamLink = true,
                imageUrl = "https://assets-start.npo.nl/resources/2023/06/30/30260132-88aa-48f6-9838-ba1397e6475c.jpg?dimensions=900x900",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Radio 2: LI_RA2_8167353",
                testingDescription = "",
                uniqueId = "LI_RA2_8167353",
                getStreamLink = true,
                imageUrl = "https://www.npoluister.nl/assets/images/omroepen/logo-radio2.png",
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Sterren NL: LI_RA2_837085",
                testingDescription = "",
                uniqueId = "LI_RA2_837085",
                getStreamLink = true,
                imageUrl = "https://www.nporadio5.nl/sterrennl/images/blue-diamonds.webp",
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Soul&Jazz: LI_RA6_837069",
                testingDescription = "",
                uniqueId = "LI_RA6_837069",
                getStreamLink = true,
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "NOS Journaal: POW_05467390",
                testingDescription = "",
                uniqueId = "POW_05467390",
                getStreamLink = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Visual Radio 3FM: LI_3FM_300881",
                testingDescription = "",
                uniqueId = "LI_3FM_300881",
                getStreamLink = true,
                avType = AVType.VIDEO,
                isLive = true,
            ),
            SourceWrapper(
                title = "Radio 3FM: LI_3FM_8167356",
                testingDescription = "",
                uniqueId = "LI_3FM_8167356",
                getStreamLink = true,
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "FUNX ICECAST: LI_FUNX_837073",
                testingDescription = "",
                uniqueId = "LI_FUNX_837073",
                getStreamLink = true,
                avType = AVType.AUDIO,
                isLive = true,
            ),
            SourceWrapper(
                title = "De Laatste Walvisvaarders: PREPR_RA1_17306750",
                testingDescription = "",
                uniqueId = "PREPR_RA1_17306750",
                getStreamLink = true,
                offlineDownloadAllowed = true,
                imageUrl = "https://images.poms.omroep.nl/image/s1072/c1072x603/s1072x603>/2012559.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Wie is de Mol - Seizoen 2024 - Afl. 3",
                testingDescription = "",
                uniqueId = "AT_300010866",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/2070960",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Wie is de Mol - Seizoen 2024 - Afl. 9",
                testingDescription = "Crash because of WebVTT",
                uniqueId = "AT_300010872",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s1080/2070960",
                avType = AVType.VIDEO,
            ),
        )
    }

    override suspend fun getSourceList() = streamLinkSourceList
}
