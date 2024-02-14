package nl.npo.player.sample_app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.databinding.ItemSettingBooleanBinding

class SettingBooleanViewHolder private constructor(
    private val binding: ItemSettingBooleanBinding,
    private val onValueChanged: (String, Boolean) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(setting: SettingsItem.Switch) = with(binding) {
        with(toggle) {
            setOnCheckedChangeListener(null)
            isChecked = setting.value
            setOnCheckedChangeListener { _, isChecked ->
                onValueChanged(setting.key, isChecked)
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, onValueChanged: (String, Boolean) -> Unit): SettingBooleanViewHolder {
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