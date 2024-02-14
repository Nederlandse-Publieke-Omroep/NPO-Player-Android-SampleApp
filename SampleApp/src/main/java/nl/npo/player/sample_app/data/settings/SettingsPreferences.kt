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
        val Styling = stringPreferencesKey("styling")
        val UserType = stringPreferencesKey("userType")
        val SettingsType = booleanPreferencesKey("settingsType")
        val ShowUi = booleanPreferencesKey("showUi")
        val AutoPlayEnabled = booleanPreferencesKey("autoPlayEnabled")
        val PauseWhenBecomingNoisy = booleanPreferencesKey("pauseWhenBecomingNoisy")
        val PauseOnSwitchToCellularNetwork = booleanPreferencesKey("pauseOnSwitchToCellularNetwork")
    }

    val styling: Flow<StylingPref>
        get() = dataStore.data.map { prefs ->
            StylingPref.getByKey(prefs[Keys.Styling].orEmpty())
        }

    suspend fun setStyling(value: StylingPref) {
        dataStore.edit { prefs ->
            prefs[Keys.Styling] = value.key
        }
    }

    val userType: Flow<UserTypePref>
        get() = dataStore.data.map { prefs ->
            UserTypePref.getByKey(prefs[Keys.UserType].orEmpty())
        }

    suspend fun setUserType(value: UserTypePref) {
        dataStore.edit { prefs ->
            prefs[Keys.UserType] = value.key
        }
    }

    val showCustomPlayerSettings: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.SettingsType] ?: true
        }

    suspend fun setShowCustomSettings(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SettingsType] = show
        }
    }

    val showUi: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.ShowUi] ?: true
        }

    suspend fun setShowUi(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ShowUi] = show
        }
    }

    val autoPlayEnabled: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.AutoPlayEnabled] ?: false
        }

    suspend fun setAutoPlayEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.AutoPlayEnabled] = enabled
        }
    }

    val pauseWhenBecomingNoisy: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.PauseWhenBecomingNoisy] ?: false
        }

    suspend fun setPauseWhenBecomingNoisy(pause: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.PauseWhenBecomingNoisy] = pause
        }
    }

    val pauseOnSwitchToCellularNetwork: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.PauseOnSwitchToCellularNetwork] ?: false
        }

    suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.PauseOnSwitchToCellularNetwork] = pause
        }
    }
}
