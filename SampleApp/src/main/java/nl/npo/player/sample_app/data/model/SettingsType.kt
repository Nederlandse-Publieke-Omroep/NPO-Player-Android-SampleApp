package nl.npo.player.sample_app.data.model

import nl.npo.player.sample_app.domain.model.SettingsType
import nl.npo.player.sample_app.presentation.settings.model.SettingsPickerOption

enum class SettingsTypePref(override val key: String): SettingsPickerOption {
    Default("default"),
    Custom("custom");

    fun toDomain() = when(this) {
        Default -> SettingsType.Default
        Custom -> SettingsType.Custom
    }

    companion object {
        fun getByKey(key: String): SettingsTypePref {
            return values().firstOrNull { it.key == key } ?: Default
        }
    }
}

fun SettingsType.toPref(): SettingsTypePref = when(this) {
    SettingsType.Default -> SettingsTypePref.Default
    SettingsType.Custom -> SettingsTypePref.Custom
}
