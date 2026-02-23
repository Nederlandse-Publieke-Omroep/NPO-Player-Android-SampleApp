package nl.npo.player.sampleApp.presentation.player

import android.content.Context
import android.util.Log
import com.google.android.datatransport.runtime.scheduling.SchedulingConfigModule_ConfigFactory.config
import dagger.hilt.android.qualifiers.ApplicationContext
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.PlayerPageTracker
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.theme.NativePlayerColors
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.library.presentation.model.NPOPlayerUIConfig
import nl.npo.player.sampleApp.shared.data.settings.SettingsPreferences.Keys.useExoplayer
import nl.npo.player.sampleApp.shared.presentation.viewmodel.UseExoplayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NPOPlayerFactoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : NPOPlayerFactory {

    private val sdk: NPOPlayerLibrary = NPOPlayerLibrary

    override fun create(
        context: Context,
        playerConfig: NPOPlayerConfig,
        npoPlayerColors: NativePlayerColors?,
        useExoplayer: Boolean,
        playerUIConfig: NPOPlayerUIConfig,
        pageTracker: PlayerPageTracker,
    ): NPOPlayer {
        return sdk.getPlayer(
            context = appContext,
            pageTracker = (pageTracker),
            npoPlayerConfig = playerConfig,
            useExoplayer = useExoplayer,
        )
    }
}
