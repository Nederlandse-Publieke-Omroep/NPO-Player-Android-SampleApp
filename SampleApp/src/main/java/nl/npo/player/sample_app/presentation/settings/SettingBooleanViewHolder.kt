package nl.npo.player.sample_app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.databinding.ItemSettingBooleanBinding
import nl.npo.player.sample_app.presentation.settings.model.SettingsItem
import nl.npo.player.sample_app.presentation.settings.model.SettingsKey
import nl.npo.player.sample_app.presentation.settings.model.SettingsSwitchOption

class SettingBooleanViewHolder private constructor(
    private val binding: ItemSettingBooleanBinding,
    private val onValueChanged: (SettingsKey, SettingsSwitchOption) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(setting: SettingsItem.Switch) = with(binding) {
        text.setText(setting.titleRes)
        with(toggle) {
            setOnCheckedChangeListener(null)
            isChecked = setting.value.value
            setOnCheckedChangeListener { _, isChecked ->
                onValueChanged(setting.key, SettingsSwitchOption(isChecked))
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, onValueChanged: (SettingsKey, SettingsSwitchOption) -> Unit): SettingBooleanViewHolder {
            return SettingBooleanViewHolder(
                ItemSettingBooleanBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onValueChanged
            )
        }
    }
}