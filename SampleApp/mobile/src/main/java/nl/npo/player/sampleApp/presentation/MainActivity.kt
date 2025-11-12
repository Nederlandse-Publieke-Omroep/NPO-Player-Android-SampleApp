package nl.npo.player.sampleApp.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import com.google.android.gms.cast.framework.CastButtonFactory
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.views.MainScreen
import nl.npo.player.sampleApp.presentation.ext.isGooglePlayServicesAvailable
import nl.npo.player.sampleApp.presentation.settings.SettingsBottomSheetDialog
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.MainViewModel
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val libraryViewModel by viewModels<LibrarySetupViewModel>()
    private var lastKnownEnvironment: Environment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLibraryInitialization()
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
        // Update the context the BitmovinCastManager is using
        // This should be done in every Activity's onCreate using the cast function
        NPOCasting.updateCastingContext(this)
        setObservers()
        logPageAnalytics("MainActivity")
    }

    override fun onStart() {
        super.onStart()
        requestMissingPermissions()
    }

    private fun checkLibraryInitialization() {
        libraryViewModel.setupLibrary(withNPOTag = true)
    }

    private fun setObservers() {
        if (isGooglePlayServicesAvailable()) {
            viewModel.enableCasting.observe(this) {
                if (it != NPOCasting.isCastingEnabled) {
                    NPOCasting.setCastingEnabled(it)
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }

        viewModel.environment.observe(this) {
            if (lastKnownEnvironment != null && lastKnownEnvironment != it) {
                val i: Intent =
                    packageManager.getLaunchIntentForPackage(packageName) ?: return@observe
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                exitProcess(0)
            }
            lastKnownEnvironment = it
        }
       // linksViewModel.streamLinkList.observeNonNull(this, ::setStreamAdapter)
        //linksViewModel.urlLinkList.observeNonNull(this, ::setURLAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_activity_main, menu)

        menu.findItem(R.id.media_route_menu_item).isVisible = NPOCasting.isCastingEnabled

        // Adding a Cast Button in the menu bar
        if (NPOCasting.isCastingEnabled) {
            CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            showSettings()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSettings() {
        SettingsBottomSheetDialog.newInstance().show(supportFragmentManager, null)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { _: Boolean? -> }

    private fun requestMissingPermissions() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
