package nl.npo.player.sample_app.data.model

import nl.npo.player.sample_app.domain.model.UserType

enum class UserTypePref(val key: String) {
    Start("start"),
    Plus("plus");

    companion object {
        fun getByKey(key: String): UserTypePref {
            return values().firstOrNull { it.key == key } ?: Start
        }
    }
}

fun UserTypePref.toDomain(): UserType = when (this) {
    UserTypePref.Start -> UserType.Start
    UserTypePref.Plus -> UserType.Plus
}

fun UserType.toPref(): UserTypePref = when (this) {
    UserType.Start -> UserTypePref.Start
    UserType.Plus -> UserTypePref.Plus
}
