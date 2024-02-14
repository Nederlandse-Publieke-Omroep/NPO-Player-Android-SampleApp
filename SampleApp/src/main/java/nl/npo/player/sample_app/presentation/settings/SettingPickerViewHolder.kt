package nl.npo.player.sample_app.presentation.settings

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.databinding.ItemSettingPickerBinding
import nl.npo.player.sample_app.presentation.settings.model.SettingsItem
import nl.npo.player.sample_app.presentation.settings.model.SettingsKey
import nl.npo.player.sample_app.presentation.settings.model.SettingsPickerOption

class SettingPickerViewHolder private constructor(
    private val binding: ItemSettingPickerBinding,
    private val onValueChanged: (SettingsKey, SettingsPickerOption) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(setting: SettingsItem.Picker) = with(binding) {
        text.setText(setting.titleRes)
        value.text = setting.value.name

        val popupMenu = createPopupMenu(setting.options) { option ->
            onValueChanged(setting.key, option)
        }

        root.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun createPopupMenu(
        options: List<SettingsPickerOption>,
        onItemClick: (SettingsPickerOption) -> Unit
    ): PopupMenu {
        return PopupMenu(itemView.context, itemView, Gravity.END).apply {
            options.forEachIndexed { index, settingsOption ->
                menu.add(
                    0,
                    index,
                    index,
                    settingsOption.name
                )
            }

            setOnMenuItemClickListener { item ->
                onItemClick(options[item.itemId])
                true
            }
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onValueChanged: (SettingsKey, SettingsPickerOption) -> Unit
        ): SettingPickerViewHolder {
            return SettingPickerViewHolder(
                ItemSettingPickerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onValueChanged
            )
        }
    }
}
