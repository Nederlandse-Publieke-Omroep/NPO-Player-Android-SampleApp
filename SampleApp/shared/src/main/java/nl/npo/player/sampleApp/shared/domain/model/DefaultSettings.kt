package nl.npo.player.sampleApp.shared.domain.model

import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.data.model.EnvironmentPref
import nl.npo.player.sampleApp.shared.data.model.StylingPref
import nl.npo.player.sampleApp.shared.data.model.UserTypePref
import kotlin.time.Duration.Companion.seconds

data class DefaultSettings(
    val stylingPref: StylingPref = StylingPref.Default,
    val userTypePref: UserTypePref = UserTypePref.Start,
    val showCustomSettings: Boolean = false,
    val showUi: Boolean = true,
    val autoPlayEnabled: Boolean = false,
    val onlyStreamLinkRandomEnabled: Boolean = false,
    val sterUiEnabled: Boolean = true,
    val pauseWhenBecomingNoisy: Boolean = false,
    val pauseOnSwitchToCellularNetwork: Boolean = false,
    val playNext: PlayNext =
        PlayNext(
            false,
            10.seconds,
            10.seconds,
            true,
        ),
    val enableCasting: Boolean,
    val environment: EnvironmentPref = EnvironmentPref.Production,
)
