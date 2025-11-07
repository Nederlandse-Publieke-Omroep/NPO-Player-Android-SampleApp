package nl.npo.player.sampleApp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.bitmovin.player.api.source.Source
import com.google.android.datatransport.runtime.scheduling.persistence.EventStoreModule_PackageNameFactory.packageName
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.databinding.ActivityMainBinding
import nl.npo.player.sampleApp.presentation.compose.view.PlayerHomeScreen
import nl.npo.player.sampleApp.presentation.ext.isGooglePlayServicesAvailable
import nl.npo.player.sampleApp.presentation.list.MainListAdapter
import nl.npo.player.sampleApp.presentation.offline.OfflineActivity
import nl.npo.player.sampleApp.presentation.player.PlayerActivity
import nl.npo.player.sampleApp.presentation.settings.SettingsBottomSheetDialog
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.MainViewModel
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val libraryViewModel by viewModels<LibrarySetupViewModel>()
    private val linksViewModel by viewModels<LinksViewModel>()
    private val streamLinkAdapter =
        MainListAdapter(emptyList(), ::onSourceWrapperListItemClicked)
    private val urlLinkAdapter =
        MainListAdapter(emptyList(), ::onSourceWrapperListItemClicked)
    private var lastKnownEnvironment: Environment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLibraryInitialization()
        //binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)
        setContent {
         val stream = linksViewModel.streamLinkList.value
            val audio = linksViewModel.urlLinkList.value
            MaterialTheme {
                PlayerHomeScreen(
                    liveItems = stream,
                    vodItems = audio,
                    onItemClick = {
                        onSourceWrapperListItemClicked(it)
                    }
                )
            }
        }


        // Update the context the BitmovinCastManager is using
        // This should be done in every Activity's onCreate using the cast function
        NPOCasting.updateCastingContext(this)

        //binding.setupViews()
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
        linksViewModel.streamLinkList.observeNonNull(this, ::setStreamAdapter)
        linksViewModel.urlLinkList.observeNonNull(this, ::setURLAdapter)
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

//    private fun ActivityMainBinding.setupViews() {
//        rvLoadUrlDirectly.adapter = urlLinkAdapter
//        rvStreamLink.adapter = streamLinkAdapter
//        btnOffline.setOnClickListener {
//            startActivity(Intent(this@MainActivity, OfflineActivity::class.java))
//        }
//        etPrid.setOnEditorActionListener { textView, i, _ ->
//            return@setOnEditorActionListener if (i == EditorInfo.IME_ACTION_SEND && textView.text.isNotBlank()) {
//                onSourceWrapperListItemClicked(
//                    SourceWrapper(
//                        title = textView.text.toString(),
//                        uniqueId = textView.text.toString(),
//                        getStreamLink = true,
//                    ),
//                )
//                true
//            } else {
//                false
//            }
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setStreamAdapter(sourceWrappers: List<SourceWrapper>) {
        streamLinkAdapter.offlineSource = sourceWrappers
        streamLinkAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setURLAdapter(sourceWrappers: List<SourceWrapper>) {
        urlLinkAdapter.offlineSource = sourceWrappers
        urlLinkAdapter.notifyDataSetChanged()
    }

    private fun onSourceWrapperListItemClicked(item: SourceWrapper) {
        startActivity(
            PlayerActivity.getStartIntent(
                this@MainActivity,
                item,
            ),
        )
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
