package nl.npo.player.sample_app.presentation.settings

import androidx.annotation.StringRes

sealed class SettingsItem(open val key: String) {
    data class Switch(
        override val key: String,
        @StringRes val name: Int,
        val value: Boolean
    ): SettingsItem(key)

    data class Picker(
        override val key: String,
        @StringRes val name: Int,
        val value: Enum<*>,
        val options: List<Enum<*>>
    ): SettingsItem(key)
}