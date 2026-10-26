package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.domain.model.CastReceiver
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

enum class CastReceiverPref(
    override val key: String,
) : SettingsPickerOption {
    V1Production("v1_production"),
    V2Production("v2_production"),
    V2Test("v2_test"),
    ;

    fun toDomain() =
        when (this) {
            V1Production -> CastReceiver.V1Production
            V2Production -> CastReceiver.V2Production
            V2Test -> CastReceiver.V2Test
        }

    companion object {
        fun getByKey(key: String): CastReceiverPref? = entries.firstOrNull { it.key == key }
    }
}

fun CastReceiver.toPref(): CastReceiverPref =
    when (this) {
        CastReceiver.V1Production -> CastReceiverPref.V1Production
        CastReceiver.V2Production -> CastReceiverPref.V2Production
        CastReceiver.V2Test -> CastReceiverPref.V2Test
    }
