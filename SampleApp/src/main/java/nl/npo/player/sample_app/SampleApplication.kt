package nl.npo.player.sample_app

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.hilt.android.HiltAndroidApp
import nl.npo.player.library.NPOCasting
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.domain.analytics.model.AnalyticsEnvironment
import nl.npo.player.library.domain.analytics.model.AnalyticsPlatform
import nl.npo.player.library.domain.common.enums.Environment
import nl.npo.player.library.npotag.mapper.AnalyticsEnvironmentMapper
import nl.npo.player.library.npotag.model.AnalyticsConfiguration
import nl.npo.player.sample_app.data.ads.AdManagerProvider
import nl.npo.player.sample_app.data.offline.service.TestDownloadService
import nl.npo.player.sample_app.presentation.cast.CastOptionsProvider
import nl.npo.tag.sdk.NpoTag
import nl.npo.tag.sdk.atinternet.ATInternetPlugin
import nl.npo.tag.sdk.govolteplugin.GovoltePlugin
import javax.inject.Inject

@HiltAndroidApp
class SampleApplication : Application() {
    var npoTag: NpoTag? = null
    lateinit var analyticsConfiguration: AnalyticsConfiguration.Standalone
    private var isPlayerInitiatedYetInternal = false

    @Inject
    lateinit var environment: Environment

    @Inject
    lateinit var analyticsEnvironment: AnalyticsEnvironment

    override fun onCreate() {
        super.onCreate()
        analyticsConfiguration = AnalyticsConfiguration.Standalone(
            brand = "nl.npo.player.sample_app",
            brandId = 634226,
            platform = getPlatform(),
            platformVersion = BuildConfig.VERSION_NAME,
            withDebug = true,
            environment = analyticsEnvironment
        )
    }

    fun isPlayerInitiatedYet(): Boolean = isPlayerInitiatedYetInternal

    fun initiatePlayerLibrary(withNPOTag: Boolean) {
        val list = listOf(ChuckerInterceptor.Builder(this).build())
        NPOPlayerLibrary.initialize(
            context = this,
            analyticsConfig = if (withNPOTag) {
                AnalyticsConfiguration.Provided(initializeNPOTag())
            } else {
                analyticsConfiguration
            },
            adManager = AdManagerProvider.getAdManager(this),
            configureOptions = {
                this.environment = this@SampleApplication.environment
                keepUIUpToDate = true
                addInterceptors(list)
            }
        )

        NPOPlayerLibrary.Offline.initializeDownloadService(TestDownloadService::class.java)

        NPOCasting.initializeCasting(getString(CastOptionsProvider.getReceiverID()))
        isPlayerInitiatedYetInternal = true
    }

    private fun initializeNPOTag(): NpoTag {
        npoTag?.let { return it }
        return NpoTag.builder()
            .withContext(this)
            .withBrand(
                brand = analyticsConfiguration.brand,
                brandId = analyticsConfiguration.brandId
            )
            .withPlatform(
                platform = analyticsConfiguration.platform.toString(),
                version = analyticsConfiguration.platformVersion
            )
            .withPluginsFactory { pluginContext ->
                setOf(
                    GovoltePlugin(
                        pluginContext = pluginContext,
                        baseUrl = "https://topspin.npo.nl/",
                    ),
                    ATInternetPlugin(pluginContext)
                )
            }
            .withEnvironment(AnalyticsEnvironmentMapper.map(analyticsEnvironment))
            .withDebug(analyticsConfiguration.withDebug)
            .build().also {
                npoTag = it
            }
    }

    private fun getPlatform(): AnalyticsPlatform {
        return if (isThisDeviceATelevision()) AnalyticsPlatform.TV_APP else AnalyticsPlatform.APP
    }

    private fun Context.isThisDeviceATelevision(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}
