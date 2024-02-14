package nl.npo.player.sample_app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.sample_app.R
import nl.npo.player.sample_app.data.model.StylingPref
import nl.npo.player.sample_app.data.model.UserTypePref
import nl.npo.player.sample_app.data.model.toPref
import nl.npo.player.sample_app.domain.SettingsRepository
import nl.npo.player.sample_app.presentation.settings.model.SettingsItem
import nl.npo.player.sample_app.presentation.settings.model.SettingsKey
import nl.npo.player.sample_app.presentation.settings.model.SettingsOption
import nl.npo.player.sample_app.presentation.settings.model.SettingsPickerOption
import nl.npo.player.sample_app.presentation.settings.model.SettingsSwitchOption
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _settingsList = MutableStateFlow(emptyList<SettingsItem>())
    val settingsList = _settingsList.asLiveData()

    init {
        viewModelScope.launch {
            generateSettingsList()
        }
    }

    fun handleSettingChange(key: SettingsKey, value: SettingsOption) {
        viewModelScope.launch {
            when (value) {
                is SettingsSwitchOption -> handleSwitch(key, value.value)
                is SettingsPickerOption -> handlePicker(value)
            }

            generateSettingsList()
        }
    }

    private suspend fun handleSwitch(key: SettingsKey, value: Boolean) {
        when (key) {
            SettingsKey.CustomSettings -> settingsRepository.setShowCustomSettings(value)
            SettingsKey.ShowUi -> settingsRepository.setShowUi(value)
            SettingsKey.AutoPlayEnabled -> settingsRepository.setAutoPlayEnabled(value)
            SettingsKey.PauseWhenBecomingNoisy -> settingsRepository.setPauseWhenBecomingNoisy(value)
            SettingsKey.PauseOnSwitchToCellularNetwork ->
                settingsRepository.setPauseOnSwitchToCellularNetwork(value)

            else -> Unit
        }
    }

    private suspend fun handlePicker(value: SettingsPickerOption) {
        when (value) {
            is StylingPref -> settingsRepository.setStyling(value.toDomain())
            is UserTypePref -> settingsRepository.setUserType(value.toDomain())
        }
    }

    private suspend fun generateSettingsList() {
        _settingsList.value = buildList {
            add(
                SettingsItem.Picker(
                    SettingsKey.Styling,
                    R.string.setting_styling,
                    settingsRepository.styling.first().toPref(),
                    StylingPref.values().asList()
                )
            )

            add(
                SettingsItem.Picker(
                    SettingsKey.UserType,
                    R.string.setting_user_type,
                    settingsRepository.userType.first().toPref(),
                    UserTypePref.values().asList()
                )
            )

            add(
                SettingsItem.Switch(
                    SettingsKey.CustomSettings,
                    R.string.setting_custom_settings,
                    SettingsSwitchOption(settingsRepository.showCustomSettings.first())
                )
            )

            add(
                SettingsItem.Switch(
                    SettingsKey.ShowUi,
                    R.string.setting_show_ui,
                    SettingsSwitchOption(settingsRepository.showUi.first())
                )
            )

            add(
                SettingsItem.Switch(
                    SettingsKey.AutoPlayEnabled,
                    R.string.setting_autoplay_enabled,
                    SettingsSwitchOption(settingsRepository.autoPlayEnabled.first())
                )
            )

            add(
                SettingsItem.Switch(
                    SettingsKey.PauseWhenBecomingNoisy,
                    R.string.setting_pause_when_noisy,
                    SettingsSwitchOption(settingsRepository.pauseWhenBecomingNoisy.first())
                )
            )

            add(
                SettingsItem.Switch(
                    SettingsKey.PauseOnSwitchToCellularNetwork,
                    R.string.setting_pause_on_cellular,
                    SettingsSwitchOption(settingsRepository.pauseOnSwitchToCellularNetwork.first())
                )
            )
        }
    }
}
