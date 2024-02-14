package nl.npo.player.sample_app.data.model

import nl.npo.player.sample_app.domain.model.Styling

enum class StylingPref(val key: String) {
    Default("default"),
    Custom("custom");

    companion object {
        fun getByKey(key: String): StylingPref {
            return values().firstOrNull { it.key == key } ?: Default
        }
    }
}

fun StylingPref.toDomain(): Styling = when(this) {
    StylingPref.Default -> Styling.Default
    StylingPref.Custom -> Styling.Custom
}

fun Styling.toPref(): StylingPref = when(this) {
    Styling.Default -> StylingPref.Default
    Styling.Custom -> StylingPref.Custom
}
