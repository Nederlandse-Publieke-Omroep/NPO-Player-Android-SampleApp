package nl.npo.player.sampleApp.shared.fake

import kotlinx.coroutines.flow.MutableStateFlow
import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.data.model.AgeProfileInt
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.domain.model.Styling
import nl.npo.player.sampleApp.shared.domain.model.UserType

class FakeSettingsRepository : SettingsRepository {
    override val styling = MutableStateFlow(Styling.Default)

    override suspend fun setStyling(type: Styling) {
        styling.value = type
    }

    override val userType = MutableStateFlow(UserType.Plus)

    override suspend fun setUserType(type: UserType) {
        userType.value = type
    }

    override val showCustomSettings = MutableStateFlow(false)

    override suspend fun setShowCustomSettings(show: Boolean) {
        showCustomSettings.value = show
    }

    override val showUi = MutableStateFlow(true)

    override suspend fun setShowUi(show: Boolean) {
        showUi.value = show
    }

    override val autoPlayEnabled = MutableStateFlow(false)

    override suspend fun setAutoPlayEnabled(enabled: Boolean) {
        autoPlayEnabled.value = enabled
    }

    override val onlyStreamLinkRandomEnabled = MutableStateFlow(false)

    override suspend fun setOnlyStreamLinkRandomEnabled(enabled: Boolean) {
        onlyStreamLinkRandomEnabled.value = enabled
    }

    override val sterUiEnabled = MutableStateFlow(false)

    override suspend fun setSterUiEnabled(enabled: Boolean) {
        sterUiEnabled.value = enabled
    }

    override val pauseWhenBecomingNoisy = MutableStateFlow(false)

    override suspend fun setPauseWhenBecomingNoisy(pause: Boolean) {
        pauseWhenBecomingNoisy.value = pause
    }

    override val pauseOnSwitchToCellularNetwork = MutableStateFlow(false)

    override suspend fun setPauseOnSwitchToCellularNetwork(pause: Boolean) {
        pauseOnSwitchToCellularNetwork.value = pause
    }

    override val shouldShowPlayNext = MutableStateFlow(PlayNext(showPlayNext = false))

    override suspend fun setShouldPlayNext(playNext: PlayNext) {
        shouldShowPlayNext.value = playNext
    }

    override val enableCasting = MutableStateFlow(true)

    override suspend fun setEnableCasting(enabled: Boolean) {
        enableCasting.value = enabled
    }

    override val environment = MutableStateFlow(Environment.Production)

    override suspend fun setEnvironment(type: Environment) {
        environment.value = type
    }

    override val ageProfile = MutableStateFlow<AgeProfileInt>(18)

    override suspend fun setAgeProfile(ageProfile: AgeProfileInt) {
        this.ageProfile.value = ageProfile
    }

    override val chapterSkippingEnabled = MutableStateFlow(false)

    override suspend fun setChapterSkippingEnabled(enabled: Boolean) {
        chapterSkippingEnabled.value = enabled
    }

    override val chapterSkippingAlwaysFeatured = MutableStateFlow(false)

    override suspend fun setChapterSkippingAlwaysFeatured(enabled: Boolean) {
        chapterSkippingAlwaysFeatured.value = enabled
    }

    override val usePreloadManagerShorts = MutableStateFlow(true)

    override suspend fun setUsePreloadManagerShorts(usePreloadManager: Boolean) {
        usePreloadManagerShorts.value = usePreloadManager
    }
}
