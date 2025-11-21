package nl.npo.player.sampleApp.presentation.compose.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val destination: Destinations,
    val icon: ImageVector,
    val label: String,
)

val bottomNavItems =
    listOf(
        BottomNavItem(Destinations.Player, Icons.Default.VideoLibrary, "Player"),
        BottomNavItem(Destinations.OfflineList, Icons.Default.OfflinePin, "Offline"),
    )
