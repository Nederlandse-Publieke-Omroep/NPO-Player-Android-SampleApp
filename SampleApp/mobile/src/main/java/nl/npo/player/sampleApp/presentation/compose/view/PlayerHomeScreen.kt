package nl.npo.player.sampleApp.presentation.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.npo.player.sampleApp.presentation.Routes
import nl.npo.player.sampleApp.presentation.compose.AudioList
import nl.npo.player.sampleApp.presentation.compose.VideoList
//import nl.npo.player.sampleApp.presentation.compose.ContentList
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@Composable
fun PlayerHomeScreen(
    liveItems: List<SourceWrapper>,
    vodItems: List<SourceWrapper>,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Routes.VIDEO_LIST,
                    onClick = { navController.navigate(Routes.VIDEO_LIST) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Video") },
                    label = { Text("Audio") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.AUDIO_LIST,
                    onClick = { navController.navigate(Routes.AUDIO_LIST) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Audio") },
                    label = { Text("video") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background( // you can use color, gradient, or image here
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1E2F),
                            Color(0xFF121212)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.VIDEO_LIST,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable(Routes.VIDEO_LIST) {
                    VideoList(
                        liveItems,
                    ) { }
                }

                composable(Routes.AUDIO_LIST) {
                    AudioList(vodItems) { }

                }
            }
        }
    }
}
