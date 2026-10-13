package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.library.presentation.model.OfflineContentQuality
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

typealias DownloadQualityInt = Int

enum class DownloadQualityPref(
    override val key: String,
) : SettingsPickerOption {
    HIGHEST("0"),
    LOWEST("1"),
    ;

    fun toDomain() =
        when (this) {
            HIGHEST -> OfflineContentQuality.HIGHEST
            LOWEST -> OfflineContentQuality.LOWEST
        }
}

fun DownloadQualityInt.toDQPref(): DownloadQualityPref =
    when (this) {
        OfflineContentQuality.HIGHEST.ordinal -> DownloadQualityPref.HIGHEST
        OfflineContentQuality.LOWEST.ordinal -> DownloadQualityPref.LOWEST
        else -> DownloadQualityPref.HIGHEST
    }
