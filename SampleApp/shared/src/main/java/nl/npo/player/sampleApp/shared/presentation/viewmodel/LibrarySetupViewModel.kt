package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import javax.inject.Inject

@HiltViewModel
class LibrarySetupViewModel
    @Inject
    constructor(
        @ApplicationContext val application: Context,
    ) : ViewModel() {
        private val _libSetupState = MutableLiveData(false)
        val libSetupState: MutableLiveData<Boolean> = _libSetupState

        fun setupLibrary(withNPOTag: Boolean) {
            viewModelScope.launch {
                val sampleApplication = application as? PlayerApplication ?: return@launch
                if (!sampleApplication.isPlayerInitiatedYet()) {
                    sampleApplication.initiatePlayerLibrary(withNPOTag)
                }
                _libSetupState.value = true
            }
        }
    }
