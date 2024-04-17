package nl.npo.player.sample_app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.npo.player.sample_app.data.model.StylingPref
import nl.npo.player.sample_app.data.model.UserTypePref
import nl.npo.player.sample_app.data.settings.module.SettingsDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>
) {
    object Keys {
        val styling = stringPreferencesKey("styling")
        val userType = stringPreferencesKey("userType")
        val settingsType = booleanPreferencesKey("settingsType")
        val showUi = booleanPreferencesKey("showUi")
        val sterUiEnabled = booleanPreferencesKey("sterUiEnabled")
        val autoPlayEnabled = booleanPreferencesKey("autoPlayEnabled")
        val showMultiplePlayers = booleanPreferencesKey("showMultiplePlayers")
        val pauseWhenBecomingNoisy = booleanPreferencesKey("pauseWhenBecomingNoisy")
        val pauseOnSwitchToCellularNetwork = booleanPreferencesKey("pauseOnSwitchToCellularNetwork")
    }

    val styling: Flow<StylingPref>
        get() = dataStore.data.map { prefs ->
            StylingPref.getByKey(prefs[Keys.styling].orEmpty())
        }

    suspend fun setStyling(value: StylingPref) {
        dataStore.edit { prefs ->
            prefs[Keys.styling] = value.key
        }
    }

    val userType: Flow<UserTypePref>
        get() = dataStore.data.map { prefs ->
            UserTypePref.getByKey(prefs[Keys.userType].orEmpty())
        }

    suspend fun setUserType(value: UserTypePref) {
        dataStore.edit { prefs ->
            prefs[Keys.userType] = value.key
        }
    }

    val showCustomPlayerSettings: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.settingsType] ?: true
        }

    suspend fun setShowCustomSettings(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.settingsType] = show
        }
    }

    val showUi: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.showUi] ?: true
        }

    suspend fun setShowUi(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.showUi] = show
        }
    }

    val autoPlayEnabled: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.autoPlayEnabled] ?: false
        }

    suspend fun setAutoPlayEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.autoPlayEnabled] = enabled
        }
    }

    val sterUiEnabled: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.sterUiEnabled] ?: true
        }

    suspend fun setSterUiEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.sterUiEnabled] = enabled
        }
    }

    val showMultiplePlayers: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.showMultiplePlayers] ?: false
        }

    suspend fun setShowMultiplePlayers(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.showMultiplePlayers] = show
        }
    }

    val pauseWhenBecomingNoisy: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.pauseWhenBecomingNoisy] ?: false
        }

    suspend fun setPauseWhenBecomingNoisy(pause: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.pauseWhenBecomingNoisy] = pause
        }
    }

    val pauseOnSwitchToCellularNetwork: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.pauseOnSwitchToCellularNetwork] ?: false
        }

    suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.pauseOnSwitchToCellularNetwork] = pause
        }
    }
}
