package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import nl.npo.player.sampleApp.presentation.compose.navigation.AppNavHost
import nl.npo.player.sampleApp.presentation.compose.navigation.BottomBar

@Composable
fun MainScreen() {
    val nav = rememberNavController()
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomBar(nav) },
    ) { innerPadding ->
        AppNavHost(
            navController = nav,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
