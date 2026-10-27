package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

enum class CastReceiverPref(
    override val key: String,
    val applicationId: String,
) : SettingsPickerOption {
    V1Production("v1_production", "CC38EB17"),
    V2Production("v2_production", "123D5E9D"),
    V2Test("v2_test", "F1D230B4"),
    ;

    companion object {
        fun getByKey(key: String): CastReceiverPref? = entries.firstOrNull { it.key == key }
    }
}
