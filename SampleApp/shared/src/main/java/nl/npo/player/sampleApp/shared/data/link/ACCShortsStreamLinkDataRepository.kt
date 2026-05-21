package nl.npo.player.sampleApp.shared.data.link

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.domain.annotation.StreamLinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@StreamLinkRepository
object ACCShortsStreamLinkDataRepository : LinkRepository {
    private val streamLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "ACC short 1",
                testingDescription = "WO_NPO_A20077243",
                uniqueId = "WO_NPO_A20077243",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 2",
                testingDescription = "WO_NPO_A20077244",
                uniqueId = "WO_NPO_A20077244",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 3",
                testingDescription = "WO_NPO_A20077245",
                uniqueId = "WO_NPO_A20077245",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 4",
                testingDescription = "WO_NPO_A20077246",
                uniqueId = "WO_NPO_A20077246",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 5",
                testingDescription = "WO_NPO_A20077247",
                uniqueId = "WO_NPO_A20077247",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 6",
                testingDescription = "WO_NPO_A20077248",
                uniqueId = "WO_NPO_A20077248",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 7",
                testingDescription = "WO_NPO_A20077249",
                uniqueId = "WO_NPO_A20077249",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 8",
                testingDescription = "WO_NPO_A20077250",
                uniqueId = "WO_NPO_A20077250",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 9",
                testingDescription = "WO_NPO_A20077251",
                uniqueId = "WO_NPO_A20077251",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 10",
                testingDescription = "WO_NPO_A20077252",
                uniqueId = "WO_NPO_A20077252",
                getStreamLink = true,
                offlineDownloadAllowed = false,
                imageUrl = "https://images.poms.omroep.nl/image/s512/2276727",
                avType = AVType.VIDEO,
                isShort = true,
            ),
            SourceWrapper(
                title = "ACC short 11",
                testingDescription = "WO_NPO_A20077253",
                uniqueId = "WO_NPO_A20077253",
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
