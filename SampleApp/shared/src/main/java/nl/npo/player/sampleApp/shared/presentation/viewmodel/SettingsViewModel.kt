package nl.npo.player.sampleApp.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.shared.R
import nl.npo.player.sampleApp.shared.data.model.EnvironmentPref
import nl.npo.player.sampleApp.shared.data.model.PlayNextPref
import nl.npo.player.sampleApp.shared.data.model.StylingPref
import nl.npo.player.sampleApp.shared.data.model.UserTypePref
import nl.npo.player.sampleApp.shared.data.model.toPref
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsItem
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsKey
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsOption
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsSwitchOption
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private var hasPlayServices = false

        private val _settingsList = MutableStateFlow(emptyList<SettingsItem>())
        val settingsList = _settingsList.asLiveData()

        fun initSettingsList(hasPlayServices: Boolean) {
            this.hasPlayServices = hasPlayServices
            viewModelScope.launch {
                generateSettingsList(hasPlayServices)
            }
        }

        fun handleSettingChange(
            key: SettingsKey,
            value: SettingsOption,
        ) {
            viewModelScope.launch {
                when (value) {
                    is SettingsSwitchOption -> handleSwitch(key, value.value)
                    is SettingsPickerOption -> handlePicker(value)
                }

                generateSettingsList(hasPlayServices)
            }
        }

        private suspend fun handleSwitch(
            key: SettingsKey,
            value: Boolean,
        ) {
            when (key) {
                SettingsKey.CustomSettings -> settingsRepository.setShowCustomSettings(value)
                SettingsKey.ShowUi -> settingsRepository.setShowUi(value)
                SettingsKey.AutoPlayEnabled -> settingsRepository.setAutoPlayEnabled(value)
                SettingsKey.OnlyStreamLinkRandomEnabled -> settingsRepository.setOnlyStreamLinkRandomEnabled(value)
                SettingsKey.SterUiEnabled -> settingsRepository.setSterUiEnabled(value)
                SettingsKey.PauseWhenBecomingNoisy -> settingsRepository.setPauseWhenBecomingNoisy(value)
                SettingsKey.PauseOnSwitchToCellularNetwork ->
                    settingsRepository.setPauseOnSwitchToCellularNetwork(value)
                SettingsKey.EnableCasting -> settingsRepository.setEnableCasting(value)
                SettingsKey.Styling,
                SettingsKey.Environment,
                SettingsKey.UserType,
                SettingsKey.ShouldPlayNext,
                -> Unit
            }
        }

        private suspend fun handlePicker(value: SettingsPickerOption) {
            when (value) {
                is PlayNextPref -> settingsRepository.setShouldPlayNext(value.toDomain())
                is StylingPref -> settingsRepository.setStyling(value.toDomain())
                is UserTypePref -> settingsRepository.setUserType(value.toDomain())
                is EnvironmentPref -> settingsRepository.setEnvironment(value.toDomain())
            }
        }

        private suspend fun generateSettingsList(hasPlayServices: Boolean) {
            _settingsList.value =
                buildList {
                    add(
                        SettingsItem.Picker(
                            SettingsKey.ShouldPlayNext,
                            R.string.setting_play_next,
                            settingsRepository.shouldShowPlayNext.first().toPref(),
                            PlayNextPref.entries,
                        ),
                    )

                    add(
                        SettingsItem.Picker(
                            SettingsKey.Styling,
                            R.string.setting_styling,
                            settingsRepository.styling.first().toPref(),
                            StylingPref.entries,
                        ),
                    )

                    add(
                        SettingsItem.Picker(
                            SettingsKey.UserType,
                            R.string.setting_user_type,
                            settingsRepository.userType.first().toPref(),
                            UserTypePref.entries,
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.CustomSettings,
                            R.string.setting_custom_settings,
                            SettingsSwitchOption(settingsRepository.showCustomSettings.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.ShowUi,
                            R.string.setting_show_ui,
                            SettingsSwitchOption(settingsRepository.showUi.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.AutoPlayEnabled,
                            R.string.setting_autoplay_enabled,
                            SettingsSwitchOption(settingsRepository.autoPlayEnabled.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.OnlyStreamLinkRandomEnabled,
                            R.string.setting_only_streamlink_random_enabled,
                            SettingsSwitchOption(settingsRepository.onlyStreamLinkRandomEnabled.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.SterUiEnabled,
                            R.string.setting_ster_ui_enabled,
                            SettingsSwitchOption(settingsRepository.sterUiEnabled.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.PauseWhenBecomingNoisy,
                            R.string.setting_pause_when_noisy,
                            SettingsSwitchOption(settingsRepository.pauseWhenBecomingNoisy.first()),
                        ),
                    )

                    add(
                        SettingsItem.Switch(
                            SettingsKey.PauseOnSwitchToCellularNetwork,
                            R.string.setting_pause_on_cellular,
                            SettingsSwitchOption(settingsRepository.pauseOnSwitchToCellularNetwork.first()),
                        ),
                    )

                    if (hasPlayServices) {
                        add(
                            SettingsItem.Switch(
                                SettingsKey.EnableCasting,
                                R.string.setting_enable_casting,
                                SettingsSwitchOption(settingsRepository.enableCasting.first()),
                            ),
                        )
                    }

                    add(
                        SettingsItem.Picker(
                            SettingsKey.Environment,
                            R.string.setting_environment,
                            settingsRepository.environment.first().toPref(),
                            EnvironmentPref.entries,
                        ),
                    )
                }
        }
    }
