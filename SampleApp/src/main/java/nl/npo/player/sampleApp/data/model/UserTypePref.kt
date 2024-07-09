package nl.npo.player.sampleApp.data.model

import nl.npo.player.sampleApp.domain.model.UserType
import nl.npo.player.sampleApp.presentation.settings.model.SettingsPickerOption

enum class UserTypePref(override val key: String) : SettingsPickerOption {
    Start("start"),
    Plus("plus"),
    ;

    fun toDomain(): UserType =
        when (this) {
            Start -> UserType.Start
            Plus -> UserType.Plus
        }

    companion object {
        fun getByKey(key: String): UserTypePref {
            return values().firstOrNull { it.key == key } ?: Start
        }
    }
}

fun UserType.toPref(): UserTypePref =
    when (this) {
        UserType.Start -> UserTypePref.Start
        UserType.Plus -> UserTypePref.Plus
    }
