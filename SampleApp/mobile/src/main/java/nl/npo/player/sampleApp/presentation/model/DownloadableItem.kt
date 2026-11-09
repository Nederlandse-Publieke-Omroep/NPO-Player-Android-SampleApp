package nl.npo.player.sampleApp.presentation.model

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseHelper
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseState
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import kotlin.time.Duration.Companion.seconds

class DownloadableItem(
    val sourceWrapper: SourceWrapper,
    private val viewmodelScope: CoroutineScope,
    private val offlineLinkRepository: LinkRepository.OfflineLinkRepository,
) {
    private val _offlineContent = MutableStateFlow(value = sourceWrapper.npoOfflineContent)
    val offlineContent: StateFlow<NPOOfflineContent?> = _offlineContent

    private val pendingCreation = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadState: StateFlow<NPODownloadState?> =
        combine(
            offlineContent.flatMapLatest { it?.downloadState ?: flowOf(null) },
            pendingCreation,
        ) { state, pending ->
            state ?: NPODownloadState.Initializing.takeIf { pending }
        }.stateIn(
            viewmodelScope,
            SharingStarted.Lazily,
            null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val drmLicenseState: StateFlow<NPOOfflineLicenseState?> =
        offlineContent.flatMapLatest { it?.offlineLicenseState ?: flowOf(null) }.stateIn(
            viewmodelScope,
            SharingStarted.Lazily,
            null,
        )

    private var deleting: Boolean = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val drmLicenseExpiration: StateFlow<NPOOfflineLicenseHelper.DRMLicenseExpiration?> =
        drmLicenseState
            .flatMapLatest<NPOOfflineLicenseState?, NPOOfflineLicenseHelper.DRMLicenseExpiration?> { licenseState ->
                if (licenseState !is NPOOfflineLicenseState.Finished &&
                    licenseState !is NPOOfflineLicenseState.Failed
                ) {
                    return@flatMapLatest flowOf(null)
                }
                flow {
                    while (!deleting) {
                        emit(offlineContent.value?.getOfflineDRMLicenseExpiration())
                        delay(5.seconds)
                    }
                    emit(null)
                }
            }.stateIn(
                viewmodelScope,
                SharingStarted.WhileSubscribed(5_000),
                null,
            )

    init {
        viewmodelScope.launch {
            drmLicenseState.collect { licenseState ->
                if (licenseState is NPOOfflineLicenseState.Failed) {
                    Log.d(
                        "SampleAppTest",
                        "Download of DRM license failed for ${sourceWrapper.uniqueId}",
                        licenseState.throwable,
                    )
                }
            }
        }
        viewmodelScope.launch {
            downloadState.collect { newDownloadState ->
                if (deleting && newDownloadState is NPODownloadState.Deleted) {
                    // Removing of offline content is complete. Reset offline content
                    replaceOfflineContent(null)
                    deleting = false
                }
            }
        }
    }

    fun onItemClicked(
        onClick: (SourceWrapper) -> Unit,
        error: (Throwable) -> Unit,
    ) {
        val offlineContent = offlineContent.value
        if (offlineContent == null) {
            createOrGetOfflineContent(
                onCreated = { created, existed ->
                    if (existed) created.startOrResumeDownload()
                },
            ) { throwable ->
                error(throwable)
            }
            return
        }

        when (offlineContent.downloadState.value) {
            is NPODownloadState.Finished -> {
                handleFinishedDownload(offlineContent, onClick, error)
            }

            is NPODownloadState.Failed -> {
                // NOOP
            }

            is NPODownloadState.Paused -> {
                offlineContent.startOrResumeDownload()
            }

            is NPODownloadState.InProgress -> {
                offlineContent.pause()
            }

            is NPODownloadState.Deleting -> {
                // Deletion is already in progress; ignore taps until it settles.
            }

            is NPODownloadState.Deleted -> {
                // NOOP
            }

            NPODownloadState.Initializing -> {
                offlineContent.startOrResumeDownload()
            }
        }
    }

    fun createOrGetOfflineContent(
        onCreated: (NPOOfflineContent, existed: Boolean) -> Unit = { _, _ -> },
        errorCallback: (Throwable) -> Unit,
    ) {
        // Already creating: ignore the extra tap.
        if (pendingCreation.value) return
        pendingCreation.value = true

        viewmodelScope.launch {
            try {
                // Content already exists (e.g. list hasn't recomposed yet): resume it
                // instead of creating a duplicate download.
                val existing = retrieveCurrentOfflineContent()
                if (existing != null) {
                    deleting = existing.downloadState.value is NPODownloadState.Deleting
                    replaceOfflineContent(existing)
                    onCreated(existing, true)
                    return@launch
                }

                val offlineContent =
                    try {
                        offlineLinkRepository.createOfflineContent(sourceWrapper)
                    } catch (e: NPOOfflineContentException) {
                        errorCallback(e)
                        return@launch
                    }
                deleting = false
                replaceOfflineContent(offlineContent)
                onCreated(offlineContent, false)
            } finally {
                pendingCreation.value = false
            }
        }
    }

    fun deleteOfflineContent() {
        val content = offlineContent.value ?: return
        deleting = true
        viewmodelScope.launch(Dispatchers.IO) {
            offlineLinkRepository.deleteOfflineContent(content)
        }
    }

    /** Retry a previously failed download. */
    fun retryDownload(errorCallback: (Throwable) -> Unit) {
        // Resolve the *live* offline rather than trusting the (possibly stale) offline content captured in the event.
        viewmodelScope.launch {
            val content = retrieveCurrentOfflineContent()
            if (content != null) {
                content.startOrResumeDownload()
            } else {
                createOrGetOfflineContent(
                    onCreated = { created, existed ->
                        if (existed) created.startOrResumeDownload()
                    },
                    errorCallback = errorCallback,
                )
            }
        }
    }

    private suspend fun retrieveCurrentOfflineContent(): NPOOfflineContent? =
        offlineLinkRepository.getOfflineContent(sourceWrapper.uniqueId)

    private fun handleFinishedDownload(
        offlineContent: NPOOfflineContent,
        onClick: (SourceWrapper) -> Unit,
        error: (Throwable) -> Unit,
    ) {
        val hasExpiredDrmLicense =
            offlineContent.getOriginalSource().drm != null &&
                offlineContent.getOfflineDRMLicenseExpiration()?.drmExpiration?.isPositive() != true

        if (!hasExpiredDrmLicense) {
            onClick(sourceWrapper.copy(npoOfflineContent = offlineContent))
            return
        }

        try {
            offlineContent.renewOfflineDRMLicense()
        } catch (e: Exception) {
            error(e)
        }
    }

    private suspend fun replaceOfflineContent(newOfflineContent: NPOOfflineContent?) {
        val oldOfflineContent = _offlineContent.value
        _offlineContent.emit(newOfflineContent)
        oldOfflineContent?.release()
    }
}
