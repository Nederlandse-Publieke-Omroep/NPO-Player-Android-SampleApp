package nl.npo.player.sample_app.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.npo.player.sample_app.data.model.SettingsTypePref
import nl.npo.player.sample_app.data.model.StylingPref
import nl.npo.player.sample_app.data.model.UserTypePref
import nl.npo.player.sample_app.data.model.toPref
import nl.npo.player.sample_app.domain.SettingsRepository
import nl.npo.player.sample_app.domain.model.SettingsType
import nl.npo.player.sample_app.domain.model.Styling
import nl.npo.player.sample_app.domain.model.UserType
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val prefs: SettingsPreferences
) : SettingsRepository {
    override val styling: Flow<Styling> =
        prefs.styling.map(StylingPref::toDomain)

    override suspend fun setStyling(type: Styling) {
        prefs.setStyling(type.toPref())
    }

    override val userType: Flow<UserType> =
        prefs.userType.map(UserTypePref::toDomain)

    override suspend fun setUserType(type: UserType) {
        prefs.setUserType(type.toPref())
    }

    override val showCustomSettings: Flow<Boolean> = prefs.showCustomPlayerSettings

    override suspend fun setShowCustomSettings(show: Boolean) {
        prefs.setShowCustomSettings(show)
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

    override val pauseOnSwitchToCellularNetwork: Flow<Boolean> =
        prefs.pauseOnSwitchToCellularNetwork

    override suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
        prefs.setPauseOnSwitchToCellularNetwork(pause)
    }
}