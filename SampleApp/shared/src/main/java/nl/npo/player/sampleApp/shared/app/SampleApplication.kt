package nl.npo.player.sampleApp.shared.app

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.coroutines.flow.first
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.AnalyticsPlatform
import nl.npo.player.library.npotag.mapper.AnalyticsEnvironmentMapper
import nl.npo.player.library.npotag.model.AnalyticsConfiguration
import nl.npo.player.sampleApp.shared.BuildConfig
import nl.npo.player.sampleApp.shared.data.ads.AdManagerProvider
import nl.npo.player.sampleApp.shared.data.extensions.toPlayerEnvironment
import nl.npo.player.sampleApp.shared.data.offline.service.TestDownloadService
import nl.npo.player.sampleApp.shared.domain.AnalyticsEnvironmentProvider
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import nl.npo.tag.sdk.NpoTag
import nl.npo.tag.sdk.govolteplugin.GovoltePlugin
import javax.inject.Inject

open class SampleApplication :
    Application(),
    PlayerApplication {
    override var npoTag: NpoTag? = null
    private var isPlayerInitiatedYetInternal = false

    @Inject
    lateinit var analyticsEnvironmentProvider: AnalyticsEnvironmentProvider

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun isPlayerInitiatedYet(): Boolean = isPlayerInitiatedYetInternal

    override suspend fun initiatePlayerLibrary(withNPOTag: Boolean) {
        isPlayerInitiatedYetInternal = true
        val list = listOf(ChuckerInterceptor.Builder(this).build())
        val enableCasting = settingsRepository.enableCasting.first()
        val environment = settingsRepository.environment.first().toPlayerEnvironment()
        if (withNPOTag) {
            // Either create your own NpoTag implementation which can be used for app analytics:
            npoTag =
                initializeNPOTag(setupAnalyticsConfiguration()).also { tag ->
                    NPOPlayerLibrary.initialize(
                        context = this,
                        analyticsConfig = AnalyticsConfiguration.Provided(tag),
                        adManager = AdManagerProvider.getAdManager(this),
                    ) {
                        this.environment = environment
                        this.enableCasting = enableCasting
                        debugLogging = true
                        addInterceptors(list)
                    }
                }
        } else {
            // Or Initialize the library with an AnalyticsConfiguration. But never both.
            NPOPlayerLibrary.initialize(
                context = this,
                analyticsConfig =
                    AnalyticsConfiguration.Standalone(
                        brand = "nl.npo.player.sample_app",
                        brandId = brandId,
                        platform = getPlatform(),
                        platformVersion = BuildConfig.VERSION_NAME,
                        withDebug = true,
                        environment = analyticsEnvironmentProvider.getAnalyticsEnvironment(),
                    ),
                adManager = AdManagerProvider.getAdManager(this),
            ) {
                this.environment = environment
                this.enableCasting = enableCasting
                debugLogging = true
                addInterceptors(list)
            }
        }
        NPOPlayerLibrary.Offline.initializeDownloadService(TestDownloadService::class.java)
    }

    private suspend fun setupAnalyticsConfiguration(): AnalyticsConfiguration.Standalone =
        AnalyticsConfiguration.Standalone(
            brand = "nl.npo.player.sample_app",
            brandId = brandId,
            platform = getPlatform(),
            platformVersion = BuildConfig.VERSION_NAME,
            withDebug = true,
            environment = analyticsEnvironmentProvider.getAnalyticsEnvironment(),
        )

    private suspend fun initializeNPOTag(analyticsConfiguration: AnalyticsConfiguration.Standalone): NpoTag =
        NpoTag
            .builder()
            .withContext(this)
            .withBrand(
                brand = analyticsConfiguration.brand,
                brandId = analyticsConfiguration.brandId,
            ).withPlatform(
                platform = analyticsConfiguration.platform.toString(),
                version = analyticsConfiguration.platformVersion,
            ).withPluginsFactory { pluginContext ->
                setOf(
                    GovoltePlugin(
                        pluginContext = pluginContext,
                        baseUrl = "https://topspin.npo.nl/",
                    ),
                )
            }.withEnvironment(AnalyticsEnvironmentMapper.map(analyticsEnvironmentProvider.getAnalyticsEnvironment()))
            .withDebug(analyticsConfiguration.withDebug)
            .build()

    private fun getPlatform(): AnalyticsPlatform = if (isThisDeviceATelevision()) AnalyticsPlatform.TV_APP else AnalyticsPlatform.APP

    private fun Context.isThisDeviceATelevision(): Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}
