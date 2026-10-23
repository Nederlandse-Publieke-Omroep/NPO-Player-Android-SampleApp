package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.library.presentation.model.OfflineContentQuality
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

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

    companion object {
        fun String.keyToPrefs(): DownloadQualityPref =
            when (this) {
                HIGHEST.key -> HIGHEST
                LOWEST.key -> LOWEST
                else -> HIGHEST
            }
    }
}
