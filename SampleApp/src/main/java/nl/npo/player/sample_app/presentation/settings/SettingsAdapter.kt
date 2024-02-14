package nl.npo.player.sample_app.presentation.settings

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import nl.npo.player.sample_app.presentation.settings.model.SettingsItem
import nl.npo.player.sample_app.presentation.settings.model.SettingsKey
import nl.npo.player.sample_app.presentation.settings.model.SettingsOption

class SettingsAdapter(
    private val onSettingChanged: (SettingsKey, SettingsOption) -> Unit,
) : ListAdapter<SettingsItem, ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ViewType.Switch.ordinal -> SettingBooleanViewHolder.create(parent, onSettingChanged)
            ViewType.Picker.ordinal -> SettingPickerViewHolder.create(parent, onSettingChanged)
            else -> error("No viewholder implemented for viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SettingBooleanViewHolder -> holder.bind(item as SettingsItem.Switch)
            is SettingPickerViewHolder -> holder.bind(item as SettingsItem.Picker)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsItem.Switch -> ViewType.Switch.ordinal
            is SettingsItem.Picker -> ViewType.Picker.ordinal
            else -> error("Not implemented")
        }
    }

    private companion object {
        val diffCallback = object : DiffUtil.ItemCallback<SettingsItem>() {
            override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
                return oldItem.key == newItem.key
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
                return oldItem == newItem
            }
        }

        enum class ViewType {
            Switch,
            Picker
        }
    }
}
