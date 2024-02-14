package nl.npo.player.sample_app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import nl.npo.player.sample_app.databinding.ItemSettingBooleanBinding
import nl.npo.player.sample_app.databinding.ItemSettingPickerBinding
import nl.npo.player.sample_app.domain.model.Setting

class SettingPickerViewHolder private constructor(
    private val binding: ItemSettingPickerBinding,
    private val onValueChanged: (String, Enum<*>) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(setting: SettingsItem.Picker) = with(binding) {
        root.setOnClickListener {
        }
    }

    private fun createPopupMenu(onItemClick: (Enum<*>) -> Unit): PopupMenu {
        return PopupMenu(itemView.context, itemView).apply {

        }
    }

    companion object {
        fun create(parent: ViewGroup, onValueChanged: (String, Enum<*>) -> Unit): SettingPickerViewHolder {
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