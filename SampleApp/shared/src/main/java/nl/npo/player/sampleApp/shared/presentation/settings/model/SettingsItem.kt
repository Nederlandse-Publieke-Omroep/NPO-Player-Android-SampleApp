package nl.npo.player.sampleApp.shared.presentation.settings.model

import androidx.annotation.StringRes

sealed class SettingsItem(
    open val key: SettingsKey,
    @StringRes open val titleRes: Int,
) {
    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    data class Switch(
        override val key: SettingsKey,
        @StringRes override val titleRes: Int,
        val value: SettingsSwitchOption,
    ) : SettingsItem(key, titleRes)

    data class Picker(
        override val key: SettingsKey,
        @StringRes override val titleRes: Int,
        val value: SettingsPickerOption,
        val options: List<SettingsPickerOption>,
    ) : SettingsItem(key, titleRes)
}
