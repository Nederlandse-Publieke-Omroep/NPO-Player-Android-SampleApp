package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.sampleApp.shared.domain.model.UserType
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption

enum class UserTypePref(
    override val key: String,
) : SettingsPickerOption {
    Start("start"),
    Plus("plus"),
    ;

    fun toDomain(): UserType =
        when (this) {
            Start -> UserType.Start
            Plus -> UserType.Plus
        }

    companion object {
        fun getByKey(key: String): UserTypePref? = entries.firstOrNull { it.key == key }
    }
}

fun UserType.toPref(): UserTypePref =
    when (this) {
        UserType.Start -> UserTypePref.Start
        UserType.Plus -> UserTypePref.Plus
    }
