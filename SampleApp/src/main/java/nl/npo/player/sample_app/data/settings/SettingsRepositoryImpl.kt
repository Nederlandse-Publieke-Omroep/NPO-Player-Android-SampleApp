package nl.npo.player.sample_app.data.settings

import kotlinx.coroutines.flow.Flow
import nl.npo.player.sample_app.domain.SettingsRepository
import nl.npo.player.sample_app.domain.model.SettingsType
import nl.npo.player.sample_app.domain.model.Styling
import nl.npo.player.sample_app.domain.model.UserType
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(private val prefs: SettingsPreferences): SettingsRepository {
    override val styling: Flow<Styling> = prefs.styling

    override suspend fun setStyling(type: Styling) {
        prefs.setStyling(type)
    }

    override val userType: Flow<UserType> = prefs.userType

    override suspend fun setUserType(type: UserType) {
        prefs.setUserType(type)
    }

    override val settingsType: Flow<SettingsType> = prefs.settingsType

    override suspend fun setSettingsType(type: SettingsType) {
        prefs.setSettingsType(type)
    }

    override val showUi: Flow<Boolean> = prefs.showUi

    override suspend fun setShowUi(show: Boolean) {
        prefs.setShowUi(show)
    }

    override val autoPlayEnabled: Flow<Boolean> = prefs.autoPlayEnabled

    override suspend fun setAutoPlayEnabled(enabled: Boolean) {
        prefs.setAutoPlayEnabled(enabled)
    }

    override val pauseWhenBecomingNoisy: Flow<Boolean> = prefs.pauseWhenBecomingNoisy

    override suspend fun setPauseWhenBecomingNoisy(pause: Boolean) {
        prefs.setPauseWhenBecomingNoisy(pause)
    }

    override val pauseOnSwitchToCellularNetwork: Flow<Boolean> = prefs.pauseOnSwitchToCellularNetwork

    override suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
        prefs.setPauseOnSwitchToCellularNetwork(pause)
    }
}