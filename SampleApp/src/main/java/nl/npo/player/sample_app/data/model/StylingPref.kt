package nl.npo.player.sample_app.data.model

import nl.npo.player.sample_app.domain.model.Styling
import nl.npo.player.sample_app.presentation.settings.model.SettingsPickerOption

enum class StylingPref(override val key: String) : SettingsPickerOption {
    Default("default"),
    Custom("custom");

    fun toDomain() = when (this) {
        Default -> Styling.Default
        Custom -> Styling.Custom
    }

    companion object {
        fun getByKey(key: String): StylingPref {
            return values().firstOrNull { it.key == key } ?: Default
        }
    }
}

fun Styling.toPref(): StylingPref = when (this) {
    Styling.Default -> StylingPref.Default
    Styling.Custom -> StylingPref.Custom
}
