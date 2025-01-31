package nl.npo.player.sampleApp.shared.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.data.model.EnvironmentPref
import nl.npo.player.sampleApp.shared.data.model.StylingPref
import nl.npo.player.sampleApp.shared.data.model.UserTypePref
import nl.npo.player.sampleApp.shared.data.model.toPref
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.domain.model.UserType
import javax.inject.Inject

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val prefs: SettingsPreferences,
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

        override val onlyStreamLinkRandomEnabled: Flow<Boolean> = prefs.onlyStreamLinkRandomEnabled

        override suspend fun setOnlyStreamLinkRandomEnabled(enabled: Boolean) {
            prefs.setOnlyStreamLinkRandomEnabled(enabled)
        }

        override val sterUiEnabled: Flow<Boolean> = prefs.sterUiEnabled

        override suspend fun setSterUiEnabled(enabled: Boolean) {
            prefs.setSterUiEnabled(enabled)
        }

        override val showNativeUIPlayer: Flow<Boolean> = prefs.showNativeUIPlayer

        override suspend fun setShowNativeUIPlayer(show: Boolean) {
            prefs.setShowNativeUIPlayer(show)
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

        override val shouldShowPlayNext: Flow<PlayNext> =
            prefs.shouldPlayNext

        override suspend fun setShouldPlayNext(playNext: PlayNext) {
            prefs.setShouldPlayNext(playNext)
        }

        override val enableCasting: Flow<Boolean> =
            prefs.enableCasting

        override suspend fun setEnableCasting(enabled: Boolean) {
            prefs.setEnableCasting(enabled)
        }

        override val environment: Flow<Environment> =
            prefs.environment.map(EnvironmentPref::toDomain)

        override suspend fun setEnvironment(type: Environment) {
            prefs.setEnvironment(type.toPref())
        }
    }
