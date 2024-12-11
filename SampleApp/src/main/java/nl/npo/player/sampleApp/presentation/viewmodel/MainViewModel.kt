package nl.npo.player.sampleApp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.npo.player.sampleApp.domain.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val enableCasting = settingsRepository.enableCasting.asLiveData()
}
