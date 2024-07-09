package nl.npo.player.sampleApp.presentation

import android.content.Intent
import android.os.Bundle
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.SampleApplication
import nl.npo.player.sampleApp.databinding.ActivitySelectNpotagTypeBinding

class SelectNPOTagTypeActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectNpotagTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectNpotagTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // There shouldn't be an NPOTag yet, as it should have not been initiated yet. If there is we skip this activity
        if ((application as? SampleApplication)?.isPlayerInitiatedYet() == true) {
            navigateToMainActivity()
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
                    (application as? SampleApplication)?.analyticsConfiguration?.brandId,
                )
            btnWithNPOTag.setOnClickListener {
                (application as? SampleApplication)?.initiatePlayerLibrary(withNPOTag = true)
                navigateToMainActivity()
            }
            btnWithoutNPOTag.setOnClickListener {
                (application as? SampleApplication)?.initiatePlayerLibrary(withNPOTag = false)
                navigateToMainActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
