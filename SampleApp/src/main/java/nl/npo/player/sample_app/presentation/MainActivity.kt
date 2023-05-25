package nl.npo.player.sample_app.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import com.google.android.gms.cast.framework.CastButtonFactory
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.sample_app.R
import nl.npo.player.sample_app.databinding.ActivityMainBinding
import nl.npo.player.sample_app.extension.observeNonNull
import nl.npo.player.sample_app.model.SourceWrapper
import nl.npo.player.sample_app.presentation.list.MainListAdapter
import nl.npo.player.sample_app.presentation.offline.OfflineActivity
import nl.npo.player.sample_app.presentation.player.PlayerActivity
import nl.npo.player.sample_app.presentation.viewmodel.LinksViewModel

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<LinksViewModel>()
    private val streamLinkAdapter =
        MainListAdapter(emptyList(), ::onSourceWrapperListItemClicked)
    private val urlLinkAdapter =
        MainListAdapter(emptyList(), ::onSourceWrapperListItemClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Update the context the BitmovinCastManager is using
        // This should be done in every Activity's onCreate using the cast function
        NPOCasting.updateCastingContext(this)

        binding.setupViews()
        setObservers()
        logPageAnalytics("MainActivity")
    }

    private fun setObservers() {
        viewModel.streamLinkList.observeNonNull(this, ::setStreamAdapter)
        viewModel.urlLinkList.observeNonNull(this, ::setURLAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_activity_main, menu)

        // Adding a Cast Button in the menu bar
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        return true
    }

    private fun ActivityMainBinding.setupViews() {
        rvLoadUrlDirectly.adapter = urlLinkAdapter
        rvStreamLink.adapter = streamLinkAdapter
        btnOffline.setOnClickListener {
            startActivity(Intent(this@MainActivity, OfflineActivity::class.java))
        }
        etPrid.setOnEditorActionListener { textView, i, _ ->
            return@setOnEditorActionListener if (i == EditorInfo.IME_ACTION_SEND && textView.text.isNotBlank()) {
                onSourceWrapperListItemClicked(
                    SourceWrapper(
                        title = textView.text.toString(),
                        uniqueId = textView.text.toString(),
                        getStreamLink = true
                    )
                )
                true
            } else false
        }
    }

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
                item
            )
        )
    }
}
