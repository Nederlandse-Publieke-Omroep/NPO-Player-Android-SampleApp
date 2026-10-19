package nl.npo.player.sampleApp.shared.data.player

import kotlinx.coroutines.flow.first
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.data.extensions.toNPOPlayerError
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.model.StreamRetrievalState
import javax.inject.Inject

class StreamSourceResolver
    @Inject
    constructor(
        private val tokenProvider: TokenProvider,
        private val settingsRepository: SettingsRepository,
    ) {
        suspend fun resolve(sourceWrapper: SourceWrapper): StreamRetrievalState? {
            val isPlusUser =
                sourceWrapper.overrideIsPlusUser
                    ?: (settingsRepository.userType.first() == UserType.Plus)
            val token = createToken(sourceWrapper.uniqueId, isPlusUser) ?: return null

            return try {
                val source = NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
                val mergedSource =
                    sourceWrapper
                        .mergeSourceWrapperWithSource(source)
                        .copy(overrideAutoPlay = settingsRepository.autoPlayEnabled.first())
                StreamRetrievalState.Success(mergedSource, sourceWrapper)
            } catch (e: NPOPlayerException) {
                e.cause?.printStackTrace()
                StreamRetrievalState.Error(e.toNPOPlayerError(), sourceWrapper)
            }
        }

        private suspend fun createToken(
            itemId: String,
            isPlusUser: Boolean,
        ): String? =
            when (val tokenResult = tokenProvider.createToken(itemId, isPlusUser)) {
                is StreamInfoResult.Success -> {
                    tokenResult.data.token
                }

                else -> {
                    null
                }
            }
    }

internal fun SourceWrapper.mergeSourceWrapperWithSource(source: NPOSourceConfig): NPOSourceConfig =
    source.copy(
        overrideStartOffset = startOffset,
        overrideImageUrl = getImageUrl(source),
//                overrideMetadata =
//                    source.metadata
//                        ?.toMutableMap()
//                        ?.apply {
//                            set(
//                                "appletest",
//                                "true",
//                            )
//                        },
        // We add this so the Cast Receiver shows the debug log when casting.
        overrideTitle =
            if (overrideStreamLinkTitleAndDescription) {
                title
            } else {
                source.title
            },
        overrideDescription =
            if (overrideStreamLinkTitleAndDescription) {
                "SampleApp override description: $testingDescription"
            } else {
                source.description
            },
        overrideNicamContentDescription =
            overrideNicamContentDescription
                ?: source.nicamContentDescription,
    )

internal fun SourceWrapper.getImageUrl(npoSourceConfig: NPOSourceConfig): String? =
    if (preferThisImageUrlOverStreamLink) {
        imageUrl ?: npoSourceConfig.imageUrl
    } else {
        npoSourceConfig.imageUrl ?: imageUrl
    }
