package nl.npo.player.sampleApp.shared.domain

import kotlinx.coroutines.flow.Flow
import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.domain.model.UserType

interface SettingsRepository {
    val useExoplayer: Flow<Boolean>

    suspend fun setUseExoplayer(useExoplayer: Boolean)

    val styling: Flow<Styling>

    suspend fun setStyling(type: Styling)

    val userType: Flow<UserType>

    suspend fun setUserType(type: UserType)

    val showCustomSettings: Flow<Boolean>

    suspend fun setShowCustomSettings(show: Boolean)

    val showUi: Flow<Boolean>

    suspend fun setShowUi(show: Boolean)

    val autoPlayEnabled: Flow<Boolean>

    suspend fun setAutoPlayEnabled(enabled: Boolean)

    val onlyStreamLinkRandomEnabled: Flow<Boolean>

    suspend fun setOnlyStreamLinkRandomEnabled(enabled: Boolean)

    val sterUiEnabled: Flow<Boolean>

    suspend fun setSterUiEnabled(enabled: Boolean)

    val pauseWhenBecomingNoisy: Flow<Boolean>

    suspend fun setPauseWhenBecomingNoisy(pause: Boolean)

    val pauseOnSwitchToCellularNetwork: Flow<Boolean>

    suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean)

    val shouldShowPlayNext: Flow<PlayNext>

    suspend fun setShouldPlayNext(playNext: PlayNext)

    val enableCasting: Flow<Boolean>

    suspend fun setEnableCasting(enabled: Boolean)

    val environment: Flow<Environment>

    suspend fun setEnvironment(type: Environment)

    val chapterSkippingEnabled: Flow<Boolean>

    suspend fun setChapterSkippingEnabled(enabled: Boolean)

    val chapterSkippingAlwaysFeatured: Flow<Boolean>

    suspend fun setChapterSkippingAlwaysFeatured(enabled: Boolean)
}
