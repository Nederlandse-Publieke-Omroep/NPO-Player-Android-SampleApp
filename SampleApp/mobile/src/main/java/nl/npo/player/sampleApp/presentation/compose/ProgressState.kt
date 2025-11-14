package nl.npo.player.sampleApp.presentation.compose

import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.shared.model.SourceWrapper

data class ProgressState(
    val list: List<SourceWrapper>?,
    val state: NPODownloadState?
)
