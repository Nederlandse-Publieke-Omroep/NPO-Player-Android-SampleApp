package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

enum class StylingPref(
    override val key: String,
) : SettingsPickerOption {
    Default("default"),
    Custom("custom"),
    ;

    fun toDomain() =
        when (this) {
            Default -> Styling.Default
            Custom -> Styling.Custom
        }

    companion object {
        fun getByKey(key: String): StylingPref? = entries.firstOrNull { it.key == key }
    }
}

fun Styling.toPref(): StylingPref =
    when (this) {
        Styling.Default -> StylingPref.Default
        Styling.Custom -> StylingPref.Custom
    }
