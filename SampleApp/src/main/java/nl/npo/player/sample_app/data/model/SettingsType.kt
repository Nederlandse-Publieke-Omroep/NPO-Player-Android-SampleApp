package nl.npo.player.sample_app.data.model

import nl.npo.player.sample_app.domain.model.SettingsType

enum class SettingsTypePref(val key: String) {
    Default("default"),
    Custom("custom");

    companion object {
        fun getByKey(key: String): SettingsTypePref {
            return values().firstOrNull { it.key == key } ?: Default
        }
    }
}

fun SettingsTypePref.toDomain(): SettingsType = when(this) {
    SettingsTypePref.Default -> SettingsType.Default
    SettingsTypePref.Custom -> SettingsType.Custom
}

fun SettingsType.toPref(): SettingsTypePref = when(this) {
    SettingsType.Default -> SettingsTypePref.Default
    SettingsType.Custom -> SettingsTypePref.Custom
}
