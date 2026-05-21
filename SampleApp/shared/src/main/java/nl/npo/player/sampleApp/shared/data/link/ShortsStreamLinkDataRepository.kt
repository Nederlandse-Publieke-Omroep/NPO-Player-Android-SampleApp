package nl.npo.player.sampleApp.shared.data.link

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@StreamLinkRepository
object ShortsStreamLinkDataRepository : LinkRepository {
    private val streamLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "short 1",
                testingDescription = "WO_ZAPP_20338724",
                uniqueId = "WO_ZAPP_20338724",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                preferThisImageUrlOverStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "short 2",
                testingDescription = "WO_ZAPP_20338725 - 20 seconds startOffset",
                uniqueId = "WO_ZAPP_20338725",
                getStreamLink = true,
                startOffset = 20.0,
                offlineDownloadAllowed = false,
                preferThisImageUrlOverStreamLink = true,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2299180",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "short 3",
                testingDescription = "WO_ZAPP_20338667",
                uniqueId = "WO_ZAPP_20338667",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                preferThisImageUrlOverStreamLink = true,
                imageUrl =
                    "https://assets.production.zapp.nl/attachments/series/000/002/060/" +
                        "cover/Recordbrekers_2026_NPO_Zapp.jpg?1761818775",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 1",
                testingDescription = "WO_NPO_20342819",
                uniqueId = "WO_NPO_20342819",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 2",
                testingDescription = "WO_NPO_20342786",
                uniqueId = "WO_NPO_20342786",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 3",
                testingDescription = "WO_NPO_20342785",
                uniqueId = "WO_NPO_20342785",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 4",
                testingDescription = "WO_NPO_20342784",
                uniqueId = "WO_NPO_20342784",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 5",
                testingDescription = "WO_NPO_20342783",
                uniqueId = "WO_NPO_20342783",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 6",
                testingDescription = "WO_NPO_20342782",
                uniqueId = "WO_NPO_20342782",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 7",
                testingDescription = "WO_NPO_20342781",
                uniqueId = "WO_NPO_20342781",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 8",
                testingDescription = "WO_NPO_20342780",
                uniqueId = "WO_NPO_20342780",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 9",
                testingDescription = "WO_NPO_20342779",
                uniqueId = "WO_NPO_20342779",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 10",
                testingDescription = "WO_NPO_20342753",
                uniqueId = "WO_NPO_20342753",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "new short 11",
                testingDescription = "WO_NPO_20342778",
                uniqueId = "WO_NPO_20342778",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
        )
    }

    override suspend fun getSourceList() = streamLinkSourceList
}
