package nl.npo.player.sampleApp.presentation.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.npo.player.sampleApp.presentation.compose.views.PlayerScreen


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.Player,
        modifier = modifier
    ) {
        composable<Destinations.Player> {
            PlayerScreen()
        }

        composable<Destinations.OfflineList> {

        }
    }
}
