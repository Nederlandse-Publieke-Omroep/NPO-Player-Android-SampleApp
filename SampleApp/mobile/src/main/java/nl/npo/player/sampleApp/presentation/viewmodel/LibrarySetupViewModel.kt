package nl.npo.player.sampleApp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.SampleApplication
import javax.inject.Inject

@HiltViewModel
class LibrarySetupViewModel
    @Inject
    constructor(
        @ApplicationContext val application: Context,
    ) : ViewModel() {
        fun setupLibrary(withNPOTag: Boolean) {
            viewModelScope.launch {
                (application.applicationContext as? SampleApplication)?.initiatePlayerLibrary(withNPOTag)
            }
        }
    }
