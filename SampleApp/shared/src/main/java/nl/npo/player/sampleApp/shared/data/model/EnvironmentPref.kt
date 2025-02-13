package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

enum class EnvironmentPref(
    override val key: String,
) : SettingsPickerOption {
    Test("test"),
    Acceptance("acceptance"),
    Production("production"),
    ;

    fun toDomain() =
        when (this) {
            Test -> Environment.Test
            Acceptance -> Environment.Acceptance
            Production -> Environment.Production
        }

    companion object {
        fun getByKey(key: String): EnvironmentPref = entries.firstOrNull { it.key == key } ?: Production
    }
}

fun Environment.toPref(): EnvironmentPref =
    when (this) {
        Environment.Test -> EnvironmentPref.Test
        Environment.Acceptance -> EnvironmentPref.Acceptance
        Environment.Production -> EnvironmentPref.Production
    }
