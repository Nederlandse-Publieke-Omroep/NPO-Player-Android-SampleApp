package nl.npo.player.sampleApp.presentation.compose.view


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nl.npo.player.sampleApp.presentation.Routes
import nl.npo.player.sampleApp.presentation.compose.AudioList
import nl.npo.player.sampleApp.presentation.compose.VideoList
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@Composable
fun PlayerHomeScreen(
    liveItems: List<SourceWrapper>,
    vodItems: List<SourceWrapper>,
    onItemClick: (SourceWrapper) -> Unit
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
                    icon = { Icon(Icons.Default.Headphones, contentDescription = "Video") },
                    label = { Text("Audio") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.AUDIO_LIST,
                    onClick = { navController.navigate(Routes.AUDIO_LIST) },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Audio") },
                    label = { Text("video") }
                )
            }
        }
    ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.AUDIO_LIST,
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {

                composable(Routes.VIDEO_LIST) {
                    val videoId = liveItems.map { it.uniqueId }
                    VideoList(
                        liveItems,
                    ) { navController.navigate("${Routes.DETAIL}/${videoId}") }
                }

                composable(Routes.AUDIO_LIST) {
                    val audioId = vodItems.map { it.uniqueId }
                    AudioList(vodItems)
                    { navController.navigate("${Routes.DETAIL}/${audioId}") }

                }
            }
        }
}
