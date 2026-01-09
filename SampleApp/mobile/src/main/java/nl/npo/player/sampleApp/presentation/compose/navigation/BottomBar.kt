package nl.npo.player.sampleApp.presentation.compose.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import nl.npo.player.sampleApp.presentation.compose.navigation.model.Destinations

@Composable
fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
        bottomNavItems.forEach { item ->
            val selected =
                when (item.destination) {
                    is Destinations.Player -> current?.hasRoute<Destinations.Player>() == true
                    is Destinations.OfflineList -> current?.hasRoute<Destinations.OfflineList>() == true
                }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = Color.White,
                    )
                },
                label = { Text(item.label, color = Color.White) },
            )
        }
    }
}
