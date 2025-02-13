package nl.npo.player.sampleApp.tv.presentation.selection

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel
import nl.npo.player.sampleApp.tv.R

/**
 * Loads [MainFragment].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val librarySetupViewModel: LibrarySetupViewModel by viewModels<LibrarySetupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            librarySetupViewModel.setupLibrary(withNPOTag = true)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
    }
}
