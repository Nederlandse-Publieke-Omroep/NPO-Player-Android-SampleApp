package nl.npo.player.sampleApp.presentation.model

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadState: StateFlow<NPODownloadState?> =
        offlineContent.flatMapLatest { it?.downloadState ?: flowOf(null) }.stateIn(
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

    private val _drmLicenseExpiration =
        MutableStateFlow<NPOOfflineLicenseHelper.DRMLicenseExpiration?>(null)
    val drmLicenseExpiration: StateFlow<NPOOfflineLicenseHelper.DRMLicenseExpiration?> =
        _drmLicenseExpiration

    private var pendingCreation: String? = null

    private var deleting: Boolean = false

    init {
        viewmodelScope.launch {
            replaceOfflineContent(retrieveCurrentOfflineContent())
        }
        viewmodelScope.launch {
            drmLicenseState.collect { licenseState ->
                if (licenseState == null) return@collect

                when (licenseState) {
                    is NPOOfflineLicenseState.Finished -> {
                        val newExpiration = offlineContent.value?.getOfflineDRMLicenseExpiration()
                        _drmLicenseExpiration.emit(newExpiration)
                    }

                    is NPOOfflineLicenseState.Failed -> {
                        Log.d(
                            "SampleAppTest",
                            "Download of DRM license failed for ${sourceWrapper.uniqueId}",
                            licenseState.throwable,
                        )
                    }

                    else -> {
                        _drmLicenseExpiration.tryEmit(null)
                    }
                }
            }
        }
        viewmodelScope.launch {
            drmLicenseExpiration.collect { licenseExpiration ->
                if (licenseExpiration != null) {
                    delay(5.seconds)
                    if (!deleting) {
                        val newExpiration = offlineContent.value?.getOfflineDRMLicenseExpiration()
                        _drmLicenseExpiration.tryEmit(newExpiration)
                    } else {
                        _drmLicenseExpiration.tryEmit(null)
                    }
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
        viewmodelScope.launch {
            val offlineContent = offlineContent.value
            if (offlineContent == null) {
                createOrGetOfflineContent(
                    onCreated = { created, existed ->
                        if (existed) created.startOrResumeDownload()
                    },
                ) { throwable ->
                    error(throwable)
                }
                return@launch
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
    }

    fun createOrGetOfflineContent(
        onCreated: (NPOOfflineContent, existed: Boolean) -> Unit = { _, _ -> },
        errorCallback: (Throwable) -> Unit,
    ) {
        val id = sourceWrapper.uniqueId

        // Already creating for this id: ignore the extra tap.
        if (pendingCreation == id) return
        pendingCreation = id

        viewmodelScope.launch {
            // Content already exists (e.g. list hasn't recomposed yet): resume it
            // instead of creating a duplicate download.
            val existing = retrieveCurrentOfflineContent()
            if (existing != null) {
                deleting = existing.downloadState.value is NPODownloadState.Deleting
                pendingCreation = null
                replaceOfflineContent(existing)
                onCreated(existing, true)
                return@launch
            }

            val offlineContent =
                try {
                    offlineLinkRepository.createOfflineContent(sourceWrapper)
                } catch (e: NPOOfflineContentException) {
                    pendingCreation = null
                    errorCallback(e)
                    return@launch
                }
            deleting = false
            pendingCreation = null
            replaceOfflineContent(offlineContent)
            onCreated(offlineContent, false)
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
                _drmLicenseExpiration.value?.drmExpiration?.isPositive() != true

        if (!hasExpiredDrmLicense) {
            onClick(sourceWrapper.copy(npoOfflineContent = offlineContent))
            return
        }

        viewmodelScope.launch {
            try {
                offlineContent.renewOfflineDRMLicense()
            } catch (e: Exception) {
                error(e)
            }
        }
    }

    private suspend fun replaceOfflineContent(newOfflineContent: NPOOfflineContent?) {
        val oldOfflineContent = _offlineContent.value
        _offlineContent.emit(newOfflineContent)
        oldOfflineContent?.release()
    }
}
