package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

typealias AgeProfileInt = Int

enum class AgeProfilePref(
    override val key: String,
) : SettingsPickerOption {
    AGE_6("6"),
    AGE_9("9"),
    AGE_12("12"),
    AGE_14("14"),
    AGE_16("16"),
    AGE_18("18"),
    ;

    fun toDomain() =
        when (this) {
            AGE_6 -> 6
            AGE_9 -> 9
            AGE_12 -> 12
            AGE_14 -> 14
            AGE_16 -> 16
            AGE_18 -> 18
        }
}

fun AgeProfileInt.toPref(): AgeProfilePref =
    when (this) {
        6 -> AgeProfilePref.AGE_6
        9 -> AgeProfilePref.AGE_9
        12 -> AgeProfilePref.AGE_12
        14 -> AgeProfilePref.AGE_14
        16 -> AgeProfilePref.AGE_16
        18 -> AgeProfilePref.AGE_18
        else -> AgeProfilePref.AGE_18
    }
