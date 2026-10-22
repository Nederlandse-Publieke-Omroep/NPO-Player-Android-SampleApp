package nl.npo.player.sampleApp.presentation.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.npo.player.library.domain.exception.NPOOfflineContentException
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.library.domain.offline.models.NPOOfflineContent
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseHelper
import nl.npo.player.library.domain.offline.models.NPOOfflineLicenseState
import nl.npo.player.sampleApp.shared.domain.LinkRepository
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import kotlin.time.Duration.Companion.seconds

data class DownloadableItem(
    val sourceWrapper: SourceWrapper,
    private val viewmodelScope: CoroutineScope,
    private val offlineLinkRepository: LinkRepository.OfflineLinkRepository,
) {
    private val _offlineContent = MutableStateFlow(value = sourceWrapper.npoOfflineContent)
    val offlineContent: StateFlow<NPOOfflineContent?> = _offlineContent

    private val _downloadState: MutableStateFlow<NPODownloadState?> = MutableStateFlow(value = null)
    val downloadState: StateFlow<NPODownloadState?> = _downloadState

    private val _drmLicenseState: MutableStateFlow<NPOOfflineLicenseState?> =
        MutableStateFlow(value = null)
    val drmLicenseState: StateFlow<NPOOfflineLicenseState?> = _drmLicenseState

    private val _drmLicenseExpiration =
        MutableStateFlow<NPOOfflineLicenseHelper.DRMLicenseExpiration?>(null)
    val drmLicenseExpiration: StateFlow<NPOOfflineLicenseHelper.DRMLicenseExpiration?> =
        _drmLicenseExpiration

    private var pendingCreation: String? = null

    private var deleting: Boolean = false

    init {
        viewmodelScope.launch {
            drmLicenseState.collect { licenseState ->
                if (licenseState == null) return@collect

                when (licenseState) {
                    is NPOOfflineLicenseState.Finished -> {
                        val newExpiration = _offlineContent.value?.getOfflineDRMLicenseExpiration()
                        _drmLicenseExpiration.emit(newExpiration)
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
        startDownloadStateJob()
        startOfflineLicenceJob()
    }

    fun onItemClicked(
        onClick: (SourceWrapper) -> Unit,
        error: (Throwable) -> Unit,
        drmLicenseExpiration: NPOOfflineLicenseHelper.DRMLicenseExpiration?,
    ) {
        viewmodelScope.launch {
            val offlineContent = retrieveCurrentOfflineContent()
            _offlineContent.tryEmit(offlineContent)
            if (offlineContent == null || offlineContent.downloadState.value is NPODownloadState.Deleted) {
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
                    handleFinishedDownload(offlineContent, drmLicenseExpiration, onClick)
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
        onCreated: (NPOOfflineContent, exited: Boolean) -> Unit = { _, _ -> },
        errorCallback: (Throwable) -> Unit,
    ) {
        val id = sourceWrapper.uniqueId

        // Already creating for this id: ignore the extra tap.
        if (pendingCreation == id) return

        viewmodelScope.launch {
            // Content already exists (e.g. list hasn't recomposed yet): resume it
            // instead of creating a duplicate download.
            val existing = retrieveCurrentOfflineContent()
            if (existing != null) {
                deleting = existing.downloadState.value is NPODownloadState.Deleting
                pendingCreation = null
                _offlineContent.tryEmit(existing)
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
            _offlineContent.tryEmit(offlineContent)
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

    private fun startDownloadStateJob() {
        var downloadStateJob: Job? = null
        viewmodelScope.launch {
            offlineContent.collect { content ->
                if (content == null) {
                    val previousDownloadState = _downloadState.value
                    downloadStateJob?.cancel()
                    downloadStateJob = null
                    _downloadState.tryEmit(null)
                    return@collect
                }
                if (downloadStateJob == null) {
                    downloadStateJob =
                        viewmodelScope.launch {
                            content.downloadState.collect { newDownloadState ->
                                _downloadState.tryEmit(newDownloadState)
                                if (deleting && newDownloadState is NPODownloadState.Deleted) {
                                    // Removing of offline content is complete. Reset offline content
                                    _offlineContent.tryEmit(null)
                                    deleting = false
                                }
                            }
                        }
                }
            }
        }
    }

    private fun startOfflineLicenceJob() {
        var offlineLicenseStateJob: Job? = null

        viewmodelScope.launch {
            offlineContent.collect { content ->
                if (content == null) {
                    offlineLicenseStateJob?.cancel()
                    offlineLicenseStateJob = null
                    _drmLicenseState.tryEmit(null)
                    return@collect
                }

                if (offlineLicenseStateJob == null) {
                    offlineLicenseStateJob =
                        viewmodelScope.launch {
                            content.offlineLicenseState.collect {
                                _drmLicenseState.tryEmit(it)
                            }
                        }
                }
            }
        }
    }

    private suspend fun retrieveCurrentOfflineContent(): NPOOfflineContent? =
        offlineLinkRepository
            .getSourceList()
            .firstOrNull { it.uniqueId == sourceWrapper.uniqueId }
            ?.npoOfflineContent

    private fun handleFinishedDownload(
        offlineContent: NPOOfflineContent,
        drmLicenseExpiration: NPOOfflineLicenseHelper.DRMLicenseExpiration?,
        onClick: (SourceWrapper) -> Unit,
    ) {
        val hasExpiredDrmLicense =
            offlineContent.getOriginalSource().drm != null &&
                drmLicenseExpiration?.drmExpiration?.isPositive() != true

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
}
