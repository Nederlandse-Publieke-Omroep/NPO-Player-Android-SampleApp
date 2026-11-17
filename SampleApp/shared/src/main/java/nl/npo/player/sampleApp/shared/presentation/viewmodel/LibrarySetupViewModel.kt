package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.npo.player.sampleApp.shared.app.PlayerApplication
import nl.npo.player.sampleApp.shared.data.model.CastReceiverPref
import nl.npo.player.sampleApp.shared.domain.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class LibrarySetupViewModel
    @Inject
    constructor(
        @ApplicationContext val application: Context,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        fun setupLibrary(
            withNPOTag: Boolean,
            onInitialized: (castReceiver: CastReceiverPref) -> Unit = {},
        ) {
            viewModelScope.launch {
                val sampleApplication = application as? PlayerApplication ?: return@launch
                if (!sampleApplication.isPlayerInitiatedYet()) {
                    sampleApplication.initiatePlayerLibrary(withNPOTag)
                }
                onInitialized(settingsRepository.castReceiver.first())
            }
        }
    }
