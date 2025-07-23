package nl.npo.player.sampleApp.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOCasting
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.databinding.ActivitySelectNpotagTypeBinding
import nl.npo.player.sampleApp.presentation.cast.CastOptionsProvider
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel

@AndroidEntryPoint
class SelectNPOTagTypeActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectNpotagTypeBinding
    private val libraryViewModel by viewModels<LibrarySetupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectNpotagTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // There shouldn't be an NPOTag yet, as it should have not been initiated yet. If there is we skip this activity
        if ((application as PlayerApplication).isPlayerInitiatedYet()) {
            navigateToMainActivity()
            return
        }
        // Normally you would log your page views here, but as we want to use this page to select
        // and initiate the NPOTag we can't do that yet...
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            tvExplanation.text =
                getString(
                    R.string.npo_tag_selection_explanation,
                    (application as? PlayerApplication)?.brandId,
                )
            btnWithNPOTag.setOnClickListener {
                setupLibrary(true)
            }
            btnWithoutNPOTag.setOnClickListener {
                setupLibrary(false)
            }
        }
    }

    private fun setupLibrary(witNPOTag: Boolean) {
        libraryViewModel.setupLibrary(withNPOTag = witNPOTag) {
            NPOCasting.initializeCasting(getString(CastOptionsProvider.getReceiverID()))
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
