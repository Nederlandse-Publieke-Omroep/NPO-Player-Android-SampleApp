package nl.npo.player.sample_app.domain

import kotlinx.coroutines.flow.Flow
import nl.npo.player.sample_app.domain.model.SettingsType
import nl.npo.player.sample_app.domain.model.Styling
import nl.npo.player.sample_app.domain.model.UserType

interface SettingsRepository {
    val styling: Flow<Styling>
    suspend fun setStyling(type: Styling)

    val userType: Flow<UserType>
    suspend fun setUserType(type: UserType)

    val settingsType: Flow<SettingsType>
    suspend fun setSettingsType(type: SettingsType)

    val showUi: Flow<Boolean>
    suspend fun setShowUi(show: Boolean)

    val autoPlayEnabled: Flow<Boolean>
    suspend fun setAutoPlayEnabled(enabled: Boolean)

    val pauseWhenBecomingNoisy: Flow<Boolean>
    suspend fun setPauseWhenBecomingNoisy(pause: Boolean)

    val pauseOnSwitchToCellularNetwork: Flow<Boolean>
    suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean)
}