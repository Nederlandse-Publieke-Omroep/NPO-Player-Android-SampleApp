package nl.npo.player.sampleApp.data.link

import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.sampleApp.domain.LinkRepository
import nl.npo.player.sampleApp.domain.annotation.URLLinkRepository
import nl.npo.player.sampleApp.model.SourceWrapper

@URLLinkRepository
object URLLinkDataRepository : LinkRepository {
    private val urlLinkSourceList: List<SourceWrapper> by lazy {
        listOf(
            SourceWrapper(
                title = "SHATTERED: DOLBY ATMOS – H.264",
                streamUrl = "https://media.developer.dolby.com/Atmos/MP4/shattered-3Mb.mp4",
                uniqueId = "shattered-3Mb.mp4",
                getStreamLink = false,
                offlineDownloadAllowed = true,
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "ANNE+GIJS",
                testingDescription = "Custom URL playback",
                streamUrl = "https://podcast.npo.nl/file/anne/29506/annegijs.mp3",
                uniqueId = "29506/annegijs.mp3",
                getStreamLink = false,
                offlineDownloadAllowed = true,
                imageUrl = "https://podcast.npo.nl/data/thumb/anne.300.1568c637575040b86689f72716ca9a16.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Ezelsoren - Kun je `alles` weten?",
                streamUrl = "https://podcast.npo.nl/file/ezelsoren/48383/kun-je-alles-weten.mp3",
                uniqueId = "48383/kun-je-alles-weten.mp3",
                getStreamLink = false,
                offlineDownloadAllowed = true,
                imageUrl = "https://podcast.npo.nl/data/thumb/ezelsoren.300.260c12b1df81859381eddb8418fa43a9.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Bolletje en pluisje - De Wind - (Start 60 seconds in)",
                streamUrl = "https://podcast.npo.nl/file/bolletje-en-pluisje/38849/de-wind.mp3",
                uniqueId = "38849/de-wind.mp3",
                getStreamLink = false,
                offlineDownloadAllowed = true,
                startOffset = 60.0,
                imageUrl = "https://podcast.npo.nl/data/thumb/bolletje-en-pluisje.300.5d9d8ccc6281bf467eb4f07f532154f5.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Vroeg! - The red pill community",
                streamUrl = "https://entry.cdn.npoaudio.nl/handle/WO_BV_20051665.mp3",
                uniqueId = "WO_BV_20051665",
                getStreamLink = false,
                offlineDownloadAllowed = true,
                imageUrl = "https://podcast.npo.nl/data/thumb/vroeg.300.55e145fb429eaf2239e89148ef811c82.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "LOCAL CONTENT: Zin in Zappelin!",
                streamUrl = "file:///android_asset/poms_at_16803685_v1643638099.mp3",
                uniqueId = "AT_16803685",
                getStreamLink = false,
                imageUrl = "https://podcast.npo.nl/data/thumb/zin-in-zappelin.300.01f147069806c59cd40c8b5aae360371.jpg",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "FUNX - Icecast stream",
                streamUrl = "https://icecast.omroep.nl/funx-amsterdam-bb-mp3",
                uniqueId = "funx-amsterdam-bb-mp3",
                getStreamLink = false,
                imageUrl =
                    "https://cms-assets.nporadio.nl/npoFunx/_600x515_crop_center-center_none/" +
                        "FunX_FMA23_Header_Desktop.jpg?v=1679646843",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "FUNX Arab - Icecast stream",
                streamUrl = "https://icecast.omroep.nl/funx-arab-bb-mp3",
                uniqueId = "funx-arab-bb-mp3",
                getStreamLink = false,
                imageUrl =
                    "https://cms-assets.nporadio.nl/npoFunx/_600x515_crop_center-center_none/" +
                        "FunX_FMA23_Header_Desktop.jpg?v=1679646843",
                avType = AVType.AUDIO,
            ),
            SourceWrapper(
                title = "Piepshow - Test video URL",
                streamUrl =
                    "https://cdn-eu.muse.ai/u/Czi97La/96c9d74a2b0dd09622557a00a9bcd4a99b480e97aff1f86261f0ca18b0902ccc/" +
                        "videos/video.mp4",
                uniqueId = "piepshow-mp4",
                getStreamLink = false,
                imageUrl =
                    "https://content.radioveronica.nl/images/66qysnomiif1/3Cy1DKrJZpB4uq4CHxA5d1/" +
                        "31a2820a631bdea44fe6bdcfd054ec68/Radio-Veronica_Martijn-Muijs_1200x675.jpg?fit=thumb&w=774&h=465&fm=webp",
                avType = AVType.VIDEO,
            ),
            SourceWrapper(
                title = "Bitmovin/Dashif sample livestream",
                streamUrl = "https://livesim.dashif.org/livesim2/tsbd_600/testpic_2s/Manifest.mpd",
                uniqueId = "testpic_2s",
                getStreamLink = false,
                imageUrl =
                    "https://content.radioveronica.nl/images/66qysnomiif1/3Cy1DKrJZpB4uq4CHxA5d1/" +
                        "31a2820a631bdea44fe6bdcfd054ec68/Radio-Veronica_Martijn-Muijs_1200x675.jpg?fit=thumb&w=774&h=465&fm=webp",
                avType = AVType.VIDEO,
            ),
        )
    }

    override suspend fun getSourceList(): List<SourceWrapper> = urlLinkSourceList
}
