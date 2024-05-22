package nl.npo.player.sample_app.data.link

import kotlinx.coroutines.flow.first
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.common.enums.AVType
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.offline.NPOOfflineContentManager
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sample_app.domain.LinkRepository
import nl.npo.player.sample_app.domain.SettingsRepository
import nl.npo.player.sample_app.domain.TokenProvider
import nl.npo.player.sample_app.domain.annotation.OfflineLinkRepository
import nl.npo.player.sample_app.domain.model.StreamInfoResult
import nl.npo.player.sample_app.domain.model.UserType
import nl.npo.player.sample_app.model.SourceWrapper
import javax.inject.Inject

@OfflineLinkRepository
class OfflineContentDataRepository @Inject constructor(
    private val npoOfflineContentManager: NPOOfflineContentManager,
    private val tokenProvider: TokenProvider,
    private val settingsRepository: SettingsRepository
) : LinkRepository.OfflineLinkRepository {
    override suspend fun getSourceList(): List<SourceWrapper> {
        return npoOfflineContentManager.getAll().map { offlineContent ->
            SourceWrapper(
                npoSourceConfig = null,
                uniqueId = offlineContent.uniqueId,
                getStreamLink = false,
                title = offlineContent.getOriginalSource().title ?: offlineContent.uniqueId,
                npoOfflineContent = offlineContent
            )
        }
    }

    @Throws(NPOOfflineContentException::class)
    override suspend fun createOfflineContent(sourceWrapper: SourceWrapper): NPOOfflineContent {
        val npoSource = sourceWrapper.npoSourceConfig ?: run {
            val isPlusUser = settingsRepository.userType.first() == UserType.Plus
            when (
                val result = tokenProvider.createToken(
                    sourceWrapper.uniqueId, isPlusUser
                )
            ) {
                is StreamInfoResult.Success -> {
                    try {
                        NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(result.data.token))
                    } catch (throwable: NPOPlayerException) {
                        throwable.printStackTrace()
                        throw NPOOfflineContentException.IOException(
                            "StreamLink retrieval failed", cause = throwable
                        )
                    }
                }

                is StreamInfoResult.Error -> throw NPOOfflineContentException.IOException(
                    "JWT retrieval failed", cause = result.exception
                )
            }
        }
        if (npoSource.isDownloadDisallowed()) throw NPOOfflineContentException.OfflineNotAllowed(
            npoSource.getDownloadNotAllowedReason()
        )
        return npoOfflineContentManager.create(npoSource)
    }

    override suspend fun deleteOfflineContent(npoOfflineContent: NPOOfflineContent) {
        npoOfflineContent.delete()
    }

    private fun NPOSourceConfig.isDownloadDisallowed(): Boolean =
        (isLiveStream == true || avType == AVType.VIDEO)

    private fun NPOSourceConfig.getDownloadNotAllowedReason(): String {
        return when {
            isLiveStream == true -> "A live stream can't be downloaded"
            avType == AVType.VIDEO -> "(NPO StreamLink) Video's aren't allowed to be downloaded"
            else -> "Unknown reason"
        }
    }
}
