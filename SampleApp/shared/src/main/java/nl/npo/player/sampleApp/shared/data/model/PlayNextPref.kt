package nl.npo.player.sampleApp.shared.data.model

import nl.npo.player.library.domain.player.ui.model.PlayNext
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption
import kotlin.time.Duration.Companion.seconds

enum class PlayNextPref(
    override val key: String,
) : SettingsPickerOption {
    Default("Default"),
    NoAutoPlayNext("No_auto_play_next"),
    ZeroOffset("Zero_offset"),
    LongDuration("Long_duration"),
    PlayNextOff("Off"),
    ;

    fun toDomain() =
        when (this) {
            Default -> PlayNext(showPlayNext = true)
            NoAutoPlayNext -> PlayNext(showPlayNext = true, autoPlayNextEnabled = false)
            ZeroOffset -> PlayNext(showPlayNext = true, offset = 0.seconds)
            LongDuration -> PlayNext(showPlayNext = true, duration = 60.seconds)
            PlayNextOff -> PlayNext(showPlayNext = false)
        }
}

fun PlayNext.toPref(): PlayNextPref =
    when (this) {
        PlayNext(showPlayNext = false) -> PlayNextPref.PlayNextOff
        PlayNext(showPlayNext = true, duration = 60.seconds) -> PlayNextPref.LongDuration
        PlayNext(showPlayNext = true, offset = 0.seconds) -> PlayNextPref.ZeroOffset
        PlayNext(showPlayNext = true, autoPlayNextEnabled = false) -> PlayNextPref.NoAutoPlayNext
        else -> PlayNextPref.Default
    }
