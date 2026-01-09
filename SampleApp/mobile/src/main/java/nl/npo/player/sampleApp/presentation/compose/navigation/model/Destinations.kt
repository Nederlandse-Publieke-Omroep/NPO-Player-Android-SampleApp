package nl.npo.player.sampleApp.presentation.compose.navigation.model

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object Player : Destinations

    @Serializable
    data object OfflineList : Destinations
}
