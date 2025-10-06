package nl.npo.player.sampleApp.shared.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.data.model.EnvironmentPref
import nl.npo.player.sampleApp.shared.data.model.StylingPref
import nl.npo.player.sampleApp.shared.data.model.UserTypePref
import nl.npo.player.sampleApp.shared.data.settings.module.SettingsDataStore
import nl.npo.player.sampleApp.shared.domain.model.DefaultSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Singleton
class SettingsPreferences
    @Inject
    constructor(
        @SettingsDataStore private val dataStore: DataStore<Preferences>,
        private val defaultSettings: DefaultSettings,
    ) {
        object Keys {
            val useExoplayer = booleanPreferencesKey("useExoplayer")
            val styling = stringPreferencesKey("styling")
            val userType = stringPreferencesKey("userType")
            val settingsType = booleanPreferencesKey("settingsType")
            val showUi = booleanPreferencesKey("showUi")
            val sterUiEnabled = booleanPreferencesKey("sterUiEnabled")
            val autoPlayEnabled = booleanPreferencesKey("autoPlayEnabled")
            val onlyStreamLinkRandom = booleanPreferencesKey("onlyStreamLinkRandom")
            val pauseWhenBecomingNoisy = booleanPreferencesKey("pauseWhenBecomingNoisy")
            val pauseOnSwitchToCellularNetwork = booleanPreferencesKey("pauseOnSwitchToCellularNetwork")
            val shouldPlayNext = booleanPreferencesKey("shouldPlayNext")
            val shouldAutoPlayNext = booleanPreferencesKey("shouldAutoPlayNext")
            val playNextDuration = intPreferencesKey("playNextDuration")
            val playNextOffset = intPreferencesKey("playNextOffset")
            val enableCasting = booleanPreferencesKey("enableCasting")
            val environment = stringPreferencesKey("environment")
            val chapterSkippingEnabled = booleanPreferencesKey("chapterSkippingEnabled")
            val chapterSkippingAlwaysFeatured = booleanPreferencesKey("chapterSkippingAlwaysFeatured")
        }

        val useExoplayer: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.useExoplayer] ?: defaultSettings.useExoplayer
                }

        suspend fun setUseExoplayer(show: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.useExoplayer] = show
            }
        }

        val styling: Flow<StylingPref>
            get() =
                dataStore.data.map { prefs ->
                    StylingPref.getByKey(prefs[Keys.styling].orEmpty()) ?: defaultSettings.stylingPref
                }

        suspend fun setStyling(value: StylingPref) {
            dataStore.edit { prefs ->
                prefs[Keys.styling] = value.key
            }
        }

        val userType: Flow<UserTypePref>
            get() =
                dataStore.data.map { prefs ->
                    UserTypePref.getByKey(prefs[Keys.userType].orEmpty())
                        ?: defaultSettings.userTypePref
                }

        suspend fun setUserType(value: UserTypePref) {
            dataStore.edit { prefs ->
                prefs[Keys.userType] = value.key
            }
        }

        val showCustomPlayerSettings: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.settingsType] ?: defaultSettings.showCustomSettings
                }

        suspend fun setShowCustomSettings(show: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.settingsType] = show
            }
        }

        val showUi: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.showUi] ?: defaultSettings.showUi
                }

        suspend fun setShowUi(show: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.showUi] = show
            }
        }

        val autoPlayEnabled: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.autoPlayEnabled] ?: defaultSettings.autoPlayEnabled
                }

        suspend fun setAutoPlayEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.autoPlayEnabled] = enabled
            }
        }

        val onlyStreamLinkRandomEnabled: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.onlyStreamLinkRandom] ?: defaultSettings.onlyStreamLinkRandomEnabled
                }

        suspend fun setOnlyStreamLinkRandomEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.onlyStreamLinkRandom] = enabled
            }
        }

        val sterUiEnabled: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.sterUiEnabled] ?: defaultSettings.sterUiEnabled
                }

        suspend fun setSterUiEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.sterUiEnabled] = enabled
            }
        }

        val pauseWhenBecomingNoisy: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.pauseWhenBecomingNoisy] ?: defaultSettings.pauseWhenBecomingNoisy
                }

        suspend fun setPauseWhenBecomingNoisy(pause: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.pauseWhenBecomingNoisy] = pause
            }
        }

        val pauseOnSwitchToCellularNetwork: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.pauseOnSwitchToCellularNetwork]
                        ?: defaultSettings.pauseOnSwitchToCellularNetwork
                }

        suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.pauseOnSwitchToCellularNetwork] = pause
            }
        }

        val shouldPlayNext: Flow<PlayNext>
            get() =
                dataStore.data.map { prefs ->
                    val default = defaultSettings.playNext
                    PlayNext(
                        showPlayNext = prefs[Keys.shouldPlayNext] ?: default.showPlayNext,
                        duration = prefs[Keys.playNextDuration]?.seconds ?: default.duration,
                        offset = prefs[Keys.playNextOffset]?.seconds ?: default.offset,
                        autoPlayNextEnabled =
                            prefs[Keys.shouldAutoPlayNext]
                                ?: default.autoPlayNextEnabled,
                    )
                }

        suspend fun setShouldPlayNext(playNext: PlayNext) {
            dataStore.edit { prefs ->
                prefs[Keys.shouldPlayNext] = playNext.showPlayNext
                prefs[Keys.shouldAutoPlayNext] = playNext.autoPlayNextEnabled
                prefs[Keys.playNextDuration] = playNext.duration.toInt(DurationUnit.SECONDS)
                prefs[Keys.playNextOffset] = playNext.offset.toInt(DurationUnit.SECONDS)
            }
        }

        val enableCasting: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.enableCasting] ?: defaultSettings.enableCasting
                }

        suspend fun setEnableCasting(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.enableCasting] = enabled
            }
        }

        val environment: Flow<EnvironmentPref>
            get() =
                dataStore.data.map { prefs ->
                    EnvironmentPref.getByKey(prefs[Keys.environment].orEmpty())
                        ?: defaultSettings.environment
                }

        suspend fun setEnvironment(value: EnvironmentPref) {
            dataStore.edit { prefs ->
                prefs[Keys.environment] = value.key
            }
        }

        val chapterSkippingEnabled: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.chapterSkippingEnabled] ?: defaultSettings.chapterSkippingEnabled
                }

        suspend fun setChapterSkippingEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.chapterSkippingEnabled] = enabled
            }
        }

        val chapterSkippingAlwaysFeatured: Flow<Boolean>
            get() =
                dataStore.data.map { prefs ->
                    prefs[Keys.chapterSkippingAlwaysFeatured]
                        ?: defaultSettings.chapterSkippingAlwaysFeatured
                }

        suspend fun setChapterSkippingAlwaysFeatured(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.chapterSkippingAlwaysFeatured] = enabled
            }
        }
    }
